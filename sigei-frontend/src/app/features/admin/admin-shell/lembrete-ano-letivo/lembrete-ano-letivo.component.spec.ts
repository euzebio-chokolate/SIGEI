import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { environment } from '@environments/environment';

import { AuthService } from '@core/auth/auth.service';
import { botao, texto } from '@testing/dom';
import { LembreteAnoLetivoComponent, anoAtualNoAcre } from './lembrete-ano-letivo.component';

const URL_ANOS = `${environment.apiUrl}/anos-referencia`;

describe('anoAtualNoAcre', () => {
  it('usa o fuso do Acre: a virada do ano chega 5 horas depois de UTC', () => {
    expect(anoAtualNoAcre(new Date('2027-01-01T03:00:00Z'))).toBe(2026);
    expect(anoAtualNoAcre(new Date('2027-01-01T05:00:00Z'))).toBe(2027);
  });
});

describe('LembreteAnoLetivoComponent', () => {
  let fixture: ComponentFixture<LembreteAnoLetivoComponent>;
  let raiz: HTMLElement;
  let http: HttpTestingController;
  const anoAtual = anoAtualNoAcre();

  async function renderizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function criar({ admin = true, preferencias = {} as object } = {}): Promise<void> {
    localStorage.setItem('sigei-preferencias', JSON.stringify(preferencias));
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]), provideHttpClient(), provideHttpClientTesting(),
        { provide: AuthService, useValue: { ehAdmin: () => admin } }
      ]
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(LembreteAnoLetivoComponent);
    raiz = fixture.nativeElement;
    await renderizar();
  }

  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  afterEach(() => http.verify());

  it('avisa quando o ano atual ainda não foi aberto', async () => {
    await criar();
    http.expectOne(URL_ANOS).flush([{ id: 1, ano: anoAtual - 2 }, { id: 2, ano: anoAtual - 1 }]);
    await renderizar();

    expect(texto(raiz)).toContain(`O ano letivo de ${anoAtual} ainda não foi aberto`);
    expect(texto(raiz)).toContain(`O ano mais recente no sistema é ${anoAtual - 1}.`);
  });

  it('não avisa quando o ano atual já existe', async () => {
    await criar();
    http.expectOne(URL_ANOS).flush([{ id: 2, ano: anoAtual }]);
    await renderizar();

    expect(texto(raiz)).toBe('');
  });

  it('com o lembrete desligado, nem consulta os anos', async () => {
    await criar({ preferencias: { avisarAtualizacaoAnual: false } });

    http.expectNone(URL_ANOS);
    expect(texto(raiz)).toBe('');
  });

  it('o administrador abre o ano seguinte depois de confirmar', async () => {
    await criar();
    http.expectOne(URL_ANOS).flush([{ id: 2, ano: anoAtual - 1 }]);
    await renderizar();

    botao(raiz, `Abrir ${anoAtual}`).click();
    await renderizar();
    expect(texto(raiz)).toContain(`Abrir o ano letivo de ${anoAtual}?`);

    // Só abrir o modal não chama a API
    http.expectNone(`${URL_ANOS}/proximo`);

    botao(raiz.querySelector('app-confirm-modal')!, `Abrir ${anoAtual}`).click();
    await renderizar();

    const requisicao = http.expectOne(`${URL_ANOS}/proximo`);
    expect(requisicao.request.method).toBe('POST');
    requisicao.flush({ id: 3, ano: anoAtual });
    await renderizar();

    expect(texto(raiz)).toBe('');
  });

  it('o perfil de consulta vê o aviso, mas não o botão de abrir', async () => {
    await criar({ admin: false });
    http.expectOne(URL_ANOS).flush([{ id: 2, ano: anoAtual - 1 }]);
    await renderizar();

    expect(texto(raiz)).toContain('Peça a um administrador para abrir o ano.');
    expect(() => botao(raiz, `Abrir ${anoAtual}`)).toThrow();
  });

  it('dispensado, some e não volta nesta sessão', async () => {
    await criar();
    http.expectOne(URL_ANOS).flush([{ id: 2, ano: anoAtual - 1 }]);
    await renderizar();

    botao(raiz, 'Dispensar').click();
    await renderizar();
    expect(texto(raiz)).toBe('');

    TestBed.resetTestingModule();
    await criar();
    http.expectNone(URL_ANOS);
    expect(texto(raiz)).toBe('');
  });
});
