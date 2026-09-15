import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

@Component({
  selector: 'app-sucesso-modal',
  standalone: true,
  templateUrl: './sucesso-modal.component.html'
})
export class SucessoModalComponent {

  @Input()
  aberto = false;

  @Input()
  titulo = 'Ação concluída';

  @Input()
  mensagem = 'Operação realizada com sucesso.';

  @Input()
  textoBotao = 'Concluir';

  @Output()
  fechar = new EventEmitter<void>();

  emitirFechar(): void {
    this.fechar.emit();
  }
}