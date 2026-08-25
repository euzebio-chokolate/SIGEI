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

export interface MatriculaNivelFormControls {
  nivel: FormControl<string>;
  qtdMasculino: FormControl<number | null>;
  qtdFeminino: FormControl<number | null>;
}

@Component({
  selector: 'app-matriculas-nivel-etapa',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './matriculas-nivel-etapa.component.html'
})
export class MatriculasNivelEtapaComponent {

  @Input({ required: true })
  matriculas!: FormArray<
    FormGroup<MatriculaNivelFormControls>
  >;

  @Input({ required: true })
  niveisMatricula: {
    nivel: string;
    descricao: string;
  }[] = [];

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

  getDescricaoNivel(index: number): string {
    return this.niveisMatricula[index]?.descricao ?? '';
  }

  calcularTotalNivel(index: number): number {
    const grupo = this.matriculas.at(index);

    const masculino = Number(
      grupo.controls.qtdMasculino.value ?? 0
    );

    const feminino = Number(
      grupo.controls.qtdFeminino.value ?? 0
    );

    return masculino + feminino;
  }

}