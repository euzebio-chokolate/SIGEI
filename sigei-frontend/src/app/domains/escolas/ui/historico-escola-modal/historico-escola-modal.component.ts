import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  RegistroAnualResponse
} from '@domains/escolas/data-access/registro-anual.service';

@Component({
  selector: 'app-historico-escola-modal',
  standalone: true,
  templateUrl: './historico-escola-modal.component.html'
})
export class HistoricoEscolaModalComponent {

  @Input()
  aberto = false;

  @Input()
  registro: RegistroAnualResponse | null = null;

  /** Usados quando o registro do ano não tem infraestrutura (comum em anos anteriores). */
  @Input()
  nomeEscola: string | null = null;

  @Input()
  codigoEscola: string | null = null;

  @Output()
  fechar = new EventEmitter<void>();

  get nome(): string {
    return this.registro?.infraestrutura?.escola?.nome ?? this.nomeEscola ?? '-';
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

  formatarNivel(nivel: string): string {

    switch (nivel) {

      case 'TOTAL_GERAL':
        return 'Total Geral';

      case 'ENSINO_INFANTIL':
        return 'Ensino Infantil';

      case 'FUNDAMENTAL_ANOS_INICIAIS':
        return 'Fundamental - Anos Iniciais';

      case 'FUNDAMENTAL_ANOS_FINAIS':
        return 'Fundamental - Anos Finais';

      case 'ENSINO_MEDIO':
        return 'Ensino Médio';

      case 'EJA':
        return 'EJA';

      case 'AEE':
        return 'AEE';

      default:
        return nivel;
    }
  }

}