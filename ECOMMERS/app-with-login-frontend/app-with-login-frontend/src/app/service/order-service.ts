import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderDto } from '../model/order-dto';

const BASE = 'http://localhost:8084/api/orders';

@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private http: HttpClient) {}

  /** POST /api/orders — SOLO { productId, quantity } */
  create(payload: { userId: number; items: Array<{ productId: number; quantity: number }> }): Observable<OrderDto> {
    const minimal = {
      userId: payload.userId,
      items: payload.items.map(i => ({ productId: i.productId, quantity: i.quantity }))
    };
    return this.http.post<OrderDto>(BASE, minimal);
  }

  /** GET /api/orders/user/{userId} */
  getByUser(userId: number): Observable<OrderDto[]> {
    return this.http.get<OrderDto[]>(`${BASE}/user/${userId}`);
  }

  /** GET /api/orders/{id} */
  getById(id: number): Observable<OrderDto> {
    return this.http.get<OrderDto>(`${BASE}/${id}`);
  }

  /** DELETE /api/orders/{id} */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/${id}`);
  }
}
