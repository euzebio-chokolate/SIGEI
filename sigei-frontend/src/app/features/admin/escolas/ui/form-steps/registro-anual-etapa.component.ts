import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';

import {
  AnoReferencia
} from '@domains/escolas/data-access/ano-referencia.service';

import { ErroCampoComponent } from '@shared/ui/erro-campo/erro-campo.component';

export interface RegistroAnualFormControls {
  anoReferenciaId: FormControl<number>;
  rede: FormControl<string>;
  localFuncionamento: FormControl<string>;
  totalAlunos: FormControl<number | null>;
}

export type StatusAnoRegistro = 'carregando' | 'existente' | 'novo' | null;

@Component({
  selector: 'app-registro-anual-etapa',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    ErroCampoComponent
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

  @Input()
  editando = false;

  @Input()
  statusAno: StatusAnoRegistro = null;

  @Input()
  salvandoAno = false;

  @Input()
  erroNovoAno = '';

  @Input()
  totalMatriculas = 0;

  @Output()
  avancar = new EventEmitter<void>();

  @Output()
  voltar = new EventEmitter<void>();

  @Output()
  adicionarAno = new EventEmitter<number>();

  readonly anoAtual = new Date().getFullYear();
  novoAno: number | null = null;

  emitirAdicionarAno(): void {
    if (!this.novoAno || this.salvandoAno) return;
    this.adicionarAno.emit(this.novoAno);
  }

  emitirAvancar(): void {
    this.avancar.emit();
  }

  emitirVoltar(): void {
    this.voltar.emit();
  }

}