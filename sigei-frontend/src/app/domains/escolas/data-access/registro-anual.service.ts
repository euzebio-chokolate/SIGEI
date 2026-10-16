import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { AnoReferenciaResumo, RegistroAnualRequest, EscolaRegistroResumo, EquipeRegistroResponse, InfraestruturaRegistroResponse, MatriculaNivelRegistroResponse, MatriculaSerieRegistroResponse, RegistroAnualResponse } from '../models/registro-anual.models';
export type { AnoReferenciaResumo, RegistroAnualRequest, EscolaRegistroResumo, EquipeRegistroResponse, InfraestruturaRegistroResponse, MatriculaNivelRegistroResponse, MatriculaSerieRegistroResponse, RegistroAnualResponse } from '../models/registro-anual.models';

@Injectable({ providedIn: 'root' })
export class RegistroAnualService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/registros-anuais`;

  salvar(dados: RegistroAnualRequest): Observable<RegistroAnualResponse> {
    return this.http.post<RegistroAnualResponse>(this.apiUrl, dados);
  }

  editar(id: number, dados: RegistroAnualRequest): Observable<RegistroAnualResponse> {
    return this.http.put<RegistroAnualResponse>(`${this.apiUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /** Todos os registros do ano, sem paginação; ano é o valor civil (2025), não o ID. */
  buscarPorAno(ano: number): Observable<RegistroAnualResponse[]> {
    return this.http.get<RegistroAnualResponse[]>(`${environment.apiUrl}/anos/${ano}/registros-anuais`);
  }

  buscarPorEscolaEAno(escolaId: number, ano: number): Observable<RegistroAnualResponse> {
    return this.http.get<RegistroAnualResponse>(`${environment.apiUrl}/escolas/${escolaId}/registros-anuais/${ano}`);
  }

  buscarHistorico(escolaId: number, anoInicial?: number | null, anoFinal?: number | null): Observable<RegistroAnualResponse[]> {
    const params: Record<string, number> = {};
    if (anoInicial) params['anoInicial'] = anoInicial;
    if (anoFinal) params['anoFinal'] = anoFinal;
    return this.http.get<RegistroAnualResponse[]>(`${environment.apiUrl}/escolas/${escolaId}/registros-anuais`, { params });
  }
}