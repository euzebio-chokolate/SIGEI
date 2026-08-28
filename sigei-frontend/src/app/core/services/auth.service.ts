import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  id: number;
  nome: string;
  email: string;
  perfil: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  login(dados: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, dados).pipe(
      tap(usuario => {
        localStorage.setItem('sigei_token', usuario.token);
        localStorage.setItem(
          'sigei_usuario',
          JSON.stringify({
            id: usuario.id,
            nome: usuario.nome,
            email: usuario.email,
            perfil: usuario.perfil
          })
        );
      })
    );
  }

  getToken(): string | null {
    return localStorage.getItem('sigei_token');
  }

  getUsuario(): Omit<LoginResponse, 'token'> | null {
    const usuario = localStorage.getItem('sigei_usuario');
    if (!usuario) return null;
    return JSON.parse(usuario);
  }

  estaLogado(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    localStorage.removeItem('sigei_token');
    localStorage.removeItem('sigei_usuario');
  }
}