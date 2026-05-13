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
  selector: 'app-signup',
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
  templateUrl: './signup.component.html'
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  submitting = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  form = this.fb.group({
    name: ['', [Validators.required]],
    line1: ['', [Validators.required]],
    line2: [''],
    line3: [''],
    town: ['', [Validators.required]],
    county: ['', [Validators.required]],
    postcode: ['', [Validators.required]],
    phoneNumber: ['', [Validators.required, Validators.pattern(/^[+][1-9]\d{1,14}$/)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit() {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const payload = {
      name: v.name!,
      address: {
        line1: v.line1!,
        line2: v.line2 || undefined,
        line3: v.line3 || undefined,
        town: v.town!,
        county: v.county!,
        postcode: v.postcode!
      },
      phoneNumber: v.phoneNumber!,
      email: v.email!,
      password: v.password!
    };

    this.submitting.set(true);
    this.http.post('/v1/users', payload).subscribe({
      next: () => {
        // Immediately log in to get a token
        this.http.post<{ token: string }>(`/v1/auth/login`, { email: v.email, password: v.password })
          .subscribe({
            next: (res) => {
              try { localStorage.setItem('auth_token', res.token); } catch {}
              this.successMessage.set('Account created and logged in');
              this.submitting.set(false);
              this.router.navigateByUrl('/');
            },
            error: (err) => {
              const msg = err?.error?.message || 'Account created, but auto-login failed. Please log in.';
              this.errorMessage.set(msg);
              this.submitting.set(false);
              // Optionally navigate to login
              // this.router.navigateByUrl('/login');
            }
          });
      },
      error: (err) => {
        const msg = err?.error?.message || 'Sign up failed';
        this.errorMessage.set(msg);
        this.submitting.set(false);
      }
    });
  }
}
