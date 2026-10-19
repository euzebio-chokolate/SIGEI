/**
 * Conversão entre graus decimais (o formato gravado pela API) e graus, minutos
 * e segundos, com as mesmas regras do CoordenadaUtils do backend: minutos de 0
 * a 59, segundos de 0 a 59,999, sul e oeste negativos e 7 casas decimais.
 */

export type TipoCoordenada = 'latitude' | 'longitude';

/** Direções aceitas, na grafia das planilhas (O = oeste, L = leste). */
export type Direcao = 'N' | 'S' | 'L' | 'O';

export interface CoordenadaGms {
  graus: number | null;
  minutos: number | null;
  segundos: number | null;
  direcao: Direcao;
}

export interface ResultadoConversao {
  valor: number | null;
  erro: string | null;
}

export const DIRECOES: Record<TipoCoordenada, { valor: Direcao; rotulo: string }[]> = {
  latitude: [
    { valor: 'S', rotulo: 'S (Sul)' },
    { valor: 'N', rotulo: 'N (Norte)' }
  ],
  longitude: [
    { valor: 'O', rotulo: 'O (Oeste)' },
    { valor: 'L', rotulo: 'L (Leste)' }
  ]
};

const LIMITE: Record<TipoCoordenada, number> = { latitude: 90, longitude: 180 };
const NOME: Record<TipoCoordenada, string> = { latitude: 'A latitude', longitude: 'A longitude' };

/** O Acre fica ao sul e a oeste, então essas são as direções iniciais. */
function direcaoPadrao(tipo: TipoCoordenada): Direcao {
  return tipo === 'latitude' ? 'S' : 'O';
}

function negativa(direcao: Direcao): boolean {
  return direcao === 'S' || direcao === 'O';
}

/**
 * Converte graus, minutos e segundos em graus decimais. Tudo vazio resulta em
 * nulo, sem erro; minutos e segundos vazios contam como zero.
 */
export function gmsParaDecimal(gms: CoordenadaGms, tipo: TipoCoordenada): ResultadoConversao {
  const { graus, minutos, segundos, direcao } = gms;

  if (graus === null && minutos === null && segundos === null) {
    return { valor: null, erro: null };
  }
  if (graus === null) {
    return { valor: null, erro: 'Informe os graus.' };
  }
  if (!Number.isInteger(graus) || graus < 0 || graus > LIMITE[tipo]) {
    return { valor: null, erro: `Os graus devem ser um número inteiro de 0 a ${LIMITE[tipo]}.` };
  }

  const min = minutos ?? 0;
  if (!Number.isInteger(min) || min < 0 || min >= 60) {
    return { valor: null, erro: 'Os minutos devem ser um número inteiro de 0 a 59.' };
  }

  const seg = segundos ?? 0;
  if (!Number.isFinite(seg) || seg < 0 || seg >= 60) {
    return { valor: null, erro: 'Os segundos devem estar entre 0 e 59,999.' };
  }

  if (!DIRECOES[tipo].some(opcao => opcao.valor === direcao)) {
    return {
      valor: null,
      erro: `${NOME[tipo]} precisa ser ${DIRECOES[tipo].map(opcao => opcao.valor).join(' ou ')}.`
    };
  }

  const absoluto = graus + min / 60 + seg / 3600;
  if (absoluto > LIMITE[tipo]) {
    return { valor: null, erro: `${NOME[tipo]} não pode ultrapassar ${LIMITE[tipo]} graus.` };
  }

  const valor = Number(absoluto.toFixed(7));
  return { valor: negativa(direcao) && valor !== 0 ? -valor : valor, erro: null };
}

/** Decompõe graus decimais em graus, minutos e segundos (segundos com 2 casas). */
export function decimalParaGms(valor: number | null | undefined, tipo: TipoCoordenada): CoordenadaGms {
  if (valor === null || valor === undefined || !Number.isFinite(valor)) {
    return { graus: null, minutos: null, segundos: null, direcao: direcaoPadrao(tipo) };
  }

  const direcao: Direcao = valor < 0
    ? (tipo === 'latitude' ? 'S' : 'O')
    : (tipo === 'latitude' ? 'N' : 'L');

  // Trabalha em centésimos de segundo para que o arredondamento não gere 60".
  const centesimos = Math.round(Math.abs(valor) * 3600 * 100);
  const graus = Math.floor(centesimos / (3600 * 100));
  const minutos = Math.floor((centesimos % (3600 * 100)) / (60 * 100));
  const segundos = (centesimos % (60 * 100)) / 100;

  return { graus, minutos, segundos, direcao };
}

/** Texto como 9°58'29,03" S, para exibir ao lado do valor decimal. */
export function formatarGms(valor: number | null | undefined, tipo: TipoCoordenada): string | null {
  if (valor === null || valor === undefined || !Number.isFinite(valor) || Math.abs(valor) > LIMITE[tipo]) {
    return null;
  }

  const { graus, minutos, segundos, direcao } = decimalParaGms(valor, tipo);
  const textoSegundos = String(segundos).replace('.', ',');
  return `${graus}°${minutos}'${textoSegundos}" ${direcao}`;
}
