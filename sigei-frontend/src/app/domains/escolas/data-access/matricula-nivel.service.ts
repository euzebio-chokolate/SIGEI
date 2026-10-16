import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { MatriculaNivelRequest, MatriculaNivelResponse } from '../models/matricula-nivel.models';
export type { MatriculaNivelRequest, MatriculaNivelResponse } from '../models/matricula-nivel.models';

@Injectable({ providedIn: 'root' })
export class MatriculaNivelService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/matriculas-nivel`;

  listar(): Observable<MatriculaNivelResponse[]> {
    return this.http.get<MatriculaNivelResponse[]>(this.apiUrl);
  }

  salvar(dados: MatriculaNivelRequest): Observable<MatriculaNivelResponse> {
    return this.http.post<MatriculaNivelResponse>(this.apiUrl, dados);
  }

  editar(id: number, dados: MatriculaNivelRequest): Observable<MatriculaNivelResponse> {
    return this.http.put<MatriculaNivelResponse>(`${this.apiUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}