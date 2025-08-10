import { ProductModel } from './product-model';

export interface CartItem {
  id: number;            // en remoto viene el id del CartItem; invitado -> 0
  productId: number;
  quantity: number;
  product?: ProductModel; // viene del backend de carts; invitado lo rellenamos nosotros
}
