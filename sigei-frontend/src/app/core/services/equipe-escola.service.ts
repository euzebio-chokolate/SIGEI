import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface EquipeEscolaRequest {
  id?: number;
  registroAnualId: number;
  temProfessorResponsavel: boolean;
  temEquipeGestora: boolean;
  temMerendeira: boolean;
  temZeladora: boolean;
  temBarqueiro: boolean;
  necessitaTransporte: boolean;
}

export interface EquipeEscolaResponse {
  id: number;
  temProfessorResponsavel: boolean;
  temEquipeGestora: boolean;
  temMerendeira: boolean;
  temZeladora: boolean;
  temBarqueiro: boolean;
  necessitaTransporte: boolean;
}

@Injectable({ providedIn: 'root' })
export class EquipeEscolaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/equipe`;

  salvar(dados: EquipeEscolaRequest): Observable<EquipeEscolaResponse> {
    return this.http.post<EquipeEscolaResponse>(`${this.apiUrl}/salvar`, dados);
  }
}