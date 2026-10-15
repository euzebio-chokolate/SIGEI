import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '@environments/environment';

import { lerErroDoDownload, nomeDoArquivo, salvarArquivo } from '@core/http/download';
import type { FiltroRelatorio, FormatoRelatorio, TipoRelatorio } from '../models/relatorio.models';
export type { FiltroRelatorio, FormatoRelatorio, TipoRelatorio } from '../models/relatorio.models';

@Injectable({ providedIn: 'root' })
export class RelatorioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/relatorios`;

  /** Gera o relatório no backend e o oferece para salvar com o nome sugerido pela API. */
  baixar(tipo: TipoRelatorio, formato: FormatoRelatorio, filtro: FiltroRelatorio = {}): Observable<string> {
    let params = new HttpParams().set('formato', formato);
    if (filtro.ano) params = params.set('ano', filtro.ano);
    if (filtro.municipioId) params = params.set('municipioId', filtro.municipioId);

    return this.http.get(`${this.apiUrl}/${tipo}`, { params, responseType: 'blob', observe: 'response' }).pipe(
      lerErroDoDownload(),
      map(resposta => {
        const nome = nomeDoArquivo(resposta, `sigei-${tipo}.${formato}`);
        salvarArquivo(resposta.body ?? new Blob(), nome);
        return nome;
      })
    );
  }
}
