// src/app/service/product-search.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProductSearchService {
  private term$ = new BehaviorSubject<string>('');
  /** Observable al que se subscriben los componentes */
  searchTerm$ = this.term$.asObservable();

  /** Llamar desde el input de la navbar */
  updateTerm(term: string) {
    this.term$.next(term);
  }
}
