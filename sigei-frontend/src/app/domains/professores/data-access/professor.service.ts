import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { ProfessorRequest, ProfessorResponse } from '../models/professor.models';
export type { ProfessorRequest, ProfessorResponse } from '../models/professor.models';

@Injectable({ providedIn: 'root' })
export class ProfessorService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/professores`;

  listar(): Observable<ProfessorResponse[]> {
    return this.http.get<ProfessorResponse[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<ProfessorResponse> {
    return this.http.get<ProfessorResponse>(`${this.apiUrl}/${id}`);
  }

  salvar(dados: ProfessorRequest): Observable<ProfessorResponse> {
    return this.http.post<ProfessorResponse>(this.apiUrl, dados);
  }

  editar(id: number, dados: ProfessorRequest): Observable<ProfessorResponse> {
    return this.http.put<ProfessorResponse>(`${this.apiUrl}/${id}`, dados);
  }

  /** Exclui o professor e as suas lotações. */
  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
