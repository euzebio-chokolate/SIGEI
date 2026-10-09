import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, shareReplay, tap, throwError } from 'rxjs';
import { environment } from '@environments/environment';

import type { AnoReferencia } from '../models/ano-referencia.models';
export type { AnoReferencia } from '../models/ano-referencia.models';

/** Tempo máximo de reaproveitamento, para enxergar anos criados por outro administrador. */
const VALIDADE_CACHE_MS = 5 * 60 * 1000;

@Injectable({ providedIn: 'root' })
export class AnoReferenciaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/anos`;

  /**
   * Os anos mudam raramente e são pedidos por várias telas (escolas, histórico,
   * professores, lotações); a lista é buscada uma vez e reaproveitada.
   */
  private cache$: Observable<AnoReferencia[]> | null = null;
  private carregadoEm = 0;

  listar(): Observable<AnoReferencia[]> {
    if (!this.cache$ || Date.now() - this.carregadoEm > VALIDADE_CACHE_MS) {
      this.carregadoEm = Date.now();
      this.cache$ = this.http.get<AnoReferencia[]>(this.apiUrl).pipe(
        // Falha não fica guardada: a próxima chamada tenta de novo
        catchError(erro => {
          this.invalidar();
          return throwError(() => erro);
        }),
        shareReplay(1)
      );
    }

    // Cópias, para uma tela que ordene ou altere a lista não afetar as outras
    return this.cache$.pipe(map(anos => anos.map(ano => ({ ...ano }))));
  }

  salvar(ano: number): Observable<AnoReferencia> {
    return this.http.post<AnoReferencia>(`${this.apiUrl}/salvar`, { ano }).pipe(
      tap(() => this.invalidar())
    );
  }

  /** Descarta a lista guardada; chamado quando um ano é criado. */
  invalidar(): void {
    this.cache$ = null;
  }
}
