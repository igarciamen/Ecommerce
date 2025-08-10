import { Component } from '@angular/core';
import { CategoryModel } from '../../model/category-model';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CategoryService } from '../../service/category-service';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-category-create-component',
  imports: [ CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './category-create-component.html',
  styleUrl: './category-create-component.css'
})
export class CategoryCreateComponent {
  form = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.minLength(2)])
  });
  submitting = false;
  error?: string;

  constructor(
    private catSvc: CategoryService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting = true;
    const payload: Partial<CategoryModel> = { name: this.form.value.name! };
    this.catSvc.create(payload).subscribe({
      next: () => this.router.navigateByUrl('/categories'),
      error: err => {
        this.error = err?.error || 'Error al crear categoría';
        this.submitting = false;
      }
    });
  }
}
