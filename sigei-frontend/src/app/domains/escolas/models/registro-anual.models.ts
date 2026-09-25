export interface AnoReferenciaResumo {
  id: number;
  ano: number;
}

export interface RegistroAnualRequest {
  id?: number;
  escolaId: number;
  anoReferenciaId: number;
  rede: string;
  localFuncionamento: string;
  totalAlunos?: number | null;
}

export interface EscolaRegistroResumo {
  id: number;
  codigo: string;
  nome: string;
}

export interface EquipeRegistroResponse {
  id: number;

  temProfessorResponsavel: boolean;
  temEquipeGestora: boolean;
  temMerendeira: boolean;
  temZeladora: boolean;
  temBarqueiro: boolean;
  necessitaTransporte: boolean;
}

export interface InfraestruturaRegistroResponse {
  id: number;

  escola: EscolaRegistroResumo;

  anoReferencia: AnoReferenciaResumo;

  anoConstrucao: number | null;
  numSalas: number | null;

  latitude: number | null;
  longitude: number | null;

  situacao: string | null;
  tipoAcesso: string | null;

  temPredio: boolean;

  tipoAgua: string | null;

  temInternet: boolean;

  tipoEnergia: string | null;

  temRegistroFotografico: boolean;

  fotoUrl: string | null;
}

export interface MatriculaNivelRegistroResponse {
  id: number;

  nivel: string;

  qtdMasculino: number | null;
  qtdFeminino: number | null;

  total: number;
}

export interface MatriculaSerieRegistroResponse {
  id: number;

  denominacao: string;
  etapaEnsino: string;

  qtdMasculino: number | null;
  qtdFeminino: number | null;

  total: number;
}

export interface RegistroAnualResponse {
  id: number;

  anoReferencia: AnoReferenciaResumo;

  rede: string;

  localFuncionamento: string;

  totalAlunos: number | null;

  equipe: EquipeRegistroResponse | null;

  infraestrutura:
    InfraestruturaRegistroResponse | null;

  matriculasNivel:
    MatriculaNivelRegistroResponse[];

  matriculasSerie:
    MatriculaSerieRegistroResponse[];
}
