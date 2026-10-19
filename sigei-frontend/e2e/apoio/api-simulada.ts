import { Page, Request, Route, test as base, expect } from '@playwright/test';

import { USUARIOS, UsuarioDeTeste } from './dados';

export interface Resposta {
  status?: number;
  json?: unknown;
}

type Manipulador = (requisicao: Request) => Resposta;

interface RotaSimulada {
  metodo: string;
  caminho: string | RegExp;
  manipulador: Manipulador;
}

/**
 * API simulada para os testes de ponta a ponta. Intercepta no navegador as
 * chamadas a /api (o mesmo caminho do proxy em produção), responde com os dados
 * definidos em cada teste e guarda as requisições para conferência. Chamadas a
 * outros domínios (camadas do mapa, CDNs) são bloqueadas, para o teste não
 * depender da internet.
 *
 * Chamadas sem simulação respondem 404 e ficam em {@link naoSimuladas}; a
 * fixture falha o teste se sobrar alguma, para que nada passe despercebido.
 */
export class ApiSimulada {
  readonly requisicoes: Request[] = [];
  readonly naoSimuladas: string[] = [];
  private readonly rotas: RotaSimulada[] = [];

  constructor(private readonly page: Page) {}

  async iniciar(): Promise<void> {
    await this.page.route('**/*', route => {
      const url = new URL(route.request().url());
      const local = url.hostname === 'localhost' || url.hostname === '127.0.0.1';
      if (local && url.pathname.startsWith('/api/')) return this.responder(route);
      if (local) return route.continue();
      return route.abort();
    });
  }

  /** Simula uma rota; a definida por último prevalece (para um teste sobrescrever o padrão). */
  em(metodo: string, caminho: string | RegExp, resposta: Resposta | Manipulador): this {
    const manipulador = typeof resposta === 'function' ? resposta : () => resposta;
    this.rotas.unshift({ metodo: metodo.toUpperCase(), caminho, manipulador });
    return this;
  }

  /** Requisições feitas a uma rota (caminho sem /api), na ordem. */
  chamadas(metodo: string, caminho: string | RegExp): Request[] {
    return this.requisicoes.filter(req =>
      req.method() === metodo.toUpperCase() && corresponde(caminhoDa(req), caminho));
  }

  private async responder(route: Route): Promise<void> {
    const requisicao = route.request();
    const caminho = caminhoDa(requisicao);
    this.requisicoes.push(requisicao);

    const rota = this.rotas.find(item => item.metodo === requisicao.method() && corresponde(caminho, item.caminho));
    if (!rota) {
      this.naoSimuladas.push(`${requisicao.method()} ${caminho}`);
      return route.fulfill({ status: 404, json: problema(404, 'Rota não simulada no teste.') });
    }

    const { status = 200, json } = rota.manipulador(requisicao);
    if (status === 204 || json === undefined) {
      return route.fulfill({ status });
    }
    return route.fulfill({ status, json, headers: status >= 400 ? { 'content-type': 'application/problem+json' } : {} });
  }
}

/** Corpo de erro no formato da API (ProblemDetail). */
export function problema(status: number, detail: string): object {
  return { status, title: 'Erro', detail };
}

/** Caminho da requisição sem o prefixo /api, como nos controllers do backend. */
function caminhoDa(requisicao: Request): string {
  return new URL(requisicao.url()).pathname.replace(/^\/api/, '');
}

function corresponde(caminho: string, padrao: string | RegExp): boolean {
  return typeof padrao === 'string' ? caminho === padrao : padrao.test(caminho);
}

/**
 * Começa o teste com a sessão de um usuário já iniciada, como depois do login:
 * os dados de exibição ficam no localStorage (os tokens reais ficariam em
 * cookies HttpOnly, que o frontend não lê).
 */
export async function entrarComo(page: Page, usuario: UsuarioDeTeste): Promise<void> {
  await page.addInitScript(dados => {
    localStorage.setItem('sigei_usuario', JSON.stringify({ ...dados, authorities: [] }));
  }, usuario);
}

/** Fixture `api`: cada teste recebe uma API simulada já ligada à página. */
export const test = base.extend<{ api: ApiSimulada }>({
  api: async ({ page }, usar) => {
    const api = new ApiSimulada(page);
    await api.iniciar();
    api.em('POST', '/auth/logout', { status: 204 });
    await usar(api);
    expect(api.naoSimuladas, 'chamadas à API sem simulação no teste').toEqual([]);
  }
});

export { expect, USUARIOS };
