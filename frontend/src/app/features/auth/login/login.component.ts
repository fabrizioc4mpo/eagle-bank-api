import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  submitting = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  onSubmit() {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const { email, password } = this.form.getRawValue();
    this.http.post<{ token: string }>(`/v1/auth/login`, { email, password })
      .subscribe({
        next: (res) => {
          // Store token for subsequent requests
          try {
            localStorage.setItem('auth_token', res.token);
          } catch {}
          this.successMessage.set('Logged in successfully');
          this.submitting.set(false);
          // Decode JWT to extract userId (sub) and navigate to profile route
          try {
            const payload = JSON.parse(atob(res.token.split('.')[1] || '')) as { sub?: string };
            const userId = payload?.sub;
            if (userId) {
              this.router.navigateByUrl(`/users/${userId}`);
            } else {
              this.router.navigateByUrl('/login');
            }
          } catch {
            this.router.navigateByUrl('/login');
          }
        },
        error: (err) => {
          const msg = err?.error?.message || 'Login failed';
          this.errorMessage.set(msg);
          this.submitting.set(false);
        }
      });
  }
}
