/**
 * Valores aceitos pela API para situação e tipo de acesso (enums SituacaoEscola e
 * TipoAcesso do backend). Fonte única para o formulário de escolas e o filtro do mapa.
 */
export interface OpcaoInfraestrutura {
  valor: string;
  rotulo: string;
}

export const SITUACOES_ESCOLA: readonly OpcaoInfraestrutura[] = [
  { valor: 'FUNCIONANDO', rotulo: 'Funcionando' },
  { valor: 'PARALISADA', rotulo: 'Paralisada' },
  { valor: 'DESATIVADA', rotulo: 'Desativada' },
];

export const TIPOS_ACESSO: readonly OpcaoInfraestrutura[] = [
  { valor: 'TERRESTRE', rotulo: 'Terrestre' },
  { valor: 'FLUVIAL', rotulo: 'Fluvial' },
  { valor: 'MISTO', rotulo: 'Misto' },
];
