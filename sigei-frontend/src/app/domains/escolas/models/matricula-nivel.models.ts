export interface MatriculaNivelRequest {
  id?: number;
  registroId: number;
  nivel: string;
  qtdMasculino: number | null;
  qtdFeminino: number | null;
  total: number;
}

export interface MatriculaNivelResponse {
  id: number;
  nivel: string;
  qtdMasculino: number | null;
  qtdFeminino: number | null;
  total: number;
}
