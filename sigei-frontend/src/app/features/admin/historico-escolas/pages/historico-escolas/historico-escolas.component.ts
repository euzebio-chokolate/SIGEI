import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AnoReferencia, AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { EscolaResponse, EscolaService } from '@domains/escolas/data-access/escola.service';
import { RegistroAnualResponse, RegistroAnualService } from '@domains/escolas/data-access/registro-anual.service';
import { HistoricoEscolaModalComponent } from '@domains/escolas/ui/historico-escola-modal/historico-escola-modal.component';
import { TrilhaAuditoriaComponent } from '@domains/escolas/ui/trilha-auditoria/trilha-auditoria.component';
import { SeAdminDirective } from '@core/auth/se-admin.directive';
import { mensagemErroCarregamento } from '@core/http/erro-http';
import { BuscaDebounceDirective } from '@shared/directives/busca-debounce.directive';

@Component({
  selector: 'app-historico-escolas',
  standalone: true,
  imports: [FormsModule, RouterLink, HistoricoEscolaModalComponent, SeAdminDirective, BuscaDebounceDirective, TrilhaAuditoriaComponent],
  templateUrl: './historico-escolas.component.html'
})
export class HistoricoEscolasComponent implements OnInit {
  private readonly anoReferenciaService = inject(AnoReferenciaService);
  private readonly registroAnualService = inject(RegistroAnualService);
  private readonly escolaService = inject(EscolaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  modo: 'ano' | 'escola' = 'ano';

  // Histórico por escola
  escolas: EscolaResponse[] = [];
  escolaSelecionadaId = 0;
  anoInicial: number | null = null;
  anoFinal: number | null = null;
  historico: RegistroAnualResponse[] = [];
  historicoConsultado = false;
  carregandoHistorico = false;
  erroHistorico = '';

  anosReferencia: AnoReferencia[] = [];
  registros: RegistroAnualResponse[] = [];
  registrosFiltrados: RegistroAnualResponse[] = [];
  registroSelecionado: RegistroAnualResponse | null = null;
  anoSelecionado = 0;
  filtroTexto = '';
  carregandoAnos = false;
  carregandoRegistros = false;
  erroMensagem = '';
  paginaAtual = 1;
  itensPorPagina = 10;

  ngOnInit(): void {
    this.carregarAnos();
    this.carregarEscolas();

    const escolaId = Number(this.route.snapshot.queryParamMap.get('escolaId'));
    if (escolaId > 0) {
      this.modo = 'escola';
      this.escolaSelecionadaId = escolaId;
      this.consultarHistorico();
    }
  }

  /** Total consolidado do ano; na falta dele, o total geral das matrículas. Null = não informado. */
  obterTotalAlunos(registro: RegistroAnualResponse): number | null {
    if (registro.totalAlunos !== null && registro.totalAlunos !== undefined) {
      return registro.totalAlunos;
    }
    const totalGeral = registro.matriculasNivel?.find(matricula => matricula.nivel === 'TOTAL_GERAL');
    return totalGeral?.total ?? null;
  }

  alterarModo(modo: 'ano' | 'escola'): void {
    this.modo = modo;
    this.erroMensagem = '';
    this.erroHistorico = '';
  }

  carregarEscolas(): void {
    this.escolaService.listar().subscribe({
      next: dados => {
        this.escolas = [...(dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome));
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar escolas:', erro);
        this.erroHistorico = mensagemErroCarregamento(erro, 'Não foi possível carregar a lista de escolas.');
        this.cdr.markForCheck();
      }
    });
  }

  consultarHistorico(): void {
    this.erroHistorico = '';

    if (!this.escolaSelecionadaId) {
      this.erroHistorico = 'Selecione uma escola.';
      return;
    }

    if (this.anoInicial && this.anoFinal && this.anoInicial > this.anoFinal) {
      this.erroHistorico = 'O ano inicial não pode ser maior que o ano final.';
      return;
    }

    this.carregandoHistorico = true;

    // Mantém a escola na URL para voltar a esta consulta (ex.: após editar um ano)
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { escolaId: this.escolaSelecionadaId },
      replaceUrl: true
    });

    this.registroAnualService
      .buscarHistorico(this.escolaSelecionadaId, this.anoInicial, this.anoFinal)
      .subscribe({
        next: dados => {
          this.historico = [...(dados ?? [])].sort((a, b) => a.anoReferencia.ano - b.anoReferencia.ano);
          this.historicoConsultado = true;
          this.carregandoHistorico = false;
          this.cdr.markForCheck();
        },
        error: erro => {
          console.error('Erro ao carregar histórico da escola:', erro);
          this.historico = [];
          this.historicoConsultado = false;
          this.erroHistorico = mensagemErroCarregamento(erro, 'Não foi possível carregar o histórico desta escola.', 'Escola não encontrada.');
          this.carregandoHistorico = false;
          this.cdr.markForCheck();
        }
      });
  }

  aoSelecionarEscola(): void {
    this.historico = [];
    this.historicoConsultado = false;
    if (this.escolaSelecionadaId) {
      this.consultarHistorico();
    }
  }

  get escolaSelecionada(): EscolaResponse | null {
    return this.escolas.find(escola => escola.id === this.escolaSelecionadaId) ?? null;
  }

  get maiorTotalHistorico(): number {
    return Math.max(0, ...this.historico.map(registro => this.obterTotalAlunos(registro) ?? 0));
  }

  larguraBarra(registro: RegistroAnualResponse): number {
    const total = this.obterTotalAlunos(registro);
    const maior = this.maiorTotalHistorico;
    return total && maior ? Math.max(2, Math.round((total / maior) * 100)) : 0;
  }

  /** Anos entre o primeiro e o último registro que não possuem dados cadastrados. */
  get anosSemRegistro(): number[] {
    if (this.historico.length < 2) return [];

    const anos = new Set(this.historico.map(registro => registro.anoReferencia.ano));
    const primeiro = this.historico[0].anoReferencia.ano;
    const ultimo = this.historico[this.historico.length - 1].anoReferencia.ano;
    const faltantes: number[] = [];

    for (let ano = primeiro + 1; ano < ultimo; ano++) {
      if (!anos.has(ano)) faltantes.push(ano);
    }

    return faltantes;
  }

  carregarAnos(): void {
    this.carregandoAnos = true;
    this.erroMensagem = '';

    this.anoReferenciaService.listar().subscribe({
      next: dados => {
        this.anosReferencia = [...(dados ?? [])].sort((a, b) => b.ano - a.ano);
        this.carregandoAnos = false;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar anos:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os anos de referência.');
        this.carregandoAnos = false;
        this.cdr.markForCheck();
      }
    });
  }

  abrirDetalhes(registro: RegistroAnualResponse): void {
    this.registroSelecionado = registro;
  }

  fecharDetalhes(): void {
    this.registroSelecionado = null;
  }

  aoSelecionarAno(): void {
    this.registros = [];
    this.registrosFiltrados = [];
    this.filtroTexto = '';
    this.paginaAtual = 1;
    this.erroMensagem = '';

    if (!this.anoSelecionado) {
      return;
    }

    this.carregandoRegistros = true;

    this.registroAnualService.buscarPorAno(this.anoSelecionado).subscribe({
      next: dados => {
        this.registros = [...(dados ?? [])].sort((a, b) => {
          const nomeA = a.infraestrutura?.escola?.nome ?? '';
          const nomeB = b.infraestrutura?.escola?.nome ?? '';
          return nomeA.localeCompare(nomeB);
        });

        this.registrosFiltrados = [...this.registros];
        this.carregandoRegistros = false;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar histórico:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os dados deste ano.');
        this.carregandoRegistros = false;
        this.cdr.markForCheck();
      }
    });
  }

  filtrarRegistros(): void {
    const termo = this.filtroTexto.trim().toLowerCase();
    this.paginaAtual = 1;

    if (!termo) {
      this.registrosFiltrados = [...this.registros];
      return;
    }

    this.registrosFiltrados = this.registros.filter(registro => {
      const escola = registro.infraestrutura?.escola?.nome?.toLowerCase().includes(termo) ?? false;
      const codigo = registro.infraestrutura?.escola?.codigo?.toLowerCase().includes(termo) ?? false;
      const rede = registro.rede?.toLowerCase().includes(termo);
      const local = registro.localFuncionamento?.toLowerCase().includes(termo);

      return escola || codigo || rede || local;
    });
  }

  get registrosPaginados(): RegistroAnualResponse[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.registrosFiltrados.slice(inicio, fim);
  }

  get totalPaginas(): number {
    return Math.ceil(this.registrosFiltrados.length / this.itensPorPagina);
  }

  paginaAnterior(): void {
    if (this.paginaAtual > 1) {
      this.paginaAtual--;
    }
  }

  proximaPagina(): void {
    if (this.paginaAtual < this.totalPaginas) {
      this.paginaAtual++;
    }
  }
}