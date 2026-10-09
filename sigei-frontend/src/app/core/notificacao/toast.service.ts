import { Injectable, signal } from '@angular/core';

export type TipoToast = 'sucesso' | 'erro' | 'aviso' | 'info';

export interface Toast {
  id: number;
  tipo: TipoToast;
  titulo?: string;
  mensagem: string;
}

const DURACAO_MS: Record<TipoToast, number> = {
  sucesso: 4000,
  info: 5000,
  aviso: 6000,
  erro: 7000
};

/** Quantidade máxima de avisos simultâneos na tela; os mais antigos saem primeiro. */
const LIMITE = 4;

@Injectable({ providedIn: 'root' })
export class ToastService {
  private proximoId = 1;
  private readonly temporizadores = new Map<number, ReturnType<typeof setTimeout>>();
  private readonly lista = signal<Toast[]>([]);

  readonly toasts = this.lista.asReadonly();

  sucesso(mensagem: string, titulo?: string): void {
    this.exibir('sucesso', mensagem, titulo);
  }

  erro(mensagem: string, titulo?: string): void {
    this.exibir('erro', mensagem, titulo);
  }

  aviso(mensagem: string, titulo?: string): void {
    this.exibir('aviso', mensagem, titulo);
  }

  info(mensagem: string, titulo?: string): void {
    this.exibir('info', mensagem, titulo);
  }

  fechar(id: number): void {
    clearTimeout(this.temporizadores.get(id));
    this.temporizadores.delete(id);
    this.lista.update(toasts => toasts.filter(toast => toast.id !== id));
  }

  private exibir(tipo: TipoToast, mensagem: string, titulo?: string): void {
    // Várias requisições falhando juntas (ex.: servidor fora do ar) geram um único aviso
    const repetido = this.lista().find(toast => toast.tipo === tipo && toast.mensagem === mensagem);
    if (repetido) {
      this.agendarFechamento(repetido.id, tipo);
      return;
    }

    const toast: Toast = { id: this.proximoId++, tipo, titulo, mensagem };
    const atuais = this.lista();
    atuais.slice(0, Math.max(0, atuais.length - LIMITE + 1)).forEach(antigo => this.fechar(antigo.id));

    this.lista.update(toasts => [...toasts, toast]);
    this.agendarFechamento(toast.id, tipo);
  }

  private agendarFechamento(id: number, tipo: TipoToast): void {
    clearTimeout(this.temporizadores.get(id));
    this.temporizadores.set(id, setTimeout(() => this.fechar(id), DURACAO_MS[tipo]));
  }
}
