import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { MunicipioResumo, TerraIndigenaRequest, TerraIndigenaResponse, TerraIndigenaEstatistica } from '../models/terra-indigena.models';
export type { MunicipioResumo, TerraIndigenaRequest, TerraIndigenaResponse, TerraIndigenaEstatistica } from '../models/terra-indigena.models';

@Injectable({ providedIn: 'root' })
export class TerraIndigenaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/terras-indigenas`;

  listarEstatisticas(): Observable<TerraIndigenaEstatistica[]> {
    return this.http.get<TerraIndigenaEstatistica[]>(`${this.apiUrl}/estatisticas`);
  }

  buscarPorId(id: number): Observable<TerraIndigenaResponse> {
    return this.http.get<TerraIndigenaResponse>(`${this.apiUrl}/${id}`);
  }

  salvar(dados: TerraIndigenaRequest): Observable<TerraIndigenaResponse> {
    return this.http.post<TerraIndigenaResponse>(this.apiUrl, dados);
  }

  editar(id: number, dados: TerraIndigenaRequest): Observable<TerraIndigenaResponse> {
    return this.http.put<TerraIndigenaResponse>(`${this.apiUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}