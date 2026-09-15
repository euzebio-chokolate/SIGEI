import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AnoReferencia, AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { RegistroAnualResponse, RegistroAnualService } from '@domains/escolas/data-access/registro-anual.service';
import { HistoricoEscolaModalComponent } from '@domains/escolas/ui/historico-escola-modal/historico-escola-modal.component';

@Component({
  selector: 'app-historico-escolas',
  standalone: true,
  imports: [FormsModule, RouterLink, HistoricoEscolaModalComponent],
  templateUrl: './historico-escolas.component.html'
})
export class HistoricoEscolasComponent implements OnInit {
  private readonly anoReferenciaService = inject(AnoReferenciaService);
  private readonly registroAnualService = inject(RegistroAnualService);
  private readonly cdr = inject(ChangeDetectorRef);

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
  }

  obterTotalAlunos(registro: RegistroAnualResponse): number {
    const totalGeral = registro.matriculasNivel?.find(matricula => matricula.nivel === 'TOTAL_GERAL');
    return totalGeral?.total ?? 0;
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
        this.erroMensagem = 'Não foi possível carregar os anos de referência.';
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
        this.erroMensagem = 'Não foi possível carregar os dados deste ano.';
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