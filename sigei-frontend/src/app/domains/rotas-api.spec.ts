import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '@environments/environment';

import { PovoRequest, PovoService } from '@domains/territorios/data-access/povo.service';
import { CadastroEscolaRequest, EscolaService } from '@domains/escolas/data-access/escola.service';
import { RegistroAnualService } from '@domains/escolas/data-access/registro-anual.service';
import { InfraestruturaService } from '@domains/escolas/data-access/infraestrutura.service';
import { LotacaoProfessorService } from '@domains/professores/data-access/lotacao-professor.service';

const API = environment.apiUrl;

/** Contrato REST da API: recursos no plural, ID da edição no caminho, sem verbos na rota. */
describe('Rotas da API', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('CRUD usa a coleção no plural e o ID no caminho', () => {
    const povos = TestBed.inject(PovoService);
    const dados: PovoRequest = { nome: 'Huni Kuin' };

    povos.salvar(dados).subscribe();
    expect(http.expectOne({ method: 'POST', url: `${API}/povos` }).request.body).toEqual(dados);

    povos.editar(7, dados).subscribe();
    http.expectOne({ method: 'PUT', url: `${API}/povos/7` }).flush({});

    povos.excluir(7).subscribe();
    http.expectOne({ method: 'DELETE', url: `${API}/povos/7` }).flush(null);
  });

  it('cadastro completo da escola é um sub-recurso', () => {
    const escolas = TestBed.inject(EscolaService);
    const dados = { escola: {} } as CadastroEscolaRequest;

    escolas.salvarCompleto(dados, null).subscribe();
    http.expectOne({ method: 'POST', url: `${API}/escolas/cadastro-completo` }).flush({});

    escolas.editarCompleto(3, dados, null).subscribe();
    http.expectOne({ method: 'PUT', url: `${API}/escolas/3/cadastro-completo` }).flush({});
  });

  it('consultas por escola, professor e ano usam caminhos aninhados', () => {
    const registros = TestBed.inject(RegistroAnualService);
    const lotacoes = TestBed.inject(LotacaoProfessorService);
    const infraestruturas = TestBed.inject(InfraestruturaService);

    registros.buscarPorEscolaEAno(3, 2025).subscribe();
    http.expectOne(`${API}/escolas/3/registros-anuais/2025`).flush({});

    registros.buscarHistorico(3, 2020, 2025).subscribe();
    http.expectOne(`${API}/escolas/3/registros-anuais?anoInicial=2020&anoFinal=2025`).flush([]);

    registros.buscarPorAno(2025).subscribe();
    http.expectOne(`${API}/anos/2025/registros-anuais`).flush([]);

    lotacoes.listarPorAno(2026).subscribe();
    http.expectOne(`${API}/anos/2026/lotacoes`).flush([]);

    lotacoes.listarPorProfessor(9).subscribe();
    http.expectOne(`${API}/professores/9/lotacoes`).flush([]);

    infraestruturas.listarMaisRecentes().subscribe();
    http.expectOne(`${API}/infraestruturas/mais-recentes`).flush([]);
  });
});
