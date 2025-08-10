import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, forkJoin, of } from 'rxjs';
import { filter, finalize, switchMap, take, tap } from 'rxjs/operators';
import { CartItem } from '../model/cart-item';
import { Cart } from '../model/cart';
import { AuthService } from './auth-service';

@Injectable({ providedIn: 'root' })
export class CartService {
  private apiUrl = 'http://localhost:8083/api/cart';
  private count$ = new BehaviorSubject<number>(0);
  public cartCount$ = this.count$.asObservable();

  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) {
    // Reacciona al login: sincroniza invitado → remoto y recarga contador
    this.auth.isLoggedIn$().subscribe(loggedIn => {
      if (loggedIn) {
        this.auth.userInfo$
          .pipe(filter(u => !!u && u.id != null), take(1))
          .subscribe(() => {
            this.syncGuestToRemote()
              .pipe(finalize(() => this.loadCount()))
              .subscribe();
          });
      } else {
        this.loadCount();
      }
    });

    this.loadCount();
  }

  // -------- utilidades de estado --------
  public refreshCartCount(): void { this.loadCount(); }

  private getStorageKey(): string {
    const uid = this.auth.getUserId();
    return uid != null ? `localCart_user_${uid}` : 'localCart_guest';
  }

  private getLocalCart(): CartItem[] {
    const s = localStorage.getItem(this.getStorageKey());
    return s ? JSON.parse(s) : [];
  }

  private setLocalCart(items: CartItem[]) {
    localStorage.setItem(this.getStorageKey(), JSON.stringify(items));
    this.count$.next(items.reduce((acc, i) => acc + i.quantity, 0));
  }

  private loadCount() {
    if (this.auth.isAuthenticated()) {
      const userId = this.auth.getUserId();
      if (userId == null) {
        this.auth.userInfo$
          .pipe(filter(u => !!u && u.id != null), take(1))
          .subscribe(u => this.fetchRemoteCount(u!.id));
        return;
      }
      this.fetchRemoteCount(userId);
    } else {
      const total = this.getLocalCart().reduce((acc, i) => acc + i.quantity, 0);
      this.count$.next(total);
    }
  }

  private fetchRemoteCount(userId: number) {
    this.http.get<number>(`${this.apiUrl}/${userId}/count/units`)
      .subscribe({ next: n => this.count$.next(n), error: () => this.count$.next(0) });
  }

  // -------- API pública --------

  /** Añadir al carrito */
  addToCart(productId: number, quantity: number): Observable<void> {
    if (this.auth.isAuthenticated()) {
      const userId = this.auth.getUserId()!;
      const params = new HttpParams()
        .set('userId', userId)
        .set('productId', productId)
        .set('quantity', quantity);
      return this.http.post<Cart>(`${this.apiUrl}/add`, null, { params }).pipe(
        tap(cart => {
          const total = cart.items.reduce((acc, i) => acc + i.quantity, 0);
          this.count$.next(total);
        }),
        switchMap(() => of(void 0))
      );
    } else {
      const items = this.getLocalCart();
      const idx = items.findIndex(i => i.productId === productId);
      if (idx > -1) items[idx].quantity += quantity;
      else items.push({ id: 0, productId, quantity });
      this.setLocalCart(items);
      return of(void 0);
    }
  }

  /** Obtener carrito (remoto o invitado) */
  getCart(): Observable<Cart | CartItem[]> {
    if (this.auth.isAuthenticated()) {
      const userId = this.auth.getUserId()!;
      return this.http.get<Cart>(`${this.apiUrl}/${userId}`);
    } else {
      return of(this.getLocalCart());
    }
  }

  /** Vaciar carrito */
  clearCart(): Observable<void> {
    if (this.auth.isAuthenticated()) {
      const userId = this.auth.getUserId()!;
      return this.http.delete<Cart>(`${this.apiUrl}/${userId}/clear`).pipe(
        tap(() => this.count$.next(0)),
        switchMap(() => of(void 0))
      );
    } else {
      this.setLocalCart([]);
      return of(void 0);
    }
  }

  /**
   * Actualizar cantidad. En remoto usa itemId; en invitado actualiza por productId
   * (para evitar colisiones de id=0).
   */
  updateItem(itemId: number, quantity: number, productIdForGuest?: number): Observable<void> {
    if (this.auth.isAuthenticated()) {
      const userId = this.auth.getUserId()!;
      const params = new HttpParams()
        .set('userId', userId)
        .set('itemId', itemId)
        .set('quantity', quantity);
      return this.http.put<Cart>(`${this.apiUrl}/item`, null, { params }).pipe(
        tap(cart => {
          const total = cart.items.reduce((acc, i) => acc + i.quantity, 0);
          this.count$.next(total);
        }),
        switchMap(() => of(void 0))
      );
    } else {
      const pid = productIdForGuest!;
      const items = this.getLocalCart().filter(i => {
        if (i.productId === pid) {
          i.quantity = quantity;
          return i.quantity > 0;
        }
        return true;
      });
      this.setLocalCart(items);
      return of(void 0);
    }
  }

  /** Sincroniza carrito invitado al loguearse */
  private syncGuestToRemote(): Observable<any> {
    const guestItems: CartItem[] = JSON.parse(localStorage.getItem('localCart_guest') || '[]');
    if (!guestItems.length) return of(void 0);

    const userId = this.auth.getUserId()!;
    const calls = guestItems.map(i => {
      const params = new HttpParams()
        .set('userId', userId)
        .set('productId', i.productId)
        .set('quantity', i.quantity);
      return this.http.post<Cart>(`${this.apiUrl}/add`, null, { params });
    });
    return forkJoin(calls).pipe(tap(() => localStorage.removeItem('localCart_guest')));
  }
}
