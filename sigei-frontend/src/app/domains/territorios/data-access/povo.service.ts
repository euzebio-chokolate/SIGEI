import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { AldeiaResumo, PovoRequest, PovoResponse } from '../models/povo.models';
export type { AldeiaResumo, PovoRequest, PovoResponse } from '../models/povo.models';

@Injectable({ providedIn: 'root' })
export class PovoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/povos`;

  listar(): Observable<PovoResponse[]> {
    return this.http.get<PovoResponse[]>(`${this.apiUrl}`);
  }

  buscarPorId(id: number): Observable<PovoResponse> {
    return this.http.get<PovoResponse>(`${this.apiUrl}/${id}`);
  }

  salvar(dados: PovoRequest): Observable<PovoResponse> {
    return this.http.post<PovoResponse>(this.apiUrl, dados);
  }

  editar(id: number, dados: PovoRequest): Observable<PovoResponse> {
    return this.http.put<PovoResponse>(`${this.apiUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}