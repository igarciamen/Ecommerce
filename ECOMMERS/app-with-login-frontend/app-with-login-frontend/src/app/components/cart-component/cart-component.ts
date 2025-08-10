import { Component } from '@angular/core';
import { CartItem } from '../../model/cart-item';
import { CartService } from '../../service/cart-service';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../service/product-service';
import { finalize, forkJoin, map, of, switchMap, tap } from 'rxjs';
import { OrderService } from '../../service/order-service';
import { OrderItemDto } from '../../model/order-item-dto';
import { AuthService } from '../../service/auth-service';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Cart } from '../../model/cart';

@Component({
  selector: 'app-cart-component',
  imports: [CommonModule],
  templateUrl: './cart-component.html',
  styleUrl: './cart-component.css'
})
export class CartComponent {
items: CartItem[] = [];
  loading = false;
  error?: string;

  constructor(
    private cartSvc: CartService,
    private productSvc: ProductService,
    private orderSvc: OrderService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.error = undefined;

    this.cartSvc.getCart().pipe(
      switchMap((data: Cart | CartItem[]) => {
        const raw: CartItem[] = Array.isArray(data) ? (data as CartItem[]) : ((data as Cart).items ?? []);
        if (!raw.length) return of(raw);

        // Si faltan productos (modo invitado), los completamos
        const toFetch = raw.filter(i => !i.product);
        if (!toFetch.length) return of(raw);

        const calls = toFetch.map(i =>
          this.productSvc.getProductById(i.productId).pipe(
            tap(prod => { i.product = prod; }) // ← solo asigna, sin cambiar el flujo
          )
        );
        return forkJoin(calls).pipe(map(() => raw));
      }),
      finalize(() => (this.loading = false))
    ).subscribe({
      next: (enriched: CartItem[]) => { this.items = enriched; },
      error: (err: HttpErrorResponse) => {
        this.items = [];
        this.error = err.error?.message || err.message || 'No se pudo cargar el carrito';
      }
    });
  }

  update(item: CartItem, qty: number) {
    this.cartSvc.updateItem(item.id, qty, item.productId).subscribe({
      next: () => this.load(),
      error: () => this.error = 'No se pudo actualizar el ítem'
    });
  }

  clear() {
    this.cartSvc.clearCart().subscribe({
      next: () => this.load(),
      error: () => this.error = 'No se pudo vaciar el carrito'
    });
  }

  /** Checkout: crea la orden (el backend calcula todo y ajusta stock) */
  checkout() {
    this.error = undefined;

    const userId = this.auth.getUserId();
    if (!userId) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/cart' } });
      return;
    }
    if (!this.items.length) {
      this.error = 'No tienes artículos en el carrito.';
      return;
    }

    const payload = {
      userId,
      items: this.items.map(i => ({
        productId: i.productId,
        quantity: i.quantity
      }))
    };

    this.loading = true;

    this.orderSvc.create(payload).pipe(
      switchMap(order => this.cartSvc.clearCart().pipe(map(() => order))),
      switchMap(order =>
        this.productSvc.refreshStocks(payload.items.map(i => i.productId)).pipe(map(() => order))
      ),
      finalize(() => (this.loading = false))
    ).subscribe({
      next: order => this.router.navigate(['/orders', order.id]),
      error: (err: HttpErrorResponse) => {
        this.error = err.error?.message || err.message || 'No se pudo crear el pedido';
      }
    });
  }
}
