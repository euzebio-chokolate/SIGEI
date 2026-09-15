export interface AldeiaResumo {
  id: number;
  nome: string;
}

export interface PovoRequest {
  id?: number;
  nome: string;
}

export interface PovoResponse {
  id: number;
  nome: string;
  aldeias: AldeiaResumo[];
}
