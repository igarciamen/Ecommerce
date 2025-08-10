import { Component } from '@angular/core';
import { OrderDto } from '../../model/order-dto';
import { OrderService } from '../../service/order-service';
import { AuthService } from '../../service/auth-service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { switchMap } from 'rxjs';
import { ProductService } from '../../service/product-service';

@Component({
  selector: 'app-order-list-component',
    standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './order-list-component.html',
  styleUrl: './order-list-component.css'
})
export class OrderListComponent {
orders: OrderDto[] = [];
  loading = false;
  error?: string;

  constructor(
    private orderSvc: OrderService,
    private auth: AuthService,
    private productSvc: ProductService
  ) {}

  ngOnInit(): void { this.loadOrders(); }

  private loadOrders(): void {
    this.loading = true;
    this.error = undefined;

    const userId = this.auth.getUserId();
    if (!userId) { this.loading = false; this.orders = []; return; }

    this.orderSvc.getByUser(userId).subscribe({
      next: (list: OrderDto[]) => { this.orders = list; this.loading = false; },
      error: (err: HttpErrorResponse) => {
        this.orders = [];
        this.error = err.error?.message || err.message || 'Error cargando pedidos';
        this.loading = false;
      }
    });
  }

  delete(o: OrderDto): void {
    if (!confirm(`¿Borrar pedido #${o.id}?`)) return;
    const ids = Array.from(new Set(o.items.map(i => i.productId)));
    this.orderSvc.delete(o.id).pipe(
      switchMap(() => this.productSvc.refreshStocks(ids))
    ).subscribe({
      next: () => { this.orders = this.orders.filter(x => x.id !== o.id); },
      error: (err: HttpErrorResponse) => {
        this.error = err.error?.message || err.message || 'Error borrando pedido';
      }
    });
  }
}
