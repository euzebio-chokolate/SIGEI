import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { nomeDoArquivo } from './download';

describe('download', () => {
  const resposta = (disposicao?: string) =>
    new HttpResponse<Blob>({
      body: new Blob(),
      headers: disposicao ? new HttpHeaders({ 'Content-Disposition': disposicao }) : new HttpHeaders()
    });

  it('lê o nome do arquivo do cabeçalho Content-Disposition', () => {
    expect(nomeDoArquivo(resposta('attachment; filename="sigei-painel-2026.pdf"'), 'x.pdf')).toBe('sigei-painel-2026.pdf');
    expect(nomeDoArquivo(resposta("attachment; filename*=UTF-8''sigei-escolas-2026-feij%C3%B3.xlsx"), 'x.xlsx'))
      .toBe('sigei-escolas-2026-feijó.xlsx');
  });

  it('usa o nome padrão sem o cabeçalho', () => {
    expect(nomeDoArquivo(resposta(), 'relatorio.pdf')).toBe('relatorio.pdf');
  });
});
