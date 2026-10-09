import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';

import { interpretarErro, mensagemErroCarregamento } from '@core/http/erro-http';
import { EscolaService } from '@domains/escolas/data-access/escola.service';
import { RevisaoHistorico, agruparPorRevisao } from '@domains/auditoria/models/auditoria.models';
import { LinhaDoTempoAuditoriaComponent } from '@domains/auditoria/ui/linha-do-tempo/linha-do-tempo-auditoria.component';
import { EstadoErroComponent } from '@shared/ui/estado-erro/estado-erro.component';

/** Revisões exibidas por vez; escolas antigas podem ter centenas. */
const POR_PAGINA = 15;

/**
 * Quem alterou o quê e quando na escola, a partir da auditoria (Envers) do
 * backend. A consulta percorre todas as versões das entidades da escola,
 * então só é feita quando o usuário pede.
 */
@Component({
  selector: 'app-trilha-auditoria',
  standalone: true,
  imports: [EstadoErroComponent, LinhaDoTempoAuditoriaComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './trilha-auditoria.component.html'
})
export class TrilhaAuditoriaComponent {
  private readonly escolaService = inject(EscolaService);

  readonly escolaId = input.required<number>();

  protected readonly carregando = signal(false);
  protected readonly erro = signal('');
  protected readonly consultado = signal(false);
  private readonly revisoes = signal<RevisaoHistorico[]>([]);
  private readonly limite = signal(POR_PAGINA);

  protected readonly visiveis = computed(() => this.revisoes().slice(0, this.limite()));
  protected readonly restantes = computed(() => Math.max(0, this.revisoes().length - this.limite()));
  protected readonly total = computed(() => this.revisoes().length);

  constructor() {
    // Outra escola selecionada: descarta a trilha anterior
    effect(() => {
      this.escolaId();
      untracked(() => {
        this.revisoes.set([]);
        this.consultado.set(false);
        this.erro.set('');
        this.limite.set(POR_PAGINA);
      });
    });
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set('');

    this.escolaService.buscarAuditoria(this.escolaId()).subscribe({
      next: alteracoes => {
        this.revisoes.set(agruparPorRevisao(alteracoes ?? []));
        this.consultado.set(true);
        this.carregando.set(false);
      },
      error: erro => {
        console.error('Erro ao carregar a trilha de auditoria:', erro);
        // 404: a escola existe, mas ainda não tem alterações auditadas (cadastrada antes da auditoria)
        if (interpretarErro(erro).status === 404) {
          this.revisoes.set([]);
          this.consultado.set(true);
        } else {
          this.erro.set(mensagemErroCarregamento(erro, 'Não foi possível carregar o histórico de alterações.'));
        }
        this.carregando.set(false);
      }
    });
  }

  mostrarMais(): void {
    this.limite.update(limite => limite + POR_PAGINA);
  }
}
