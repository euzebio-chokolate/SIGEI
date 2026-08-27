import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AnoReferencia {
  id: number;
  ano: number;
}

@Injectable({ providedIn: 'root' })
export class AnoReferenciaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/anos`;

  listar(): Observable<AnoReferencia[]> {
    return this.http.get<AnoReferencia[]>(this.apiUrl);
  }
}