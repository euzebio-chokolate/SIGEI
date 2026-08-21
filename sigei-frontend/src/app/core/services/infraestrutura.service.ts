import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

export interface EscolaResumo {
  id: number;
  codigo: string;
  nome: string;
}

export interface InfraestruturaEscola {
  id: number;
  escola: EscolaResumo;
  anoConstrucao: number;
  numSalas: number;
  latitude: number;
  longitude: number;
  situacao: string;
  tipoAcesso: string;
  temPredio: boolean;
  tipoAgua: string;
  temInternet: boolean;
  tipoEnergia: string;
  temRegistroFotografico: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class InfraestruturaService {
  private readonly apiUrl = `${environment.apiUrl}/infraestrutura`;

  private http = inject(HttpClient);

  listarTodas(): Observable<InfraestruturaEscola[]> {
    return this.http.get<InfraestruturaEscola[]>(this.apiUrl);
  }
}
