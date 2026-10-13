import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { interpretarErro } from '@core/http/erro-http';
import { AnoReferencia, AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { MunicipioService } from '@domains/territorios/data-access/municipio.service';
import { Municipio } from '@domains/territorios/models/municipio.models';
import { RelatorioService } from '@domains/relatorios/data-access/relatorio.service';
import {
  FORMATOS_RELATORIO,
  FormatoRelatorio,
  OPCOES_RELATORIO,
  TipoRelatorio
} from '@domains/relatorios/models/relatorio.models';

/**
 * Relatórios exportáveis em PDF e planilha, filtrados por ano e município.
 * Disponível a administradores e usuários de consulta: os arquivos não trazem
 * CPF, matrícula nem número do contrato.
 */
@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './relatorios.component.html'
})
export class RelatoriosComponent implements OnInit {
  private readonly relatorioService = inject(RelatorioService);
  private readonly anoReferenciaService = inject(AnoReferenciaService);
  private readonly municipioService = inject(MunicipioService);

  readonly opcoes = OPCOES_RELATORIO;
  readonly formatos = FORMATOS_RELATORIO;

  readonly anos = signal<AnoReferencia[]>([]);
  readonly municipios = signal<Municipio[]>([]);

  ano: number | null = null;
  municipioId: number | null = null;

  /** Relatório em geração, como "escolas-pdf". */
  readonly gerando = signal<string | null>(null);
  readonly erro = signal('');
  readonly ultimoArquivo = signal('');

  ngOnInit(): void {
    this.anoReferenciaService.listar().subscribe({
      next: anos => {
        const ordenados = [...(anos ?? [])].sort((a, b) => b.ano - a.ano);
        this.anos.set(ordenados);
        this.ano ??= ordenados[0]?.ano ?? null;
      },
      error: () => this.erro.set('Não foi possível carregar os anos de referência.')
    });

    this.municipioService.listar().subscribe({
      next: municipios => this.municipios.set([...municipios].sort((a, b) => a.nome.localeCompare(b.nome))),
      // Sem a lista, os relatórios continuam disponíveis para todos os municípios
      error: erro => console.error('Erro ao carregar municípios para o filtro:', erro)
    });
  }

  emGeracao(tipo: TipoRelatorio, formato: FormatoRelatorio): boolean {
    return this.gerando() === `${tipo}-${formato}`;
  }

  baixar(tipo: TipoRelatorio, formato: FormatoRelatorio): void {
    if (this.gerando()) return;

    this.gerando.set(`${tipo}-${formato}`);
    this.erro.set('');
    this.ultimoArquivo.set('');

    this.relatorioService.baixar(tipo, formato, { ano: this.ano, municipioId: this.municipioId }).subscribe({
      next: nome => {
        this.gerando.set(null);
        this.ultimoArquivo.set(nome);
      },
      error: erro => {
        this.gerando.set(null);
        this.erro.set(interpretarErro(erro, 'Não foi possível gerar o relatório.').mensagem);
      }
    });
  }
}
