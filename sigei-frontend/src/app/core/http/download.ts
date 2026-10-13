import { HttpResponse } from '@angular/common/http';

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
