import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
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

type ListBankAccountsResponse = {
  accounts: BankAccountResponse[];
};

@Component({
  selector: 'app-accounts-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatListModule, MatButtonModule, MatIconModule],
  templateUrl: './accounts-list.component.html',
  styleUrls: ['./accounts-list.component.scss']
})
export class AccountsListComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  loading = signal(true);
  error = signal<string | null>(null);
  accounts = signal<BankAccountResponse[]>([]);
  userId: string | null = null;

  ngOnInit(): void {
    const token = this.getToken();
    if (!token) {
      this.router.navigateByUrl('/login');
      return;
    }
    this.userId = this.extractUserId(token);

    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.http.get<ListBankAccountsResponse>('/v1/accounts', { headers }).subscribe({
      next: (res) => {
        this.accounts.set(res?.accounts ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        if (err?.status === 401) {
          this.router.navigateByUrl('/login');
          return;
        }
        const msg = err?.error?.message || 'Failed to load accounts';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  private getToken(): string | null {
    try { return localStorage.getItem('auth_token'); } catch { return null; }
  }

  private extractUserId(token: string): string | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = JSON.parse(atob(parts[1]));
      return payload?.sub ?? null;
    } catch {
      return null;
    }
  }
}
