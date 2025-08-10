import { Component } from '@angular/core';
import { OrderDto } from '../../model/order-dto';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OrderService } from '../../service/order-service';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductService } from '../../service/product-service';
import { AuthService } from '../../service/auth-service';
import { forkJoin, map, of, switchMap } from 'rxjs';
import { FormsModule, NgForm } from '@angular/forms';

@Component({
  selector: 'app-order-detail-component',
    standalone: true,
  imports: [ RouterLink, FormsModule,CommonModule],
  templateUrl: './order-detail-component.html',
  styleUrl: './order-detail-component.css'
})
export class OrderDetailComponent {
  order?: OrderDto;
  loading = false;
  error?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderSvc: OrderService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(id)) {
      this.error = 'ID de pedido inválido';
      return;
    }

    this.loading = true;
    this.orderSvc.getById(id).subscribe({
      next: (o: OrderDto) => { this.order = o; this.loading = false; },
      error: (err: HttpErrorResponse) => {
        this.error = err.error?.message || err.message || 'Error cargando pedido';
        this.loading = false;
      }
    });
  }

  get itemsSubtotal(): number {
    return this.order?.items?.reduce((acc, it) => acc + it.quantity * (it.unitPrice ?? 0), 0) ?? 0;
  }

  onPay(form: NgForm): void {
    if (form.invalid) { form.control.markAllAsTouched(); return; }
    this.loading = true;
    setTimeout(() => {
      this.loading = false;
      alert('Pago realizado correctamente ✅');
    }, 1000);
  }

  contactSeller(item: { productId: number }): void {
    // Si necesitas navegar a mensajes, aquí tienes sellerId desde el producto enriquecido:
    const sellerId = (this.order?.items.find(i => i.productId === item.productId)?.product as any)?.seller?.id;
    if (!sellerId) return;
    this.router.navigate(['/messages/start'], { queryParams: { sellerId, productId: item.productId } });
  }
}
