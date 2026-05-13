import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-accounts-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule
  ],
  templateUrl: './account-create.component.html',
  styleUrls: ['./account-create.component.scss']
})
export class AccountsCreateComponent {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  submitting = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  form = this.fb.group({
    name: ['', [Validators.required]],
    accountType: ['personal', [Validators.required]]
  });

  private getToken(): string | null {
    try {
      return localStorage.getItem('auth_token');
    } catch {
      return null;
    }
  }

  onSubmit(): void {
    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const token = this.getToken();
    if (!token) {
      this.router.navigateByUrl('/login');
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    const body = this.form.getRawValue();

    this.submitting.set(true);
    this.http.post('/v1/accounts', body, { headers }).subscribe({
      next: () => {
        this.successMessage.set('Account created successfully');
        this.submitting.set(false);
        this.router.navigateByUrl('/accounts');
      },
      error: (err) => {
        const msg = err?.error?.message || 'Failed to create account';
        this.errorMessage.set(msg);
        this.submitting.set(false);
        if (err?.status === 401) {
          this.router.navigateByUrl('/login');
        }
      }
    });
  }
}
