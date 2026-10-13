export interface DashboardResumo { totalEscolas: number; totalMatriculas: number; totalMunicipios: number; totalTerrasIndigenas: number; }
export interface DashboardEvolucao { ano: number; total: number; }
export interface DashboardItem { descricao: string; total: number; }
export interface DashboardInfraestrutura { totalAvaliado: number; comInternet: number; percentualInternet: number; comEnergia: number; percentualEnergia: number; comPredio: number; percentualPredio: number; }
/** Lotações do ano; em porTipoContrato, descricao é o valor gravado (EFETIVO, PSS...) ou nulo. */
export interface DashboardProfessores { totalProfessores: number; totalLotacoes: number; vagasEmAberto: number; lotacoesNoNucleo: number; porTipoContrato: DashboardItem[]; }
export interface DashboardResponse {
  anoReferencia: number;
  resumo: DashboardResumo;
  evolucaoMatriculas: DashboardEvolucao[];
  evolucaoEscolas: DashboardEvolucao[];
  matriculasPorNivel: DashboardItem[];
  infraestrutura: DashboardInfraestrutura;
  escolasPorMunicipio: DashboardItem[];
  situacaoEscolas: DashboardItem[];
  professores: DashboardProfessores;
}
