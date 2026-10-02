import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, map, Observable, shareReplay, tap } from 'rxjs';
import { environment } from '@environments/environment';

import type { LoginRequest, LoginResponse, AlterarSenhaRequest, UsuarioAutenticado } from './auth.models';
export type { LoginRequest, LoginResponse, AlterarSenhaRequest, UsuarioAutenticado } from './auth.models';

/**
 * Os tokens de acesso e de renovação ficam em cookies HttpOnly gravados pelo
 * backend e nunca passam pelo JavaScript. O navegador guarda aqui apenas os
 * dados de exibição do usuário, usados pelos guards de rota; a autorização
 * efetiva é sempre decidida pelo backend.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly chaveUsuario = 'sigei_usuario';
  private readonly chaveSenhaTemporaria = 'sigei_senha_temporaria';

  /** Renovação em andamento, compartilhada pelas requisições que receberem 401 ao mesmo tempo. */
  private renovacao$: Observable<void> | null = null;

  constructor() {
    // Remove tokens gravados por versões anteriores, que usavam o localStorage
    localStorage.removeItem('sigei_token');
    localStorage.removeItem('sigei_refresh_token');
  }

  login(dados: LoginRequest): Observable<LoginResponse> {
    // Descarta uma sessão expirada antes de iniciar uma nova autenticação.
    this.limparSessao();

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, dados).pipe(
      tap(resposta => this.guardarUsuario(resposta))
    );
  }

  /**
   * Pede ao backend novos cookies de sessão a partir do cookie de renovação.
   * Chamadas simultâneas reaproveitam a mesma requisição, pois cada refresh token só vale uma vez.
   */
  renovarSessao(): Observable<void> {
    if (this.renovacao$) return this.renovacao$;

    this.renovacao$ = this.http.post<LoginResponse>(`${this.apiUrl}/refresh`, null).pipe(
      tap(resposta => this.guardarUsuario(resposta)),
      map(() => undefined),
      finalize(() => this.renovacao$ = null),
      shareReplay(1)
    );

    return this.renovacao$;
  }

  alterarSenha(dados: AlterarSenhaRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/alterar-senha`, dados);
  }

  getUsuario(): UsuarioAutenticado | null {
    const usuario = localStorage.getItem(this.chaveUsuario);
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

  /**
   * Indica se há uma sessão iniciada neste navegador. Os cookies HttpOnly não
   * são legíveis; se a sessão tiver expirado, a primeira requisição recusada
   * tenta renová-la e, se não conseguir, encerra a sessão local.
   */
  estaLogado(): boolean {
    return this.getUsuario() !== null;
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
    // Revoga a sessão e remove os cookies no servidor; falhas de rede não impedem a saída local
    this.http.post<void>(`${this.apiUrl}/logout`, null).subscribe({ error: () => undefined });
    this.limparSessao();
  }

  /** Remove a sessão apenas do navegador, sem avisar o servidor. */
  limparSessao(): void {
    localStorage.removeItem(this.chaveUsuario);
    sessionStorage.removeItem(this.chaveSenhaTemporaria);
  }

  private guardarUsuario(resposta: LoginResponse): void {
    localStorage.setItem(this.chaveUsuario, JSON.stringify(this.normalizarUsuario(resposta)));
  }

  private normalizarUsuario(resposta: LoginResponse): UsuarioAutenticado {
    const authorities = this.extrairAuthorities(resposta.authorities);
    const perfil = resposta.perfil ?? this.perfilDasAuthorities(authorities);
    const alterarSenha = resposta.alterarSenha === true || authorities.includes('ROLE_TROCAR_SENHA');

    return {
      id: resposta.id,
      nome: resposta.nome,
      email: resposta.email,
      perfil,
      alterarSenha,
      authorities
    };
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
