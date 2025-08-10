import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterModule, RouterOutlet } from '@angular/router';
import { AuthService } from './service/auth-service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CategoryModel } from './model/category-model';
import { CategoryService } from './service/category-service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators'; 
import { ProductSearchService } from './service/product-search.service';
import { CartService } from './service/cart-service';

@Component({
  selector: 'app-root',
  //standalone: true,
  imports: [
RouterOutlet, RouterLink, FormsModule, RouterLinkActive,CommonModule
  ],

  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
    categories: CategoryModel[] = [];
    username$: Observable<string|null>;
    searchTerm = '';
    cartCount$: Observable<number>;

  constructor(
     private searchService: ProductSearchService,
    public auth: AuthService,
    private categoryService: CategoryService,
    private cartSvc: CartService,
    public router: Router
  ) {
    this.username$ = this.auth.userInfo$.pipe(
      map(info => info?.username ?? null)
    );

      if (this.auth.isAuthenticated()) {
      this.auth.fetchUserInfo().subscribe();
    }

 this.cartCount$ = this.cartSvc.cartCount$;

  this.cartSvc.refreshCartCount();
  }

  ngOnInit(): void {
    // Si ya tenemos token, recarga userInfo para repoblar el BehaviorSubject
    if (this.auth.isAuthenticated()) {
      this.auth.fetchUserInfo().subscribe();
    }

    this.loadCategories();
    this.categoryService.refreshNeeded$.subscribe(() => this.loadCategories());
  }

  private loadCategories(): void {
    this.categoryService.list().subscribe(cats => this.categories = cats);
  }

  onLogout(event: MouseEvent) {
    event.preventDefault();
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  onSearch(term: string) {
    // 1) actualiza término en el servicio
    this.searchService.updateTerm(term);
    // 2) navega a Products para que ProductListComponent recoja el cambio
    this.router.navigate(['/products']);
  }


}
