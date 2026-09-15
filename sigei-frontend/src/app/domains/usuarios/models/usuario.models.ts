export type PerfilUsuario = 'ADMIN' | 'USUARIO';

export interface UsuarioResponse {
  id: number;
  nome: string;
  email: string;
  perfil: PerfilUsuario;
  ativo: boolean;
}

export interface UsuarioRequest {
  id?: number;
  nome: string;
  email: string;
  senha: string;
  perfil: PerfilUsuario;
  ativo: boolean;
}
