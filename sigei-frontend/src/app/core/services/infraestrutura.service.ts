import {
  Injectable,
  inject
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  environment
} from '../../environments/environment';

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

@Injectable({
  providedIn: 'root'
})
export class InfraestruturaService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/infraestrutura`;

  listarTodas(): Observable<InfraestruturaResponse[]> {
    return this.http.get<InfraestruturaResponse[]>(
      this.apiUrl
    );
  }

  salvar(
    dados: InfraestruturaRequest
  ): Observable<InfraestruturaResponse> {
    return this.http.post<InfraestruturaResponse>(
      `${this.apiUrl}/salvar`,
      dados
    );
  }

  adicionarFoto(
    infraestruturaId: number,
    foto: File
  ): Observable<InfraestruturaResponse> {
    const formData = new FormData();

    formData.append(
      'foto',
      foto
    );

    return this.http.post<InfraestruturaResponse>(
      `${this.apiUrl}/${infraestruturaId}/foto`,
      formData
    );
  }

  editar(
    dados: InfraestruturaRequest
  ): Observable<InfraestruturaResponse> {
    return this.http.put<InfraestruturaResponse>(
      `${this.apiUrl}/editar`,
      dados
    );
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/excluir/${id}`
    );
  }

}