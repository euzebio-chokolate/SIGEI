import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface MatriculaNivelRequest {
  id?: number;
  registroId: number;
  nivel: string;
  qtdMasculino: number | null;
  qtdFeminino: number | null;
  total: number;
}

export interface MatriculaNivelResponse {
  id: number;
  nivel: string;
  qtdMasculino: number | null;
  qtdFeminino: number | null;
  total: number;
}

@Injectable({ providedIn: 'root' })
export class MatriculaNivelService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/matriculaNivel`;

  listar(): Observable<MatriculaNivelResponse[]> {
    return this.http.get<MatriculaNivelResponse[]>(this.apiUrl);
  }

  salvar(dados: MatriculaNivelRequest): Observable<MatriculaNivelResponse> {
    return this.http.post<MatriculaNivelResponse>(`${this.apiUrl}/salvar`, dados);
  }

  editar(dados: MatriculaNivelRequest): Observable<MatriculaNivelResponse> {
    return this.http.put<MatriculaNivelResponse>(`${this.apiUrl}/editar`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }
}