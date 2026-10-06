import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SITUACOES_ESCOLA, TIPOS_ACESSO } from '@domains/escolas/models/infraestrutura-opcoes';

export interface MapaFiltros {
  situacao: string;
  tipoAcesso: string;
  apenasComInternet: boolean;
  apenasComPredio: boolean;
}

@Component({
  selector: 'app-mapa-filtros',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './mapa-filtros.component.html',
})
export class MapaFiltrosComponent {
  @Input() totalResultados = 0;
  @Output() filtrosAlterados = new EventEmitter<MapaFiltros>();

  readonly situacoes = SITUACOES_ESCOLA;
  readonly tiposAcesso = TIPOS_ACESSO;

  isAberto = true;

  filtros: MapaFiltros = this.criarFiltrosVazios();

  get quantidadeFiltrosAtivos(): number {
    return [
      this.filtros.situacao,
      this.filtros.tipoAcesso,
      this.filtros.apenasComInternet,
      this.filtros.apenasComPredio,
    ].filter(Boolean).length;
  }

  toggleGaveta(): void {
    this.isAberto = !this.isAberto;
  }

  emitirFiltros(): void {
    this.filtrosAlterados.emit({ ...this.filtros });
  }

  limparFiltros(): void {
    this.filtros = this.criarFiltrosVazios();
    this.emitirFiltros();
  }

  private criarFiltrosVazios(): MapaFiltros {
    return {
      situacao: '',
      tipoAcesso: '',
      apenasComInternet: false,
      apenasComPredio: false,
    };
  }
}
