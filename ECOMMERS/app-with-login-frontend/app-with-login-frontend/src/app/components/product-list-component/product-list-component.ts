import { Component, HostListener } from '@angular/core';
import { PageResponse } from '../../model/page-response';
import { ProductModel } from '../../model/product-model';
import { AuthService } from '../../service/auth-service';
import { ProductService } from '../../service/product-service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { debounceTime, distinctUntilChanged, finalize } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ProductSearchService } from '../../service/product-search.service';
import { CategoryService } from '../../service/category-service';
import { CategoryModel } from '../../model/category-model';
import { CartService } from '../../service/cart-service';

@Component({
  selector: 'app-product-list-component',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-list-component.html',
  styleUrl: './product-list-component.css'
})
export class ProductListComponent {
 products: Array<ProductModel & { sellerName?: string }> = [];
  page = 0;
  size = 6;
  totalPages = 0;
  loading = false;

  public categoryName = '';
  totalElements = 0;
  public categoryId: number | null = null;

  quantityMap: Record<number, number> = {};
  public currentTerm = '';

  constructor(
    private productSvc: ProductService,
    private searchSvc: ProductSearchService,
    private route: ActivatedRoute,
    private cartSvc: CartService,
    private categoryService: CategoryService
  ) {}

  ngOnInit() {
    // Actualizaciones de stock → fusiona producto completo
    this.productSvc.stockUpdates$.subscribe((pNew) => {
      const idx = this.products.findIndex(p => p.id === pNew.id);
      if (idx >= 0) {
        const prev = this.products[idx];
        this.products[idx] = { ...prev, ...pNew };
        const currentQty = this.quantityMap[pNew.id] ?? 1;
        this.quantityMap[pNew.id] = Math.min(currentQty, pNew.stock);
      }
    });

    this.searchSvc.searchTerm$
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(term => {
        this.currentTerm = term.trim();
        this.page = 0;
        this.products = [];
        this.totalPages = 0;
        this.loadProducts();
      });

    this.route.queryParams.subscribe(params => {
      const cat = params['categoryId'];
      this.categoryId = cat !== undefined ? +cat : null;

      if (this.categoryId != null) {
        this.categoryService.list().subscribe((cats: CategoryModel[]) => {
          const found = cats.find(c => c.id === this.categoryId);
          this.categoryName = found ? found.name : '';
        });
      } else {
        this.categoryName = '';
      }

      this.page = 0;
      this.products = [];
      this.totalPages = 0;
      this.currentTerm = '';
      this.loadProducts();
    });

    this.loadProducts();
  }

  addToCart(product: ProductModel, qty: number) {
    this.cartSvc.addToCart(product.id, qty).subscribe({
      next: () => console.log(`Añadidos ${qty} x ${product.name} al carrito`),
      error: () => alert('Error al añadir al carrito')
    });
  }

  loadProducts() {
    if (this.loading || (this.totalPages && this.page >= this.totalPages)) return;

    this.loading = true;
    let call$;
    if (this.categoryId != null) {
      call$ = this.productSvc.listByCategory(this.categoryId, this.page, this.size);
    } else if (this.currentTerm) {
      call$ = this.productSvc.search(this.currentTerm, this.page, this.size);
    } else {
      call$ = this.productSvc.list(this.page, this.size);
    }

    call$
      .pipe(finalize(() => (this.loading = false)))
      .subscribe((resp: PageResponse<ProductModel>) => {
        this.totalElements = resp.totalElements;
        const enriched = resp.content.map(p => {
          this.quantityMap[p.id] = this.quantityMap[p.id] ?? 1;
          return { ...p, sellerName: p.seller?.username ?? 'Desconocido' }; // ← sin llamadas extra
        });

        this.products = [...this.products, ...enriched];
        this.totalPages = resp.totalPages;
        this.page++;
      });
  }

  onQuantityChange(productId: number, qty: number) {
    const max = this.products.find(p => p.id === productId)?.stock ?? 1;
    this.quantityMap[productId] = Math.min(Math.max(qty, 1), max);
  }

  @HostListener('window:scroll', [])
  onScroll(): void {
    const pos = window.scrollY + window.innerHeight;
    const max = document.documentElement.scrollHeight;
    if (pos > max - 100) this.loadProducts();
  }
}
