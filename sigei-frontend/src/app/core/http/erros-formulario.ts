import { AbstractControl, FormGroup } from '@angular/forms';

import { ErroHttp, interpretarErro } from './erro-http';

/**
 * Associa a um campo uma mensagem de regra de negócio, que o backend devolve no
 * `detail` sem indicar o campo (ex.: 409 "Já existe um usuário cadastrado com esse e-mail").
 */
export interface RegraErroCampo {
  campo: string;
  padrao: RegExp;
}

/**
 * Marca nos campos do formulário os erros recusados pelo backend, com a chave
 * `servidor`. O erro some sozinho quando o usuário altera o campo, pois os
 * validadores voltam a rodar.
 *
 * @returns true se ao menos um campo recebeu o erro; nesse caso a tela pode
 * dispensar o alerta geral.
 */
export function aplicarErrosServidor(form: FormGroup, erro: ErroHttp, regras: RegraErroCampo[] = []): boolean {
  let aplicou = false;

  for (const [campo, mensagens] of Object.entries(erro.campos)) {
    const controle = form.get(campo);
    if (controle) {
      marcar(controle, mensagens.join(' '));
      aplicou = true;
    }
  }

  if (!aplicou) {
    const regra = regras.find(({ padrao }) => padrao.test(erro.mensagem));
    const controle = regra && form.get(regra.campo);
    if (controle) {
      marcar(controle, erro.mensagem);
      aplicou = true;
    }
  }

  return aplicou;
}

/**
 * Trata a falha ao enviar um formulário: marca os campos recusados e devolve
 * a mensagem para o alerta geral da tela, ou '' quando não há o que exibir
 * (erros já marcados nos campos, ou falha já avisada pelo interceptador).
 */
export function mensagemErroFormulario(
  form: FormGroup,
  erro: unknown,
  padrao: string,
  regras: RegraErroCampo[] = []
): string {
  const falha = interpretarErro(erro, padrao);
  const marcouCampos = aplicarErrosServidor(form, falha, regras);
  return marcouCampos || falha.global ? '' : falha.mensagem;
}

function marcar(controle: AbstractControl, mensagem: string): void {
  controle.setErrors({ ...controle.errors, servidor: mensagem });
  controle.markAsTouched();
}
