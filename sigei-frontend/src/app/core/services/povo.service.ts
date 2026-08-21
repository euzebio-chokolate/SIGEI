import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

export interface AldeiaResumo {
  id: number;
  nome: string;
}

export interface PovoRequest {
  id?: number;
  nome: string;
}

export interface PovoResponse {
  id: number;
  nome: string;
  aldeias: AldeiaResumo[];
}

@Injectable({
  providedIn: 'root',
})
export class PovoService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/povo`;

  listar(): Observable<PovoResponse[]> {
    return this.http.get<PovoResponse[]>(`${this.apiUrl}`);
  }

  buscarPorId(id: number): Observable<PovoResponse> {
    return this.http.get<PovoResponse>(`${this.apiUrl}/${id}`);
  }

  salvar(dados: PovoRequest): Observable<PovoResponse> {
    return this.http.post<PovoResponse>(`${this.apiUrl}/salvar`, dados);
  }

  editar(
      dados: PovoRequest
    ): Observable<PovoResponse> {
      return this.http.put<PovoResponse>(
        `${this.apiUrl}/editar`,
        dados
      );
    }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }
}
