import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EscolaResponse, EscolaService } from '../../../core/services/escola.service';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';
import { AtualizacaoAnualService } from '../../../core/services/atualizacao-anual.service';
import { AnoModalComponent } from '../../../shared/components/ano-modal/ano-modal.component';
import { SucessoModalComponent } from '../../../shared/components/sucesso-modal/sucesso-modal.component';
import { HistoricoEscolaModalComponent } from '../../../shared/components/historico-escola-modal/historico-escola-modal.component';
import { RegistroAnualResponse } from '../../../core/services/registro-anual.service';

@Component({
  selector: 'app-escolas-lista',
  standalone: true,
  imports: [RouterLink, FormsModule, ConfirmModalComponent, AnoModalComponent, SucessoModalComponent, HistoricoEscolaModalComponent],
  templateUrl: './escolas-lista.component.html'
})
export class EscolasListaComponent implements OnInit {
  private readonly escolaService = inject(EscolaService);
  private readonly atualizacaoAnualService = inject(AtualizacaoAnualService);
  private readonly cdr = inject(ChangeDetectorRef);

  escolasLista: EscolaResponse[] = [];
  escolasFiltradas: EscolaResponse[] = [];
  escolaParaExcluir: EscolaResponse | null = null;
  excluindoId: number | null = null;
  filtroTexto = '';
  paginaAtual = 1;
  itensPorPagina = 10;
  carregando = true;
  erroMensagem = '';
  totalEscolas = 0;
  totalAldeias = 0;
  totalTerras = 0;
  modalAtualizacaoAberto = false;
  atualizandoAno = false;
  modalSucessoAberto = false;
  tituloSucesso = '';
  mensagemSucesso = '';
  registroSelecionado: RegistroAnualResponse | null = null;
  visualizandoId: number | null = null;

  ngOnInit(): void {
    this.carregarEscolas();
  }

  carregarEscolas(): void {
    this.carregando = true;
    this.erroMensagem = '';

    this.escolaService.listar().subscribe({
      next: (dados) => {
        this.escolasLista = [...(dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome));
        this.escolasFiltradas = [...this.escolasLista];
        this.calcularTotais();
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar escolas:', erro);
        this.erroMensagem = 'Não foi possível carregar as escolas.';
        this.carregando = false;
        this.cdr.markForCheck();
      }
    });
  }

  calcularTotais(): void {
    this.totalEscolas = this.escolasLista.length;
    this.totalAldeias = new Set(
      this.escolasLista.map((escola) => escola.aldeia?.id).filter((id) => id !== undefined)
    ).size;
    this.totalTerras = new Set(
      this.escolasLista.map((escola) => escola.aldeia?.terraIndigena?.id).filter((id) => id !== undefined)
    ).size;
  }

  filtrarEscolas(): void {
    const termo = this.filtroTexto.trim().toLowerCase();
    this.paginaAtual = 1;

    if (!termo) {
      this.escolasFiltradas = [...this.escolasLista];
      return;
    }

    this.escolasFiltradas = this.escolasLista.filter((escola) => {
      const nome = escola.nome.toLowerCase().includes(termo);
      const codigo = escola.codigo.toLowerCase().includes(termo);
      const tipo = escola.tipoUnidade.toLowerCase().includes(termo);
      const aldeia = escola.aldeia.nome.toLowerCase().includes(termo);
      const terra = escola.aldeia.terraIndigena.nome.toLowerCase().includes(termo);
      const municipio = escola.aldeia.terraIndigena.municipio.nome.toLowerCase().includes(termo);
      const povo = escola.aldeia.povo.nome.toLowerCase().includes(termo);

      return nome || codigo || tipo || aldeia || terra || municipio || povo;
    });
  }

  abrirModalExcluir(escola: EscolaResponse): void {
    this.escolaParaExcluir = escola;
  }

  abrirDetalhes(escola: EscolaResponse): void {
    if (this.visualizandoId !== null) return;

    this.visualizandoId = escola.id;
    this.erroMensagem = '';

    this.escolaService.buscarPorId(escola.id).subscribe({
      next: detalhe => {
        const registro = detalhe.registroAnual;

        if (!registro) {
          this.erroMensagem = 'Esta escola ainda não possui um registro anual para visualização.';
          this.visualizandoId = null;
          this.cdr.markForCheck();
          return;
        }

        this.registroSelecionado = {
          ...registro,
          matriculasSerie: registro.matriculasSerie.map(matricula => ({
            ...matricula,
            total: (matricula.qtdMasculino ?? 0) + (matricula.qtdFeminino ?? 0)
          }))
        };
        this.visualizandoId = null;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar detalhes da escola:', erro);
        this.erroMensagem = 'Não foi possível carregar os detalhes desta escola.';
        this.visualizandoId = null;
        this.cdr.markForCheck();
      }
    });
  }

  fecharDetalhes(): void {
    this.registroSelecionado = null;
  }

  cancelarExclusao(): void {
    if (this.excluindoId !== null) return;
    this.escolaParaExcluir = null;
  }

  confirmarExclusao(): void {
  if (!this.escolaParaExcluir) return;

  const escola = this.escolaParaExcluir;
  this.excluindoId = escola.id;

  this.escolaService.excluir(escola.id).subscribe({
    next: () => {
      this.escolasLista = this.escolasLista.filter(item => item.id !== escola.id);
      this.escolasFiltradas = this.escolasFiltradas.filter(item => item.id !== escola.id);
      this.calcularTotais();

      this.escolaParaExcluir = null;
      this.excluindoId = null;

      this.tituloSucesso = 'Escola excluída';
      this.mensagemSucesso = `A escola ${escola.nome} foi excluída com sucesso.`;
      this.modalSucessoAberto = true;

      this.cdr.markForCheck();
    },
    error: erro => {
      console.error('Erro ao excluir escola:', erro);
      this.excluindoId = null;
      this.cdr.markForCheck();
    }
  });
}

abrirModalAtualizacaoAnual(): void {
  this.modalAtualizacaoAberto = true;
}

cancelarAtualizacaoAnual(): void {

  if (this.atualizandoAno) {
    return;
  }

  this.modalAtualizacaoAberto = false;
}

confirmarAtualizacaoAnual(): void {

  if (this.atualizandoAno) {
    return;
  }

  this.atualizandoAno = true;

  this.atualizacaoAnualService
    .iniciar()
    .subscribe({

      next: resultado => {

        console.log(
          'Atualização anual concluída:',
          resultado
        );

        // Para o loading
        this.atualizandoAno = false;

        // Fecha o modal do ano
        this.modalAtualizacaoAberto = false;

        // Prepara mensagem
        this.tituloSucesso =
          'Novo ano criado';

        this.mensagemSucesso =
          resultado ||
          'A atualização anual foi concluída com sucesso.';

        // Abre modal de sucesso
        this.modalSucessoAberto = true;

        // Atualiza os dados da página
        this.carregarEscolas();

        this.cdr.markForCheck();
      },

      error: erro => {

        console.error(
          'Erro ao iniciar atualização anual:',
          erro
        );

        this.atualizandoAno = false;

        this.cdr.markForCheck();
      }

    });
}
  fecharModalSucesso(): void {
    this.modalSucessoAberto = false;
  }

  get escolasPaginadas(): EscolaResponse[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.escolasFiltradas.slice(inicio, fim);
  }

  get totalPaginas(): number {
    return Math.ceil(this.escolasFiltradas.length / this.itensPorPagina);
  }

  paginaAnterior(): void {
    if (this.paginaAtual > 1) this.paginaAtual--;
  }

  proximaPagina(): void {
    if (this.paginaAtual < this.totalPaginas) this.paginaAtual++;
  }
}
