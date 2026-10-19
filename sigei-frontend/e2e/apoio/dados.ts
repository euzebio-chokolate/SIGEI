/** Dados de exemplo das respostas da API, no formato dos DTOs do backend. */

export interface UsuarioDeTeste {
  id: number;
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'USUARIO';
  alterarSenha: boolean;
}

export const USUARIOS = {
  admin: { id: 1, nome: 'Ana Administradora', email: 'admin@sigei.com', perfil: 'ADMIN', alterarSenha: false },
  usuario: { id: 2, nome: 'Uriel Consulta', email: 'usuario@sigei.com', perfil: 'USUARIO', alterarSenha: false },
  senhaTemporaria: { id: 3, nome: 'Novo Usuário', email: 'novo@sigei.com', perfil: 'USUARIO', alterarSenha: true }
} satisfies Record<string, UsuarioDeTeste>;

export const MUNICIPIO = { id: 7, nome: 'Feijó' };
export const TERRA = { id: 11, nome: 'Alto Rio Envira', municipio: MUNICIPIO };
export const POVO = { id: 5, nome: 'Ashaninka' };

export const ALDEIAS = [
  { id: 21, nome: 'Alto Bonito', terraIndigena: TERRA, povo: POVO },
  { id: 22, nome: 'Coqueiro', terraIndigena: TERRA, povo: POVO }
];

export const ANOS = [
  { id: 2, ano: 2025 },
  { id: 1, ano: 2024 }
];

export const DASHBOARD = {
  anoReferencia: 2025,
  resumo: { totalEscolas: 245, totalMatriculas: 9876, totalMunicipios: 12, totalTerrasIndigenas: 30 },
  evolucaoMatriculas: [{ ano: 2024, total: 9500 }, { ano: 2025, total: 9876 }],
  evolucaoEscolas: [{ ano: 2024, total: 240 }, { ano: 2025, total: 245 }],
  matriculasPorNivel: [{ descricao: 'FUNDAMENTAL_I', total: 4000 }],
  infraestrutura: {
    totalAvaliado: 245, comInternet: 80, percentualInternet: 32.65,
    comEnergia: 200, percentualEnergia: 81.63, comPredio: 190, percentualPredio: 77.55
  },
  escolasPorMunicipio: [{ descricao: 'Feijó', total: 40 }],
  situacaoEscolas: [{ descricao: 'FUNCIONANDO', total: 230 }],
  professores: { totalProfessores: 500, totalLotacoes: 600, vagasEmAberto: 10, lotacoesNoNucleo: 5, porTipoContrato: [] }
};

function infraestrutura(id: number, nome: string, latitude: number | null, longitude: number | null, situacao = 'FUNCIONANDO') {
  return {
    id,
    escola: { id: 100 + id, codigo: `1200000${id}`, nome },
    anoReferencia: ANOS[0],
    anoConstrucao: 2015, numSalas: 2, latitude, longitude,
    situacao, tipoAcesso: 'FLUVIAL', temPredio: true, tipoAgua: 'RIO',
    temInternet: false, tipoEnergia: 'SOLAR', temRegistroFotografico: false, fotoUrl: null
  };
}

/** Escolas do mapa: duas com coordenadas e uma sem (que não vira marcador). */
export const ESCOLAS_MAPA = [
  infraestrutura(1, 'Escola Indígena Alto Bonito', -8.1585, -70.3521),
  infraestrutura(2, 'Escola Indígena Coqueiro', -8.2001, -70.4012, 'PARALISADA'),
  infraestrutura(3, 'Escola Indígena Sem Coordenadas', null, null)
];

export const LAYOUTS = [
  { tipo: 'infraestrutura', titulo: 'Escolas e infraestrutura', instrucoes: ['Cada linha é uma escola.'], colunas: [
    { titulo: 'CÓDIGO', obrigatoria: true, descricao: 'Código INEP da escola.', sinonimos: ['INEP'] }
  ] },
  { tipo: 'matriculas', titulo: 'Matrículas', instrucoes: [], colunas: [] },
  { tipo: 'professores', titulo: 'Quadro de professores', instrucoes: [], colunas: [] }
];

export const RESULTADO_IMPORTACAO_ESCOLAS = {
  linhasLidas: 3, municipiosCriados: 0, terrasIndigenasCriadas: 0, povosCriados: 0,
  aldeiasCriadas: 1, escolasCriadas: 2, registrosAnuaisCriados: 2, infraestruturasCriadas: 2,
  equipesCriadas: 2, registrosAtualizados: 0, linhasIgnoradas: 1,
  erros: ['Linha 4: Município não cadastrado: Xapuri Velho']
};
