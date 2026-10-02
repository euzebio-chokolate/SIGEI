export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  id: number;
  nome: string;
  email: string;
  perfil: string;
  alterarSenha: boolean;
  authorities?: string[];
}

export interface AlterarSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
  confirmacaoSenha: string;
}

export interface UsuarioAutenticado extends LoginResponse {
  alterarSenha: boolean;
  authorities: string[];
}
