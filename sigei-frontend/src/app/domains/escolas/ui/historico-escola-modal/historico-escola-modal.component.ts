import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  RegistroAnualResponse
} from '@domains/escolas/data-access/registro-anual.service';
import { ProfessoresEscolaComponent } from '@domains/professores/ui/professores-escola/professores-escola.component';
import {
  rotuloOpcao,
  SITUACOES_ESCOLA,
  TIPOS_ACESSO,
  TIPOS_AGUA,
  TIPOS_ENERGIA
} from '@domains/escolas/models/infraestrutura-opcoes';
import { rotuloNivel } from '@domains/escolas/models/matricula-opcoes';

@Component({
  selector: 'app-historico-escola-modal',
  standalone: true,
  imports: [ProfessoresEscolaComponent],
  templateUrl: './historico-escola-modal.component.html'
})
export class HistoricoEscolaModalComponent {

  readonly situacoes = SITUACOES_ESCOLA;
  readonly tiposAcesso = TIPOS_ACESSO;
  readonly tiposAgua = TIPOS_AGUA;
  readonly tiposEnergia = TIPOS_ENERGIA;
  readonly rotulo = rotuloOpcao;
  readonly rotuloNivel = rotuloNivel;

  @Input()
  aberto = false;

  @Input()
  registro: RegistroAnualResponse | null = null;

  /** Usados quando o registro do ano não tem infraestrutura (comum em anos anteriores). */
  @Input()
  nomeEscola: string | null = null;

  @Input()
  codigoEscola: string | null = null;

  @Input()
  escolaId: number | null = null;

  @Output()
  fechar = new EventEmitter<void>();

  get nome(): string {
    return this.registro?.infraestrutura?.escola?.nome ?? this.nomeEscola ?? '-';
  }

  /** Escola das lotações exibidas; sem o ID informado, vem da infraestrutura do registro. */
  get idEscola(): number | null {
    return this.escolaId ?? this.registro?.infraestrutura?.escola?.id ?? null;
  }

  get codigo(): string {
    return this.registro?.infraestrutura?.escola?.codigo ?? this.codigoEscola ?? '-';
  }

  fecharModal(): void {
    this.fechar.emit();
  }

  simNao(valor: boolean | null | undefined): string {
    return valor ? 'Sim' : 'Não';
  }

  obterTotalGeral(): number | string {

    if (this.registro?.totalAlunos !== null && this.registro?.totalAlunos !== undefined) {
      return this.registro.totalAlunos;
    }

    const total =
      this.registro?.matriculasNivel
        ?.find(
          item =>
            item.nivel === 'TOTAL_GERAL'
        );

    return total?.total ?? 'Não informado';
  }

}