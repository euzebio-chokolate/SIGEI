import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '@environments/environment';

import { RelatorioService } from './relatorio.service';

describe('RelatorioService', () => {
  let service: RelatorioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(RelatorioService);
    http = TestBed.inject(HttpTestingController);
    // O download é simulado: jsdom não salva arquivos
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:teste');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);
  });

  afterEach(() => {
    http.verify();
    vi.restoreAllMocks();
  });

  it('envia o formato e só os filtros preenchidos, e devolve o nome do arquivo', () => {
    let nome = '';
    service.baixar('escolas', 'pdf', { ano: 2026, municipioId: null }).subscribe(valor => (nome = valor));

    const requisicao = http.expectOne(r => r.url === `${environment.apiUrl}/relatorios/escolas`);
    expect(requisicao.request.params.keys().sort()).toEqual(['ano', 'formato']);
    expect(requisicao.request.params.get('formato')).toBe('pdf');
    expect(requisicao.request.responseType).toBe('blob');
    requisicao.flush(new Blob(['%PDF']), {
      headers: { 'Content-Disposition': 'attachment; filename="sigei-escolas-2026.pdf"' }
    });

    expect(nome).toBe('sigei-escolas-2026.pdf');
  });
});
