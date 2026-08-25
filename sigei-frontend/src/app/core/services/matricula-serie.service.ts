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

@Injectable({
  providedIn: 'root'
})
export class MatriculaSerieService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/matriculaSerie`;

  listar(): Observable<MatriculaSerieResponse[]> {
    return this.http.get<MatriculaSerieResponse[]>(
      this.apiUrl
    );
  }

  salvar(
    dados: MatriculaSerieRequest
  ): Observable<MatriculaSerieResponse> {
    return this.http.post<MatriculaSerieResponse>(
      `${this.apiUrl}/salvar`,
      dados
    );
  }

  editar(
    dados: MatriculaSerieRequest
  ): Observable<MatriculaSerieResponse> {
    return this.http.put<MatriculaSerieResponse>(
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