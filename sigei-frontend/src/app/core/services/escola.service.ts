import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface MunicipioResumo {
  id: number;
  nome: string;
}

export interface PovoResumo {
  id: number;
  nome: string;
}

export interface TerraIndigenaResumo {
  id: number;
  nome: string;
  municipio?: MunicipioResumo;
}

export interface AldeiaResumo {
  id: number;
  nome: string;
  terraIndigena?: TerraIndigenaResumo;
  povo?: PovoResumo;
}

export interface EscolaResumo {
  id: number;
  codigo: string;
  nome: string;
}

export interface EscolaResponse {
  id: number;
  codigo: string;
  nome: string;
  tipoUnidade: string;
  escolaSede: EscolaResumo | null;
  aldeia: {
    id: number;
    nome: string;
    terraIndigena: {
      id: number;
      nome: string;
      municipio: MunicipioResumo;
    };
    povo: PovoResumo;
  };
}

export interface AnoReferenciaResumo {
  id: number;
  ano: number;
}

export interface EquipeEscola {
  id: number;
  temProfessorResponsavel: boolean;
  temEquipeGestora: boolean;
  temMerendeira: boolean;
  temZeladora: boolean;
  temBarqueiro: boolean;
  necessitaTransporte: boolean;
}

export interface InfraestruturaEscolaDetalhe {
  id: number;
  escola: EscolaResumo;
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

export interface MatriculaNivel {
  id: number;
  nivel: string;
  qtdMasculino: number | null;
  qtdFeminino: number | null;
  total: number;
}

export interface MatriculaSerie {
  id: number;
  denominacao: string;
  etapaEnsino: string;
  qtdMasculino: number | null;
  qtdFeminino: number | null;
}

export interface RegistroAnualDetalhe {
  id: number;
  anoReferencia: AnoReferenciaResumo;
  rede: string;
  localFuncionamento: string;
  equipe: EquipeEscola | null;
  infraestrutura: InfraestruturaEscolaDetalhe | null;
  matriculasNivel: MatriculaNivel[];
  matriculasSerie: MatriculaSerie[];
}

export interface EscolaDetalheResponse {
  id: number;
  codigo: string;
  nome: string;
  tipoUnidade: string;
  escolaSede: EscolaResumo | null;
  municipio: MunicipioResumo;
  terraIndigena: {
    id: number;
    nome: string;
  };
  aldeia: {
    id: number;
    nome: string;
  };
  registroAnual: RegistroAnualDetalhe | null;
}

@Injectable({ providedIn: 'root' })
export class EscolaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/escola`;

  salvar(
    dados: {
      codigo: string;
      nome: string;
      tipoUnidade: string;
      escolaSedeId: number | null;
      aldeiaId: number;
    }
  ): Observable<EscolaResponse> {
    return this.http.post<EscolaResponse>(`${this.apiUrl}/salvar`, dados);
  }

  listar(): Observable<EscolaResponse[]> {
    return this.http.get<EscolaResponse[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<EscolaDetalheResponse> {
    return this.http.get<EscolaDetalheResponse>(`${this.apiUrl}/${id}`);
  }

  editar(
    dados: {
      id?: number;
      codigo: string;
      nome: string;
      tipoUnidade: string;
      escolaSedeId: number | null;
      aldeiaId: number;
    }
    ): Observable<EscolaResponse> {
        return this.http.put<EscolaResponse>(`${this.apiUrl}/editar`,dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }
}