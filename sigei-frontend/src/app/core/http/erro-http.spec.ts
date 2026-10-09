import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup } from '@angular/forms';

import { interpretarErro, mensagemErroCarregamento, mensagemErroExclusao } from './erro-http';
import { aplicarErrosServidor, mensagemErroFormulario } from './erros-formulario';

const resposta = (status: number, error: unknown) => new HttpErrorResponse({ status, error });

describe('interpretarErro', () => {
  it('lê o mapa de validação do @Valid', () => {
    const erro = interpretarErro(resposta(400, { nome: ['Nome não pode ser vazio'] }));
    expect(erro.campos).toEqual({ nome: ['Nome não pode ser vazio'] });
    expect(erro.mensagem).toBe('Nome não pode ser vazio');
    expect(erro.global).toBe(false);
  });

  it('resume quando há vários campos inválidos', () => {
    const erro = interpretarErro(resposta(400, { nome: ['a'], email: ['b'] }));
    expect(erro.mensagem).toBe('Alguns campos precisam de correção.');
  });

  it('usa o texto de regra de negócio enviado pelo backend', () => {
    const erro = interpretarErro(resposta(409, 'Já existe um usuário cadastrado com esse e-mail'));
    expect(erro.mensagem).toBe('Já existe um usuário cadastrado com esse e-mail');
    expect(erro.campos).toEqual({});
  });

  it('esconde mensagens técnicas do backend', () => {
    expect(interpretarErro(resposta(404, 'No value present'), 'Escola não encontrada.').mensagem)
      .toBe('Escola não encontrada.');
    expect(interpretarErro(resposta(409, 'could not execute statement; SQL [n/a]; constraint [uk_email]')).mensagem)
      .toBe('Esta operação conflita com um registro já existente.');
  });

  it('marca como global as falhas de conexão, servidor e permissão', () => {
    expect(interpretarErro(resposta(0, null)).global).toBe(true);
    expect(interpretarErro(resposta(500, { status: 500, error: 'Internal Server Error' })).mensagem)
      .toBe('Ocorreu um erro no servidor. Tente novamente em instantes.');
    expect(interpretarErro(resposta(403, null)).global).toBe(true);
  });
});

describe('mensagemErroCarregamento', () => {
  it('usa a mensagem de "não encontrado" no 404', () => {
    expect(mensagemErroCarregamento(resposta(404, 'No value present'), 'Falhou.', 'Escola não encontrada.'))
      .toBe('Escola não encontrada.');
  });

  it('diz o que faltou quando o interceptador já explicou a causa', () => {
    expect(mensagemErroCarregamento(resposta(500, null), 'Não foi possível carregar as escolas.'))
      .toBe('Não foi possível carregar as escolas.');
  });
});

describe('mensagemErroExclusao', () => {
  it('troca a mensagem do banco no 409 pelo aviso de vínculo', () => {
    const erro = resposta(409, 'could not execute statement; constraint [fk_aldeia_povo]');
    expect(mensagemErroExclusao(erro, 'Falhou.', 'Existem aldeias vinculadas.')).toBe('Existem aldeias vinculadas.');
  });

  it('não repete o aviso das falhas globais', () => {
    expect(mensagemErroExclusao(resposta(0, null), 'Falhou.', 'Em uso.')).toBeNull();
  });
});

describe('mensagemErroFormulario', () => {
  it('não repete no alerta geral o que já foi marcado no campo', () => {
    const form = new FormGroup({ cpf: new FormControl('') });
    const mensagem = mensagemErroFormulario(form, resposta(400, 'CPF já cadastrado para outro professor.'), 'Falhou.', [
      { campo: 'cpf', padrao: /CPF/ }
    ]);
    expect(mensagem).toBe('');
    expect(form.controls.cpf.errors).toEqual({ servidor: 'CPF já cadastrado para outro professor.' });
  });

  it('devolve a mensagem quando nenhum campo corresponde', () => {
    const form = new FormGroup({ nome: new FormControl('') });
    expect(mensagemErroFormulario(form, resposta(404, 'No value present'), 'Não foi possível salvar.'))
      .toBe('Não foi possível salvar.');
  });
});

describe('aplicarErrosServidor', () => {
  const criarForm = () => new FormGroup({ email: new FormControl(''), nome: new FormControl('') });

  it('marca o campo indicado pelo mapa de validação', () => {
    const form = criarForm();
    const aplicou = aplicarErrosServidor(form, interpretarErro(resposta(400, { email: ['E-mail inválido'] })));
    expect(aplicou).toBe(true);
    expect(form.controls.email.errors).toEqual({ servidor: 'E-mail inválido' });
    expect(form.controls.email.touched).toBe(true);
  });

  it('associa mensagem em texto ao campo pela regra', () => {
    const form = criarForm();
    const erro = interpretarErro(resposta(409, 'Já existe um usuário cadastrado com esse e-mail'));
    expect(aplicarErrosServidor(form, erro, [{ campo: 'email', padrao: /e-?mail/i }])).toBe(true);
    expect(form.controls.email.hasError('servidor')).toBe(true);
  });

  it('limpa o erro do servidor quando o valor muda', () => {
    const form = criarForm();
    aplicarErrosServidor(form, interpretarErro(resposta(400, { nome: ['x'] })));
    form.controls.nome.setValue('Novo nome');
    expect(form.controls.nome.errors).toBeNull();
  });
});
