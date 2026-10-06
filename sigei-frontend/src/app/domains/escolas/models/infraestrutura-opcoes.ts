/**
 * Valores aceitos pela API para situação, acesso, água e energia (enums
 * SituacaoEscola, TipoAcesso, TipoAgua e TipoEnergia do backend). Fonte única para
 * o formulário de escolas, o filtro do mapa e a exibição dos rótulos.
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

export const TIPOS_AGUA: readonly OpcaoInfraestrutura[] = [
  { valor: 'REDE_PUBLICA', rotulo: 'Rede pública' },
  { valor: 'POCO', rotulo: 'Poço' },
  { valor: 'CACIMBA', rotulo: 'Cacimba' },
  { valor: 'RIO', rotulo: 'Rio' },
  { valor: 'IGARAPE', rotulo: 'Igarapé' },
  { valor: 'VERTENTE', rotulo: 'Vertente' },
  { valor: 'RIO_VERTENTE', rotulo: 'Rio / Vertente' },
  { valor: 'OUTRO', rotulo: 'Outro' },
  { valor: 'NAO', rotulo: 'Não possui' },
];

export const TIPOS_ENERGIA: readonly OpcaoInfraestrutura[] = [
  { valor: 'REDE_PUBLICA', rotulo: 'Rede pública' },
  { valor: 'GERADOR', rotulo: 'Gerador' },
  { valor: 'SOLAR', rotulo: 'Solar' },
  { valor: 'OUTRO', rotulo: 'Outro' },
  { valor: 'NAO', rotulo: 'Não possui' },
];

/** Rótulo de exibição de um valor gravado; valores desconhecidos aparecem como estão. */
export function rotuloOpcao(
  opcoes: readonly OpcaoInfraestrutura[],
  valor: string | null | undefined
): string {
  if (!valor) return '-';
  return opcoes.find(opcao => opcao.valor === valor)?.rotulo ?? valor;
}
