import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import { lerErroDoDownload } from '@core/http/download';

import type {
  ImportacaoEscolasResultado,
  ImportacaoMatriculasResultado,
  ImportacaoProfessoresResultado,
  LayoutImportacao,
  ResultadoImportacao,
  TipoImportacao
} from '../models/importacao.models';
export type {
  ImportacaoEscolasResultado,
  ImportacaoMatriculasResultado,
  ImportacaoProfessoresResultado,
  LayoutImportacao,
  ResultadoImportacao,
  TipoImportacao
} from '../models/importacao.models';

@Injectable({ providedIn: 'root' })
export class ImportacaoService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/importacoes`;

  /** Colunas aceitas e instruções de cada tipo de importação. */
  listarLayouts(): Observable<LayoutImportacao[]> {
    return this.http.get<LayoutImportacao[]>(`${this.apiUrl}/layouts`);
  }

  /** Planilha modelo (.xlsx) com o cabeçalho e uma aba de instruções. */
  baixarModelo(tipo: TipoImportacao): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/modelos/${tipo}`, { responseType: 'blob' }).pipe(lerErroDoDownload());
  }

  /**
   * Envia a planilha para o ano informado. A resposta 200 traz contadores e as
   * linhas não importadas; falhas da planilha inteira (colunas obrigatórias
   * ausentes, ano não iniciado) voltam como 400 com o motivo em texto.
   */
  importar(tipo: 'infraestrutura', arquivo: File, ano: number): Observable<ImportacaoEscolasResultado>;
  importar(tipo: 'matriculas', arquivo: File, ano: number): Observable<ImportacaoMatriculasResultado>;
  importar(tipo: 'professores', arquivo: File, ano: number): Observable<ImportacaoProfessoresResultado>;
  importar(tipo: TipoImportacao, arquivo: File, ano: number): Observable<ResultadoImportacao>;
  importar(tipo: TipoImportacao, arquivo: File, ano: number): Observable<ResultadoImportacao> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    return this.http.post<ResultadoImportacao>(`${this.apiUrl}/${tipo}`, formData, { params: { ano } });
  }
}
