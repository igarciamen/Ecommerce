import { CartItem } from "./cart-item";


export interface Cart {
  id: number;
  userId: number;
  items: CartItem[];
}
