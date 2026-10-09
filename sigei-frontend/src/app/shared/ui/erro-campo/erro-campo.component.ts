import { Component, input } from '@angular/core';
import { AbstractControl, ValidationErrors } from '@angular/forms';

type GeradorMensagem = string | ((detalhe: any) => string);

const MENSAGENS_PADRAO: Record<string, GeradorMensagem> = {
  required: 'Campo obrigatório.',
  email: 'Informe um e-mail válido.',
  minlength: ({ requiredLength }) => `Informe pelo menos ${requiredLength} caracteres.`,
  maxlength: ({ requiredLength }) => `Use no máximo ${requiredLength} caracteres.`,
  min: ({ min }) => `O valor mínimo é ${min}.`,
  max: ({ max }) => `O valor máximo é ${max}.`,
  pattern: 'Formato inválido.'
};

/**
 * Mensagem de erro exibida abaixo de um campo de formulário, depois que o
 * usuário interage com ele. O erro devolvido pelo backend (`servidor`) tem
 * prioridade sobre os validadores locais.
 *
 * `<app-erro-campo [controle]="form.controls.nome" [mensagens]="{ required: 'Informe o nome.' }" />`
 */
@Component({
  selector: 'app-erro-campo',
  standalone: true,
  template: `
    @if (mensagem; as texto) {
      <p class="text-xs text-rose-500 mt-1.5 flex items-start gap-1.5" role="alert">
        <i class="fa-solid fa-circle-exclamation mt-0.5"></i>
        <span>{{ texto }}</span>
      </p>
    }
  `
})
export class ErroCampoComponent {
  readonly controle = input.required<AbstractControl>();
  /** Mensagens específicas do campo, por chave de validação; substituem as padrões. */
  readonly mensagens = input<Record<string, GeradorMensagem>>({});

  get mensagem(): string | null {
    const controle = this.controle();
    if (!controle.invalid || !controle.touched || !controle.errors) return null;
    return this.traduzir(controle.errors);
  }

  private traduzir(erros: ValidationErrors): string {
    if (typeof erros['servidor'] === 'string') return erros['servidor'];

    const chave = Object.keys(erros)[0];
    const gerador = this.mensagens()[chave] ?? MENSAGENS_PADRAO[chave] ?? 'Valor inválido.';
    return typeof gerador === 'function' ? gerador(erros[chave]) : gerador;
  }
}
