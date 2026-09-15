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
  alterarSenha: boolean;
  authorities?: string[];
}

export interface AlterarSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
  confirmacaoSenha: string;
}

export interface UsuarioAutenticado extends Omit<LoginResponse, 'token'> {
  alterarSenha: boolean;
  authorities: string[];
}
