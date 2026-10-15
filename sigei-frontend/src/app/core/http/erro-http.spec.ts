import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup } from '@angular/forms';

import { interpretarErro, mensagemErroCarregamento, mensagemErroExclusao } from './erro-http';
import { aplicarErrosServidor, mensagemErroFormulario } from './erros-formulario';

const resposta = (status: number, error: unknown) => new HttpErrorResponse({ status, error });

/** Corpo de erro da API (ProblemDetail). */
const problema = (status: number, detail: string, errors?: Record<string, string[]>) =>
  resposta(status, { status, title: 'Título', detail, instance: '/rota', ...(errors ? { errors } : {}) });

describe('interpretarErro', () => {
  it('lê as mensagens por campo da validação', () => {
    const erro = interpretarErro(problema(400, 'Nome não pode ser vazio', { nome: ['Nome não pode ser vazio'] }));
    expect(erro.campos).toEqual({ nome: ['Nome não pode ser vazio'] });
    expect(erro.mensagem).toBe('Nome não pode ser vazio');
    expect(erro.global).toBe(false);
  });

  it('usa o detail enviado pelo backend', () => {
    const erro = interpretarErro(problema(409, 'Já existe um usuário cadastrado com esse e-mail'));
    expect(erro.mensagem).toBe('Já existe um usuário cadastrado com esse e-mail');
    expect(erro.campos).toEqual({});
  });

  it('usa o padrão da tela quando a resposta não é da API', () => {
    expect(interpretarErro(resposta(404, '<html>Not Found</html>'), 'Escola não encontrada.').mensagem)
      .toBe('Escola não encontrada.');
    expect(interpretarErro(resposta(409, null)).mensagem)
      .toBe('Esta operação conflita com um registro já existente.');
  });

  it('marca como global as falhas de conexão, servidor e permissão', () => {
    expect(interpretarErro(resposta(0, null)).global).toBe(true);
    expect(interpretarErro(problema(500, 'Ocorreu um erro inesperado no servidor.')).mensagem)
      .toBe('Ocorreu um erro no servidor. Tente novamente em instantes.');
    expect(interpretarErro(problema(403, 'Você não tem permissão para realizar esta ação.')).global).toBe(true);
  });
});

describe('mensagemErroCarregamento', () => {
  it('usa a mensagem de "não encontrado" no 404', () => {
    expect(mensagemErroCarregamento(problema(404, 'Escola não encontrada'), 'Falhou.', 'Escola não encontrada.'))
      .toBe('Escola não encontrada.');
  });

  it('diz o que faltou quando o interceptador já explicou a causa', () => {
    expect(mensagemErroCarregamento(resposta(500, null), 'Não foi possível carregar as escolas.'))
      .toBe('Não foi possível carregar as escolas.');
  });
});

describe('mensagemErroExclusao', () => {
  it('mostra os vínculos informados pelo backend no 409', () => {
    const erro = problema(409, 'Não é possível excluir o povo: ele tem 2 aldeias vinculadas.');
    expect(mensagemErroExclusao(erro, 'Falhou.')).toBe('Não é possível excluir o povo: ele tem 2 aldeias vinculadas.');
  });

  it('não repete o aviso das falhas globais', () => {
    expect(mensagemErroExclusao(resposta(0, null), 'Falhou.')).toBeNull();
  });
});

describe('mensagemErroFormulario', () => {
  it('não repete no alerta geral o que já foi marcado no campo', () => {
    const form = new FormGroup({ cpf: new FormControl('') });
    const mensagem = mensagemErroFormulario(form, problema(400, 'CPF já cadastrado para outro professor.'), 'Falhou.', [
      { campo: 'cpf', padrao: /CPF/ }
    ]);
    expect(mensagem).toBe('');
    expect(form.controls.cpf.errors).toEqual({ servidor: 'CPF já cadastrado para outro professor.' });
  });

  it('devolve a mensagem quando nenhum campo corresponde', () => {
    const form = new FormGroup({ nome: new FormControl('') });
    expect(mensagemErroFormulario(form, resposta(404, null), 'Não foi possível salvar.'))
      .toBe('Não foi possível salvar.');
  });
});

describe('aplicarErrosServidor', () => {
  const criarForm = () => new FormGroup({ email: new FormControl(''), nome: new FormControl('') });

  it('marca o campo indicado pela validação', () => {
    const form = criarForm();
    const aplicou = aplicarErrosServidor(form, interpretarErro(problema(400, 'E-mail inválido', { email: ['E-mail inválido'] })));
    expect(aplicou).toBe(true);
    expect(form.controls.email.errors).toEqual({ servidor: 'E-mail inválido' });
    expect(form.controls.email.touched).toBe(true);
  });

  it('associa o detail ao campo pela regra', () => {
    const form = criarForm();
    const erro = interpretarErro(problema(409, 'Já existe um usuário cadastrado com esse e-mail'));
    expect(aplicarErrosServidor(form, erro, [{ campo: 'email', padrao: /e-?mail/i }])).toBe(true);
    expect(form.controls.email.hasError('servidor')).toBe(true);
  });

  it('limpa o erro do servidor quando o valor muda', () => {
    const form = criarForm();
    aplicarErrosServidor(form, interpretarErro(problema(400, 'x', { nome: ['x'] })));
    form.controls.nome.setValue('Novo nome');
    expect(form.controls.nome.errors).toBeNull();
  });
});
