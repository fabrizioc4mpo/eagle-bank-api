import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

type BankAccountResponse = {
  accountNumber: string;
  sortCode: '10-10-10';
  name: string;
  accountType: 'personal';
  balance: number;
  currency: 'GBP';
  createdTimestamp: string;
  updatedTimestamp: string;
};

@Component({
  selector: 'app-account-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatListModule, MatButtonModule, MatIconModule],
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.scss']
})
export class AccountDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  loading = signal(true);
  error = signal<string | null>(null);
  account = signal<BankAccountResponse | null>(null);
  deleting = signal(false);

  ngOnInit(): void {
    const token = this.getToken();
    if (!token) {
      this.router.navigateByUrl('/login');
      return;
    }

    const accountNumber = this.route.snapshot.paramMap.get('accountNumber');
    if (!accountNumber) {
      this.error.set('Missing account number');
      this.loading.set(false);
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.http.get<BankAccountResponse>(`/v1/accounts/${accountNumber}`, { headers }).subscribe({
      next: (res) => {
        this.account.set(res);
        this.loading.set(false);
      },
      error: (err) => {
        if (err?.status === 401) {
          this.router.navigateByUrl('/login');
          return;
        }
        const msg = err?.error?.message || (err?.status === 404 ? 'Account not found' : 'Failed to load account');
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  private getToken(): string | null {
    try { return localStorage.getItem('auth_token'); } catch { return null; }
  }

  onDelete(): void {
    const acc = this.account();
    if (!acc) { return; }

    if (!confirm('Are you sure you want to delete this account? This action cannot be undone.')) {
      return;
    }

    const token = this.getToken();
    if (!token) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.deleting.set(true);
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.http.delete(`/v1/accounts/${acc.accountNumber}`, { headers }).subscribe({
      next: () => {
        this.deleting.set(false);
        this.router.navigateByUrl('/accounts');
      },
      error: (err) => {
        this.deleting.set(false);
        if (err?.status === 401) {
          this.router.navigateByUrl('/login');
          return;
        }
        const msg = err?.error?.message || 'Failed to delete account';
        this.error.set(msg);
      }
    });
  }
}
