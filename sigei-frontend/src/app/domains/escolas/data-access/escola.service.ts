import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { MunicipioResumo, PovoResumo, TerraIndigenaResumo, AldeiaResumo, EscolaResumo, EscolaResponse, AnoReferenciaResumo, EquipeEscola, InfraestruturaEscolaDetalhe, MatriculaNivel, MatriculaSerie, RegistroAnualDetalhe, EscolaDetalheResponse } from '../models/escola.models';
export type { MunicipioResumo, PovoResumo, TerraIndigenaResumo, AldeiaResumo, EscolaResumo, EscolaResponse, AnoReferenciaResumo, EquipeEscola, InfraestruturaEscolaDetalhe, MatriculaNivel, MatriculaSerie, RegistroAnualDetalhe, EscolaDetalheResponse } from '../models/escola.models';

@Injectable({ providedIn: 'root' })
export class EscolaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/escola`;

  salvar(
    dados: {
      codigo: string;
      nome: string;
      tipoUnidade: string;
      escolaSedeId: number | null;
      aldeiaId: number;
    }
  ): Observable<EscolaResponse> {
    return this.http.post<EscolaResponse>(`${this.apiUrl}/salvar`, dados);
  }

  listar(): Observable<EscolaResponse[]> {
    return this.http.get<EscolaResponse[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<EscolaDetalheResponse> {
    return this.http.get<EscolaDetalheResponse>(`${this.apiUrl}/${id}`);
  }

  editar(
    dados: {
      id?: number;
      codigo: string;
      nome: string;
      tipoUnidade: string;
      escolaSedeId: number | null;
      aldeiaId: number;
    }
    ): Observable<EscolaResponse> {
        return this.http.put<EscolaResponse>(`${this.apiUrl}/editar`,dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }
}