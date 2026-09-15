import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AldeiaResponse, AldeiaService } from '@domains/territorios/data-access/aldeia.service';
import { ConfirmModalComponent } from '@shared/ui/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-aldeias-lista',
  standalone: true,
  imports: [RouterLink, FormsModule, ConfirmModalComponent],
  template: `
    <div class="space-y-5 font-sans pb-10">
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i>
          Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800"> Aldeias </span>
      </nav>

      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Aldeias</h1>
          <p class="text-sm text-slate-500 mt-1">
            Consulte as aldeias, terras indígenas, municípios e povos vinculados.
          </p>
        </div>

        <a routerLink="/admin/aldeias/novo" class="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all shrink-0">
          <i class="fa-solid fa-plus text-xs"></i>
          Nova Aldeia
        </a>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Aldeias Cadastradas
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalAldeias }}
            </p>
          </div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center text-amber-600 text-lg">
            <i class="fa-solid fa-tents"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Terras Indígenas
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalTerras }}
            </p>
          </div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 text-lg">
            <i class="fa-solid fa-map-location-dot"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Povos
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalPovos }}
            </p>
          </div>
          <div class="w-12 h-12 bg-violet-50 rounded-xl flex items-center justify-center text-violet-600 text-lg">
            <i class="fa-solid fa-people-group"></i>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div class="p-5 border-b border-slate-100 bg-slate-50/50 flex flex-col md:flex-row gap-3 justify-between items-center">
          <div class="relative w-full md:w-96">
            <i class="fa-solid fa-search absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm"></i>
            <input
              type="text"
              [(ngModel)]="filtroTexto"
              (ngModelChange)="filtrarAldeias()"
              placeholder="Buscar aldeia, terra, município ou povo..."
              class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all text-slate-700 placeholder:text-slate-400 shadow-sm"
            />
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/80 text-slate-400 text-[11px] font-bold uppercase tracking-wider border-b border-slate-100">
                <th class="px-6 py-3.5">Aldeia</th>
                <th class="px-6 py-3.5">Terra Indígena</th>
                <th class="px-6 py-3.5">Município</th>
                <th class="px-6 py-3.5">Povo</th>
                <th class="px-6 py-3.5 text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 text-sm text-slate-700">
              @for (aldeia of aldeiasPaginadas; track aldeia.id) {
                <tr class="hover:bg-slate-50/60 transition-colors">
                  <td class="px-6 py-4 font-bold text-slate-800">
                    <div class="flex items-center gap-3">
                      <div class="w-9 h-9 rounded-lg bg-amber-50 text-amber-600 flex items-center justify-center">
                        <i class="fa-solid fa-tents text-xs"></i>
                      </div>
                      {{ aldeia.nome }}
                    </div>
                  </td>

                  <td class="px-6 py-4">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-100">
                      <i class="fa-solid fa-map-location-dot text-[10px]"></i>
                      {{ aldeia.terraIndigena.nome }}
                    </span>
                  </td>

                  <td class="px-6 py-4">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-100">
                      <i class="fa-solid fa-city text-[10px]"></i>
                      {{ aldeia.terraIndigena.municipio.nome }}
                    </span>
                  </td>

                  <td class="px-6 py-4">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-violet-50 text-violet-700 border border-violet-100">
                      <i class="fa-solid fa-people-group text-[10px]"></i>
                      {{ aldeia.povo.nome }}
                    </span>
                  </td>

                  <td class="px-6 py-4 text-right">
                    <div class="flex items-center justify-end gap-1">
                      <a
                        [routerLink]="['/admin/aldeias/editar', aldeia.id]"
                        class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-all"
                        title="Editar"
                      >
                        <i class="fa-solid fa-pen-to-square text-xs"></i>
                      </a>
                      <button
                        type="button"
                        (click)="abrirModalExclusao(aldeia)"
                        [disabled]="excluindoId === aldeia.id"
                        class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-rose-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
                        title="Excluir"
                      >
                        @if (excluindoId === aldeia.id) {
                          <i class="fa-solid fa-circle-notch animate-spin text-xs"></i>
                        } @else {
                          <i class="fa-solid fa-trash text-xs"></i>
                        }
                      </button>
                    </div>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="5" class="px-6 py-8 text-center text-slate-400 text-sm">
                    @if (carregando) {
                      <div class="flex items-center justify-center gap-2">
                        <i class="fa-solid fa-circle-notch animate-spin text-emerald-600"></i>
                        <span> Carregando aldeias... </span>
                      </div>
                    } @else if (erroMensagem) {
                      <span class="text-rose-500 font-semibold">
                        {{ erroMensagem }}
                      </span>
                    } @else {
                      Nenhuma aldeia encontrada.
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <div class="px-6 py-4 border-t border-slate-100 bg-slate-50/30 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <span>
            Exibindo <strong>{{ aldeiasPaginadas.length }}</strong> de <strong>{{ aldeiasFiltradas.length }}</strong> aldeias
          </span>

          @if (totalPaginas > 1) {
            <div class="flex items-center gap-2">
              <button
                type="button"
                (click)="paginaAnterior()"
                [disabled]="paginaAtual === 1"
                class="w-8 h-8 rounded-lg border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              >
                <i class="fa-solid fa-chevron-left text-[10px]"></i>
              </button>

              <span class="px-2">
                Página <strong class="text-slate-700">{{ paginaAtual }}</strong> de <strong class="text-slate-700">{{ totalPaginas }}</strong>
              </span>

              <button
                type="button"
                (click)="proximaPagina()"
                [disabled]="paginaAtual === totalPaginas"
                class="w-8 h-8 rounded-lg border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              >
                <i class="fa-solid fa-chevron-right text-[10px]"></i>
              </button>
            </div>
          }
        </div>
      </div>

      <app-confirm-modal
        [aberto]="aldeiaParaExcluir !== null"
        titulo="Excluir Aldeia"
        [mensagem]="aldeiaParaExcluir ? 'Deseja realmente excluir a aldeia &quot;' + aldeiaParaExcluir.nome + '&quot;? Esta ação não poderá ser desfeita.' : ''"
        textoConfirmar="Excluir"
        textoCancelar="Cancelar"
        [carregando]="excluindoId !== null"
        (confirmar)="confirmarExclusao()"
        (cancelar)="fecharModalExclusao()"
      ></app-confirm-modal>
    </div>
  `,
})
export class AldeiasListaComponent implements OnInit {
  // --- Injeções de Dependência ---
  private readonly aldeiaService = inject(AldeiaService);
  private readonly cdr = inject(ChangeDetectorRef);

  // --- Propriedades de Estado ---
  aldeiasLista: AldeiaResponse[] = [];
  aldeiasFiltradas: AldeiaResponse[] = [];
  filtroTexto = '';
  carregando = true;
  erroMensagem = '';

  // --- Totais ---
  totalAldeias = 0;
  totalTerras = 0;
  totalPovos = 0;

  // --- Propriedades de Paginação ---
  paginaAtual = 1;
  itensPorPagina = 10;

  // --- Propriedades de Modais ---
  aldeiaParaExcluir: AldeiaResponse | null = null;
  excluindoId: number | null = null;

  // --- Ciclo de Vida ---
  ngOnInit(): void {
    this.carregarAldeias();
  }

  // --- Getters de Paginação ---
  get aldeiasPaginadas(): AldeiaResponse[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.aldeiasFiltradas.slice(inicio, fim);
  }

  get totalPaginas(): number {
    return Math.ceil(this.aldeiasFiltradas.length / this.itensPorPagina);
  }

  // --- Métodos de Dados e Filtros ---
  carregarAldeias(): void {
    this.carregando = true;
    this.erroMensagem = '';

    this.aldeiaService.listar().subscribe({
      next: (dados) => {
        this.aldeiasLista = [...(dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome));
        this.aldeiasFiltradas = [...this.aldeiasLista];
        this.calcularTotais();
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar aldeias:', erro);
        this.erroMensagem = 'Não foi possível carregar as aldeias.';
        this.carregando = false;
        this.cdr.markForCheck();
      },
    });
  }

  calcularTotais(): void {
    this.totalAldeias = this.aldeiasLista.length;
    this.totalTerras = new Set(this.aldeiasLista.map((aldeia) => aldeia.terraIndigena.id)).size;
    this.totalPovos = new Set(this.aldeiasLista.map((aldeia) => aldeia.povo.id)).size;
  }

  filtrarAldeias(): void {
    const termo = this.filtroTexto.trim().toLowerCase();
    this.paginaAtual = 1;

    if (!termo) {
      this.aldeiasFiltradas = [...this.aldeiasLista];
      return;
    }

    this.aldeiasFiltradas = this.aldeiasLista.filter(
      (aldeia) =>
        aldeia.nome.toLowerCase().includes(termo) ||
        aldeia.terraIndigena.nome.toLowerCase().includes(termo) ||
        aldeia.terraIndigena.municipio.nome.toLowerCase().includes(termo) ||
        aldeia.povo.nome.toLowerCase().includes(termo),
    );
  }

  // --- Métodos de Paginação ---
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

  // --- Métodos de Controle de Modais / Exclusão ---
  abrirModalExclusao(aldeia: AldeiaResponse): void {
    this.aldeiaParaExcluir = aldeia;
  }

  fecharModalExclusao(): void {
    if (this.excluindoId !== null) {
      return;
    }
    this.aldeiaParaExcluir = null;
  }

  confirmarExclusao(): void {
    if (!this.aldeiaParaExcluir) {
      return;
    }

    const aldeia = this.aldeiaParaExcluir;
    this.excluindoId = aldeia.id;
    this.erroMensagem = '';

    this.aldeiaService.excluir(aldeia.id).subscribe({
      next: () => {
        this.aldeiasLista = this.aldeiasLista.filter(item => item.id !== aldeia.id);
        this.aldeiasFiltradas = this.aldeiasFiltradas.filter(item => item.id !== aldeia.id);
        
        this.calcularTotais();

        if (this.paginaAtual > this.totalPaginas && this.paginaAtual > 1) {
          this.paginaAtual--;
        }

        this.excluindoId = null;
        this.aldeiaParaExcluir = null;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao excluir aldeia:', erro);
        this.erroMensagem = 'Não foi possível excluir a aldeia. Verifique se existem escolas vinculadas.';
        this.excluindoId = null;
        this.aldeiaParaExcluir = null;
        this.cdr.markForCheck();
      }
    });
  }
}