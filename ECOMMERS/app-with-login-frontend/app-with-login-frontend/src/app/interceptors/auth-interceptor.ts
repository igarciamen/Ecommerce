// src/app/interceptor/auth.interceptor.ts
import { Injectable, Injector } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../service/auth-service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private injector: Injector) {}  // ← inyecta Injector

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const url = req.url.toLowerCase();
    // No adjuntes token en signup/login
    if (url.endsWith('/api/auth/login') || url.endsWith('/api/auth/signup')) {
      return next.handle(req);
    }
    // obtén AuthService perezosamente
    const auth = this.injector.get(AuthService);
    const token = auth.getToken();
    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return next.handle(req);
  }
}
