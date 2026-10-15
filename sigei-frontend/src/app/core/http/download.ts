import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { MonoTypeOperatorFunction, catchError, from, switchMap, throwError } from 'rxjs';

/**
 * Em requisições com `responseType: 'blob'`, o corpo de erro (ProblemDetail em JSON)
 * também chega como Blob. Este operador o converte de volta em objeto, para que
 * `interpretarErro` encontre a mensagem da API.
 */
export function lerErroDoDownload<T>(): MonoTypeOperatorFunction<T> {
  return catchError((erro: unknown) => {
    if (!(erro instanceof HttpErrorResponse) || !(erro.error instanceof Blob)) {
      return throwError(() => erro);
    }
    return from(erro.error.text()).pipe(
      switchMap(texto => {
        let corpo: unknown = null;
        try {
          corpo = JSON.parse(texto);
        } catch {
          // Corpo que não é JSON (proxy, página de erro): fica sem a mensagem da API
        }
        return throwError(() => new HttpErrorResponse({
          error: corpo,
          headers: erro.headers,
          status: erro.status,
          statusText: erro.statusText,
          url: erro.url ?? undefined
        }));
      })
    );
  });
}

/** Oferece o conteúdo ao navegador como arquivo para salvar. */
export function salvarArquivo(conteudo: Blob, nome: string): void {
  const url = URL.createObjectURL(conteudo);
  const link = document.createElement('a');
  link.href = url;
  link.download = nome;
  link.click();
  URL.revokeObjectURL(url);
}

/** Nome do arquivo no cabeçalho Content-Disposition, ou o padrão quando ausente. */
export function nomeDoArquivo(resposta: HttpResponse<Blob>, padrao: string): string {
  const disposicao = resposta.headers.get('Content-Disposition') ?? '';
  const nome = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposicao)?.[1];
  return nome ? decodeURIComponent(nome) : padrao;
}
