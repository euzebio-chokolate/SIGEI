import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { FiltroAuditoria, PaginaAuditoria } from '../models/auditoria.models';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auditoria`;

  /** Alterações de todo o sistema, da mais recente à mais antiga. Somente ADMIN. */
  buscar(filtro: FiltroAuditoria, pagina: number, tamanho: number): Observable<PaginaAuditoria> {
    let params = new HttpParams().set('pagina', pagina).set('tamanho', tamanho);
    for (const [chave, valor] of Object.entries(filtro)) {
      if (valor !== null && valor !== undefined && valor !== '' && valor !== false) {
        params = params.set(chave, String(valor));
      }
    }
    return this.http.get<PaginaAuditoria>(this.apiUrl, { params });
  }
}
