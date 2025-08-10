import { Component } from '@angular/core';
import { ProductModel } from '../../model/product-model';
import { ProductService } from '../../service/product-service';
import { AuthService } from '../../service/auth-service';
import { Router, RouterLink, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-manager-component',
  standalone: true,
  imports: [ FormsModule,  RouterModule,CommonModule ],
  templateUrl: './product-manager-component.html',
  styleUrl: './product-manager-component.css'
})
export class ProductManagerComponent {
  products: ProductModel[] = [];
  loading = false;
  error?: string;

  constructor(
    private productSvc: ProductService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadMyProducts();
  }

  private loadMyProducts() {
    this.loading = true;
    this.productSvc.list(0, 1000).subscribe({
      next: resp => {
        const me = this.auth.getUserId();
        this.products = resp.content.filter(p => p.sellerId === me);
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar tus productos';
        this.loading = false;
      }
    });
  }

  onDelete(id: number) {
    if (!confirm('¿Eliminar este producto?')) return;
    this.productSvc.delete(id).subscribe({
      next: () => this.loadMyProducts(),
      error: () => alert('Error al eliminar')
    });
  }

  onEdit(p: ProductModel) {
    this.router.navigate(['/products/edit', p.id]);
  }
}
