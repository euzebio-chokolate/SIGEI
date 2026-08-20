import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interface para os dados resumidos da escola que vêm dentro da infraestrutura
export interface EscolaResumo {
  id: number;
  codigo: string;
  nome: string;
}

// Interface principal do JSON de Infraestrutura
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
  providedIn: 'root'
})
export class InfraestruturaService {
  
  private apiUrl = 'http://localhost:8080/infraestrutura';
  private http = inject(HttpClient); // Sintaxe moderna do Angular 17+

  listarTodas(): Observable<InfraestruturaEscola[]> {
    return this.http.get<InfraestruturaEscola[]>(this.apiUrl);
  }
}