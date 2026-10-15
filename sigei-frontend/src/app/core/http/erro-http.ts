import { HttpContext, HttpContextToken, HttpErrorResponse } from '@angular/common/http';

/**
 * Corpo de erro da API: ProblemDetail (RFC 9457), em todas as respostas de erro
 * (ver ManipuladorGlobalException e ErroController no backend).
 */
interface ProblemDetail {
  status?: number;
  /** Tipo do erro ("Conflito", "Requisição inválida"). */
  title?: string;
  /** Mensagem para o usuário. */
  detail?: string;
  /** Rota da requisição. */
  instance?: string;
  /** Validação: mensagens por campo. */
  errors?: Record<string, string[]>;
}

/** Erro de uma requisição já interpretado. */
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
 * O corpo, se for um ProblemDetail. Respostas que não vêm da API (proxy fora do
 * ar, página HTML de erro) e downloads em blob não têm um.
 */
function lerProblema(corpo: unknown): ProblemDetail | null {
  return corpo && typeof corpo === 'object' && !(corpo instanceof Blob) ? (corpo as ProblemDetail) : null;
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
 * Quando o registro ainda tem vínculos, o backend responde 409 dizendo quais
 * ("Não é possível excluir o povo: ele tem 2 aldeias vinculadas.").
 */
export function mensagemErroExclusao(erro: unknown, padrao: string): string | null {
  const falha = interpretarErro(erro, padrao);
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
  const global = ehErroGlobal(status);
  const problema = lerProblema(erro.error);
  // Falhas de conexão e do servidor ficam com a mensagem genérica, mais útil ao usuário
  const detalhe = status === 0 || status >= 500 ? null : problema?.detail?.trim() || null;

  const mensagem =
    detalhe ??
    (global ? MENSAGENS_POR_STATUS[status] ?? MENSAGEM_SERVIDOR : null) ??
    padrao ??
    MENSAGENS_POR_STATUS[status] ??
    MENSAGEM_SERVIDOR;

  return { status, mensagem, campos: problema?.errors ?? {}, global };
}
