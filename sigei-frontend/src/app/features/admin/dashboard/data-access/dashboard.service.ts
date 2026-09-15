import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { DashboardResumo, DashboardEvolucao, DashboardItem, DashboardInfraestrutura, DashboardResponse } from '../models/dashboard.models';
export type { DashboardResumo, DashboardEvolucao, DashboardItem, DashboardInfraestrutura, DashboardResponse } from '../models/dashboard.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;

  buscar(ano?: number): Observable<DashboardResponse> {
    const params = ano === undefined ? undefined : new HttpParams().set('ano', ano);
    return this.http.get<DashboardResponse>(this.apiUrl, { params });
  }
}
