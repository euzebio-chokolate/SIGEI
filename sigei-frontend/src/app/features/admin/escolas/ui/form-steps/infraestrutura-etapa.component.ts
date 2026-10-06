import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  inject,
  Input,
  Output
} from '@angular/core';

import {
  FormControl,
  FormGroup,
  ReactiveFormsModule
} from '@angular/forms';
import { environment } from '@environments/environment';
import { SITUACOES_ESCOLA, TIPOS_ACESSO } from '@domains/escolas/models/infraestrutura-opcoes';

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

  private readonly cdr = inject(ChangeDetectorRef);

  @Input({ required: true })
  form!: FormGroup<InfraestruturaFormControls>;

  @Input()
  fotoUrlAtual: string | null = null;

  @Output()
  avancar = new EventEmitter<void>();

  @Output()
  voltar = new EventEmitter<void>();

  @Output()
  fotoAlterada = new EventEmitter<File | null>();

  readonly situacoes = SITUACOES_ESCOLA;
  readonly tiposAcesso = TIPOS_ACESSO;

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
      this.cdr.markForCheck();
    };

    reader.onerror = () => {
      this.fotoSelecionada = null;
      this.fotoPreview = null;
      this.erroFoto = 'Não foi possível carregar a prévia da imagem.';
      this.fotoAlterada.emit(null);
      input.value = '';
      this.cdr.markForCheck();
    };

    reader.readAsDataURL(arquivo);
  }

  get imagemExibida(): string | null {
    if (this.fotoPreview) {
      return this.fotoPreview;
    }
    return this.resolverFotoUrl(this.fotoUrlAtual);
  }

  get possuiImagem(): boolean {
    return !!this.imagemExibida;
  }

  removerFoto(): void {
    this.fotoSelecionada = null;
    this.fotoPreview = null;
    this.fotoUrlAtual = null;
    this.erroFoto = '';
    this.fotoAlterada.emit(null);
  }

  aoAlterarRegistroFotografico(): void {
    const possuiRegistro = this.form.controls.temRegistroFotografico.value;

    if (!possuiRegistro) {
      this.fotoSelecionada = null;
      this.fotoPreview = null;
      this.fotoUrlAtual = null;
      this.fotoAlterada.emit(null);
    }
  }

  private resolverFotoUrl(fotoUrl: string | null): string | null {
    if (!fotoUrl) {
      return null;
    }

    if (/^(data:|blob:|https?:\/\/)/i.test(fotoUrl)) {
      return fotoUrl;
    }

    const apiUrl = environment.apiUrl.replace(/\/$/, '');
    const caminho = fotoUrl.replace(/^\//, '');
    return `${apiUrl}/${caminho}`;
  }

}
