// src/app/components/product-create-component/product-create-component.ts

import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { ProductService } from '../../service/product-service';
import { CategoryService } from '../../service/category-service';
import { AuthService } from '../../service/auth-service';
import { CategoryModel } from '../../model/category-model';

@Component({
  selector: 'app-product-create-component',
  standalone: true,
  imports: [ CommonModule, ReactiveFormsModule ],
  templateUrl: './product-create-component.html',
  styleUrls: ['./product-create-component.css']
})
export class ProductCreateComponent implements OnInit {
  form = new FormGroup({
    name: new FormControl('', [Validators.required]),
    description: new FormControl(''),
    price: new FormControl(0, [Validators.required, Validators.min(0)]),
    stock: new FormControl(0, [Validators.required, Validators.min(0)]),
    categoryId: new FormControl<number | null>(null, [Validators.required]),
    image: new FormControl<File | null>(null)
  });

  categories: CategoryModel[] = [];
  submitting = false;
  error?: string;

  constructor(
    private productSvc: ProductService,
    private categorySvc: CategoryService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.auth.isAuthenticated()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/products/new' } });
      return;
    }

    this.categorySvc.list().subscribe({
      next: cats => this.categories = cats,
      error: () => this.error = 'No se pudieron cargar las categorías'
    });
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.form.patchValue({ image: input.files[0] });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.submitting = true;
    const { name, description, price, stock, categoryId, image } = this.form.value;

    this.productSvc.create({
      name: name!,
      description: description || undefined,
      price: price!,
      stock: stock!,
      categoryId: categoryId!,
      imageFile: image || undefined
    }).subscribe({
      next: () => this.router.navigateByUrl('/products'),
      error: err => {
        this.error = err.error?.message || err.message || 'Error al crear producto';
        this.submitting = false;
      }
    });
  }
}
