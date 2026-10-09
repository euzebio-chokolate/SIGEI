import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '@environments/environment';

import { AuditoriaService } from './auditoria.service';

describe('AuditoriaService', () => {
  let service: AuditoriaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuditoriaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia só os filtros preenchidos, com a paginação', () => {
    service.buscar({ entidade: 'Escola', operacao: null, usuarioId: 7, sistema: false, de: '2026-10-01', ate: '' }, 2, 20).subscribe();

    const requisicao = http.expectOne(r => r.url === `${environment.apiUrl}/auditoria`);
    const params = requisicao.request.params;
    expect(params.keys().sort()).toEqual(['de', 'entidade', 'pagina', 'tamanho', 'usuarioId']);
    expect(params.get('entidade')).toBe('Escola');
    expect(params.get('usuarioId')).toBe('7');
    expect(params.get('pagina')).toBe('2');
    requisicao.flush({ alteracoes: [], pagina: 2, tamanho: 20, totalAlteracoes: 0, totalPaginas: 0 });
  });

  it('envia o filtro de alterações do sistema', () => {
    service.buscar({ sistema: true }, 0, 20).subscribe();
    const requisicao = http.expectOne(r => r.url === `${environment.apiUrl}/auditoria`);
    expect(requisicao.request.params.get('sistema')).toBe('true');
    requisicao.flush({ alteracoes: [], pagina: 0, tamanho: 20, totalAlteracoes: 0, totalPaginas: 0 });
  });
});
