import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import type { Pagina } from '@core/http/pagina';

import type {
  LotacaoProfessorRequest,
  LotacaoProfessorResponse
} from '../models/professor.models';
export type {
  LotacaoProfessorRequest,
  LotacaoProfessorResponse
} from '../models/professor.models';

@Injectable({ providedIn: 'root' })
export class LotacaoProfessorService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/lotacao`;

  listarPorAno(ano: number): Observable<LotacaoProfessorResponse[]> {
    return this.http.get<LotacaoProfessorResponse[]>(`${this.apiUrl}/buscarPorAno`, { params: { ano } });
  }

  /** Lotações de uma escola no ano; o tamanho máximo da página (100) cobre qualquer escola. */
  listarPorEscolaEAno(escolaId: number, ano: number): Observable<Pagina<LotacaoProfessorResponse>> {
    return this.http.get<Pagina<LotacaoProfessorResponse>>(this.apiUrl, { params: { escolaId, ano, tamanho: 100 } });
  }

  listarPorProfessor(professorId: number): Observable<LotacaoProfessorResponse[]> {
    return this.http.get<LotacaoProfessorResponse[]>(`${this.apiUrl}/buscarPorProfessor`, { params: { professorId } });
  }

  buscarPorId(id: number): Observable<LotacaoProfessorResponse> {
    return this.http.get<LotacaoProfessorResponse>(`${this.apiUrl}/${id}`);
  }

  salvar(dados: LotacaoProfessorRequest): Observable<LotacaoProfessorResponse> {
    return this.http.post<LotacaoProfessorResponse>(`${this.apiUrl}/salvar`, dados);
  }

  editar(dados: LotacaoProfessorRequest): Observable<LotacaoProfessorResponse> {
    return this.http.put<LotacaoProfessorResponse>(`${this.apiUrl}/editar`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }
}
