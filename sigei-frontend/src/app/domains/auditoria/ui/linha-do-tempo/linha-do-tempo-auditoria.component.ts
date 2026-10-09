import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';

import {
  AlteracaoHistorico,
  OperacaoHistorico,
  RevisaoHistorico,
  campoVisivel,
  formatarValor,
  rotuloCampo,
  rotuloEntidade
} from '@domains/auditoria/models/auditoria.models';

const OPERACOES: Record<OperacaoHistorico, { rotulo: string; classe: string }> = {
  CRIACAO: { rotulo: 'Criação', classe: 'bg-emerald-50 text-emerald-700 border-emerald-100' },
  ALTERACAO: { rotulo: 'Alteração', classe: 'bg-sky-50 text-sky-700 border-sky-100' },
  EXCLUSAO: { rotulo: 'Exclusão', classe: 'bg-rose-50 text-rose-700 border-rose-100' }
};

/**
 * Linha do tempo das revisões: autor, data e, de cada registro alterado, os
 * campos antes e depois. Só apresenta; quem usa busca e agrupa os dados.
 */
@Component({
  selector: 'app-linha-do-tempo-auditoria',
  standalone: true,
  imports: [DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './linha-do-tempo-auditoria.component.html'
})
export class LinhaDoTempoAuditoriaComponent {
  readonly revisoes = input.required<RevisaoHistorico[]>();
  /**
   * Na auditoria geral, o tipo e a descrição do registro são o título de cada
   * alteração; na trilha de uma escola, a descrição repete a escola e fica oculta.
   */
  readonly exibirDescricao = input(true);

  protected readonly operacoes = OPERACOES;
  protected readonly rotuloEntidade = rotuloEntidade;
  protected readonly rotuloCampo = rotuloCampo;
  protected readonly formatarValor = formatarValor;

  protected camposVisiveis(alteracao: AlteracaoHistorico) {
    return alteracao.campos.filter(campoVisivel);
  }
}
