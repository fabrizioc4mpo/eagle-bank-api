import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

type TransactionResponse = {
  id: string;
  type: 'deposit' | 'withdrawal' | string;
  amount: number;
  currency: string; // GBP
  reference?: string;
  userId: string;
  createdTimestamp: string;
};

type ListTransactionsResponse = { transactions: TransactionResponse[] };

type BankAccountResponse = {
  accountNumber: string;
  sortCode: string;
  name: string;
  accountType: string;
  balance: number;
  currency: string; // GBP
  createdTimestamp: string;
  updatedTimestamp: string;
  userId?: string;
};

@Component({
  selector: 'app-account-transactions',
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
  templateUrl: './account-transactions.component.html',
  styleUrls: ['./account-transactions.component.scss']
})
export class AccountTransactionsComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  loading = signal(true);
  error = signal<string | null>(null);
  transactions = signal<TransactionResponse[] | null>(null);
  accountNumber: string | null = null;
  account = signal<BankAccountResponse | null>(null);

  depositing = signal(false);
  withdrawing = signal(false);

  depositForm = this.fb.nonNullable.group({
    amount: this.fb.nonNullable.control<number | null>(null, { validators: [Validators.required, Validators.min(0), Validators.max(10000)] }),
    reference: this.fb.nonNullable.control<string>('')
  });
  withdrawForm = this.fb.nonNullable.group({
    amount: this.fb.nonNullable.control<number | null>(null, { validators: [Validators.required, Validators.min(0), Validators.max(10000)] }),
    reference: this.fb.nonNullable.control<string>('')
  });

  ngOnInit(): void {
    const token = this.getToken();
    if (!token) {
      this.router.navigateByUrl('/login');
      return;
    }

    this.accountNumber = this.route.snapshot.paramMap.get('accountNumber');
    if (!this.accountNumber) {
      this.error.set('Missing account number');
      this.loading.set(false);
      return;
    }

    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    // Load account for balance
    this.http.get<BankAccountResponse>(`/v1/accounts/${this.accountNumber}`, { headers }).subscribe({
      next: (acc) => this.account.set(acc),
      error: () => {}
    });

    // Load transactions list (wrapped)
    this.http.get<ListTransactionsResponse>(`/v1/accounts/${this.accountNumber}/transactions`, { headers }).subscribe({
      next: (res) => {
        this.transactions.set(res?.transactions || []);
        this.loading.set(false);
      },
      error: (err) => {
        if (err?.status === 401) {
          this.router.navigateByUrl('/login');
          return;
        }
        const msg = err?.error?.message || 'Failed to load transactions';
        this.error.set(msg);
        this.loading.set(false);
      }
    });
  }

  submitDeposit(): void {
    if (!this.accountNumber || this.depositForm.invalid) return;
    const token = this.getToken();
    if (!token) { this.router.navigateByUrl('/login'); return; }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    const payload = {
      amount: Number(this.depositForm.value.amount || 0),
      currency: 'GBP',
      type: 'deposit',
      reference: this.depositForm.value.reference || undefined
    } as const;
    this.depositing.set(true);
    this.http.post<TransactionResponse>(`/v1/accounts/${this.accountNumber}/transactions`, payload, { headers }).subscribe({
      next: () => {
        this.depositing.set(false);
        this.depositForm.reset();
        this.refreshData();
      },
      error: (err) => {
        this.depositing.set(false);
        const msg = err?.error?.message || 'Failed to deposit';
        this.error.set(msg);
      }
    });
  }

  submitWithdraw(): void {
    if (!this.accountNumber || this.withdrawForm.invalid) return;
    const token = this.getToken();
    if (!token) { this.router.navigateByUrl('/login'); return; }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    const payload = {
      amount: Number(this.withdrawForm.value.amount || 0),
      currency: 'GBP',
      type: 'withdrawal',
      reference: this.withdrawForm.value.reference || undefined
    } as const;
    this.withdrawing.set(true);
    this.http.post<TransactionResponse>(`/v1/accounts/${this.accountNumber}/transactions`, payload, { headers }).subscribe({
      next: () => {
        this.withdrawing.set(false);
        this.withdrawForm.reset();
        this.refreshData();
      },
      error: (err) => {
        this.withdrawing.set(false);
        const msg = err?.error?.message || 'Failed to withdraw';
        this.error.set(msg);
      }
    });
  }

  private refreshData(): void {
    if (!this.accountNumber) return;
    const token = this.getToken();
    if (!token) { this.router.navigateByUrl('/login'); return; }
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.loading.set(true);
    // refresh account and transactions
    this.http.get<BankAccountResponse>(`/v1/accounts/${this.accountNumber}`, { headers }).subscribe({
      next: (acc) => this.account.set(acc),
      error: () => {}
    });
    this.http.get<ListTransactionsResponse>(`/v1/accounts/${this.accountNumber}/transactions`, { headers }).subscribe({
      next: (res) => {
        this.transactions.set(res?.transactions || []);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  private getToken(): string | null {
    try { return localStorage.getItem('auth_token'); } catch { return null; }
  }
}
