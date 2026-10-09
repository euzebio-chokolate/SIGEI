import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PovoResponse, PovoService } from '@domains/territorios/data-access/povo.service';
import { SeAdminDirective } from '@core/auth/se-admin.directive';
import { mensagemErroCarregamento, mensagemErroExclusao } from '@core/http/erro-http';
import { ToastService } from '@core/notificacao/toast.service';
import { ListModalComponent, ListModalItem } from '@shared/ui/list-modal/list-modal.component';
import { ConfirmModalComponent } from '@shared/ui/confirm-modal/confirm-modal.component';
import { SkeletonLinhaComponent } from '@shared/ui/skeleton/skeleton-linha.component';
import { EstadoErroComponent } from '@shared/ui/estado-erro/estado-erro.component';

@Component({
  selector: 'app-povos-lista',
  standalone: true,
  imports: [RouterLink, FormsModule, ListModalComponent, ConfirmModalComponent, SeAdminDirective, SkeletonLinhaComponent, EstadoErroComponent],
  template: `
    <div class="space-y-5 font-sans pb-10">
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i>
          Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800"> Povos </span>
      </nav>

      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Povos Indígenas</h1>
          <p class="text-sm text-slate-500 mt-1">
            Consulte os povos indígenas e as aldeias vinculadas.
          </p>
        </div>

        <a *seAdmin routerLink="/admin/povos/novo" class="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all shrink-0">
          <i class="fa-solid fa-plus text-xs"></i>
          Novo Povo
        </a>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Povos Cadastrados
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalPovos }}
            </p>
          </div>
          <div class="w-12 h-12 bg-violet-50 rounded-xl flex items-center justify-center text-violet-600 text-lg">
            <i class="fa-solid fa-people-group"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Vínculos com Aldeias
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">
              {{ totalVinculosAldeias }}
            </p>
          </div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center text-amber-600 text-lg">
            <i class="fa-solid fa-tents"></i>
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
              (ngModelChange)="filtrarPovos()"
              placeholder="Buscar povo por nome..."
              class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all text-slate-700 placeholder:text-slate-400 shadow-sm"
            />
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/80 text-slate-400 text-[11px] font-bold uppercase tracking-wider border-b border-slate-100">
                <th class="px-6 py-3.5">Povo</th>
                <th class="px-6 py-3.5 text-center">Aldeias vinculadas</th>
                <th class="px-6 py-3.5">Aldeias</th>
                <th *seAdmin class="px-6 py-3.5 text-right">Ações</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-slate-100 text-sm text-slate-700">
              @for (povo of povosPaginados; track povo.id) {
                <tr class="hover:bg-slate-50/60 transition-colors">
                  <td class="px-6 py-4 font-bold text-slate-800">
                    <div class="flex items-center gap-3">
                      <div class="w-9 h-9 rounded-lg bg-violet-50 text-violet-600 flex items-center justify-center">
                        <i class="fa-solid fa-people-group text-xs"></i>
                      </div>
                      {{ povo.nome }}
                    </div>
                  </td>

                  <td class="px-6 py-4 text-center">
                    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-100">
                      <i class="fa-solid fa-tents text-[10px]"></i>
                      {{ povo.aldeias.length }}
                    </span>
                  </td>

                  <td class="px-6 py-4">
                    @if (povo.aldeias.length > 0) {
                      <div class="flex flex-wrap items-center gap-1.5">
                        @for (aldeia of povo.aldeias.slice(0, 3); track aldeia.id) {
                          <span class="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-semibold bg-slate-100 text-slate-600">
                            {{ aldeia.nome }}
                          </span>
                        }

                        @if (povo.aldeias.length > 3) {
                          <span class="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-semibold bg-violet-50 text-violet-700 border border-violet-100">
                            +{{ povo.aldeias.length - 3 }}
                          </span>
                          <button
                            type="button"
                            (click)="abrirAldeias(povo)"
                            class="text-xs font-semibold text-emerald-600 hover:text-emerald-700 hover:underline ml-1 transition-colors"
                          >
                            Ver todas
                          </button>
                        }
                      </div>
                    } @else {
                      <span class="text-xs text-slate-400"> Nenhuma aldeia vinculada </span>
                    }
                  </td>

                  <td *seAdmin class="px-6 py-4 text-right">
                    <div class="flex items-center justify-end gap-1">
                      <a
                        [routerLink]="['/admin/povos/editar', povo.id]"
                        class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-all"
                        title="Editar"
                      >
                        <i class="fa-solid fa-pen-to-square text-xs"></i>
                      </a>
                      
                      <button
                        type="button"
                        (click)="abrirModalExclusao(povo)"
                        [disabled]="excluindoId === povo.id"
                        class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-rose-50 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
                        title="Excluir"
                      >
                        @if (excluindoId === povo.id) {
                          <i class="fa-solid fa-circle-notch animate-spin text-xs"></i>
                        } @else {
                          <i class="fa-solid fa-trash text-xs"></i>
                        }
                      </button>
                    </div>
                  </td>
                </tr>
              } @empty {
                @if (carregando) {
                  @for (linha of [1, 2, 3, 4, 5, 6]; track linha) {
                    <tr appSkeletonLinha [colunas]="3"></tr>
                  }
                } @else {
                  <tr>
                    <td colspan="4" class="px-6 py-8 text-center text-slate-400 text-sm">
                      @if (erroMensagem) {
                        <app-estado-erro [mensagem]="erroMensagem" (tentarNovamente)="carregarPovos()" />
                      } @else {
                        Nenhum povo encontrado.
                      }
                    </td>
                  </tr>
                }
              }
            </tbody>
          </table>
        </div>

        <div class="px-6 py-4 border-t border-slate-100 bg-slate-50/30 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <span>
            Exibindo <strong>{{ povosPaginados.length }}</strong> de <strong>{{ povosFiltrados.length }}</strong> povos
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

      <app-list-modal
        [aberto]="povoSelecionado !== null"
        titulo="Aldeias vinculadas"
        [subtitulo]="povoSelecionado ? povoSelecionado.nome + ' — ' + povoSelecionado.aldeias.length + ' aldeia(s)' : ''"
        [itens]="aldeiasModal"
        (fechar)="fecharAldeias()"
      ></app-list-modal>

      <app-confirm-modal
        [aberto]="povoParaExcluir !== null"
        titulo="Excluir Povo"
        [mensagem]="povoParaExcluir ? 'Deseja realmente excluir o povo &quot;' + povoParaExcluir.nome + '&quot;? Esta ação não poderá ser desfeita.' : ''"
        textoConfirmar="Excluir"
        textoCancelar="Cancelar"
        [carregando]="excluindoId !== null"
        (confirmar)="confirmarExclusao()"
        (cancelar)="fecharModalExclusao()"
      ></app-confirm-modal>
    </div>
  `,
})
export class PovosListaComponent implements OnInit {
  // --- Injeções de Dependência ---
  private readonly povoService = inject(PovoService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);

  // --- Propriedades de Estado da Listagem ---
  povosLista: PovoResponse[] = [];
  povosFiltrados: PovoResponse[] = [];
  filtroTexto = '';
  totalPovos = 0;
  totalVinculosAldeias = 0;
  carregando = true;
  erroMensagem = '';

  // --- Propriedades de Paginação ---
  paginaAtual = 1;
  itensPorPagina = 10;

  // --- Propriedades de Modais ---
  povoSelecionado: PovoResponse | null = null;
  aldeiasModal: ListModalItem[] = [];
  povoParaExcluir: PovoResponse | null = null;
  excluindoId: number | null = null;

  // --- Ciclo de Vida ---
  ngOnInit(): void {
    this.carregarPovos();
  }

  // --- Getters ---
  get povosPaginados(): PovoResponse[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = inicio + this.itensPorPagina;
    return this.povosFiltrados.slice(inicio, fim);
  }

  get totalPaginas(): number {
    return Math.ceil(this.povosFiltrados.length / this.itensPorPagina);
  }

  // --- Métodos de Dados e Filtros ---
  carregarPovos(): void {
    this.carregando = true;
    this.erroMensagem = '';

    this.povoService.listar().subscribe({
      next: (dados) => {
        this.povosLista = [...(dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome));
        this.povosFiltrados = [...this.povosLista];
        this.calcularTotais();
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar povos:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os povos.');
        this.carregando = false;
        this.cdr.markForCheck();
      },
    });
  }

  calcularTotais(): void {
    this.totalPovos = this.povosLista.length;
    this.totalVinculosAldeias = this.povosLista.reduce(
      (total, povo) => total + povo.aldeias.length,
      0,
    );
  }

  filtrarPovos(): void {
    const termo = this.filtroTexto.trim().toLowerCase();
    this.paginaAtual = 1;

    if (!termo) {
      this.povosFiltrados = [...this.povosLista];
      return;
    }

    this.povosFiltrados = this.povosLista.filter((povo) => {
      const nomePovo = povo.nome.toLowerCase().includes(termo);
      const nomeAldeia = povo.aldeias.some((aldeia) => aldeia.nome.toLowerCase().includes(termo));
      return nomePovo || nomeAldeia;
    });
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

  // --- Métodos de Controle de Modais (Aldeias) ---
  abrirAldeias(povo: PovoResponse): void {
    this.povoSelecionado = povo;
    this.aldeiasModal = povo.aldeias.map((aldeia) => ({
      id: aldeia.id,
      nome: aldeia.nome,
    }));
  }

  fecharAldeias(): void {
    this.povoSelecionado = null;
    this.aldeiasModal = [];
  }

  // --- Métodos de Controle de Modais (Exclusão) ---
  abrirModalExclusao(povo: PovoResponse): void {
    this.povoParaExcluir = povo;
  }

  fecharModalExclusao(): void {
    if (this.excluindoId !== null) {
      return;
    }
    this.povoParaExcluir = null;
  }

  confirmarExclusao(): void {
    if (!this.povoParaExcluir) {
      return;
    }

    const povo = this.povoParaExcluir;
    this.excluindoId = povo.id;
    this.erroMensagem = '';

    this.povoService.excluir(povo.id).subscribe({
      next: () => {
        this.povosLista = this.povosLista.filter((item) => item.id !== povo.id);
        this.povosFiltrados = this.povosFiltrados.filter((item) => item.id !== povo.id);

        this.calcularTotais();

        if (this.paginaAtual > this.totalPaginas && this.paginaAtual > 1) {
          this.paginaAtual--;
        }

        this.toast.sucesso(`O povo ${povo.nome} foi excluído.`, 'Povo excluído');
        this.excluindoId = null;
        this.povoParaExcluir = null;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao excluir povo:', erro);
        const mensagem = mensagemErroExclusao(erro, 'Não foi possível excluir o povo.', 'Não é possível excluir o povo: existem aldeias vinculadas a ele.');
        if (mensagem) this.toast.erro(mensagem);
        this.excluindoId = null;
        this.povoParaExcluir = null;
        this.cdr.markForCheck();
      },
    });
  }
}