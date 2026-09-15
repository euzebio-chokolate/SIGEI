import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '@environments/environment';

import type { LoginRequest, LoginResponse, AlterarSenhaRequest, UsuarioAutenticado } from './auth.models';
export type { LoginRequest, LoginResponse, AlterarSenhaRequest, UsuarioAutenticado } from './auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly chaveSenhaTemporaria = 'sigei_senha_temporaria';

  login(dados: LoginRequest): Observable<LoginResponse> {
    // Descarta uma sessão expirada antes de iniciar uma nova autenticação.
    localStorage.removeItem('sigei_token');
    localStorage.removeItem('sigei_usuario');

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, dados).pipe(
      tap(resposta => {
        const usuario = this.normalizarUsuario(resposta);
        localStorage.setItem('sigei_token', resposta.token);
        localStorage.setItem(
          'sigei_usuario',
          JSON.stringify(usuario)
        );
      })
    );
  }

  alterarSenha(dados: AlterarSenhaRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/alterar-senha`, dados);
  }

  getToken(): string | null {
    return localStorage.getItem('sigei_token');
  }

  getUsuario(): UsuarioAutenticado | null {
    const usuario = localStorage.getItem('sigei_usuario');
    if (!usuario) return null;
    try {
      const salvo = JSON.parse(usuario) as Partial<UsuarioAutenticado>;
      return {
        id: salvo.id ?? 0,
        nome: salvo.nome ?? '',
        email: salvo.email ?? '',
        perfil: salvo.perfil ?? '',
        alterarSenha: salvo.alterarSenha === true,
        authorities: Array.isArray(salvo.authorities) ? salvo.authorities : []
      };
    } catch {
      this.logout();
      return null;
    }
  }

  estaLogado(): boolean {
    return !!this.getToken();
  }

  deveAlterarSenha(): boolean {
    const usuario = this.getUsuario();
    return !!usuario?.alterarSenha || usuario?.authorities.includes('ROLE_TROCAR_SENHA') === true;
  }

  ehAdmin(): boolean {
    const usuario = this.getUsuario();
    if (!usuario || this.deveAlterarSenha()) return false;
    return usuario.perfil === 'ADMIN' || usuario.authorities.includes('ROLE_ADMIN');
  }

  guardarSenhaTemporaria(senha: string): void {
    sessionStorage.setItem(this.chaveSenhaTemporaria, senha);
  }

  consumirSenhaTemporaria(): string {
    const senha = sessionStorage.getItem(this.chaveSenhaTemporaria) ?? '';
    sessionStorage.removeItem(this.chaveSenhaTemporaria);
    return senha;
  }

  logout(): void {
    localStorage.removeItem('sigei_token');
    localStorage.removeItem('sigei_usuario');
    sessionStorage.removeItem(this.chaveSenhaTemporaria);
  }


  private normalizarUsuario(resposta: LoginResponse): UsuarioAutenticado {
    const claims = this.lerClaimsJwt(resposta.token);
    const authorities = this.extrairAuthorities(resposta.authorities ?? claims?.['authorities'] ?? claims?.['roles'] ?? claims?.['scope']);
    const perfil = resposta.perfil ?? this.perfilDasAuthorities(authorities);
    const alterarSenha = resposta.alterarSenha === true || claims?.['alterarSenha'] === true || authorities.includes('ROLE_TROCAR_SENHA');

    return {
      id: resposta.id,
      nome: resposta.nome,
      email: resposta.email,
      perfil,
      alterarSenha,
      authorities
    };
  }

  private lerClaimsJwt(token: string): Record<string, unknown> | null {
    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(decodeURIComponent(escape(atob(base64)))) as Record<string, unknown>;
    } catch {
      return null;
    }
  }

  private extrairAuthorities(valor: unknown): string[] {
    if (Array.isArray(valor)) return valor.filter((item): item is string => typeof item === 'string');
    if (typeof valor === 'string') return valor.split(/[ ,]+/).filter(Boolean);
    return [];
  }

  private perfilDasAuthorities(authorities: string[]): string {
    if (authorities.includes('ROLE_ADMIN')) return 'ADMIN';
    if (authorities.includes('ROLE_USUARIO')) return 'USUARIO';
    return '';
  }
}
