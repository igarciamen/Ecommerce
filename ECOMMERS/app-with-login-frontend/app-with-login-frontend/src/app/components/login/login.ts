// src/app/components/login/login.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../../service/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.html'
})
export class LoginComponent {
  loginForm: FormGroup;
  returnUrl = '/products';
  errorMessage = '';
  loading = false;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      login: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    const rq = this.route.snapshot.queryParams['returnUrl'];
    if (rq) this.returnUrl = rq;
  }

  get login()    { return this.loginForm.get('login'); }
  get password() { return this.loginForm.get('password'); }

  onSubmit() {
    this.errorMessage = '';
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const { login, password } = this.loginForm.value;

    this.auth.login(login, password)
      .pipe(
        finalize(() => this.loading = false)
      )
      .subscribe({
        next: () => this.router.navigateByUrl(this.returnUrl),
        error: err => {
          if (err.status === 401) {
            this.errorMessage = 'Usuario o contraseña incorrectos';
          } else if (err.error?.message) {
            this.errorMessage = err.error.message;
          } else {
            this.errorMessage = 'Error de conexión, inténtalo más tarde';
          }
        }
      });
  }
}
