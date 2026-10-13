/** Relatórios de GET /relatorios/{tipo}. */
export type TipoRelatorio = 'painel' | 'escolas' | 'matriculas' | 'professores';

export type FormatoRelatorio = 'pdf' | 'xlsx';

export interface OpcaoRelatorio {
  tipo: TipoRelatorio;
  titulo: string;
  descricao: string;
  icone: string;
  conteudo: string[];
}

export const OPCOES_RELATORIO: OpcaoRelatorio[] = [
  {
    tipo: 'painel',
    titulo: 'Indicadores consolidados',
    descricao: 'Os números do painel gerencial em um documento.',
    icone: 'fa-chart-pie',
    conteudo: ['Resumo geral', 'Matrículas por nível', 'Infraestrutura e situação', 'Totais por município', 'Evolução histórica']
  },
  {
    tipo: 'escolas',
    titulo: 'Escolas e infraestrutura',
    descricao: 'Cadastro das escolas do ano, com localização e infraestrutura.',
    icone: 'fa-school',
    conteudo: ['Município, terra indígena, aldeia e povo', 'Sede ou anexo', 'Situação, acesso, prédio e salas', 'Internet, água, energia e alunos']
  },
  {
    tipo: 'matriculas',
    titulo: 'Matrículas por etapa',
    descricao: 'Matrículas de cada escola por etapa de ensino e por sexo.',
    icone: 'fa-user-graduate',
    conteudo: ['Por escola', 'Por município', 'Por série / ano']
  },
  {
    tipo: 'professores',
    titulo: 'Professores e lotações',
    descricao: 'Quadro de lotações do ano, com vagas em aberto e núcleo.',
    icone: 'fa-chalkboard-user',
    conteudo: ['Lotações com cargo, segmento e contrato', 'Por tipo de contrato', 'Por município', 'Sem CPF, matrícula e número do contrato']
  }
];

export const FORMATOS_RELATORIO: { formato: FormatoRelatorio; rotulo: string; icone: string }[] = [
  { formato: 'pdf', rotulo: 'PDF', icone: 'fa-file-pdf' },
  { formato: 'xlsx', rotulo: 'Planilha', icone: 'fa-file-excel' }
];

export interface FiltroRelatorio {
  ano?: number | null;
  municipioId?: number | null;
}
