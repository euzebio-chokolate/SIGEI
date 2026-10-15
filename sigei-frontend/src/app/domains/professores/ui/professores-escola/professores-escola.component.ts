import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Subscription } from 'rxjs';

import { mensagemErroCarregamento } from '@core/http/erro-http';
import { LotacaoProfessorResponse, LotacaoProfessorService } from '@domains/professores/data-access/lotacao-professor.service';
import { rotuloTipoContrato } from '@domains/professores/models/professor.models';
import { EstadoErroComponent } from '@shared/ui/estado-erro/estado-erro.component';

/**
 * Professores lotados na escola no ano, inclusive as vagas em aberto. Não
 * mostra CPF, matrícula nem número do contrato, então serve a todos os perfis.
 */
@Component({
  selector: 'app-professores-escola',
  standalone: true,
  imports: [EstadoErroComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './professores-escola.component.html'
})
export class ProfessoresEscolaComponent {
  private readonly lotacaoService = inject(LotacaoProfessorService);

  readonly escolaId = input.required<number>();
  readonly ano = input.required<number>();

  protected readonly rotuloContrato = rotuloTipoContrato;
  protected readonly carregando = signal(false);
  protected readonly erro = signal('');
  protected readonly lotacoes = signal<LotacaoProfessorResponse[]>([]);
  /** Lotações além das 100 trazidas (não deve ocorrer: as maiores escolas têm poucas dezenas). */
  protected readonly naoExibidas = signal(0);

  protected readonly professores = computed(() =>
    new Set(this.lotacoes().filter(lotacao => lotacao.professor).map(lotacao => lotacao.professor!.id)).size
  );
  protected readonly vagas = computed(() => this.lotacoes().filter(lotacao => !lotacao.professor).length);

  private consulta?: Subscription;

  constructor() {
    effect(() => {
      const escolaId = this.escolaId();
      const ano = this.ano();
      untracked(() => this.carregar(escolaId, ano));
    });
  }

  carregar(escolaId = this.escolaId(), ano = this.ano()): void {
    this.consulta?.unsubscribe();
    this.carregando.set(true);
    this.erro.set('');
    this.lotacoes.set([]);
    this.naoExibidas.set(0);

    this.consulta = this.lotacaoService.listarPorEscolaEAno(escolaId, ano).subscribe({
      next: pagina => {
        this.lotacoes.set(pagina.itens ?? []);
        this.naoExibidas.set(Math.max(0, pagina.totalItens - (pagina.itens?.length ?? 0)));
        this.carregando.set(false);
      },
      error: erro => {
        console.error('Erro ao carregar os professores da escola:', erro);
        this.erro.set(mensagemErroCarregamento(erro, 'Não foi possível carregar os professores desta escola.'));
        this.carregando.set(false);
      }
    });
  }
}
