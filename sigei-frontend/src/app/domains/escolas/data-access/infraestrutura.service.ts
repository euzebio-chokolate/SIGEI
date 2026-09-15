import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { EscolaResumo, AnoReferenciaResumo, InfraestruturaRequest, InfraestruturaResponse } from '../models/infraestrutura.models';
export type { EscolaResumo, AnoReferenciaResumo, InfraestruturaRequest, InfraestruturaResponse } from '../models/infraestrutura.models';

@Injectable({ providedIn: 'root' })
export class InfraestruturaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/infraestrutura`;

  listarTodas(): Observable<InfraestruturaResponse[]> {
    return this.http.get<InfraestruturaResponse[]>(this.apiUrl);
  }

  salvar(dados: InfraestruturaRequest): Observable<InfraestruturaResponse> {
    return this.http.post<InfraestruturaResponse>(`${this.apiUrl}/salvar`, dados);
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

  excluirFoto(infraestruturaId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${infraestruturaId}/foto`
    );
  }

  editar(dados: InfraestruturaRequest): Observable<InfraestruturaResponse> {
    return this.http.put<InfraestruturaResponse>(`${this.apiUrl}/editar`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }
}
