export interface ProductModel {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  categoryId: number;
  sellerId: number;
  imageUrl?: string;
  createdAt: string;

  // Enriquecidos por Products MS:
  seller?: {
    id: number;
    username: string;
    email?: string;
    roles?: string[];
  };
  category?: {
    id: number;
    name: string;
  };
}
