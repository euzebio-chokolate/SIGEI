import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/**
 * Formulário "fantasma" exibido enquanto o registro em edição é carregado,
 * para que o usuário não comece a preencher campos que ainda serão sobrescritos.
 */
@Component({
  selector: 'app-skeleton-formulario',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="space-y-6"
      [class]="contorno() ? 'bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6' : ''"
      role="status"
      aria-busy="true"
    >
      <span class="sr-only">{{ rotulo() }}</span>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-5" aria-hidden="true">
        @for (campo of lista(); track campo) {
          <div>
            <div class="h-3 w-24 rounded-full bg-slate-200/80 animate-pulse"></div>
            <div class="mt-2.5 h-10 rounded-xl bg-slate-100 animate-pulse"></div>
          </div>
        }
      </div>
      <div class="flex justify-end gap-3 pt-2" aria-hidden="true">
        <div class="h-10 w-24 rounded-xl bg-slate-100 animate-pulse"></div>
        <div class="h-10 w-36 rounded-xl bg-slate-200/80 animate-pulse"></div>
      </div>
    </div>
  `
})
export class SkeletonFormularioComponent {
  readonly campos = input(4);
  /** Texto lido por leitores de tela. */
  readonly rotulo = input('Carregando...');
  /** false quando o skeleton fica dentro de um card que já tem borda e fundo. */
  readonly contorno = input(true);

  protected readonly lista = computed(() => Array.from({ length: this.campos() }, (_, indice) => indice));
}
