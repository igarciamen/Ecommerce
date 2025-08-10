// src/app/service/auth-service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, catchError, switchMap } from 'rxjs/operators';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { UserInfo } from '../model/user-info';

interface JwtResponse { token: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private authUrl  = 'http://localhost:8080/api/auth';
  private userUrl  = 'http://localhost:8080/api/user';
  private tokenKey = 'authToken';

  private userInfoSubject = new BehaviorSubject<UserInfo | null>(null);
  public  userInfo$      = this.userInfoSubject.asObservable();

  private loggedIn$ = new BehaviorSubject<boolean>(!!this.getToken());
  // Exponer el observable para CartService
  public isLoggedIn$() { return this.loggedIn$.asObservable(); }

  constructor(private http: HttpClient) {
    const token = this.getToken();
    if (token) {
      this.loggedIn$.next(true);
      this.fetchUserInfo().subscribe({ error: () => this.logout() });
    }
  }

  /** Hace login y devuelve UserInfo */
  login(login: string, password: string): Observable<UserInfo | null> {
    return this.http.post<JwtResponse>(`${this.authUrl}/login`, { login, password })
      .pipe(
        tap(res => {
          localStorage.setItem(this.tokenKey, res.token);
          // 🚀 Emitimos login exitoso para que CartService recargue el carrito
          this.loggedIn$.next(true);
        }),
        switchMap(() => this.fetchUserInfo())
      );
  }

  /** Registra y luego hace login automático */
  signup(username: string, email: string, password: string, confirmPassword: string): Observable<UserInfo | null> {
    return this.http.post<any>(`${this.authUrl}/signup`, {
      username, email, password, confirmPassword
    }).pipe(
      switchMap(() => this.login(username, password))
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.userInfoSubject.next(null);
    this.loggedIn$.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  fetchUserInfo(): Observable<UserInfo | null> {
    if (!this.getToken()) {
      this.userInfoSubject.next(null);
      return of(null);
    }
    return this.http.get<UserInfo>(`${this.userUrl}/me`)
      .pipe(
        tap(info => this.userInfoSubject.next(info)),
        catchError(err => {
          this.userInfoSubject.next(null);
          this.loggedIn$.next(false);
          return of(null);
        })
      );
  }

  /** Devuelve el ID del usuario cargado en userInfo$, o null si no hay sesión */
  getUserId(): number | null {
    return this.userInfoSubject.value?.id ?? null;
  }
  getUsername(): string | null {
    return this.userInfoSubject.value?.username ?? null;
  }
  getEmail(): string | null {
    return this.userInfoSubject.value?.email ?? null;
  }
  getRoles(): string[] {
    return this.userInfoSubject.value?.roles ?? [];
  }
  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }
  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getUserById(id: number): Observable<UserInfo> {
    return this.http.get<UserInfo>(`${this.userUrl}/${id}`);
  }
}
