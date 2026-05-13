import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'signup',
    loadComponent: () => import('./features/auth/signup/signup.component').then(m => m.SignupComponent)
  },
  {
    path: 'accounts',
    loadComponent: () => import('./features/accounts/accounts-list/accounts-list.component').then(m => m.AccountsListComponent)
  },
  {
    path: 'accounts/new',
    loadComponent: () => import('./features/accounts/account-create/account-create.component').then(m => m.AccountsCreateComponent)
  },
  {
    path: 'accounts/:accountNumber',
    loadComponent: () => import('./features/accounts/account-detail/account-detail.component').then(m => m.AccountDetailComponent)
  },
  {
    path: 'accounts/:accountNumber/transactions',
    loadComponent: () => import('./features/accounts/account-transactions/account-transactions.component').then(m => m.AccountTransactionsComponent)
  },
  {
    path: 'users/:userId',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  { path: '**', redirectTo: 'login' }
];
