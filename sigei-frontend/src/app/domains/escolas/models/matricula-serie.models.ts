export interface MatriculaSerieRequest {
  id?: number;
  registroId: number;
  denominacao: string;
  etapaEnsino: string;
  qtdMasculino: number | null;
  qtdFeminino: number | null;
  total: number;
}

export interface MatriculaSerieResponse {
  id: number;
  denominacao: string;
  etapaEnsino: string;
  qtdMasculino: number | null;
  qtdFeminino: number | null;
  total: number;
}
