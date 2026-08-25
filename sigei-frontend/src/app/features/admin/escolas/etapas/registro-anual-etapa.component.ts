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

import {
  AnoReferencia
} from '../../../../core/services/ano-referencia.service';

export interface RegistroAnualFormControls {
  anoReferenciaId: FormControl<number>;
  rede: FormControl<string>;
  localFuncionamento: FormControl<string>;
}

@Component({
  selector: 'app-registro-anual-etapa',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './registro-anual-etapa.component.html'
})
export class RegistroAnualEtapaComponent {

  @Input({ required: true })
  form!: FormGroup<RegistroAnualFormControls>;

  @Input()
  anosReferencia: AnoReferencia[] = [];

  @Input()
  carregandoAnos = false;

  @Input()
  erroMensagem = '';

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