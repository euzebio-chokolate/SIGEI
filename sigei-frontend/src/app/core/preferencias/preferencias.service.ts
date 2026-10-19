import { DOCUMENT } from '@angular/common';
import { Injectable, effect, inject, signal } from '@angular/core';

export type PaginaInicial = 'dashboard' | 'escolas' | 'historico-escolas';
export type CamadaMapa = 'padrao' | 'satelite' | 'terreno' | 'escuro';

/** Preferências deste navegador, definidas na tela de configurações. */
export interface Preferencias {
  /** Tela aberta depois do login e ao acessar /admin. */
  paginaInicial: PaginaInicial;
  camadaMapa: CamadaMapa;
  abrirPopupAoPassarMouse: boolean;
  exibirFotosNoMapa: boolean;
  /** No computador, o menu lateral mostra só os ícones. */
  menuCompacto: boolean;
  /** Desliga transições e animações (também as do mapa). */
  reduzirAnimacoes: boolean;
  /** Avisa quando o ano letivo atual ainda não foi aberto no sistema. */
  avisarAtualizacaoAnual: boolean;
}

export const PAGINAS_INICIAIS: { valor: PaginaInicial; rotulo: string }[] = [
  { valor: 'dashboard', rotulo: 'Painel gerencial' },
  { valor: 'escolas', rotulo: 'Escolas' },
  { valor: 'historico-escolas', rotulo: 'Histórico escolar' }
];

export const CAMADAS_MAPA: { valor: CamadaMapa; rotulo: string }[] = [
  { valor: 'padrao', rotulo: 'Mapa padrão' },
  { valor: 'satelite', rotulo: 'Satélite' },
  { valor: 'terreno', rotulo: 'Terreno' },
  { valor: 'escuro', rotulo: 'Mapa escuro' }
];

export const PREFERENCIAS_PADRAO: Preferencias = {
  paginaInicial: 'dashboard',
  camadaMapa: 'padrao',
  abrirPopupAoPassarMouse: true,
  exibirFotosNoMapa: true,
  menuCompacto: false,
  reduzirAnimacoes: false,
  avisarAtualizacaoAnual: true
};

const CHAVE = 'sigei-preferencias';
const CLASSE_SEM_ANIMACOES = 'reduzir-animacoes';

/**
 * Fonte única das preferências: a tela de configurações grava e as demais telas
 * leem daqui, sempre com valores válidos (o que estiver fora das opções volta ao
 * padrão, e chaves antigas, como as da identificação institucional, são
 * descartadas). As preferências ficam no localStorage, valem só neste navegador
 * e nunca guardam dados de sessão.
 */
@Injectable({ providedIn: 'root' })
export class PreferenciasService {
  private readonly documento = inject(DOCUMENT);
  private readonly estado = signal<Preferencias>(ler());

  readonly preferencias = this.estado.asReadonly();

  constructor() {
    effect(() => {
      this.documento.documentElement.classList.toggle(CLASSE_SEM_ANIMACOES, this.estado().reduzirAnimacoes);
    });
  }

  salvar(preferencias: Preferencias): void {
    const validas = validar(preferencias);
    this.estado.set(validas);
    try {
      localStorage.setItem(CHAVE, JSON.stringify(validas));
    } catch {
      // Sem armazenamento (navegação privada restrita): vale até fechar a aba
    }
  }

  restaurarPadrao(): void {
    this.estado.set({ ...PREFERENCIAS_PADRAO });
    try {
      localStorage.removeItem(CHAVE);
    } catch {
      // Idem
    }
  }

  /** Comandos de rota da página inicial, para Router.navigate e createUrlTree. */
  rotaInicial(): string[] {
    return ['/admin', this.estado().paginaInicial];
  }
}

function ler(): Preferencias {
  try {
    const salvo = localStorage.getItem(CHAVE);
    return salvo ? validar(JSON.parse(salvo)) : { ...PREFERENCIAS_PADRAO };
  } catch {
    return { ...PREFERENCIAS_PADRAO };
  }
}

function validar(valor: unknown): Preferencias {
  const dados = (valor && typeof valor === 'object' ? valor : {}) as Record<string, unknown>;
  const booleano = (chave: keyof Preferencias) =>
    typeof dados[chave] === 'boolean' ? dados[chave] as boolean : PREFERENCIAS_PADRAO[chave] as boolean;
  const opcao = <T extends string>(chave: keyof Preferencias, opcoes: { valor: T }[]): T =>
    opcoes.some(item => item.valor === dados[chave]) ? dados[chave] as T : PREFERENCIAS_PADRAO[chave] as T;

  return {
    paginaInicial: opcao('paginaInicial', PAGINAS_INICIAIS),
    camadaMapa: opcao('camadaMapa', CAMADAS_MAPA),
    abrirPopupAoPassarMouse: booleano('abrirPopupAoPassarMouse'),
    exibirFotosNoMapa: booleano('exibirFotosNoMapa'),
    menuCompacto: booleano('menuCompacto'),
    reduzirAnimacoes: booleano('reduzirAnimacoes'),
    avisarAtualizacaoAnual: booleano('avisarAtualizacaoAnual')
  };
}
