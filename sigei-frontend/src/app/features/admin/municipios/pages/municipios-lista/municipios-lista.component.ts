import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { MunicipioService, MunicipioEstatistica } from '@domains/territorios/data-access/municipio.service';
import { mensagemErroCarregamento } from '@core/http/erro-http';

@Component({
  selector: 'app-municipios-lista',
  standalone: true,
  imports: [RouterLink, FormsModule],
  template: `
    <div class="space-y-5 font-sans pb-10">
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i>
          Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800"> Municípios </span>
      </nav>

      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Municípios</h1>
          <p class="text-sm text-slate-500 mt-1">
            Consulte a cobertura e estatísticas de terras, aldeias e escolas por município.
          </p>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Municípios Cadastrados
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalMunicipios }}
            </p>
          </div>
          <div class="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600 text-lg">
            <i class="fa-solid fa-city"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Terras Indígenas
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalTerrasIndigenas }}
            </p>
          </div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 text-lg">
            <i class="fa-solid fa-map-location-dot"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Total de Aldeias
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
              Escolas Indígenas
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalEscolas }}
            </p>
          </div>
          <div class="w-12 h-12 bg-sky-50 rounded-xl flex items-center justify-center text-sky-600 text-lg">
            <i class="fa-solid fa-school"></i>
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
              (ngModelChange)="filtrarMunicipios()"
              placeholder="Buscar município por nome..."
              class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all text-slate-700 placeholder:text-slate-400 shadow-sm"
            />
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/80 text-slate-400 text-[11px] font-bold uppercase tracking-wider border-b border-slate-100">
                <th class="px-6 py-3.5">Município</th>
                <th class="px-6 py-3.5 text-center">Terras Indígenas</th>
                <th class="px-6 py-3.5 text-center">Aldeias</th>
                <th class="px-6 py-3.5 text-center">Escolas</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 text-sm text-slate-700">
              @for (mun of municipiosPaginados; track mun.id) {
                <tr class="hover:bg-slate-50/60 transition-colors group">
                  <td class="px-6 py-4 font-bold text-slate-800">
                    {{ mun.nome }}
                  </td>
                  <td class="px-6 py-4 text-center font-medium text-slate-600">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-100">
                      <i class="fa-solid fa-map-location-dot text-[10px]"></i>
                      {{ mun.quantidadeTerrasIndigenas }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-center font-medium text-slate-600">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-100">
                      <i class="fa-solid fa-tents text-[10px]"></i>
                      {{ mun.quantidadeAldeias }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-center font-medium text-slate-600">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-sky-50 text-sky-700 border border-sky-100">
                      <i class="fa-solid fa-school text-[10px]"></i>
                      {{ mun.quantidadeEscolas }}
                    </span>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="4" class="px-6 py-8 text-center text-slate-400 text-sm">
                    @if (carregando) {
                      <div class="flex items-center justify-center gap-2">
                        <i class="fa-solid fa-circle-notch animate-spin text-emerald-600"></i>
                        <span> Carregando estatísticas dos municípios... </span>
                      </div>
                    } @else if (erroMensagem) {
                      <span class="text-rose-500 font-semibold">
                        {{ erroMensagem }}
                      </span>
                    } @else {
                      Nenhum município encontrado.
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <div class="px-6 py-4 border-t border-slate-100 bg-slate-50/30 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <div>
            Exibindo <strong class="text-slate-700">{{ fimExibicao }}</strong> de <strong class="text-slate-700">{{ municipiosFiltrados.length }}</strong> municípios
          </div>

          <div class="flex items-center gap-2">
            <button
              type="button"
              (click)="irParaPagina(1)"
              [disabled]="paginaAtual === 1"
              class="w-8 h-8 rounded-lg border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 hover:text-emerald-600 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              title="Primeira página"
            >
              <i class="fa-solid fa-angles-left text-[10px]"></i>
            </button>

            <button
              type="button"
              (click)="paginaAnterior()"
              [disabled]="paginaAtual === 1"
              class="w-8 h-8 rounded-lg border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 hover:text-emerald-600 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              title="Página anterior"
            >
              <i class="fa-solid fa-chevron-left text-[10px]"></i>
            </button>

            <span class="min-w-[100px] text-center px-3 py-2 bg-emerald-50 text-emerald-700 rounded-lg font-semibold">
              Página {{ paginaAtual }} de {{ totalPaginas || 1 }}
            </span>

            <button
              type="button"
              (click)="proximaPagina()"
              [disabled]="paginaAtual >= totalPaginas"
              class="w-8 h-8 rounded-lg border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 hover:text-emerald-600 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              title="Próxima página"
            >
              <i class="fa-solid fa-chevron-right text-[10px]"></i>
            </button>

            <button
              type="button"
              (click)="irParaPagina(totalPaginas)"
              [disabled]="paginaAtual >= totalPaginas"
              class="w-8 h-8 rounded-lg border border-slate-200 bg-white flex items-center justify-center text-slate-500 hover:bg-slate-50 hover:text-emerald-600 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
              title="Última página"
            >
              <i class="fa-solid fa-angles-right text-[10px]"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class MunicipiosListaComponent implements OnInit {
  private municipioService = inject(MunicipioService);
  private cdr = inject(ChangeDetectorRef);

  municipiosLista: MunicipioEstatistica[] = [];
  municipiosFiltrados: MunicipioEstatistica[] = [];
  filtroTexto = '';
  carregando = true;
  erroMensagem = '';
  
  totalMunicipios = 0;
  totalTerrasIndigenas = 0;
  totalAldeias = 0;
  totalEscolas = 0;

  readonly itensPorPagina = 6;
  paginaAtual = 1;

  ngOnInit(): void {
    this.carregarEstatisticas();
  }

  get totalPaginas(): number {
    return Math.ceil(this.municipiosFiltrados.length / this.itensPorPagina);
  }

  get municipiosPaginados(): MunicipioEstatistica[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.municipiosFiltrados.slice(inicio, fim);
  }

  get inicioExibicao(): number {
    if (this.municipiosFiltrados.length === 0) {
      return 0;
    }
    return (this.paginaAtual - 1) * this.itensPorPagina + 1;
  }

  get fimExibicao(): number {
    return Math.min(this.paginaAtual * this.itensPorPagina, this.municipiosFiltrados.length);
  }

  carregarEstatisticas(): void {
    this.carregando = true;
    this.erroMensagem = '';

    this.municipioService.listarEstatisticas().subscribe({
      next: (dados) => {
        this.municipiosLista = dados ?? [];
        this.municipiosFiltrados = [...this.municipiosLista];
        this.calcularTotais(this.municipiosLista);
        
        this.paginaAtual = 1;
        this.carregando = false;
        
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar estatísticas dos municípios:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar as estatísticas dos municípios.');
        this.carregando = false;
        this.paginaAtual = 1;
        this.cdr.markForCheck();
      },
    });
  }

  calcularTotais(dados: MunicipioEstatistica[]): void {
    this.totalMunicipios = dados.length;
    this.totalTerrasIndigenas = dados.reduce((acc, mun) => acc + (mun.quantidadeTerrasIndigenas || 0), 0);
    this.totalAldeias = dados.reduce((acc, mun) => acc + (mun.quantidadeAldeias || 0), 0);
    this.totalEscolas = dados.reduce((acc, mun) => acc + (mun.quantidadeEscolas || 0), 0);
  }

  filtrarMunicipios(): void {
    const termo = this.filtroTexto.trim().toLowerCase();

    if (!termo) {
      this.municipiosFiltrados = [...this.municipiosLista];
    } else {
      this.municipiosFiltrados = this.municipiosLista.filter((municipio) =>
        municipio.nome?.toLowerCase().includes(termo),
      );
    }

    this.paginaAtual = 1;
  }

  // --- Métodos de Paginação ---
  irParaPagina(pagina: number): void {
    if (this.totalPaginas === 0) {
      this.paginaAtual = 1;
      return;
    }

    if (pagina < 1) {
      this.paginaAtual = 1;
      return;
    }

    if (pagina > this.totalPaginas) {
      this.paginaAtual = this.totalPaginas;
      return;
    }

    this.paginaAtual = pagina;
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
