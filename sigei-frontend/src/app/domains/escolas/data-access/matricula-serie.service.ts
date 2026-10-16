import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { MatriculaSerieRequest, MatriculaSerieResponse } from '../models/matricula-serie.models';
export type { MatriculaSerieRequest, MatriculaSerieResponse } from '../models/matricula-serie.models';

@Injectable({ providedIn: 'root' })
export class MatriculaSerieService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/matriculas-serie`;

  listar(): Observable<MatriculaSerieResponse[]> {
    return this.http.get<MatriculaSerieResponse[]>(this.apiUrl);
  }

  salvar(dados: MatriculaSerieRequest): Observable<MatriculaSerieResponse> {
    return this.http.post<MatriculaSerieResponse>(this.apiUrl, dados);
  }

  editar(id: number, dados: MatriculaSerieRequest): Observable<MatriculaSerieResponse> {
    return this.http.put<MatriculaSerieResponse>(`${this.apiUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}