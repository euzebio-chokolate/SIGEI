import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, TestRequest, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { environment } from '@environments/environment';

import { botao, campo, selecionarArquivo, texto } from '@testing/dom';
import { ImportacoesComponent } from './importacoes.component';

const URL_ESCOLAS = `${environment.apiUrl}/importacoes/infraestrutura`;

const RESULTADO_ESCOLAS = {
  linhasLidas: 3,
  municipiosCriados: 0,
  terrasIndigenasCriadas: 1,
  povosCriados: 0,
  aldeiasCriadas: 2,
  escolasCriadas: 2,
  registrosAnuaisCriados: 2,
  infraestruturasCriadas: 2,
  equipesCriadas: 2,
  registrosAtualizados: 0,
  linhasIgnoradas: 1,
  erros: ['Linha 4: Município não cadastrado: Xapuri Velho']
};

describe('ImportacoesComponent', () => {
  let fixture: ComponentFixture<ImportacoesComponent>;
  let raiz: HTMLElement;
  let http: HttpTestingController;

  async function renderizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function escolherPlanilha(nome = 'escolas-2025.xlsx'): Promise<void> {
    selecionarArquivo(campo(raiz, 'input[type="file"]'), new File(['conteúdo'], nome));
    await renderizar();
  }

  function envio(simulacao: boolean): TestRequest {
    return http.expectOne(r => r.url === URL_ESCOLAS && r.params.get('simulacao') === String(simulacao));
  }

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });
    http = TestBed.inject(HttpTestingController);

    fixture = TestBed.createComponent(ImportacoesComponent);
    raiz = fixture.nativeElement;
    await renderizar();

    http.expectOne(`${environment.apiUrl}/importacoes/layouts`).flush([
      { tipo: 'infraestrutura', titulo: 'Escolas e infraestrutura', instrucoes: [], colunas: [] },
      { tipo: 'matriculas', titulo: 'Matrículas', instrucoes: [], colunas: [] },
      { tipo: 'professores', titulo: 'Quadro de professores', instrucoes: [], colunas: [] }
    ]);
    http.expectOne(`${environment.apiUrl}/anos-referencia`).flush([{ id: 1, ano: 2025 }]);
    await renderizar();
  });

  afterEach(() => http.verify());

  it('pede a planilha antes de pré-visualizar', () => {
    expect(botao(raiz, 'Pré-visualizar').disabled).toBe(true);
    expect(texto(raiz)).toContain('Selecione a planilha (.xlsx).');
  });

  it('recusa arquivo que não é .xlsx sem enviá-lo', async () => {
    await escolherPlanilha('escolas.csv');

    expect(texto(raiz)).toContain('O arquivo deve estar no formato .xlsx.');
    expect(botao(raiz, 'Pré-visualizar').disabled).toBe(true);
  });

  it('pré-visualiza sem gravar e só grava ao confirmar, com o mesmo arquivo e ano', async () => {
    await escolherPlanilha();
    botao(raiz, 'Pré-visualizar').click();
    await renderizar();

    const previa = envio(true);
    expect(previa.request.params.get('ano')).toBe('2025');
    const arquivo = (previa.request.body as FormData).get('arquivo') as File;
    previa.flush(RESULTADO_ESCOLAS);
    await renderizar();

    expect(texto(raiz)).toContain('Pré-visualização: nada foi gravado ainda');
    expect(texto(raiz)).toContain('1 linha(s) não será(ão) importada(s)');
    expect(texto(raiz)).toContain('Linha 4: Município não cadastrado: Xapuri Velho');
    // Enquanto a prévia está aberta, a ação é confirmar ou cancelar
    expect(() => botao(raiz, 'Pré-visualizar')).toThrow();

    botao(raiz, 'Confirmar e gravar').click();
    await renderizar();

    const gravacao = envio(false);
    expect(gravacao.request.params.get('ano')).toBe('2025');
    expect((gravacao.request.body as FormData).get('arquivo')).toBe(arquivo);
    gravacao.flush(RESULTADO_ESCOLAS);
    await renderizar();

    expect(texto(raiz)).toContain('Importação concluída');
    expect(texto(raiz)).not.toContain('Pré-visualização');
  });

  it('cancelar descarta a prévia sem gravar', async () => {
    await escolherPlanilha();
    botao(raiz, 'Pré-visualizar').click();
    await renderizar();
    envio(true).flush(RESULTADO_ESCOLAS);
    await renderizar();

    botao(raiz, 'Cancelar').click();
    await renderizar();

    expect(texto(raiz)).not.toContain('Pré-visualização');
    expect(botao(raiz, 'Pré-visualizar').disabled).toBe(false);
  });

  it('trocar o arquivo descarta a prévia', async () => {
    await escolherPlanilha();
    botao(raiz, 'Pré-visualizar').click();
    await renderizar();
    envio(true).flush(RESULTADO_ESCOLAS);
    await renderizar();

    await escolherPlanilha('outra.xlsx');

    expect(texto(raiz)).not.toContain('Pré-visualização');
  });

  it('não deixa confirmar quando nenhuma linha é válida', async () => {
    await escolherPlanilha();
    botao(raiz, 'Pré-visualizar').click();
    await renderizar();
    envio(true).flush({ ...RESULTADO_ESCOLAS, linhasLidas: 1, linhasIgnoradas: 1 });
    await renderizar();

    expect(botao(raiz, 'Confirmar e gravar').disabled).toBe(true);
    expect(texto(raiz)).toContain('Nenhuma linha válida para gravar.');
  });

  it('mostra o motivo quando a planilha inteira é recusada', async () => {
    await escolherPlanilha();
    botao(raiz, 'Pré-visualizar').click();
    await renderizar();

    envio(true).flush(
      { status: 400, title: 'Requisição inválida', detail: 'Colunas obrigatórias ausentes: CÓDIGO.' },
      { status: 400, statusText: 'Bad Request' }
    );
    await renderizar();

    expect(texto(raiz)).toContain('Colunas obrigatórias ausentes: CÓDIGO.');
    expect(texto(raiz)).not.toContain('Confirmar e gravar');
  });
});
