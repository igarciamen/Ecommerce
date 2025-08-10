import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject, throwError, forkJoin, of } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { ProductModel } from '../model/product-model';
import { AuthService } from './auth-service';
import { PageResponse } from '../model/page-response';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private baseUrl = 'http://localhost:8082/api/products';
  private _refreshNeeded$ = new Subject<void>();
  get refreshNeeded$() { return this._refreshNeeded$.asObservable(); }

  private _stockUpdates$ = new Subject<ProductModel>();
  get stockUpdates$(): Observable<ProductModel> { return this._stockUpdates$.asObservable(); }

  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) {}

  list(page: number, size: number): Observable<PageResponse<ProductModel>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<ProductModel>>(this.baseUrl, { params });
  }

  create(product: {
    name: string;
    description?: string;
    price: number;
    stock: number;
    categoryId: number;
    imageFile?: File;
  }): Observable<ProductModel> {
    const sellerId = this.auth.getUserId();
    if (!sellerId) return throwError(() => new Error('Debes iniciar sesión para crear un producto'));

    const formData = new FormData();
    formData.append('name', product.name);
    if (product.description) formData.append('description', product.description);
    formData.append('price', product.price.toString());
    formData.append('stock', product.stock.toString());
    formData.append('categoryId', product.categoryId.toString());
    formData.append('sellerId', sellerId.toString());
    if (product.imageFile) formData.append('image', product.imageFile, product.imageFile.name);

    return this.http.post<ProductModel>(this.baseUrl, formData).pipe(tap(() => this._refreshNeeded$.next()));
  }

  search(term: string, page: number, size: number): Observable<PageResponse<ProductModel>> {
    const params = new HttpParams().set('q', term).set('page', page).set('size', size);
    return this.http.get<PageResponse<ProductModel>>(`${this.baseUrl}/search`, { params });
  }

  listByCategory(categoryId: number, page: number, size: number): Observable<PageResponse<ProductModel>> {
    const params = new HttpParams().set('categoryId', categoryId).set('page', page).set('size', size);
    return this.http.get<PageResponse<ProductModel>>(`${this.baseUrl}/by-category`, { params });
  }

  getProductById(id: number): Observable<ProductModel> {
    return this.http.get<ProductModel>(`${this.baseUrl}/${id}`);
  }

  delete(productId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${productId}`);
  }

  update(product: {
    id: number;
    name: string;
    description?: string;
    price: number;
    stock: number;
    categoryId: number;
    imageFile?: File;
  }): Observable<ProductModel> {
    const sellerId = this.auth.getUserId();
    const formData = new FormData();
    formData.append('name', product.name);
    if (product.description) formData.append('description', product.description);
    formData.append('price', product.price.toString());
    formData.append('stock', product.stock.toString());
    formData.append('categoryId', product.categoryId.toString());
    formData.append('sellerId', (sellerId ?? 0).toString());
    if (product.imageFile) formData.append('image', product.imageFile, product.imageFile.name);

    return this.http.put<ProductModel>(`${this.baseUrl}/${product.id}`, formData);
  }

  /** Emite el PRODUCTO completo actualizado para las vistas (stock, etc.) */
  refreshStocks(ids: number[]): Observable<void> {
    const unique = Array.from(new Set(ids));
    if (!unique.length) return of(void 0);
    return forkJoin(unique.map(id => this.getProductById(id))).pipe(
      tap(products => products.forEach(p => this._stockUpdates$.next(p))),
      map(() => void 0)
    );
  }
}
