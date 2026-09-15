import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.estaLogado()) {
    return authService.deveAlterarSenha()
      ? router.createUrlTree(['/alterar-senha'])
      : true;
  }

  return router.createUrlTree(['/login']);
};

export const authenticatedGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return authService.estaLogado() ? true : router.createUrlTree(['/login']);
};

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.estaLogado()) return router.createUrlTree(['/login']);
  if (authService.deveAlterarSenha()) return router.createUrlTree(['/alterar-senha']);
  return authService.ehAdmin() ? true : router.createUrlTree(['/admin/dashboard']);
};

export const fallbackGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.estaLogado()) return router.createUrlTree(['/login']);
  if (authService.deveAlterarSenha()) return router.createUrlTree(['/alterar-senha']);
  return router.createUrlTree(['/admin/dashboard']);
};
