import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { TipoToast, ToastService } from '@core/notificacao/toast.service';

const ESTILOS: Record<TipoToast, { icone: string; cor: string; titulo: string }> = {
  sucesso: { icone: 'fa-circle-check', cor: 'bg-emerald-50 text-emerald-600', titulo: 'Tudo certo' },
  erro: { icone: 'fa-circle-exclamation', cor: 'bg-rose-50 text-rose-600', titulo: 'Algo deu errado' },
  aviso: { icone: 'fa-triangle-exclamation', cor: 'bg-amber-50 text-amber-600', titulo: 'Atenção' },
  info: { icone: 'fa-circle-info', cor: 'bg-sky-50 text-sky-600', titulo: 'Informação' }
};

/** Avisos flutuantes da aplicação. Fica uma única vez no componente raiz. */
@Component({
  selector: 'app-toast-container',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="fixed z-[1000] bottom-4 inset-x-4 sm:inset-x-auto sm:right-4 sm:bottom-auto sm:top-4
             flex flex-col gap-3 sm:w-96 pointer-events-none"
      aria-live="polite"
      aria-atomic="false"
    >
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          class="pointer-events-auto bg-white rounded-2xl shadow-xl border border-slate-200
                 p-4 flex items-start gap-3 animate-in fade-in slide-in-from-top-2"
          [attr.role]="toast.tipo === 'erro' ? 'alert' : 'status'"
        >
          <div class="w-9 h-9 rounded-xl flex items-center justify-center shrink-0 {{ estilos[toast.tipo].cor }}">
            <i class="fa-solid {{ estilos[toast.tipo].icone }}"></i>
          </div>

          <div class="flex-1 min-w-0">
            <p class="text-sm font-bold text-slate-800">
              {{ toast.titulo ?? estilos[toast.tipo].titulo }}
            </p>
            <p class="text-sm text-slate-500 mt-0.5 leading-relaxed break-words">
              {{ toast.mensagem }}
            </p>
          </div>

          <button
            type="button"
            (click)="toastService.fechar(toast.id)"
            class="w-8 h-8 -mr-1 -mt-1 rounded-lg text-slate-400 hover:bg-slate-100 hover:text-slate-600
                   flex items-center justify-center shrink-0"
            aria-label="Fechar aviso"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
      }
    </div>
  `
})
export class ToastContainerComponent {
  protected readonly toastService = inject(ToastService);
  protected readonly estilos = ESTILOS;
}
