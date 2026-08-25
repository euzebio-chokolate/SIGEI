import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  FormControl,
  FormGroup,
  ReactiveFormsModule
} from '@angular/forms';

export interface EquipeFormControls {
  temProfessorResponsavel: FormControl<boolean>;
  temEquipeGestora: FormControl<boolean>;
  temMerendeira: FormControl<boolean>;
  temZeladora: FormControl<boolean>;
  temBarqueiro: FormControl<boolean>;
  necessitaTransporte: FormControl<boolean>;
}

@Component({
  selector: 'app-equipe-etapa',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './equipe-etapa.component.html'
})
export class EquipeEtapaComponent {

  @Input({ required: true })
  form!: FormGroup<EquipeFormControls>;

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

}