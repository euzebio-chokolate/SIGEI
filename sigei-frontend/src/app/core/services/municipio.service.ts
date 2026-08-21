import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

export interface Municipio {
  id: number;
  nome: string;
}

export interface MunicipioRequest {
  nome: string;
}

export interface MunicipioEstatistica {
  id: number;
  nome: string;
  quantidadeTerrasIndigenas: number;
  quantidadeAldeias: number;
  quantidadeEscolas: number;
}

@Injectable({
  providedIn: 'root',
})
export class MunicipioService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${environment.apiUrl}/municipio`;

  listarEstatisticas(): Observable<MunicipioEstatistica[]> {
    return this.http.get<MunicipioEstatistica[]>(`${this.apiUrl}/estatisticas`);
  }

  listar(): Observable<Municipio[]> {
    return this.http.get<Municipio[]>(`${this.apiUrl}/buscarTudo`);
  }

  buscarPorId(id: number): Observable<Municipio> {
    return this.http.get<Municipio>(`${this.apiUrl}/${id}`);
  }

  salvar(dados: MunicipioRequest): Observable<Municipio> {
    return this.http.post<Municipio>(this.apiUrl, dados);
  }

  editar(id: number, dados: MunicipioRequest): Observable<Municipio> {
    return this.http.put<Municipio>(`${this.apiUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
