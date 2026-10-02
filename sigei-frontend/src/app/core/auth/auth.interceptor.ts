import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, of, switchMap, throwError } from 'rxjs';

import { AuthService } from './auth.service';

const ROTAS_DE_SESSAO = /\/auth\/(login|refresh|logout)(?:\?|$)/;

const naoAutorizado = (erro: unknown): boolean =>
  erro instanceof HttpErrorResponse && erro.status === 401;

/**
 * A sessão trafega em cookies HttpOnly enviados automaticamente pelo navegador,
 * e o token CSRF é anexado pelo suporte nativo do HttpClient (cookie XSRF-TOKEN
 * → cabeçalho X-XSRF-TOKEN). Este interceptador só trata a expiração: diante
 * de um 401, renova a sessão e repete a requisição uma única vez.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (ROTAS_DE_SESSAO.test(req.url)) {
    return next(req);
  }

  const authService = inject(AuthService);
  const router = inject(Router);

  const encerrarSessao = () => {
    authService.limparSessao();
    router.navigate(['/login']);
  };

  return next(req).pipe(
    catchError((erro: unknown) => {
      if (!naoAutorizado(erro) || !authService.estaLogado()) {
        return throwError(() => erro);
      }

      return authService.renovarSessao().pipe(
        // Renovação recusada: outra aba pode ter renovado com o mesmo cookie e o
        // navegador já ter os cookies novos, então a requisição é repetida assim mesmo
        catchError((falha: unknown) => naoAutorizado(falha) ? of(undefined) : throwError(() => falha)),
        switchMap(() => next(req)),
        catchError((ultima: unknown) => {
          if (naoAutorizado(ultima)) encerrarSessao();
          return throwError(() => ultima);
        })
      );
    })
  );
};
