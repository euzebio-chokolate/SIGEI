import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

import { AuthService } from '@core/auth/auth.service';
import { AnoReferencia, AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { ProfessorResponse, ProfessorService } from '@domains/professores/data-access/professor.service';
import {
  ImportacaoProfessoresResultado,
  LotacaoProfessorResponse,
  LotacaoProfessorService
} from '@domains/professores/data-access/lotacao-professor.service';
import {
  ANO_QUADRO_PROFESSORES,
  OPCOES_TIPO_CONTRATO,
  formatarData,
  mascararCpf,
  rotuloSexo,
  rotuloTipoContrato
} from '@domains/professores/models/professor.models';
import { ConfirmModalComponent } from '@shared/ui/confirm-modal/confirm-modal.component';
import { SucessoModalComponent } from '@shared/ui/sucesso-modal/sucesso-modal.component';

type Aba = 'lotacoes' | 'professores';
type FiltroLocal = 'todas' | 'escolas' | 'nucleo' | 'vagas';

@Component({
  selector: 'app-professores-lista',
  standalone: true,
  imports: [FormsModule, RouterLink, ConfirmModalComponent, SucessoModalComponent],
  templateUrl: './professores-lista.component.html'
})
export class ProfessoresListaComponent implements OnInit {
  private readonly professorService = inject(ProfessorService);
  private readonly lotacaoService = inject(LotacaoProfessorService);
  private readonly anoReferenciaService = inject(AnoReferenciaService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly ehAdmin = this.authService.ehAdmin();
  readonly opcoesTipoContrato = OPCOES_TIPO_CONTRATO;
  readonly anoQuadro = ANO_QUADRO_PROFESSORES;
  readonly rotuloTipoContrato = rotuloTipoContrato;
  readonly rotuloSexo = rotuloSexo;
  readonly formatarData = formatarData;
  readonly mascararCpf = mascararCpf;

  aba: Aba = 'lotacoes';
  itensPorPagina = 15;

  anos: AnoReferencia[] = [];
  anoSelecionado: number | null = null;

  lotacoes: LotacaoProfessorResponse[] = [];
  lotacoesFiltradas: LotacaoProfessorResponse[] = [];
  municipios: string[] = [];
  filtroLotacao = '';
  filtroMunicipio = '';
  filtroTipoContrato = '';
  filtroLocal: FiltroLocal = 'todas';
  paginaLotacoes = 1;
  carregandoLotacoes = false;

  professores: ProfessorResponse[] = [];
  professoresFiltrados: ProfessorResponse[] = [];
  filtroProfessor = '';
  somentePendentes = false;
  paginaProfessores = 1;
  carregandoProfessores = false;

  erroMensagem = '';

  lotacaoParaExcluir: LotacaoProfessorResponse | null = null;
  professorParaExcluir: ProfessorResponse | null = null;
  excluindo = false;

  importando = false;
  resultadoImportacao: ImportacaoProfessoresResultado | null = null;
  erroImportacao = '';

  modalSucessoAberto = false;
  tituloSucesso = '';
  mensagemSucesso = '';

  ngOnInit(): void {
    this.carregarAnos();
    this.carregarProfessores();
  }

  // ---------------------------------------------------------------- Carga

  carregarAnos(): void {
    this.anoReferenciaService.listar().subscribe({
      next: anos => {
        this.anos = [...(anos ?? [])].sort((a, b) => b.ano - a.ano);
        // Abre no ano do quadro de professores, se já iniciado; senão, no mais recente
        const padrao = this.anos.find(item => item.ano === this.anoQuadro) ?? this.anos[0];
        this.anoSelecionado = padrao?.ano ?? null;
        this.carregarLotacoes();
      },
      error: erro => this.registrarErro('Não foi possível carregar os anos de referência.', erro)
    });
  }

  carregarLotacoes(): void {
    if (this.anoSelecionado === null) {
      this.lotacoes = [];
      this.aplicarFiltrosLotacao();
      return;
    }

    this.carregandoLotacoes = true;
    this.erroMensagem = '';

    this.lotacaoService.listarPorAno(this.anoSelecionado).subscribe({
      next: dados => {
        this.lotacoes = dados ?? [];
        this.municipios = [...new Set(this.lotacoes.map(lotacao => lotacao.municipio.nome))]
          .sort((a, b) => a.localeCompare(b));
        this.carregandoLotacoes = false;
        this.aplicarFiltrosLotacao();
      },
      error: erro => {
        this.carregandoLotacoes = false;
        this.registrarErro('Não foi possível carregar as lotações.', erro);
      }
    });
  }

  carregarProfessores(): void {
    this.carregandoProfessores = true;

    this.professorService.listar().subscribe({
      next: dados => {
        this.professores = dados ?? [];
        this.carregandoProfessores = false;
        this.aplicarFiltrosProfessor();
      },
      error: erro => {
        this.carregandoProfessores = false;
        this.registrarErro('Não foi possível carregar os professores.', erro);
      }
    });
  }

  // ------------------------------------------------------------- Filtros

  aplicarFiltrosLotacao(): void {
    const termo = this.normalizar(this.filtroLotacao);
    this.paginaLotacoes = 1;

    this.lotacoesFiltradas = this.lotacoes.filter(lotacao => {
      if (this.filtroMunicipio && lotacao.municipio.nome !== this.filtroMunicipio) return false;
      if (this.filtroTipoContrato && lotacao.tipoContrato !== this.filtroTipoContrato) return false;
      if (this.filtroLocal === 'escolas' && !lotacao.escola) return false;
      if (this.filtroLocal === 'nucleo' && lotacao.escola) return false;
      if (this.filtroLocal === 'vagas' && lotacao.professor) return false;
      if (!termo) return true;

      return [
        lotacao.professor?.nome,
        lotacao.professor?.etnia,
        lotacao.escola?.nome,
        lotacao.escola?.codigo,
        lotacao.cargo,
        lotacao.segmento,
        lotacao.amparoLegal
      ].some(valor => this.normalizar(valor).includes(termo));
    });

    this.cdr.markForCheck();
  }

  aplicarFiltrosProfessor(): void {
    const termo = this.normalizar(this.filtroProfessor);
    this.paginaProfessores = 1;

    this.professoresFiltrados = this.professores.filter(professor => {
      if (this.somentePendentes && !this.dadosPendentes(professor)) return false;
      if (!termo) return true;

      return [professor.nome, professor.etnia, professor.formacao]
        .some(valor => this.normalizar(valor).includes(termo));
    });

    this.cdr.markForCheck();
  }

  limparFiltrosLotacao(): void {
    this.filtroLotacao = '';
    this.filtroMunicipio = '';
    this.filtroTipoContrato = '';
    this.filtroLocal = 'todas';
    this.aplicarFiltrosLotacao();
  }

  selecionarAba(aba: Aba): void {
    this.aba = aba;
  }

  /** CPF ou matrícula ainda não preenchidos (só o ADMIN recebe esses campos). */
  dadosPendentes(professor: ProfessorResponse): boolean {
    return this.ehAdmin && (!professor.cpf || !professor.matricula);
  }

  // ------------------------------------------------------------ Resumo

  get totalProfessoresNoAno(): number {
    return new Set(this.lotacoes.filter(l => l.professor).map(l => l.professor!.id)).size;
  }

  get totalVagas(): number {
    return this.lotacoes.filter(lotacao => !lotacao.professor).length;
  }

  get totalEscolasAtendidas(): number {
    return new Set(this.lotacoes.filter(l => l.escola).map(l => l.escola!.id)).size;
  }

  get totalPendentes(): number {
    return this.professores.filter(professor => this.dadosPendentes(professor)).length;
  }

  // ----------------------------------------------------------- Paginação

  get lotacoesPaginadas(): LotacaoProfessorResponse[] {
    const inicio = (this.paginaLotacoes - 1) * this.itensPorPagina;
    return this.lotacoesFiltradas.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginasLotacoes(): number {
    return Math.max(1, Math.ceil(this.lotacoesFiltradas.length / this.itensPorPagina));
  }

  get professoresPaginados(): ProfessorResponse[] {
    const inicio = (this.paginaProfessores - 1) * this.itensPorPagina;
    return this.professoresFiltrados.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginasProfessores(): number {
    return Math.max(1, Math.ceil(this.professoresFiltrados.length / this.itensPorPagina));
  }

  // ------------------------------------------------------------ Exclusão

  confirmarExclusaoLotacao(): void {
    const lotacao = this.lotacaoParaExcluir;
    if (!lotacao) return;

    this.excluindo = true;
    this.lotacaoService.excluir(lotacao.id).subscribe({
      next: () => {
        this.lotacoes = this.lotacoes.filter(item => item.id !== lotacao.id);
        this.aplicarFiltrosLotacao();
        this.lotacaoParaExcluir = null;
        this.excluindo = false;
        this.mostrarSucesso('Lotação excluída', 'A lotação foi excluída com sucesso.');
      },
      error: erro => {
        this.excluindo = false;
        this.lotacaoParaExcluir = null;
        this.registrarErro('Não foi possível excluir a lotação.', erro);
      }
    });
  }

  confirmarExclusaoProfessor(): void {
    const professor = this.professorParaExcluir;
    if (!professor) return;

    this.excluindo = true;
    this.professorService.excluir(professor.id).subscribe({
      next: () => {
        this.professores = this.professores.filter(item => item.id !== professor.id);
        this.lotacoes = this.lotacoes.filter(item => item.professor?.id !== professor.id);
        this.aplicarFiltrosProfessor();
        this.aplicarFiltrosLotacao();
        this.professorParaExcluir = null;
        this.excluindo = false;
        this.mostrarSucesso('Professor excluído', `${professor.nome} e as suas lotações foram excluídos.`);
      },
      error: erro => {
        this.excluindo = false;
        this.professorParaExcluir = null;
        this.registrarErro('Não foi possível excluir o professor.', erro);
      }
    });
  }

  cancelarExclusao(): void {
    if (this.excluindo) return;
    this.lotacaoParaExcluir = null;
    this.professorParaExcluir = null;
  }

  // ---------------------------------------------------------- Importação

  importarPlanilha(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const arquivo = campo.files?.[0];
    campo.value = '';

    if (!arquivo || this.importando) return;

    this.importando = true;
    this.erroImportacao = '';
    this.resultadoImportacao = null;

    this.lotacaoService.importarPlanilha(arquivo).subscribe({
      next: resultado => {
        this.importando = false;
        this.resultadoImportacao = resultado;
        this.carregarProfessores();
        this.carregarLotacoes();
      },
      error: (erro: HttpErrorResponse) => {
        this.importando = false;
        this.erroImportacao = typeof erro.error === 'string' && erro.error
          ? erro.error
          : 'Não foi possível importar a planilha.';
        this.cdr.markForCheck();
      }
    });
  }

  fecharImportacao(): void {
    this.resultadoImportacao = null;
    this.erroImportacao = '';
  }

  // ---------------------------------------------------------------- Apoio

  fecharModalSucesso(): void {
    this.modalSucessoAberto = false;
  }

  private mostrarSucesso(titulo: string, mensagem: string): void {
    this.tituloSucesso = titulo;
    this.mensagemSucesso = mensagem;
    this.modalSucessoAberto = true;
    this.cdr.markForCheck();
  }

  private registrarErro(mensagem: string, erro: unknown): void {
    console.error(mensagem, erro);
    const detalhe = erro instanceof HttpErrorResponse && typeof erro.error === 'string' ? erro.error : '';
    this.erroMensagem = detalhe ? `${mensagem} ${detalhe}` : mensagem;
    this.cdr.markForCheck();
  }

  private normalizar(valor: string | null | undefined): string {
    return (valor ?? '')
      .normalize('NFD')
      .replace(/\p{M}/gu, '')
      .toLowerCase()
      .trim();
  }
}
