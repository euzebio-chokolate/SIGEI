import { Routes } from '@angular/router';
import { authenticatedGuard } from '@core/auth/auth.guard';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then(module => module.LoginComponent),
    title: 'SIGEI - Login'
  },
  {
    path: 'alterar-senha',
    canActivate: [authenticatedGuard],
    loadComponent: () =>
      import('./pages/alterar-senha/alterar-senha.component').then(
        module => module.AlterarSenhaComponent
      ),
    title: 'SIGEI - Alterar senha'
  }
];
