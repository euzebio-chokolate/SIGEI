import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('sigei_token');
  const requisicaoDeLogin = /\/auth\/login(?:\?|$)/.test(req.url);

  // Um token expirado salvo no navegador não pode bloquear uma nova tentativa
  // de autenticação. O endpoint de login deve sempre ser chamado sem Bearer.
  if (!token || requisicaoDeLogin) {
    return next(req);
  }

  const reqAutenticada = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(reqAutenticada);
};
