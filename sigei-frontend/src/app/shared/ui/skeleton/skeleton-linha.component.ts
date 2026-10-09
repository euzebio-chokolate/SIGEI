import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

/** Larguras alternadas para as barras não ficarem uniformes demais. */
const LARGURAS = ['w-3/4', 'w-1/2', 'w-2/3', 'w-5/12', 'w-7/12'];

/**
 * Linha de tabela "fantasma" exibida enquanto a lista é carregada, no lugar
 * do spinner: mantém o layout da tabela e evita o salto quando os dados chegam.
 *
 * `@for (linha of [1, 2, 3, 4, 5]; track linha) { <tr appSkeletonLinha [colunas]="4"></tr> }`
 */
@Component({
  selector: 'tr[appSkeletonLinha]',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { 'aria-hidden': 'true' },
  template: `
    @for (largura of larguras(); track $index) {
      <td class="px-6 py-4">
        <div class="h-3.5 rounded-full bg-slate-200/80 animate-pulse {{ largura }}"></div>
        @if ($first) {
          <div class="mt-2 h-2.5 w-1/3 rounded-full bg-slate-100 animate-pulse"></div>
        }
      </td>
    }
  `
})
export class SkeletonLinhaComponent {
  readonly colunas = input.required<number>();

  protected readonly larguras = computed(() =>
    Array.from({ length: this.colunas() }, (_, indice) => LARGURAS[indice % LARGURAS.length])
  );
}
