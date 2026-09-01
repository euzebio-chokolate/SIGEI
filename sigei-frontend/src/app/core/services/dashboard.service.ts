import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DashboardResumo { totalEscolas: number; totalMatriculas: number; totalMunicipios: number; totalTerrasIndigenas: number; }
export interface DashboardEvolucao { ano: number; total: number; }
export interface DashboardItem { descricao: string; total: number; }
export interface DashboardInfraestrutura { totalAvaliado: number; comInternet: number; percentualInternet: number; comEnergia: number; percentualEnergia: number; comPredio: number; percentualPredio: number; }
export interface DashboardResponse {
  anoReferencia: number;
  resumo: DashboardResumo;
  evolucaoMatriculas: DashboardEvolucao[];
  evolucaoEscolas: DashboardEvolucao[];
  matriculasPorNivel: DashboardItem[];
  infraestrutura: DashboardInfraestrutura;
  escolasPorMunicipio: DashboardItem[];
  situacaoEscolas: DashboardItem[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;

  buscar(ano?: number): Observable<DashboardResponse> {
    const params = ano === undefined ? undefined : new HttpParams().set('ano', ano);
    return this.http.get<DashboardResponse>(this.apiUrl, { params });
  }
}
