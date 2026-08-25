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

import { RouterLink } from '@angular/router';

import {
  AldeiaResponse
} from '../../../../core/services/aldeia.service';

export interface EscolaFormControls {
  codigo: FormControl<string>;
  nome: FormControl<string>;
  tipoUnidade: FormControl<string>;
  aldeiaId: FormControl<number>;
  escolaSedeId: FormControl<number | null>;
}

@Component({
  selector: 'app-escola-etapa',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './escola-etapa.component.html'
})
export class EscolaEtapaComponent {

  @Input({ required: true })
  form!: FormGroup<EscolaFormControls>;

  @Input()
  aldeias: AldeiaResponse[] = [];

  @Input()
  escolasSede: {
    id: number;
    codigo: string;
    nome: string;
  }[] = [];

  @Input()
  carregandoAldeias = false;

  @Input()
  erroMensagem = '';

  @Output()
  avancar = new EventEmitter<void>();

  @Output()
  alterarTipoUnidade = new EventEmitter<void>();

  emitirAvancar(): void {
    this.avancar.emit();
  }

  emitirAlteracaoTipo(): void {
    this.alterarTipoUnidade.emit();
  }

}