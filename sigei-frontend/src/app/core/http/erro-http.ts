import { HttpContext, HttpContextToken, HttpErrorResponse } from '@angular/common/http';

/**
 * Erro de uma requisição já interpretado. O backend responde em formatos
 * diferentes conforme a exceção (ver ManipuladorGlobalException):
 * - 400 de validação (@Valid): objeto { campo: [mensagens] };
 * - 400, 401, 403, 404, 409 e 503: texto puro com a mensagem;
 * - demais falhas: corpo padrão do Spring ou nenhum corpo.
 */
export interface ErroHttp {
  status: number;
  /** Mensagem pronta para exibir ao usuário. */
  mensagem: string;
  /** Mensagens por campo, quando o backend recusou a validação do formulário. */
  campos: Record<string, string[]>;
  /** Já informado ao usuário pelo interceptador de erros (falha de conexão, servidor, permissão). */
  global: boolean;
}

/**
 * Desliga o aviso automático do interceptador para uma requisição, quando a
 * tela trata o erro por conta própria: `{ context: semAvisoGlobal() }`.
 */
export const AVISO_GLOBAL_DE_ERRO = new HttpContextToken<boolean>(() => true);

export function semAvisoGlobal(): HttpContext {
  return new HttpContext().set(AVISO_GLOBAL_DE_ERRO, false);
}

const MENSAGENS_POR_STATUS: Record<number, string> = {
  0: 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.',
  400: 'Algum dado informado é inválido. Confira os campos e tente novamente.',
  401: 'Sua sessão expirou. Entre novamente.',
  403: 'Você não tem permissão para realizar esta ação.',
  404: 'O registro solicitado não foi encontrado.',
  409: 'Esta operação conflita com um registro já existente.',
  413: 'O arquivo enviado é maior que o permitido.',
  503: 'O serviço está temporariamente indisponível. Tente novamente em instantes.'
};

const MENSAGEM_SERVIDOR = 'Ocorreu um erro no servidor. Tente novamente em instantes.';

/** Status que o interceptador já comunica ao usuário com um aviso. */
export function ehErroGlobal(status: number): boolean {
  return status === 0 || status === 403 || status >= 500;
}

/**
 * Mensagens que expõem detalhes internos (SQL, classes Java, Optional vazio)
 * e não devem chegar à tela.
 */
const PADRAO_MENSAGEM_TECNICA =
  /No value present|could not execute|constraint|SQL|java\.|org\.|jakarta\.|Exception|\bat [\w$.]+\(/i;

function mensagemExibivel(texto: unknown): string | null {
  if (typeof texto !== 'string') return null;
  const limpa = texto.trim();
  if (!limpa || limpa.length > 300 || limpa.startsWith('<') || PADRAO_MENSAGEM_TECNICA.test(limpa)) return null;
  return limpa;
}

function extrairCampos(corpo: unknown): Record<string, string[]> {
  if (!corpo || typeof corpo !== 'object' || Array.isArray(corpo)) return {};

  const campos: Record<string, string[]> = {};
  for (const [campo, valor] of Object.entries(corpo)) {
    if (!Array.isArray(valor)) return {};
    const mensagens = valor.filter((item): item is string => typeof item === 'string');
    if (mensagens.length) campos[campo] = mensagens;
  }
  return campos;
}

function mensagemDoCorpo(corpo: unknown): string | null {
  const texto = mensagemExibivel(corpo);
  if (texto) return texto;

  // Corpo de erro padrão do Spring: { timestamp, status, error, message, path }
  if (corpo && typeof corpo === 'object' && 'message' in corpo) {
    return mensagemExibivel((corpo as { message: unknown }).message);
  }
  return null;
}

/**
 * Mensagem para quando a tela não consegue carregar seus dados. Nas falhas
 * globais o interceptador já explicou a causa; aqui basta dizer o que faltou.
 */
export function mensagemErroCarregamento(erro: unknown, padrao: string, naoEncontrado?: string): string {
  const falha = interpretarErro(erro, padrao);
  if (falha.status === 404 && naoEncontrado) return naoEncontrado;
  return falha.global ? padrao : falha.mensagem;
}

/**
 * Mensagem para uma exclusão recusada, ou null se o interceptador já avisou.
 * O backend responde 409 quando o registro ainda tem vínculos (chave estrangeira),
 * com uma mensagem do banco que não serve para a tela: nesse caso vale `emUso`.
 */
export function mensagemErroExclusao(erro: unknown, padrao: string, emUso: string): string | null {
  const status = erro instanceof HttpErrorResponse ? erro.status : -1;
  const falha = interpretarErro(erro, status === 409 ? emUso : padrao);
  return falha.global ? null : falha.mensagem;
}

/**
 * Converte qualquer erro de requisição em um {@link ErroHttp}.
 * @param padrao mensagem usada quando o backend não envia uma mensagem aproveitável.
 */
export function interpretarErro(erro: unknown, padrao?: string): ErroHttp {
  if (!(erro instanceof HttpErrorResponse)) {
    return { status: -1, mensagem: padrao ?? MENSAGEM_SERVIDOR, campos: {}, global: false };
  }

  const status = erro.status;
  const campos = status === 400 ? extrairCampos(erro.error) : {};
  const primeiraDoCampo = Object.values(campos)[0]?.[0];
  const doServidor = status === 0 || status >= 500 ? null : mensagemDoCorpo(erro.error);

  const mensagem =
    (Object.keys(campos).length > 1 ? 'Alguns campos precisam de correção.' : primeiraDoCampo) ??
    doServidor ??
    (ehErroGlobal(status) ? MENSAGENS_POR_STATUS[status] ?? MENSAGEM_SERVIDOR : null) ??
    padrao ??
    MENSAGENS_POR_STATUS[status] ??
    MENSAGEM_SERVIDOR;

  return { status, mensagem, campos, global: ehErroGlobal(status) };
}
