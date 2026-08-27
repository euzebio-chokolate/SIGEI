import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

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
}

export interface EscolaRegistroResumo {
  id: number;
  codigo: string;
  nome: string;
}

export interface AnoReferenciaResumo {
  id: number;
  ano: number;
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

  equipe: EquipeRegistroResponse | null;

  infraestrutura:
    InfraestruturaRegistroResponse | null;

  matriculasNivel:
    MatriculaNivelRegistroResponse[];

  matriculasSerie:
    MatriculaSerieRegistroResponse[];
}

@Injectable({ providedIn: 'root' })
export class RegistroAnualService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/registroAnual`;

  listar(): Observable<RegistroAnualResponse[]> {
    return this.http.get<RegistroAnualResponse[]>(this.apiUrl);
  }

  salvar(dados: RegistroAnualRequest): Observable<RegistroAnualResponse> {
    return this.http.post<RegistroAnualResponse>(`${this.apiUrl}/salvar`, dados);
  }

  editar(dados: RegistroAnualRequest): Observable<RegistroAnualResponse> {
    return this.http.put<RegistroAnualResponse>(`${this.apiUrl}/editar`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }

  buscarPorAno(ano: number): Observable<RegistroAnualResponse[]> {
    return this.http.get<RegistroAnualResponse[]>(`${this.apiUrl}/buscarPorAno`, { params: {ano} });
  }
}