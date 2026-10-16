import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '@environments/environment';

import { AnoReferencia, AnoReferenciaService } from './ano-referencia.service';

const URL = `${environment.apiUrl}/anos-referencia`;
const ANOS: AnoReferencia[] = [{ id: 1, ano: 2025 }, { id: 2, ano: 2026 }] as AnoReferencia[];

describe('AnoReferenciaService (cache)', () => {
  let service: AnoReferenciaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AnoReferenciaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('busca os anos uma única vez para várias telas', () => {
    service.listar().subscribe();
    service.listar().subscribe();
    http.expectOne(URL).flush(ANOS);
    service.listar().subscribe(anos => expect(anos.length).toBe(2));
    http.expectNone(URL);
  });

  it('entrega cópias: ordenar em uma tela não altera a lista das outras', () => {
    let primeira: AnoReferencia[] = [];
    service.listar().subscribe(anos => (primeira = anos));
    http.expectOne(URL).flush(ANOS);
    primeira.reverse();
    service.listar().subscribe(anos => expect(anos[0].ano).toBe(2025));
  });

  it('volta a buscar depois de salvar um ano', () => {
    service.listar().subscribe();
    http.expectOne(URL).flush(ANOS);
    service.salvar(2027).subscribe();
    http.expectOne(URL).flush({ id: 3, ano: 2027 });
    service.listar().subscribe();
    http.expectOne(URL).flush([...ANOS, { id: 3, ano: 2027 }]);
  });

  it('volta a buscar depois de iniciar o novo ano', () => {
    service.listar().subscribe();
    http.expectOne(URL).flush(ANOS);
    service.iniciarProximo().subscribe();
    http.expectOne(`${URL}/proximo`).flush({ id: 3, ano: 2027 });
    service.listar().subscribe();
    http.expectOne(URL).flush(ANOS);
  });

  it('não guarda a falha: a próxima chamada tenta de novo', () => {
    service.listar().subscribe({ error: () => undefined });
    http.expectOne(URL).flush('erro', { status: 500, statusText: 'Server Error' });
    service.listar().subscribe();
    http.expectOne(URL).flush(ANOS);
  });
});
