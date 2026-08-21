import { Component, EventEmitter, Input, Output } from '@angular/core';

export interface ListModalItem {
  id: number;
  nome: string;
}

@Component({
  selector: 'app-list-modal',
  standalone: true,
  template: `
    @if (aberto) {
      <div
        class="fixed inset-0 z-50 flex items-center justify-center
               bg-slate-900/50 backdrop-blur-sm px-4"
      >
        <div
          class="w-full max-w-lg bg-white rounded-2xl
                 shadow-2xl border border-slate-200 overflow-hidden"
        >
          <!-- Cabeçalho -->
          <div
            class="px-6 py-5 border-b border-slate-100
                   flex items-start justify-between gap-4"
          >
            <div>
              <h2 class="text-lg font-bold text-slate-800">
                {{ titulo }}
              </h2>

              @if (subtitulo) {
                <p class="text-sm text-slate-500 mt-1">
                  {{ subtitulo }}
                </p>
              }
            </div>

            <button
              type="button"
              (click)="fechar.emit()"
              class="w-9 h-9 rounded-lg
                     flex items-center justify-center
                     text-slate-400 hover:text-slate-700
                     hover:bg-slate-100 transition-all"
              title="Fechar"
            >
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>

          <!-- Conteúdo -->
          <div class="p-6">
            @if (itens.length > 0) {
              <div class="space-y-2 max-h-96 overflow-y-auto pr-1">
                @for (item of itens; track item.id) {
                  <div
                    class="flex items-center gap-3
                           px-4 py-3 rounded-xl
                           border border-slate-100
                           bg-slate-50/60"
                  >
                    <div
                      class="w-9 h-9 rounded-lg
                             bg-emerald-50 text-emerald-600
                             flex items-center justify-center shrink-0"
                    >
                      <i class="fa-solid fa-tents text-xs"></i>
                    </div>

                    <span class="text-sm font-semibold text-slate-700">
                      {{ item.nome }}
                    </span>
                  </div>
                }
              </div>
            } @else {
              <div class="py-8 text-center text-sm text-slate-400">Nenhum item encontrado.</div>
            }
          </div>

          <!-- Rodapé -->
          <div
            class="px-6 py-4 bg-slate-50
                   border-t border-slate-100
                   flex justify-end"
          >
            <button
              type="button"
              (click)="fechar.emit()"
              class="px-4 py-2.5 rounded-xl
                     text-sm font-semibold
                     text-slate-600
                     hover:bg-slate-200/70
                     transition-all"
            >
              Fechar
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ListModalComponent {
  @Input() aberto = false;

  @Input() titulo = 'Itens';

  @Input() subtitulo = '';

  @Input() itens: ListModalItem[] = [];

  @Output() fechar = new EventEmitter<void>();
}
