export interface MunicipioResumo {
  id: number;
  nome: string;
}

export interface TerraIndigenaRequest {
  id?: number;
  nome: string;
  municipioId: number;
}

export interface TerraIndigenaResponse {
  id: number;
  nome: string;
  municipio: MunicipioResumo;
}

export interface TerraIndigenaEstatistica {
  id: number;
  nome: string;
  municipio: MunicipioResumo;
  quantidadeAldeias: number;
  quantidadeEscolas: number;
}
