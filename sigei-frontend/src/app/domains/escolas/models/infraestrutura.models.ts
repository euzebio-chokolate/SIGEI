export interface EscolaResumo {
  id: number;
  codigo: string;
  nome: string;
}

export interface AnoReferenciaResumo {
  id: number;
  ano: number;
}

export interface InfraestruturaRequest {
  id?: number;
  registroAnualId: number;
  anoConstrucao: number | null;
  numSalas: number | null;
  latitude: number | null;
  longitude: number | null;
  situacao: string;
  tipoAcesso: string;
  temPredio: boolean;
  tipoAgua: string;
  temInternet: boolean;
  tipoEnergia: string;
  temRegistroFotografico: boolean;
}

export interface InfraestruturaResponse {
  id: number;
  escola: EscolaResumo;
  anoReferencia: AnoReferenciaResumo;
  anoConstrucao: number | null;
  numSalas: number | null;
  latitude: number | null;
  longitude: number | null;
  situacao: string;
  tipoAcesso: string;
  temPredio: boolean;
  tipoAgua: string;
  temInternet: boolean;
  tipoEnergia: string;
  temRegistroFotografico: boolean;
  fotoUrl: string | null;
}
