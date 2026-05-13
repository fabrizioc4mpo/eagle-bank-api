import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

type AddressDto = {
  line1?: string;
  line2?: string;
  line3?: string;
  town?: string;
  county?: string;
  postcode?: string;
} | null;

type UserResponse = {
  id: string;
  name: string;
  email: string;
  phoneNumber: string;
  address: AddressDto;
  createdTimestamp?: string;
  updatedTimestamp?: string;
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
  })
export class DashboardComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  user = signal<UserResponse | null>(null);
  editing = signal(false);
  saving = signal(false);
  form: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', [Validators.required]],
    password: [''], // optional
    line1: [''],
    line2: [''],
    line3: [''],
    town: [''],
    county: [''],
    postcode: ['']
  });

  ngOnInit(): void {
    const token = this.getToken();
    if (!token) {
      this.router.navigateByUrl('/login');
      return;
    }

    const userId = this.extractUserId(token);
    if (!userId) {
      this.router.navigateByUrl('/login');
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.http.get<UserResponse>(`/v1/users/${userId}`, { headers }).subscribe({
      next: (u) => {
        this.user.set(u);
        this.patchForm(u);
      },
      error: () => this.router.navigateByUrl('/login')
    });
  }

  private getToken(): string | null {
    try {
      return localStorage.getItem('auth_token');
    } catch {
      return null;
    }
  }

  private extractUserId(token: string): string | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = JSON.parse(atob(parts[1]));
      // Backend uses subject (sub) as userId
      return payload?.sub ?? null;
    } catch {
      return null;
    }
  }

  protected formatAddress(addr: AddressDto): string {
    if (!addr) return '';
    const bits = [addr.line1, addr.line2, addr.line3, addr.town, addr.county, addr.postcode]
      .filter(Boolean);
    return bits.join(', ');
  }

  private patchForm(u: UserResponse) {
    this.form.patchValue({
      name: u.name,
      email: u.email,
      phoneNumber: u.phoneNumber,
      line1: u.address?.line1 ?? '',
      line2: u.address?.line2 ?? '',
      line3: u.address?.line3 ?? '',
      town: u.address?.town ?? '',
      county: u.address?.county ?? '',
      postcode: u.address?.postcode ?? ''
    });
  }

  startEdit(): void { this.editing.set(true); }
  cancelEdit(): void {
    const u = this.user();
    if (u) this.patchForm(u);
    this.editing.set(false);
  }

  save(): void {
    const u = this.user();
    if (!u) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // Build PATCH body only with provided fields
    const v = this.form.getRawValue();
    const body: any = {
      name: v.name,
      email: v.email,
      phoneNumber: v.phoneNumber
    };
    if (v.password && v.password.trim().length > 0) {
      body.password = v.password;
    }
    const anyAddressFields = [v.line1, v.line2, v.line3, v.town, v.county, v.postcode]
      .some((x) => x && String(x).trim().length > 0);
    if (anyAddressFields) {
      body.address = {
        line1: v.line1 || undefined,
        line2: v.line2 || undefined,
        line3: v.line3 || undefined,
        town: v.town || undefined,
        county: v.county || undefined,
        postcode: v.postcode || undefined
      };
    } else {
      // Explicitly set address to null to clear? We'll omit to keep unchanged
    }

    const token = this.getToken();
    if (!token) {
      this.router.navigateByUrl('/login');
      return;
    }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.saving.set(true);
    this.http.patch<UserResponse>(`/v1/users/${u.id}`, body, { headers }).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.patchForm(updated);
        this.editing.set(false);
        this.saving.set(false);
        alert('Profile updated successfully');
      },
      error: (err) => {
        this.saving.set(false);
        const msg = err?.error?.message || 'Failed to update profile';
        alert(msg);
      }
    });
  }

  onDeleteAccount(): void {
    const u = this.user();
    if (!u) return;
    const confirmed = confirm('Are you sure you want to delete your account? This action cannot be undone.');
    if (!confirmed) return;

    const token = this.getToken();
    if (!token) {
      this.router.navigateByUrl('/login');
      return;
    }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.http.delete(`/v1/users/${u.id}`, { headers }).subscribe({
      next: () => {
        try { localStorage.removeItem('auth_token'); } catch {}
        alert('Your account has been deleted.');
        this.router.navigateByUrl('/signup');
      },
      error: (err) => {
        const msg = err?.error?.message || 'Failed to delete account';
        alert(msg);
      }
    });
  }
}
