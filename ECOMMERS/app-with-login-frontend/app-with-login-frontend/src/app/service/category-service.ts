// src/app/service/category.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { CategoryModel } from '../model/category-model';



@Injectable({ providedIn: 'root' })
export class CategoryService {
  private baseUrl = 'http://localhost:8081/api/categories';
  private _refreshNeeded$ = new Subject<void>();
  get refreshNeeded$() { return this._refreshNeeded$.asObservable(); }

  constructor(private http: HttpClient) {}

  list(): Observable<CategoryModel[]> {
    return this.http.get<CategoryModel[]>(this.baseUrl);
  }

  create(cat: Partial<CategoryModel>): Observable<CategoryModel> {
    return this.http.post<CategoryModel>(this.baseUrl, cat)
      .pipe(
        tap(() => this._refreshNeeded$.next())
      );
  }
}
