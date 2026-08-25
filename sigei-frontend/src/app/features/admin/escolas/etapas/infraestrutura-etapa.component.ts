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

export interface InfraestruturaFormControls {
  anoConstrucao: FormControl<number | null>;
  numSalas: FormControl<number | null>;
  latitude: FormControl<number | null>;
  longitude: FormControl<number | null>;
  situacao: FormControl<string>;
  tipoAcesso: FormControl<string>;
  temPredio: FormControl<boolean>;
  tipoAgua: FormControl<string>;
  temInternet: FormControl<boolean>;
  tipoEnergia: FormControl<string>;
  temRegistroFotografico: FormControl<boolean>;
}

@Component({
  selector: 'app-infraestrutura-etapa',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './infraestrutura-etapa.component.html'
})
export class InfraestruturaEtapaComponent {

  @Input({ required: true })
  form!: FormGroup<InfraestruturaFormControls>;

  @Output()
  avancar = new EventEmitter<void>();

  @Output()
  voltar = new EventEmitter<void>();

  @Output()
  fotoAlterada = new EventEmitter<File | null>();

  fotoSelecionada: File | null = null;
  fotoPreview: string | null = null;
  erroFoto = '';

  emitirAvancar(): void {
    this.avancar.emit();
  }

  emitirVoltar(): void {
    this.voltar.emit();
  }

  selecionarFoto(event: Event): void {
    this.erroFoto = '';
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0];

    if (!arquivo) {
      this.removerFoto();
      return;
    }

    /*
     * Confere o tipo do arquivo
     */
    if (!arquivo.type.startsWith('image/')) {
      this.erroFoto = 'Selecione um arquivo de imagem.';
      input.value = '';
      return;
    }

    /*
     * Limite opcional de 10 MB
     */
    const tamanhoMaximo = 10 * 1024 * 1024;

    if (arquivo.size > tamanhoMaximo) {
      this.erroFoto = 'A imagem deve possuir no máximo 10 MB.';
      input.value = '';
      return;
    }

    this.fotoSelecionada = arquivo;
    this.fotoAlterada.emit(arquivo);

    const reader = new FileReader();

    reader.onload = () => {
      this.fotoPreview = reader.result as string;
    };

    reader.readAsDataURL(arquivo);
  }

  removerFoto(): void {
    this.fotoSelecionada = null;
    this.fotoPreview = null;
    this.erroFoto = '';
    this.fotoAlterada.emit(null);
  }

  aoAlterarRegistroFotografico(): void {
    const possuiRegistro = this.form.controls.temRegistroFotografico.value;

    if (!possuiRegistro) {
      this.removerFoto();
    }
  }

}