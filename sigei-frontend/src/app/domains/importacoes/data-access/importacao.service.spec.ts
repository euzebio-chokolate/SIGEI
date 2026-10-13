import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '@environments/environment';

import { ImportacaoService } from './importacao.service';

describe('ImportacaoService', () => {
  let service: ImportacaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ImportacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia a planilha no campo arquivo, com o ano como parâmetro', () => {
    const arquivo = new File(['x'], 'censo.xlsx');
    service.importar('matriculas', arquivo, 2027).subscribe();

    const requisicao = http.expectOne(r => r.url === `${environment.apiUrl}/importacoes/matriculas`);
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.params.get('ano')).toBe('2027');
    expect((requisicao.request.body as FormData).get('arquivo')).toBe(arquivo);
    requisicao.flush({ linhasLidas: 0, erros: [] });
  });

  it('baixa o modelo como arquivo', () => {
    service.baixarModelo('professores').subscribe();

    const requisicao = http.expectOne(`${environment.apiUrl}/importacoes/modelos/professores`);
    expect(requisicao.request.responseType).toBe('blob');
    requisicao.flush(new Blob());
  });
});
