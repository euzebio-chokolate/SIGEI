import { Routes } from '@angular/router';
import { authGuard, fallbackGuard } from '@core/auth/auth.guard';
import { AUTH_ROUTES } from '@features/auth/auth.routes';
import { MAPA_PUBLICO_ROUTES } from '@features/mapa-publico/mapa-publico.routes';

export const routes: Routes = [
  ...MAPA_PUBLICO_ROUTES,
  ...AUTH_ROUTES,
  {
    path: 'admin',
    canActivate: [authGuard],
    loadChildren: () =>
      import('@features/admin/admin.routes').then(module => module.ADMIN_ROUTES)
  },
  {
    path: '**',
    canActivate: [fallbackGuard],
    loadComponent: () =>
      import('@features/auth/pages/login/login.component').then(module => module.LoginComponent)
  }
];
