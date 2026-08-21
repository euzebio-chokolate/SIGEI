import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

export interface MunicipioResumo {
  id: number;
  nome: string;
}

export interface TerraIndigenaResumo {
  id: number;
  nome: string;
  municipio: MunicipioResumo;
}

export interface PovoResumo {
  id: number;
  nome: string;
}

export interface AldeiaRequest {
  id?: number;
  nome: string;
  terraIndigenaId: number;
  povoId: number;
}

export interface AldeiaResponse {
  id: number;
  nome: string;
  terraIndigena: TerraIndigenaResumo;
  povo: PovoResumo;
}

@Injectable({
  providedIn: 'root'
})
export class AldeiaService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    `${environment.apiUrl}/aldeia`;

  listar(): Observable<AldeiaResponse[]> {
    return this.http.get<AldeiaResponse[]>(
      `${this.apiUrl}`
    );
  }

  buscarPorId(id: number): Observable<AldeiaResponse> {
    return this.http.get<AldeiaResponse>(
      `${this.apiUrl}/${id}`
    );
  }

  salvar(
    dados: AldeiaRequest
  ): Observable<AldeiaResponse> {
    return this.http.post<AldeiaResponse>(
      `${this.apiUrl}/salvar`,
      dados
    );
  }

  editar(
  dados: AldeiaRequest
): Observable<AldeiaResponse> {
  return this.http.put<AldeiaResponse>(
    `${this.apiUrl}/editar`,
    dados
  );
}

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/excluir/${id}`
    );
  }
}