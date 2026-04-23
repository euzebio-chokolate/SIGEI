import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class EscolaService {

  private api = 'http://localhost:8080/escolas';

  constructor(private http: HttpClient) {}

  listar() {
    return this.http.get<any[]>(this.api);
  }
}