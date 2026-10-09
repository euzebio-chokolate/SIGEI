import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type {
  ImportacaoProfessoresResultado,
  LotacaoProfessorRequest,
  LotacaoProfessorResponse
} from '../models/professor.models';
export type {
  ImportacaoProfessoresResultado,
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

  /**
   * Importa a planilha do quadro de professores (ano 2026, que precisa estar iniciado).
   * Pode ser repetida: células vazias não apagam CPF, matrícula e contrato já preenchidos.
   */
  importarPlanilha(arquivo: File): Observable<ImportacaoProfessoresResultado> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.http.post<ImportacaoProfessoresResultado>(`${environment.apiUrl}/importacoes/professores`, formData);
  }
}
