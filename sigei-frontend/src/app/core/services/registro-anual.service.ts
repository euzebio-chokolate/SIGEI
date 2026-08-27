import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AnoReferenciaResumo {
  id: number;
  ano: number;
}

export interface RegistroAnualRequest {
  id?: number;
  escolaId: number;
  anoReferenciaId: number;
  rede: string;
  localFuncionamento: string;
}

export interface RegistroAnualResponse {
  id: number;
  anoReferencia: AnoReferenciaResumo;
  rede: string;
  localFuncionamento: string;
}

@Injectable({ providedIn: 'root' })
export class RegistroAnualService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/registroAnual`;

  listar(): Observable<RegistroAnualResponse[]> {
    return this.http.get<RegistroAnualResponse[]>(this.apiUrl);
  }

  salvar(dados: RegistroAnualRequest): Observable<RegistroAnualResponse> {
    return this.http.post<RegistroAnualResponse>(`${this.apiUrl}/salvar`, dados);
  }

  editar(dados: RegistroAnualRequest): Observable<RegistroAnualResponse> {
    return this.http.put<RegistroAnualResponse>(`${this.apiUrl}/editar`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }
}