import {
  OpcaoInfraestrutura,
  rotuloOpcao,
  SITUACOES_ESCOLA,
  TIPOS_ACESSO,
  TIPOS_AGUA,
  TIPOS_ENERGIA
} from '@domains/escolas/models/infraestrutura-opcoes';
import { rotuloNivel } from '@domains/escolas/models/matricula-opcoes';
import { rotuloSexo, rotuloTipoContrato } from '@domains/professores/models/professor.models';

/** Espelha o AlteracaoHistoricoDTO do backend (GET /auditoria e GET /escola/{id}/historico). */
export type OperacaoHistorico = 'CRIACAO' | 'ALTERACAO' | 'EXCLUSAO';

export interface CampoAlterado {
  campo: string;
  valorAnterior: unknown;
  valorNovo: unknown;
}

export interface AlteracaoHistorico {
  revisao: number;
  /** Instant em ISO-8601 (UTC). */
  dataHora: string;
  /** Nulo quando a alteração foi feita pelo sistema (ex.: importação, atualização anual). */
  usuarioId: number | null;
  usuarioNome: string | null;
  usuarioEmail: string | null;
  /** Nome da classe auditada no backend (Escola, Aldeia, Professor...). */
  entidade: string;
  entidadeId: number;
  /** Identifica o registro: nome, série ou, para partes do registro anual, a escola. Pode faltar. */
  descricao: string | null;
  /** Ano do registro anual a que a entidade pertence; nulo quando não se aplica. */
  ano: number | null;
  operacao: OperacaoHistorico;
  campos: CampoAlterado[];
}

/** Espelha o PaginaAuditoriaDTO: paginação por alteração, não por revisão. */
export interface PaginaAuditoria {
  alteracoes: AlteracaoHistorico[];
  pagina: number;
  tamanho: number;
  totalAlteracoes: number;
  totalPaginas: number;
}

export interface FiltroAuditoria {
  entidade?: string | null;
  operacao?: OperacaoHistorico | null;
  usuarioId?: number | null;
  /** Só alterações sem autor (inicializadores, cargas, migrações). */
  sistema?: boolean | null;
  /** yyyy-MM-dd, inclusive, no horário do Acre. */
  de?: string | null;
  ate?: string | null;
}

/** Uma revisão da Envers: as alterações gravadas juntas, na mesma transação. */
export interface RevisaoHistorico {
  revisao: number;
  dataHora: string;
  usuarioNome: string | null;
  usuarioEmail: string | null;
  alteracoes: AlteracaoHistorico[];
}

/** As entidades auditadas, na ordem em que aparecem no filtro. */
export const ENTIDADES_AUDITADAS: { valor: string; rotulo: string }[] = [
  { valor: 'Escola', rotulo: 'Escola' },
  { valor: 'RegistroAnual', rotulo: 'Registro anual' },
  { valor: 'EquipeEscola', rotulo: 'Equipe' },
  { valor: 'InfraestruturaEscola', rotulo: 'Infraestrutura' },
  { valor: 'MatriculaNivel', rotulo: 'Matrícula por nível' },
  { valor: 'MatriculaSerie', rotulo: 'Matrícula por série' },
  { valor: 'Professor', rotulo: 'Professor' },
  { valor: 'LotacaoProfessor', rotulo: 'Lotação de professor' },
  { valor: 'Aldeia', rotulo: 'Aldeia' },
  { valor: 'Povo', rotulo: 'Povo' },
  { valor: 'TerraIndigena', rotulo: 'Terra indígena' },
  { valor: 'Municipio', rotulo: 'Município' },
  { valor: 'AnoReferencia', rotulo: 'Ano de referência' },
  { valor: 'Usuario', rotulo: 'Usuário' }
];

export const OPERACOES_HISTORICO: { valor: OperacaoHistorico; rotulo: string }[] = [
  { valor: 'CRIACAO', rotulo: 'Criação' },
  { valor: 'ALTERACAO', rotulo: 'Alteração' },
  { valor: 'EXCLUSAO', rotulo: 'Exclusão' }
];

const ROTULOS_ENTIDADE: Record<string, string> = Object.fromEntries(
  ENTIDADES_AUDITADAS.map(({ valor, rotulo }) => [valor, rotulo])
);

const ROTULOS_CAMPO: Record<string, string> = {
  // Escola e registro anual
  codigo: 'Código',
  nome: 'Nome',
  tipoUnidade: 'Tipo da unidade',
  escolaSedeId: 'Escola sede (ID)',
  aldeiaId: 'Aldeia (ID)',
  escolaId: 'Escola (ID)',
  anoReferenciaId: 'Ano de referência (ID)',
  registroAnualId: 'Registro anual (ID)',
  registroId: 'Registro anual (ID)',
  rede: 'Rede',
  localFuncionamento: 'Local de funcionamento',
  totalAlunos: 'Total de alunos',
  // Equipe
  temProfessorResponsavel: 'Professor responsável',
  temEquipeGestora: 'Equipe gestora',
  temMerendeira: 'Merendeira',
  temZeladora: 'Zeladora',
  temBarqueiro: 'Barqueiro',
  necessitaTransporte: 'Necessita transporte',
  // Infraestrutura
  anoConstrucao: 'Ano de construção',
  numSalas: 'Número de salas',
  latitude: 'Latitude',
  longitude: 'Longitude',
  situacao: 'Situação',
  tipoAcesso: 'Tipo de acesso',
  temPredio: 'Possui prédio',
  tipoAgua: 'Abastecimento de água',
  temInternet: 'Internet',
  tipoEnergia: 'Energia',
  temRegistroFotografico: 'Registro fotográfico',
  fotoUrl: 'Foto',
  // Matrículas
  nivel: 'Nível',
  denominacao: 'Série',
  etapaEnsino: 'Etapa de ensino',
  qtdMasculino: 'Masculino',
  qtdFeminino: 'Feminino',
  total: 'Total',
  // Territórios
  terraIndigenaId: 'Terra indígena (ID)',
  povoId: 'Povo (ID)',
  municipioId: 'Município (ID)',
  ano: 'Ano',
  // Professores e lotações
  cpf: 'CPF',
  matricula: 'Matrícula',
  etnia: 'Etnia',
  sexo: 'Sexo',
  formacao: 'Formação',
  professorId: 'Professor (ID)',
  cargo: 'Cargo',
  segmento: 'Segmento',
  numeroContrato: 'Número do contrato',
  inicioContrato: 'Início do contrato',
  amparoLegal: 'Amparo legal',
  tipoContrato: 'Tipo de contrato',
  // Usuários
  email: 'E-mail',
  perfil: 'Perfil',
  ativo: 'Ativo',
  alterarSenha: 'Troca de senha pendente'
};

/** Campos internos que não dizem nada a quem lê o histórico. */
const CAMPOS_OCULTOS = new Set(['id', 'fotoPublicId']);

export function rotuloEntidade(entidade: string): string {
  return ROTULOS_ENTIDADE[entidade] ?? entidade;
}

export function rotuloCampo(campo: string): string {
  // Campo sem rótulo conhecido: "temAreaLazer" vira "Tem Area Lazer"
  return ROTULOS_CAMPO[campo] ?? campo.replace(/([A-Z])/g, ' $1').replace(/^./, letra => letra.toUpperCase());
}

export function campoVisivel(campo: CampoAlterado): boolean {
  return !CAMPOS_OCULTOS.has(campo.campo);
}

/** Campos gravados como códigos (FUNDAMENTAL_ANOS_INICIAIS) e o rótulo de cada um. */
const OPCOES_CAMPO: Record<string, readonly OpcaoInfraestrutura[]> = {
  situacao: SITUACOES_ESCOLA,
  tipoAcesso: TIPOS_ACESSO,
  tipoAgua: TIPOS_AGUA,
  tipoEnergia: TIPOS_ENERGIA
};

export function formatarValor(campo: string, valor: unknown): string {
  if (valor === null || valor === undefined || valor === '') return '—';
  if (campo === 'fotoUrl') return 'Enviada';
  if (typeof valor === 'string') {
    if (OPCOES_CAMPO[campo]) return rotuloOpcao(OPCOES_CAMPO[campo], valor);
    if (campo === 'nivel' || campo === 'etapaEnsino') return rotuloNivel(valor);
    if (campo === 'tipoContrato') return rotuloTipoContrato(valor);
    if (campo === 'sexo') return rotuloSexo(valor);
  }
  if (typeof valor === 'boolean') return valor ? 'Sim' : 'Não';
  // O ano não leva separador de milhar
  if (typeof valor === 'number') return campo === 'ano' || campo === 'anoConstrucao' ? String(valor) : valor.toLocaleString('pt-BR');
  // LocalDate (ex.: início do contrato) chega como yyyy-MM-dd
  if (typeof valor === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(valor)) return valor.split('-').reverse().join('/');
  return String(valor);
}

/** Junta as alterações da mesma revisão; a lista do backend já vem da mais recente à mais antiga. */
export function agruparPorRevisao(alteracoes: AlteracaoHistorico[]): RevisaoHistorico[] {
  const grupos: RevisaoHistorico[] = [];
  for (const alteracao of alteracoes) {
    const atual = grupos.at(-1);
    if (atual?.revisao === alteracao.revisao) {
      atual.alteracoes.push(alteracao);
    } else {
      grupos.push({
        revisao: alteracao.revisao,
        dataHora: alteracao.dataHora,
        usuarioNome: alteracao.usuarioNome,
        usuarioEmail: alteracao.usuarioEmail,
        alteracoes: [alteracao]
      });
    }
  }
  return grupos;
}
