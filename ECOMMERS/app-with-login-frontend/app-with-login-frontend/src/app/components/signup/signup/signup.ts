// src/app/components/signup/signup.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../service/auth-service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './signup.html'
})
export class SignupComponent {
  signupForm: FormGroup;
  errorMessage = '';
  loading = false;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      username: ['', [Validators.required]],
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.matchPasswords('password', 'confirmPassword')
    });
  }

  private matchPasswords(passwordKey: string, confirmKey: string): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const pw = group.get(passwordKey)?.value;
      const cpw = group.get(confirmKey)?.value;
      return pw === cpw ? null : { passwordsMismatch: true };
    };
  }

  onSubmit() {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }
    this.errorMessage = '';
    this.loading = true;

    const { username, email, password, confirmPassword } = this.signupForm.value;
    this.auth.signup(username, email, password, confirmPassword)
      .subscribe({
        next: () => {
          // Ya está logueado y con userInfo cargado
          this.router.navigate(['/products']);
        },
        error: err => {
          this.loading = false;
          this.errorMessage = err.error?.message || 'Error en el registro';
        }
      });
  }

  get username() { return this.signupForm.get('username'); }
  get email()    { return this.signupForm.get('email'); }
  get password() { return this.signupForm.get('password'); }
  get confirmPassword() { return this.signupForm.get('confirmPassword'); }
}
