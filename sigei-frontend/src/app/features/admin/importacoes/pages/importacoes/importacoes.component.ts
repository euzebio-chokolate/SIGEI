import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { interpretarErro, mensagemErroCarregamento } from '@core/http/erro-http';
import { validarArquivo } from '@core/forms/validadores';
import { salvarArquivo } from '@core/http/download';
import { AnoReferencia, AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { ImportacaoService } from '@domains/importacoes/data-access/importacao.service';
import {
  IndicadorResultado,
  LayoutImportacao,
  OPCOES_IMPORTACAO,
  OpcaoImportacao,
  ResultadoImportacao,
  TipoImportacao,
  indicadoresResultado
} from '@domains/importacoes/models/importacao.models';
import { EstadoErroComponent } from '@shared/ui/estado-erro/estado-erro.component';

/** Faixa aceita pelo backend ao criar um ano pela importação de escolas. */
const ANO_MINIMO = 2000;

interface ResultadoExibido {
  tipo: TipoImportacao;
  ano: number;
  arquivo: string;
  indicadores: IndicadorResultado[];
  linhasIgnoradas: number;
  erros: string[];
}

/**
 * Importação das planilhas de escolas e infraestrutura, matrículas e quadro de
 * professores, para qualquer ano. As colunas são reconhecidas pelo cabeçalho; a
 * tela mostra as colunas aceitas e oferece um modelo para preencher.
 */
@Component({
  selector: 'app-importacoes',
  standalone: true,
  imports: [FormsModule, RouterLink, EstadoErroComponent],
  templateUrl: './importacoes.component.html'
})
export class ImportacoesComponent implements OnInit {
  private readonly importacaoService = inject(ImportacaoService);
  private readonly anoReferenciaService = inject(AnoReferenciaService);

  readonly opcoes = OPCOES_IMPORTACAO;
  readonly anoMinimo = ANO_MINIMO;
  readonly anoMaximo = new Date().getFullYear() + 1;

  readonly tipo = signal<TipoImportacao>('infraestrutura');
  readonly layouts = signal<LayoutImportacao[]>([]);
  readonly anos = signal<AnoReferencia[]>([]);
  readonly carregando = signal(true);
  readonly erroCarregamento = signal('');

  ano: number | null = null;
  arquivo: File | null = null;

  readonly importando = signal(false);
  readonly baixandoModelo = signal(false);
  readonly erroImportacao = signal('');
  readonly resultado = signal<ResultadoExibido | null>(null);

  readonly opcao = computed<OpcaoImportacao>(() => this.opcoes.find(opcao => opcao.tipo === this.tipo())!);
  readonly layout = computed(() => this.layouts().find(layout => layout.tipo === this.tipo()) ?? null);
  readonly obrigatorias = computed(() => this.layout()?.colunas.filter(coluna => coluna.obrigatoria) ?? []);
  readonly opcionais = computed(() => this.layout()?.colunas.filter(coluna => !coluna.obrigatoria) ?? []);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erroCarregamento.set('');

    this.importacaoService.listarLayouts().subscribe({
      next: layouts => {
        this.layouts.set(layouts);
        this.carregando.set(false);
      },
      error: erro => {
        this.carregando.set(false);
        this.erroCarregamento.set(mensagemErroCarregamento(erro, 'Não foi possível carregar as colunas aceitas nas importações.'));
      }
    });

    this.carregarAnos();
  }

  private carregarAnos(): void {
    this.anoReferenciaService.listar().subscribe({
      next: anos => {
        const ordenados = [...(anos ?? [])].sort((a, b) => b.ano - a.ano);
        this.anos.set(ordenados);
        if (this.ano === null) {
          this.ano = ordenados[0]?.ano ?? new Date().getFullYear();
        }
      },
      // Sem a lista, o ano ainda pode ser digitado na importação de escolas
      error: erro => console.error('Erro ao carregar os anos de referência:', erro)
    });
  }

  selecionarTipo(tipo: TipoImportacao): void {
    if (this.importando()) return;
    this.tipo.set(tipo);
    this.resultado.set(null);
    this.erroImportacao.set('');

    // Matrículas e professores só aceitam anos existentes
    if (!this.opcao().criaAno && !this.anos().some(item => item.ano === this.ano)) {
      this.ano = this.anos()[0]?.ano ?? null;
    }
  }

  anoExiste(ano: number | null): boolean {
    return this.anos().some(item => item.ano === ano);
  }

  selecionarArquivo(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const arquivo = campo.files?.[0] ?? null;
    campo.value = '';
    if (!arquivo) return;

    const problema = validarArquivo(arquivo, { extensoes: ['.xlsx'] });
    this.erroImportacao.set(problema ?? '');
    this.arquivo = problema ? null : arquivo;
    this.resultado.set(null);
  }

  /** Motivo pelo qual a importação ainda não pode ser enviada; vazio quando pode. */
  pendencia(): string {
    if (this.ano === null || !Number.isInteger(this.ano)) return 'Informe o ano de referência.';
    if (this.ano < ANO_MINIMO || this.ano > this.anoMaximo) {
      return `Informe um ano entre ${ANO_MINIMO} e ${this.anoMaximo}.`;
    }
    if (!this.opcao().criaAno && !this.anoExiste(this.ano)) {
      return `O ano ${this.ano} ainda não foi iniciado.`;
    }
    if (!this.arquivo) return 'Selecione a planilha (.xlsx).';
    return '';
  }

  importar(): void {
    if (this.importando() || this.pendencia()) return;

    const tipo = this.tipo();
    const ano = this.ano!;
    const arquivo = this.arquivo!;

    this.importando.set(true);
    this.erroImportacao.set('');
    this.resultado.set(null);

    this.importacaoService.importar(tipo, arquivo, ano).subscribe({
      next: resultado => {
        this.importando.set(false);
        this.arquivo = null;
        this.resultado.set(this.exibir(tipo, ano, arquivo.name, resultado));

        if (tipo === 'infraestrutura' && !this.anoExiste(ano)) {
          // A importação de escolas pode ter criado o ano
          this.anoReferenciaService.invalidar();
          this.carregarAnos();
        }
      },
      error: erro => {
        this.importando.set(false);
        // Falhas da planilha inteira voltam como 400 com o motivo em texto
        this.erroImportacao.set(interpretarErro(erro, 'Não foi possível importar a planilha.').mensagem);
      }
    });
  }

  baixarModelo(): void {
    if (this.baixandoModelo()) return;
    const tipo = this.tipo();
    this.baixandoModelo.set(true);

    this.importacaoService.baixarModelo(tipo).subscribe({
      next: planilha => {
        this.baixandoModelo.set(false);
        salvarArquivo(planilha, `modelo-importacao-${tipo}.xlsx`);
      },
      error: erro => {
        this.baixandoModelo.set(false);
        this.erroImportacao.set(interpretarErro(erro, 'Não foi possível baixar o modelo.').mensagem);
      }
    });
  }

  tituloDe(tipo: TipoImportacao): string {
    return this.layouts().find(layout => layout.tipo === tipo)?.titulo ?? tipo;
  }

  private exibir(tipo: TipoImportacao, ano: number, arquivo: string, resultado: ResultadoImportacao): ResultadoExibido {
    return {
      tipo,
      ano,
      arquivo,
      indicadores: indicadoresResultado(tipo, resultado),
      linhasIgnoradas: resultado.linhasIgnoradas,
      erros: resultado.erros
    };
  }
}
