import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

/**
 * Falha ao carregar uma lista, com a opção de repetir a requisição sem
 * recarregar a página inteira.
 */
@Component({
  selector: 'app-estado-erro',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex flex-col items-center gap-3 py-4 text-center" role="alert">
      <span class="grid h-11 w-11 place-items-center rounded-xl bg-rose-50 text-rose-500">
        <i class="fa-solid fa-triangle-exclamation"></i>
      </span>
      <p class="text-sm font-semibold text-slate-600">{{ mensagem() }}</p>
      <button
        type="button"
        (click)="tentarNovamente.emit()"
        class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-600 shadow-sm transition hover:bg-slate-50"
      >
        <i class="fa-solid fa-rotate-right text-[11px]"></i>
        Tentar novamente
      </button>
    </div>
  `
})
export class EstadoErroComponent {
  readonly mensagem = input.required<string>();
  readonly tentarNovamente = output<void>();
}
