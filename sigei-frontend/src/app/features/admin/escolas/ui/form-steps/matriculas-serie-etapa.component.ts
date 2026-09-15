import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  FormArray,
  FormControl,
  FormGroup,
  ReactiveFormsModule
} from '@angular/forms';

export interface MatriculaSerieFormControls {
  denominacao: FormControl<string>;
  etapaEnsino: FormControl<string>;
  qtdMasculino: FormControl<number | null>;
  qtdFeminino: FormControl<number | null>;
}

@Component({
  selector: 'app-matriculas-serie-etapa',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './matriculas-serie-etapa.component.html'
})
export class MatriculasSerieEtapaComponent {

  @Input({ required: true })
  matriculas!: FormArray<
    FormGroup<MatriculaSerieFormControls>
  >;

  @Input()
  totalMasculino = 0;

  @Input()
  totalFeminino = 0;

  @Input()
  totalGeral = 0;

  @Output()
  avancar = new EventEmitter<void>();

  @Output()
  voltar = new EventEmitter<void>();

  emitirAvancar(): void {
    this.avancar.emit();
  }

  emitirVoltar(): void {
    this.voltar.emit();
  }

  calcularTotalSerie(index: number): number {
    const grupo = this.matriculas.at(index);

    const masculino = Number(
      grupo.controls.qtdMasculino.value ?? 0
    );

    const feminino = Number(
      grupo.controls.qtdFeminino.value ?? 0
    );

    return masculino + feminino;
  }

  formatarEtapa(etapa: string): string {
    switch (etapa) {
      case 'FUNDAMENTAL_ANOS_INICIAIS':
        return 'Fundamental - Anos Iniciais';

      case 'FUNDAMENTAL_ANOS_FINAIS':
        return 'Fundamental - Anos Finais';

      case 'ENSINO_MEDIO':
        return 'Ensino Médio';

      default:
        return etapa;
    }
  }

}