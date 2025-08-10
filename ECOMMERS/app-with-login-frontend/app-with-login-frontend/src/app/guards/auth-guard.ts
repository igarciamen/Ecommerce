// src/app/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import {
  CanActivate,
  Router,
  ActivatedRouteSnapshot,
  RouterStateSnapshot
} from '@angular/router';
import { AuthService } from '../service/auth-service';


@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    // 1. Si no está logueado, redirige a login
    if (!this.auth.isAuthenticated()) {
      this.router.navigate(
        ['/login'],
        { queryParams: { returnUrl: state.url } }
      );
      return false;
    }

    // 2. Si la ruta define roles, comprobamos que alguno coincida
    const requiredRoles = (route.data['roles'] as string[]) || [];
    if (requiredRoles.length > 0) {
      const hasRole = requiredRoles.some(role =>
        this.auth.hasRole(role)
      );
      if (!hasRole) {
        // Opcional: podrías redirigir a una página de "acceso denegado"
        // this.router.navigate(['/unauthorized']);
        return false;
      }
    }

    // 3. Todo ok: deja avanzar
    return true;
  }
}