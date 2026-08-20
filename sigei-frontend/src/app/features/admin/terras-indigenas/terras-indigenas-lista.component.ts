import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ConfirmModalComponent } from '../../../shared/components/confirm-modal/confirm-modal.component';

import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import {
  TerraIndigenaEstatistica,
  TerraIndigenaService,
} from '../../../core/services/terra-indigena.service';

@Component({
  selector: 'app-terras-indigenas-lista',
  standalone: true,
  imports: [RouterLink, FormsModule, ConfirmModalComponent],
  template: `
    <div class="space-y-5 font-sans pb-10">
      <!-- Breadcrumb -->
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a
          routerLink="/admin/dashboard"
          class="hover:text-emerald-600 transition-colors flex items-center gap-1.5"
        >
          <i class="fa-solid fa-house text-slate-400"></i>
          Início
        </a>

        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>

        <span class="text-slate-800"> Terras Indígenas </span>
      </nav>

      <!-- Título -->
      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Terras Indígenas</h1>

          <p class="text-sm text-slate-500 mt-1">
            Consulte as terras indígenas, municípios, aldeias e escolas vinculadas.
          </p>
        </div>
        <a
          routerLink="/admin/terras-indigenas/novo"
          class="bg-emerald-600 hover:bg-emerald-700
           text-white px-4 py-2.5 rounded-xl
           text-sm font-semibold shadow-sm hover:shadow
           flex items-center gap-2 transition-all
           shrink-0"
        >
          <i class="fa-solid fa-plus text-xs"></i>
          Nova Terra Indígena
        </a>
      </div>

      <!-- Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <!-- Terras -->
        <div
          class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between"
        >
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Terras Indígenas
            </span>

            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalTerrasIndigenas }}
            </p>
          </div>

          <div
            class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 text-lg"
          >
            <i class="fa-solid fa-map-location-dot"></i>
          </div>
        </div>

        <!-- Aldeias -->
        <div
          class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between"
        >
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Total de Aldeias
            </span>

            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalAldeias }}
            </p>
          </div>

          <div
            class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center text-amber-600 text-lg"
          >
            <i class="fa-solid fa-tents"></i>
          </div>
        </div>

        <!-- Escolas -->
        <div
          class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between"
        >
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Escolas Indígenas
            </span>

            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalEscolas }}
            </p>
          </div>

          <div
            class="w-12 h-12 bg-sky-50 rounded-xl flex items-center justify-center text-sky-600 text-lg"
          >
            <i class="fa-solid fa-school"></i>
          </div>
        </div>
      </div>

      <!-- Tabela -->
      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <!-- Busca -->
        <div
          class="p-5 border-b border-slate-100 bg-slate-50/50 flex flex-col md:flex-row gap-3 justify-between items-center"
        >
          <div class="relative w-full md:w-96">
            <i
              class="fa-solid fa-search absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm"
            ></i>

            <input
              type="text"
              [(ngModel)]="filtroTexto"
              (ngModelChange)="filtrarTerras()"
              placeholder="Buscar terra indígena..."
              class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all text-slate-700 placeholder:text-slate-400 shadow-sm"
            />
          </div>
        </div>

        <!-- Tabela -->
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr
                class="bg-slate-50/80 text-slate-400 text-[11px] font-bold uppercase tracking-wider border-b border-slate-100"
              >
                <th class="px-6 py-3.5">Terra Indígena</th>

                <th class="px-6 py-3.5">Município</th>

                <th class="px-6 py-3.5 text-center">Aldeias</th>

                <th class="px-6 py-3.5 text-center">Escolas</th>

                <th class="px-6 py-3.5 text-right">Ações</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-slate-100 text-sm text-slate-700">
              @for (terra of terrasPaginadas; track terra.id) {
                <tr class="hover:bg-slate-50/60 transition-colors">
                  <!-- Nome -->
                  <td class="px-6 py-4 font-bold text-slate-800">
                    <div class="flex items-center gap-3">
                      <div
                        class="w-9 h-9 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center"
                      >
                        <i class="fa-solid fa-map-location-dot text-xs"></i>
                      </div>

                      {{ terra.nome }}
                    </div>
                  </td>

                  <!-- Município -->
                  <td class="px-6 py-4">
                    <span
                      class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-100"
                    >
                      <i class="fa-solid fa-city text-[10px]"></i>

                      {{ terra.municipio.nome }}
                    </span>
                  </td>

                  <!-- Aldeias -->
                  <td class="px-6 py-4 text-center">
                    <span
                      class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-100"
                    >
                      <i class="fa-solid fa-tents text-[10px]"></i>

                      {{ terra.quantidadeAldeias }}
                    </span>
                  </td>

                  <!-- Escolas -->
                  <td class="px-6 py-4 text-center">
                    <span
                      class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-sky-50 text-sky-700 border border-sky-100"
                    >
                      <i class="fa-solid fa-school text-[10px]"></i>

                      {{ terra.quantidadeEscolas }}
                    </span>
                  </td>

                  <td class="px-6 py-4 text-right">
                    <div class="flex items-center justify-end gap-1">
                      <!-- Editar -->
                      <a
                        [routerLink]="['/admin/terras-indigenas/editar', terra.id]"
                        class="w-8 h-8 rounded-lg
             flex items-center justify-center
             text-slate-400
             hover:text-blue-600
             hover:bg-blue-50
             transition-all"
                        title="Editar"
                      >
                        <i class="fa-solid fa-pen-to-square text-xs"></i>
                      </a>

                      <!-- Excluir -->
                      <button
                        type="button"
                        (click)="abrirModalExclusao(terra)"
                        [disabled]="excluindoId === terra.id"
                        class="w-8 h-8 rounded-lg
         flex items-center justify-center
         text-slate-400
         hover:text-rose-600
         hover:bg-rose-50
         disabled:opacity-40
         disabled:cursor-not-allowed
         transition-all"
                        title="Excluir"
                      >
                        @if (excluindoId === terra.id) {
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

                        <span> Carregando terras indígenas... </span>
                      </div>
                    } @else if (erroMensagem) {
                      <span class="text-rose-500 font-semibold">
                        {{ erroMensagem }}
                      </span>
                    } @else {
                      Nenhuma terra indígena encontrada.
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <!-- Rodapé -->
        <div
          class="px-6 py-4 border-t border-slate-100 bg-slate-50/30
         flex flex-col sm:flex-row items-center justify-between
         gap-3 text-xs text-slate-500"
        >
          <span>
            Exibindo
            <strong>{{ terrasPaginadas.length }}</strong>
            de
            <strong>{{ terrasFiltradas.length }}</strong>
            terras indígenas
          </span>

          @if (totalPaginas > 1) {
            <div class="flex items-center gap-2">
              <button
                type="button"
                (click)="paginaAnterior()"
                [disabled]="paginaAtual === 1"
                class="w-8 h-8 rounded-lg border border-slate-200
               bg-white flex items-center justify-center
               text-slate-500 hover:bg-slate-50
               disabled:opacity-40 disabled:cursor-not-allowed
               transition-all"
              >
                <i class="fa-solid fa-chevron-left text-[10px]"></i>
              </button>

              <span class="px-2">
                Página
                <strong class="text-slate-700">{{ paginaAtual }}</strong>
                de
                <strong class="text-slate-700">{{ totalPaginas }}</strong>
              </span>

              <button
                type="button"
                (click)="proximaPagina()"
                [disabled]="paginaAtual === totalPaginas"
                class="w-8 h-8 rounded-lg border border-slate-200
               bg-white flex items-center justify-center
               text-slate-500 hover:bg-slate-50
               disabled:opacity-40 disabled:cursor-not-allowed
               transition-all"
              >
                <i class="fa-solid fa-chevron-right text-[10px]"></i>
              </button>
            </div>
          }
        </div>
      </div>
      <app-confirm-modal
        [aberto]="terraParaExcluir !== null"
        titulo="Excluir Terra Indígena"
        [mensagem]="
          terraParaExcluir
            ? 'Deseja realmente excluir a terra indígena &quot;' +
              terraParaExcluir.nome +
              '&quot;? Esta ação não poderá ser desfeita.'
            : ''
        "
        textoConfirmar="Excluir"
        textoCancelar="Cancelar"
        [carregando]="excluindoId !== null"
        (confirmar)="confirmarExclusao()"
        (cancelar)="fecharModalExclusao()"
      ></app-confirm-modal>
    </div>
  `,
})
export class TerrasIndigenasListaComponent implements OnInit {
  private terraIndigenaService = inject(TerraIndigenaService);
  private cdr = inject(ChangeDetectorRef);

  terrasLista: TerraIndigenaEstatistica[] = [];
  terrasFiltradas: TerraIndigenaEstatistica[] = [];

  filtroTexto = '';

  paginaAtual = 1;
  itensPorPagina = 10;

  carregando = true;
  erroMensagem = '';

  totalTerrasIndigenas = 0;
  totalAldeias = 0;
  totalEscolas = 0;

  ngOnInit(): void {
    this.carregarEstatisticas();
  }

  carregarEstatisticas(): void {
    this.carregando = true;
    this.erroMensagem = '';

    this.terraIndigenaService.listarEstatisticas().subscribe({
      next: (dados) => {
        console.log('✅ Estatísticas das terras indígenas:', dados);

        this.terrasLista = dados ?? [];
        this.terrasFiltradas = [...this.terrasLista];

        this.calcularTotais();

        this.carregando = false;

        this.cdr.markForCheck();
      },

      error: (erro) => {
        console.error('❌ Erro ao carregar terras indígenas:', erro);

        this.erroMensagem = 'Não foi possível carregar as terras indígenas.';

        this.carregando = false;

        this.cdr.markForCheck();
      },
    });
  }

  calcularTotais(): void {
    this.totalTerrasIndigenas = this.terrasLista.length;

    this.totalAldeias = this.terrasLista.reduce(
      (total, terra) => total + terra.quantidadeAldeias,
      0,
    );

    this.totalEscolas = this.terrasLista.reduce(
      (total, terra) => total + terra.quantidadeEscolas,
      0,
    );
  }

  filtrarTerras(): void {
    const termo = this.filtroTexto.trim().toLowerCase();

    this.paginaAtual = 1;

    if (!termo) {
      this.terrasFiltradas = [...this.terrasLista];
      return;
    }

    this.terrasFiltradas = this.terrasLista.filter(
      (terra) =>
        terra.nome.toLowerCase().includes(termo) ||
        terra.municipio.nome.toLowerCase().includes(termo),
    );
  }

  get terrasPaginadas(): TerraIndigenaEstatistica[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;

    return this.terrasFiltradas.slice(inicio, fim);
  }

  get totalPaginas(): number {
    return Math.ceil(this.terrasFiltradas.length / this.itensPorPagina);
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

  excluindoId: number | null = null;
  terraParaExcluir: TerraIndigenaEstatistica | null = null;

  abrirModalExclusao(terra: TerraIndigenaEstatistica): void {
    this.terraParaExcluir = terra;
  }

  fecharModalExclusao(): void {
    if (this.excluindoId !== null) {
      return;
    }

    this.terraParaExcluir = null;
  }

  confirmarExclusao(): void {
    if (!this.terraParaExcluir) {
      return;
    }

    const terra = this.terraParaExcluir;

    this.excluindoId = terra.id;
    this.erroMensagem = '';

    this.terraIndigenaService.excluir(terra.id).subscribe({
      next: () => {
        this.terrasLista = this.terrasLista.filter((item) => item.id !== terra.id);

        this.terrasFiltradas = this.terrasFiltradas.filter((item) => item.id !== terra.id);

        this.calcularTotais();

        if (this.paginaAtual > this.totalPaginas && this.paginaAtual > 1) {
          this.paginaAtual--;
        }

        this.excluindoId = null;
        this.terraParaExcluir = null;

        this.cdr.markForCheck();
      },

      error: (erro) => {
        console.error('Erro ao excluir terra indígena:', erro);

        this.erroMensagem =
          'Não foi possível excluir a terra indígena. Verifique se existem aldeias vinculadas.';

        this.excluindoId = null;
        this.terraParaExcluir = null;

        this.cdr.markForCheck();
      },
    });
  }
}
