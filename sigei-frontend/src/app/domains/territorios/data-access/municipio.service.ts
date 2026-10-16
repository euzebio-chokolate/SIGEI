import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { Municipio, MunicipioEstatistica } from '../models/municipio.models';
export type { Municipio, MunicipioEstatistica } from '../models/municipio.models';

@Injectable({ providedIn: 'root' })
export class MunicipioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/municipios`;

  listarEstatisticas(): Observable<MunicipioEstatistica[]> {
    return this.http.get<MunicipioEstatistica[]>(`${this.apiUrl}/estatisticas`);
  }

  listar(): Observable<Municipio[]> {
    return this.http.get<Municipio[]>(this.apiUrl);
  }

}
