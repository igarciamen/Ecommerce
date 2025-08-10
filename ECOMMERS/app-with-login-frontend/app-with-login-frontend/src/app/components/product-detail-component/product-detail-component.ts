import { Component } from '@angular/core';
import { ProductModel } from '../../model/product-model';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../service/product-service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CartService } from '../../service/cart-service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-product-detail-component',
    standalone: true,
  imports: [CommonModule ,FormsModule],
  templateUrl: './product-detail-component.html',
  styleUrl: './product-detail-component.css'
})
export class ProductDetailComponent {
product?: ProductModel;
  quantity = 1;
  loading = false;
  error?: string;

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cartSvc: CartService,
    private productSvc: ProductService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(id)) {
      this.error = 'ID de producto inválido';
      return;
    }

    // cantidad arrastrada desde la lista
    let navQty = this.router.getCurrentNavigation()?.extras.state?.['quantity'];
    if (navQty == null) navQty = (history.state as any)['quantity'];
    if (navQty != null) this.quantity = navQty;

    this.loading = true;
    this.productSvc.getProductById(id).subscribe({
      next: p => {
        this.product = p;
        // si stock=0 forzamos cantidad 0
        this.quantity = Math.min(this.quantity, p.stock);
        this.loading = false;
      },
      error: () => {
        this.error = 'Error cargando producto';
        this.loading = false;
      }
    });

    // 🔔 escuchar cambios de stock y refrescar la vista
    this.productSvc.stockUpdates$
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ id: pid, stock }) => {
        if (this.product && this.product.id === pid) {
          this.product.stock = stock;
          this.quantity = Math.min(this.quantity, stock); // 0 si se agota
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  addToCart(): void {
    if (!this.product || this.product.stock === 0 || this.quantity <= 0) return;
    this.cartSvc.addToCart(this.product.id, this.quantity).subscribe({
      next: () => alert('Añadido al carrito'),
      error: () => this.error = 'Error al añadir al carrito'
    });
  }
}
