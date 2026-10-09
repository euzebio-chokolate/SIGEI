import { DestroyRef, Directive, inject, input, output } from '@angular/core';

/**
 * Emite depois que o usuário para de digitar, em vez de a cada tecla.
 * O ngModel continua atualizando na hora; só o filtro (que percorre a lista
 * inteira, já que o backend não pagina) espera a pausa.
 *
 * `<input [(ngModel)]="filtro" (buscaDebounce)="filtrar()" />`
 */
@Directive({
  selector: 'input[buscaDebounce]',
  standalone: true,
  host: {
    '(input)': 'agendar()',
    // Enter aplica na hora, sem esperar o intervalo
    '(keydown.enter)': 'emitirAgora()'
  }
})
export class BuscaDebounceDirective {
  readonly buscaDebounce = output<void>();
  readonly intervaloMs = input(250);

  private temporizador: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.cancelar());
  }

  protected agendar(): void {
    this.cancelar();
    this.temporizador = setTimeout(() => this.emitirAgora(), this.intervaloMs());
  }

  protected emitirAgora(): void {
    this.cancelar();
    this.buscaDebounce.emit();
  }

  private cancelar(): void {
    if (this.temporizador !== null) {
      clearTimeout(this.temporizador);
      this.temporizador = null;
    }
  }
}
