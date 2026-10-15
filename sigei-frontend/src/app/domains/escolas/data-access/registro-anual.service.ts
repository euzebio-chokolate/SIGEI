import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { AnoReferenciaResumo, RegistroAnualRequest, EscolaRegistroResumo, EquipeRegistroResponse, InfraestruturaRegistroResponse, MatriculaNivelRegistroResponse, MatriculaSerieRegistroResponse, RegistroAnualResponse } from '../models/registro-anual.models';
export type { AnoReferenciaResumo, RegistroAnualRequest, EscolaRegistroResumo, EquipeRegistroResponse, InfraestruturaRegistroResponse, MatriculaNivelRegistroResponse, MatriculaSerieRegistroResponse, RegistroAnualResponse } from '../models/registro-anual.models';

@Injectable({ providedIn: 'root' })
export class RegistroAnualService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/registroAnual`;

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

  buscarPorEscolaEAno(escolaId: number, ano: number): Observable<RegistroAnualResponse> {
    return this.http.get<RegistroAnualResponse>(`${this.apiUrl}/buscarPorEscolaEAno`, { params: { escolaId, ano } });
  }

  buscarHistorico(escolaId: number, anoInicial?: number | null, anoFinal?: number | null): Observable<RegistroAnualResponse[]> {
    const params: Record<string, number> = { escolaId };
    if (anoInicial) params['anoInicial'] = anoInicial;
    if (anoFinal) params['anoFinal'] = anoFinal;
    return this.http.get<RegistroAnualResponse[]>(`${this.apiUrl}/historico`, { params });
  }
}