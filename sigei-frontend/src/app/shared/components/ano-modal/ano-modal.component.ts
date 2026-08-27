import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-ano-modal',
  standalone: true,
  templateUrl: './ano-modal.component.html'
})
export class AnoModalComponent {
  @Input() aberto = false;
  @Input() carregando = false;

  @Output() confirmar = new EventEmitter<void>();
  @Output() cancelar = new EventEmitter<void>();

  emitirConfirmar(): void {
    if (this.carregando) return;
    this.confirmar.emit();
  }

  emitirCancelar(): void {
    if (this.carregando) return;
    this.cancelar.emit();
  }
}