import { Component } from '@angular/core';
import { CategoryService } from '../../service/category-service';
import { ProductService } from '../../service/product-service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CategoryModel } from '../../model/category-model';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-product-edit.component',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './product-edit.component.html',
  styleUrl: './product-edit.component.css'
})
export class ProductEditComponent {
 form = new FormGroup({
    name: new FormControl('', [Validators.required]),
    description: new FormControl(''),
    price: new FormControl(0, [Validators.required, Validators.min(0)]),
    stock: new FormControl(0, [Validators.required, Validators.min(0)]),
    categoryId: new FormControl<number | null>(null, [Validators.required]),
    image: new FormControl<File | null>(null)
  });

  categories: CategoryModel[] = [];
  loading = false;
  error?: string;
  private productId!: number;
  public existingImageUrl?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productSvc: ProductService,
    private categorySvc: CategoryService
  ) {}

  ngOnInit(): void {
    // 1) Obtener ID
    this.productId = Number(this.route.snapshot.paramMap.get('id'));
    if (isNaN(this.productId)) {
      this.error = 'ID de producto inválido';
      return;
    }

    // 2) Cargar categorías
    this.categorySvc.list().subscribe({
      next: cats => this.categories = cats,
      error: () => this.error = 'No se pudieron cargar categorías'
    });

    // 3) Cargar producto
    this.loading = true;
    this.productSvc.getProductById(this.productId).subscribe({
      next: p => {
        this.existingImageUrl = p.imageUrl;
        this.form.patchValue({
          name: p.name,
          description: p.description,
          price: p.price,
          stock: p.stock,
          categoryId: p.categoryId
        });
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message || 'Error cargando producto';
        this.loading = false;
      }
    });
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.form.patchValue({ image: input.files[0] });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const { name, description, price, stock, categoryId, image } = this.form.value;
    this.productSvc.update({
      id: this.productId,
      name: name!,
      description: description || undefined,
      price: price!,
      stock: stock!,
      categoryId: categoryId!,
      imageFile: image || undefined
    }).subscribe({
      next: () => this.router.navigateByUrl('/products/manage'),
      error: err => {
        this.error = err.error?.message || 'Error actualizando producto';
        this.loading = false;
      }
    });
  }
}
