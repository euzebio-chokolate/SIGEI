import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

export interface EscolaMapaItem {
  id: number;
  latitude?: number | string | null;
  longitude?: number | string | null;
  situacao?: string | null;
  tipoAcesso?: string | null;
  temInternet?: boolean;
  temPredio?: boolean;
  fotoUrl?: string | null;
  escola?: {
    id?: number;
    nome?: string | null;
    codigo?: string | number | null;
  } | null;
}

@Component({
  selector: 'app-mapa-busca',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './mapa-busca.component.html',
})
export class MapaBuscaComponent {
  @Input() dadosEscolas: EscolaMapaItem[] = [];
  @Input() carregando = false;
  @Output() buscaAlterada = new EventEmitter<string>();
  @Output() escolaSelecionada = new EventEmitter<EscolaMapaItem>();

  termoBusca = '';
  sugestoes: EscolaMapaItem[] = [];
  mostrarSugestoes = false;

  aoDigitar(): void {
    const termo = this.normalizar(this.termoBusca);

    this.sugestoes = termo
      ? this.dadosEscolas
          .filter((item) => {
            const nome = this.normalizar(item.escola?.nome);
            const codigo = this.normalizar(item.escola?.codigo);
            return nome.includes(termo) || codigo.includes(termo);
          })
          .slice(0, 10)
      : [];

    this.mostrarSugestoes = termo.length > 0;
    this.buscaAlterada.emit(this.termoBusca.trim());
  }

  selecionar(item: EscolaMapaItem): void {
    this.termoBusca = String(item.escola?.nome || item.escola?.codigo || '');
    this.fecharSugestoes();
    this.buscaAlterada.emit(this.termoBusca);
    this.escolaSelecionada.emit(item);
  }

  limpar(): void {
    this.termoBusca = '';
    this.fecharSugestoes();
    this.buscaAlterada.emit('');
  }

  fecharComAtraso(): void {
    window.setTimeout(() => this.fecharSugestoes(), 150);
  }

  isParalisada(situacao?: string | null): boolean {
    const valor = this.normalizar(situacao).toUpperCase();
    return valor === 'PARALISADA' || valor === 'PARALIZADA';
  }

  private fecharSugestoes(): void {
    this.mostrarSugestoes = false;
    this.sugestoes = [];
  }

  private normalizar(valor: unknown): string {
    return String(valor ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLowerCase();
  }
}
