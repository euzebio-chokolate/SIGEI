import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { EscolaResumo, AnoReferenciaResumo, InfraestruturaRequest, InfraestruturaResponse } from '../models/infraestrutura.models';
export type { EscolaResumo, AnoReferenciaResumo, InfraestruturaRequest, InfraestruturaResponse } from '../models/infraestrutura.models';

@Injectable({ providedIn: 'root' })
export class InfraestruturaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/infraestruturas`;

  listarTodas(): Observable<InfraestruturaResponse[]> {
    return this.http.get<InfraestruturaResponse[]>(this.apiUrl);
  }

  /** Uma infraestrutura por escola: a do registro anual mais recente. */
  listarMaisRecentes(): Observable<InfraestruturaResponse[]> {
    return this.http.get<InfraestruturaResponse[]>(`${this.apiUrl}/mais-recentes`);
  }

  salvar(dados: InfraestruturaRequest): Observable<InfraestruturaResponse> {
    return this.http.post<InfraestruturaResponse>(this.apiUrl, dados);
  }

  adicionarFoto(
    infraestruturaId: number,
    foto: File
  ): Observable<InfraestruturaResponse> {
    const formData = new FormData();
    formData.append('foto', foto);

    return this.http.post<InfraestruturaResponse>(
      `${this.apiUrl}/${infraestruturaId}/foto`,
      formData
    );
  }

  editar(id: number, dados: InfraestruturaRequest): Observable<InfraestruturaResponse> {
    return this.http.put<InfraestruturaResponse>(`${this.apiUrl}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
