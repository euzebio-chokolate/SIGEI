import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { ToastService } from '@core/notificacao/toast.service';
import { AVISO_GLOBAL_DE_ERRO, ehErroGlobal, interpretarErro } from './erro-http';

/** Rotas de sessão têm o erro tratado pela própria tela (login) ou pelo authInterceptor. */
const ROTAS_DE_SESSAO = /\/auth\/(login|refresh|logout)(?:\?|$)/;

/**
 * Avisa o usuário das falhas que não dependem da tela: servidor fora do ar,
 * erro interno e falta de permissão. Os erros de negócio e de validação
 * (400, 404, 409) seguem para o componente, que os mostra junto ao formulário.
 *
 * Deve ficar antes do authInterceptor para enxergar só o resultado final,
 * depois da eventual renovação da sessão.
 */
export const erroInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((erro: unknown) => {
      if (
        erro instanceof HttpErrorResponse &&
        ehErroGlobal(erro.status) &&
        req.context.get(AVISO_GLOBAL_DE_ERRO) &&
        !ROTAS_DE_SESSAO.test(req.url)
      ) {
        toast.erro(interpretarErro(erro).mensagem);
      }

      return throwError(() => erro);
    })
  );
};
