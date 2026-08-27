import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AtualizacaoAnualService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/atualizacao-anual`;

  iniciar(): Observable<any> {
    return this.http.post(`${this.apiUrl}/iniciar`, {});
  }
}