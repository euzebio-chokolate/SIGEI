export interface NivelMatriculaConfig {
  nivel: string;
  descricao: string;
}

export interface SerieMatriculaConfig {
  denominacao: string;
  etapaEnsino: string;
}

export const NIVEIS_MATRICULA: readonly NivelMatriculaConfig[] = [
  { nivel: 'ENSINO_INFANTIL', descricao: 'Ensino Infantil' },
  { nivel: 'FUNDAMENTAL_ANOS_INICIAIS', descricao: 'Fundamental - Anos Iniciais' },
  { nivel: 'FUNDAMENTAL_ANOS_FINAIS', descricao: 'Fundamental - Anos Finais' },
  { nivel: 'ENSINO_MEDIO', descricao: 'Ensino Médio' },
  { nivel: 'EJA', descricao: 'EJA' },
  { nivel: 'AEE', descricao: 'AEE' }
];

export const SERIES_MATRICULA: readonly SerieMatriculaConfig[] = [
  { denominacao: '1º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_INICIAIS' },
  { denominacao: '2º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_INICIAIS' },
  { denominacao: '3º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_INICIAIS' },
  { denominacao: '4º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_INICIAIS' },
  { denominacao: '5º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_INICIAIS' },
  { denominacao: '6º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_FINAIS' },
  { denominacao: '7º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_FINAIS' },
  { denominacao: '8º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_FINAIS' },
  { denominacao: '9º ANO', etapaEnsino: 'FUNDAMENTAL_ANOS_FINAIS' },
  { denominacao: '1ª SÉRIE', etapaEnsino: 'ENSINO_MEDIO' },
  { denominacao: '2ª SÉRIE', etapaEnsino: 'ENSINO_MEDIO' },
  { denominacao: '3ª SÉRIE', etapaEnsino: 'ENSINO_MEDIO' },
  { denominacao: 'CICLO 1', etapaEnsino: 'EJA' },
  { denominacao: 'CICLO 2', etapaEnsino: 'EJA' }
];
