export interface MunicipioResumo {
  id: number;
  nome: string;
}

export interface TerraIndigenaResumo {
  id: number;
  nome: string;
  municipio: MunicipioResumo;
}

export interface PovoResumo {
  id: number;
  nome: string;
}

export interface AldeiaRequest {
  id?: number;
  nome: string;
  terraIndigenaId: number;
  povoId: number;
}

export interface AldeiaResponse {
  id: number;
  nome: string;
  terraIndigena: TerraIndigenaResumo;
  povo: PovoResumo;
}
