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

@Injectable({
  providedIn: 'root'
})
export class TerraIndigenaService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/terra`;

  listarEstatisticas(): Observable<TerraIndigenaEstatistica[]> {
    return this.http.get<TerraIndigenaEstatistica[]>(
      `${this.apiUrl}/estatisticas`
    );
  }

  buscarPorId(id: number): Observable<TerraIndigenaResponse> {
    return this.http.get<TerraIndigenaResponse>(
      `${this.apiUrl}/${id}`
    );
  }

  salvar(dados: TerraIndigenaRequest): Observable<TerraIndigenaResponse> {
    return this.http.post<TerraIndigenaResponse>(
      `${this.apiUrl}/salvar`,
      dados
    );
  }

  editar(
    dados: TerraIndigenaRequest
  ): Observable<TerraIndigenaResponse> {
    return this.http.put<TerraIndigenaResponse>(
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