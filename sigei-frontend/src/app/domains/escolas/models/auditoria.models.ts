/** Espelha o AlteracaoHistoricoDTO do backend (GET /escola/{id}/historico). */
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
  /** Nome da classe auditada: Escola, RegistroAnual, EquipeEscola, InfraestruturaEscola, MatriculaNivel, MatriculaSerie. */
  entidade: string;
  entidadeId: number;
  /** Ano do registro anual a que a entidade pertence; nulo para a escola. */
  ano: number | null;
  operacao: OperacaoHistorico;
  campos: CampoAlterado[];
}

/** Uma revisão da Envers: as alterações gravadas juntas, na mesma transação. */
export interface RevisaoHistorico {
  revisao: number;
  dataHora: string;
  usuarioNome: string | null;
  usuarioEmail: string | null;
  alteracoes: AlteracaoHistorico[];
}

const ROTULOS_ENTIDADE: Record<string, string> = {
  Escola: 'Escola',
  RegistroAnual: 'Registro anual',
  EquipeEscola: 'Equipe',
  InfraestruturaEscola: 'Infraestrutura',
  MatriculaNivel: 'Matrícula por nível',
  MatriculaSerie: 'Matrícula por série'
};

const ROTULOS_CAMPO: Record<string, string> = {
  codigo: 'Código',
  nome: 'Nome',
  tipoUnidade: 'Tipo da unidade',
  escolaSedeId: 'Escola sede (ID)',
  aldeiaId: 'Aldeia (ID)',
  escolaId: 'Escola (ID)',
  anoReferenciaId: 'Ano de referência (ID)',
  registroAnualId: 'Registro anual (ID)',
  rede: 'Rede',
  localFuncionamento: 'Local de funcionamento',
  totalAlunos: 'Total de alunos',
  temProfessorResponsavel: 'Professor responsável',
  temEquipeGestora: 'Equipe gestora',
  temMerendeira: 'Merendeira',
  temZeladora: 'Zeladora',
  temBarqueiro: 'Barqueiro',
  necessitaTransporte: 'Necessita transporte',
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
  nivel: 'Nível',
  denominacao: 'Série',
  etapaEnsino: 'Etapa de ensino',
  qtdMasculino: 'Masculino',
  qtdFeminino: 'Feminino',
  total: 'Total'
};

/** Campos internos que não dizem nada a quem lê o histórico. */
const CAMPOS_OCULTOS = new Set(['id', 'fotoPublicId']);

export function rotuloEntidade(entidade: string): string {
  return ROTULOS_ENTIDADE[entidade] ?? entidade;
}

export function rotuloCampo(campo: string): string {
  // Campo sem rótulo conhecido: "temAreaLazer" vira "Tem area lazer"
  return ROTULOS_CAMPO[campo] ?? campo.replace(/([A-Z])/g, ' $1').replace(/^./, letra => letra.toUpperCase());
}

export function campoVisivel(campo: CampoAlterado): boolean {
  return !CAMPOS_OCULTOS.has(campo.campo);
}

export function formatarValor(campo: string, valor: unknown): string {
  if (valor === null || valor === undefined || valor === '') return '—';
  if (campo === 'fotoUrl') return 'Enviada';
  if (typeof valor === 'boolean') return valor ? 'Sim' : 'Não';
  if (typeof valor === 'number') return valor.toLocaleString('pt-BR');
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
