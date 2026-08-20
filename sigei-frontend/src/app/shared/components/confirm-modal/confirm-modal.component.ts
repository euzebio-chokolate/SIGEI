import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  template: `
    @if (aberto) {
      <!-- Fundo escuro -->
      <div
        class="fixed inset-0 z-50 flex items-center justify-center
               bg-slate-900/50 backdrop-blur-sm px-4"
      >
        <!-- Modal -->
        <div
          class="w-full max-w-md bg-white rounded-2xl
                 shadow-2xl border border-slate-200
                 overflow-hidden animate-in fade-in zoom-in-95"
        >
          <!-- Conteúdo -->
          <div class="p-6">
            <div class="flex items-start gap-4">
              <!-- Ícone -->
              <div
                class="w-12 h-12 rounded-xl bg-rose-50
                       text-rose-600 flex items-center
                       justify-center shrink-0"
              >
                <i class="fa-solid fa-triangle-exclamation text-lg"></i>
              </div>

              <div class="flex-1">
                <h2 class="text-lg font-bold text-slate-800">
                  {{ titulo }}
                </h2>

                <p class="text-sm text-slate-500 mt-2 leading-relaxed">
                  {{ mensagem }}
                </p>
              </div>
            </div>
          </div>

          <!-- Botões -->
          <div
            class="px-6 py-4 bg-slate-50 border-t border-slate-100
                   flex items-center justify-end gap-3"
          >
            <button
              type="button"
              (click)="cancelar.emit()"
              [disabled]="carregando"
              class="px-4 py-2.5 rounded-xl text-sm font-semibold
                     text-slate-600 hover:bg-slate-200/70
                     transition-all disabled:opacity-50"
            >
              {{ textoCancelar }}
            </button>

            <button
              type="button"
              (click)="confirmar.emit()"
              [disabled]="carregando"
              class="px-4 py-2.5 rounded-xl text-sm font-semibold
                     bg-rose-600 hover:bg-rose-700
                     text-white transition-all shadow-sm
                     disabled:bg-rose-400
                     disabled:cursor-not-allowed
                     flex items-center gap-2"
            >
              @if (carregando) {
                <i
                  class="fa-solid fa-circle-notch
                         animate-spin text-xs"
                ></i>

                Aguarde...
              } @else {
                <i class="fa-solid fa-trash text-xs"></i>

                {{ textoConfirmar }}
              }
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmModalComponent {
  @Input() aberto = false;

  @Input() titulo = 'Confirmar ação';

  @Input() mensagem = 'Tem certeza que deseja continuar?';

  @Input() textoConfirmar = 'Confirmar';

  @Input() textoCancelar = 'Cancelar';

  @Input() carregando = false;

  @Output() confirmar = new EventEmitter<void>();

  @Output() cancelar = new EventEmitter<void>();
}
