import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { AnoReferencia } from '../models/ano-referencia.models';
export type { AnoReferencia } from '../models/ano-referencia.models';

@Injectable({ providedIn: 'root' })
export class AnoReferenciaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/anos`;

  listar(): Observable<AnoReferencia[]> {
    return this.http.get<AnoReferencia[]>(this.apiUrl);
  }

  salvar(ano: number): Observable<AnoReferencia> {
    return this.http.post<AnoReferencia>(`${this.apiUrl}/salvar`, { ano });
  }
}