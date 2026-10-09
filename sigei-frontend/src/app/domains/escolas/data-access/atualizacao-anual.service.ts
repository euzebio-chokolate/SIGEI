import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '@environments/environment';

import { AnoReferenciaService } from './ano-referencia.service';

@Injectable({ providedIn: 'root' })
export class AtualizacaoAnualService {
  private readonly http = inject(HttpClient);
  private readonly anoReferenciaService = inject(AnoReferenciaService);
  private readonly apiUrl = `${environment.apiUrl}/atualizacao-anual`;

  iniciar(): Observable<string> {
    return this.http.post(`${this.apiUrl}/iniciar`, {}, { responseType: 'text' }).pipe(
      // O novo ano passa a existir no backend: a lista de anos guardada fica desatualizada
      tap(() => this.anoReferenciaService.invalidar())
    );
  }
}
