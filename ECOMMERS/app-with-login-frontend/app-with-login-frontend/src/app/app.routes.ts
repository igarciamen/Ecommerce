import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { SignupComponent } from './components/signup/signup/signup';
import { Dashboard } from './dashboard/dashboard';
import { AuthGuard } from './guards/auth-guard';
import { CategoryCreateComponent } from './components/category-create-component/category-create-component';
import { ProductCreateComponent } from './components/product-create-component/product-create-component';
import { ProductListComponent } from './components/product-list-component/product-list-component';
import { ProductDetailComponent } from './components/product-detail-component/product-detail-component';
import { ProductManagerComponent } from './components/product-manager-component/product-manager-component';
import { ProductEditComponent } from './components/product-edit.component/product-edit.component';
import { CartComponent } from './components/cart-component/cart-component';
import { OrderListComponent } from './components/order-list-component/order-list-component';
import { OrderDetailComponent } from './components/order-detail-component/order-detail-component';


export const routes: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },

  // públicas
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },

  // products (ESPECÍFICAS PRIMERO)
  { path: 'products/new', component: ProductCreateComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_SELLER','ROLE_ADMIN'] } },
  { path: 'products/manage', component: ProductManagerComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_SELLER','ROLE_ADMIN'] } },
  { path: 'products/edit/:id', component: ProductEditComponent, canActivate: [AuthGuard], data: { roles: ['ROLE_SELLER','ROLE_ADMIN'] } },
  { path: 'products', component: ProductListComponent },
   { path: 'cart',     component: CartComponent },
    { path: 'orders', component: OrderListComponent, canActivate: [AuthGuard] },
  { path: 'orders/:id', component: OrderDetailComponent, canActivate: [AuthGuard] },
  { path: 'products/:id', component: ProductDetailComponent }, // ← DEJA ESTA AL FINAL

  // categorías
  { path: 'categories/new', component: CategoryCreateComponent },

  // protegidas
  { path: 'dashboard', component: Dashboard, canActivate: [AuthGuard], data: { roles: ['ROLE_SELLER','ROLE_USER','ROLE_ADMIN'] } },

  { path: '**', redirectTo: 'products' }


];
