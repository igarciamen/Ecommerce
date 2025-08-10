import { OrderItemDto } from './order-item-dto';

export interface OrderDto {
  id: number;                 // ← era orderId
  userId: number;
  createdAt: string;          // LocalDateTime como string
  items: OrderItemDto[];
  totalAmount: number;
  user?: any;                 // Enriquecido por backend (opcional)
}
