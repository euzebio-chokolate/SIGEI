import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Municipio {
  id: number;
  nome: string;
}

export interface MunicipioRequest {
  nome: string;
}

export interface MunicipioEstatistica {
  id: number;
  nome: string;
  quantidadeTerrasIndigenas: number;
  quantidadeAldeias: number;
  quantidadeEscolas: number;
}

@Injectable({
  providedIn: 'root'
})
export class MunicipioService {

  private readonly apiUrl = 'http://localhost:8080/municipio';

  private http = inject(HttpClient);

  listarEstatisticas(): Observable<MunicipioEstatistica[]> {
    return this.http.get<MunicipioEstatistica[]>(
      `${this.apiUrl}/estatisticas`
    );
  }

  listar(): Observable<Municipio[]> {
    return this.http.get<Municipio[]>(
      `${this.apiUrl}/buscarTudo`
    );
  }

  buscarPorId(id: number): Observable<Municipio> {
    return this.http.get<Municipio>(
      `${this.apiUrl}/${id}`
    );
  }

  salvar(dados: MunicipioRequest): Observable<Municipio> {
    return this.http.post<Municipio>(
      this.apiUrl,
      dados
    );
  }

  editar(id: number, dados: MunicipioRequest): Observable<Municipio> {
    return this.http.put<Municipio>(
      `${this.apiUrl}/${id}`,
      dados
    );
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }
}