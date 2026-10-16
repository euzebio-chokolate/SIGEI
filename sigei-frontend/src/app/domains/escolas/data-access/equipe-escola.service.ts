import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { EquipeEscolaRequest, EquipeEscolaResponse } from '../models/equipe-escola.models';
export type { EquipeEscolaRequest, EquipeEscolaResponse } from '../models/equipe-escola.models';

@Injectable({ providedIn: 'root' })
export class EquipeEscolaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/equipes`;

  salvar(dados: EquipeEscolaRequest): Observable<EquipeEscolaResponse> {
    return this.http.post<EquipeEscolaResponse>(this.apiUrl, dados);
  }

  editar(id: number, dados: EquipeEscolaRequest): Observable<EquipeEscolaResponse> {
    return this.http.put<EquipeEscolaResponse>(`${this.apiUrl}/${id}`, dados);
  }
}