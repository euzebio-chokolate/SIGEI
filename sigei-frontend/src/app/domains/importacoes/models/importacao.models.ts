import type { ImportacaoProfessoresResultado } from '@domains/professores/models/professor.models';
export type { ImportacaoProfessoresResultado } from '@domains/professores/models/professor.models';

/** Tipos aceitos por POST /importacoes/{tipo} e GET /importacoes/modelos/{tipo}. */
export type TipoImportacao = 'infraestrutura' | 'matriculas' | 'professores';

/** Coluna aceita no cabeçalho da planilha (GET /importacoes/layouts). */
export interface ColunaLayout {
  titulo: string;
  obrigatoria: boolean;
  descricao: string;
  sinonimos: string[];
}

export interface LayoutImportacao {
  tipo: TipoImportacao;
  titulo: string;
  instrucoes: string[];
  colunas: ColunaLayout[];
}

export interface ImportacaoEscolasResultado {
  linhasLidas: number;
  municipiosCriados: number;
  terrasIndigenasCriadas: number;
  povosCriados: number;
  aldeiasCriadas: number;
  escolasCriadas: number;
  registrosAnuaisCriados: number;
  infraestruturasCriadas: number;
  equipesCriadas: number;
  registrosAtualizados: number;
  linhasIgnoradas: number;
  erros: string[];
}

export interface ImportacaoMatriculasResultado {
  linhasLidas: number;
  escolasEncontradas: number;
  registrosAnuaisEncontrados: number;
  niveisInseridos: number;
  seriesInseridas: number;
  registrosAnuaisAtualizados: number;
  linhasIgnoradas: number;
  erros: string[];
}

export type ResultadoImportacao =
  | ImportacaoEscolasResultado
  | ImportacaoMatriculasResultado
  | ImportacaoProfessoresResultado;

export interface OpcaoImportacao {
  tipo: TipoImportacao;
  icone: string;
  resumo: string;
  /** Como o ano é tratado: a importação de escolas cria o ano; as demais exigem um ano existente. */
  criaAno: boolean;
}

/** Na ordem em que as planilhas devem ser importadas num ano novo. */
export const OPCOES_IMPORTACAO: OpcaoImportacao[] = [
  {
    tipo: 'infraestrutura',
    icone: 'fa-school',
    resumo: 'Cadastro das escolas, infraestrutura e equipe. Cria o ano, se ainda não existir.',
    criaAno: true
  },
  {
    tipo: 'matriculas',
    icone: 'fa-user-graduate',
    resumo: 'Matrículas por etapa das escolas já cadastradas no ano.',
    criaAno: false
  },
  {
    tipo: 'professores',
    icone: 'fa-chalkboard-user',
    resumo: 'Professores e lotações do ano, que precisa estar iniciado.',
    criaAno: false
  }
];

export interface IndicadorResultado {
  rotulo: string;
  valor: number;
}

/** Principais contadores de cada importação, para o resumo do resultado. */
export function indicadoresResultado(tipo: TipoImportacao, resultado: ResultadoImportacao): IndicadorResultado[] {
  switch (tipo) {
    case 'infraestrutura': {
      const r = resultado as ImportacaoEscolasResultado;
      return [
        { rotulo: 'Linhas lidas', valor: r.linhasLidas },
        { rotulo: 'Escolas novas', valor: r.escolasCriadas },
        { rotulo: 'Registros do ano criados', valor: r.registrosAnuaisCriados },
        { rotulo: 'Atualizações', valor: r.registrosAtualizados },
        { rotulo: 'Aldeias novas', valor: r.aldeiasCriadas },
        { rotulo: 'Terras indígenas novas', valor: r.terrasIndigenasCriadas },
        { rotulo: 'Povos novos', valor: r.povosCriados }
      ];
    }
    case 'matriculas': {
      const r = resultado as ImportacaoMatriculasResultado;
      return [
        { rotulo: 'Linhas lidas', valor: r.linhasLidas },
        { rotulo: 'Escolas encontradas', valor: r.escolasEncontradas },
        { rotulo: 'Registros atualizados', valor: r.registrosAnuaisAtualizados }
      ];
    }
    case 'professores': {
      const r = resultado as ImportacaoProfessoresResultado;
      return [
        { rotulo: 'Linhas lidas', valor: r.linhasLidas },
        { rotulo: 'Professores novos', valor: r.professoresCriados },
        { rotulo: 'Professores atualizados', valor: r.professoresAtualizados },
        { rotulo: 'Lotações novas', valor: r.lotacoesCriadas },
        { rotulo: 'Lotações atualizadas', valor: r.lotacoesAtualizadas },
        { rotulo: 'Vagas em aberto', valor: r.vagasEmAberto },
        { rotulo: 'Lotações no núcleo', valor: r.lotacoesNoNucleo }
      ];
    }
  }
}
