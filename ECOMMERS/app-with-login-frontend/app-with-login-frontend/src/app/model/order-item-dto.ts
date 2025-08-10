import { ProductModel } from './product-model';

export interface OrderItemDto {
  productId: number;
  quantity: number;

  // Campos que llegan en la respuesta (los mantiene tu backend):
  productName?: string;
  unitPrice?: number;

  // Enriquecido por backend (producto completo con seller/category):
  product?: ProductModel;
}
