import { decimalParaGms, formatarGms, gmsParaDecimal } from './coordenadas';

describe('gmsParaDecimal', () => {
  it('converte sul e oeste em valores negativos, com 7 casas', () => {
    expect(gmsParaDecimal({ graus: 9, minutos: 58, segundos: 29, direcao: 'S' }, 'latitude'))
      .toEqual({ valor: -9.9747222, erro: null });
    expect(gmsParaDecimal({ graus: 69, minutos: 56, segundos: 56.4, direcao: 'O' }, 'longitude'))
      .toEqual({ valor: -69.949, erro: null });
  });

  it('mantém norte e leste positivos', () => {
    expect(gmsParaDecimal({ graus: 10, minutos: 30, segundos: 0, direcao: 'N' }, 'latitude').valor).toBe(10.5);
    expect(gmsParaDecimal({ graus: 10, minutos: 30, segundos: 0, direcao: 'L' }, 'longitude').valor).toBe(10.5);
  });

  it('trata tudo vazio como coordenada não informada', () => {
    expect(gmsParaDecimal({ graus: null, minutos: null, segundos: null, direcao: 'S' }, 'latitude'))
      .toEqual({ valor: null, erro: null });
  });

  it('considera minutos e segundos vazios como zero', () => {
    expect(gmsParaDecimal({ graus: 9, minutos: null, segundos: null, direcao: 'S' }, 'latitude').valor).toBe(-9);
  });

  it('exige os graus quando minutos ou segundos foram preenchidos', () => {
    expect(gmsParaDecimal({ graus: null, minutos: 10, segundos: null, direcao: 'S' }, 'latitude').erro)
      .toBe('Informe os graus.');
  });

  it('recusa minutos e segundos fora de 0 a 59', () => {
    expect(gmsParaDecimal({ graus: 9, minutos: 60, segundos: 0, direcao: 'S' }, 'latitude').erro)
      .toBe('Os minutos devem ser um número inteiro de 0 a 59.');
    expect(gmsParaDecimal({ graus: 9, minutos: 1, segundos: 60, direcao: 'S' }, 'latitude').erro)
      .toBe('Os segundos devem estar entre 0 e 59,999.');
  });

  it('recusa valores acima do limite de cada tipo', () => {
    expect(gmsParaDecimal({ graus: 91, minutos: 0, segundos: 0, direcao: 'S' }, 'latitude').erro)
      .toBe('Os graus devem ser um número inteiro de 0 a 90.');
    expect(gmsParaDecimal({ graus: 90, minutos: 0, segundos: 1, direcao: 'S' }, 'latitude').erro)
      .toBe('A latitude não pode ultrapassar 90 graus.');
    expect(gmsParaDecimal({ graus: 180, minutos: 0, segundos: 0, direcao: 'O' }, 'longitude').valor).toBe(-180);
  });

  it('recusa direção que não corresponde ao tipo', () => {
    expect(gmsParaDecimal({ graus: 9, minutos: 0, segundos: 0, direcao: 'O' }, 'latitude').erro)
      .toBe('A latitude precisa ser S ou N.');
  });
});

describe('decimalParaGms', () => {
  it('decompõe o valor e escolhe a direção pelo sinal', () => {
    expect(decimalParaGms(-69.949, 'longitude')).toEqual({ graus: 69, minutos: 56, segundos: 56.4, direcao: 'O' });
    expect(decimalParaGms(10.5, 'latitude')).toEqual({ graus: 10, minutos: 30, segundos: 0, direcao: 'N' });
  });

  it('não produz 60 segundos no arredondamento', () => {
    expect(decimalParaGms(-9.9999999, 'latitude')).toEqual({ graus: 10, minutos: 0, segundos: 0, direcao: 'S' });
  });

  it('usa sul e oeste quando não há valor', () => {
    expect(decimalParaGms(null, 'latitude').direcao).toBe('S');
    expect(decimalParaGms(null, 'longitude').direcao).toBe('O');
  });

  it('volta ao mesmo decimal, com a precisão dos segundos', () => {
    const original = -9.9747222;
    const convertido = gmsParaDecimal(decimalParaGms(original, 'latitude'), 'latitude').valor!;
    expect(Math.abs(convertido - original)).toBeLessThan(0.000003);
  });
});

describe('formatarGms', () => {
  it('formata com vírgula decimal', () => {
    expect(formatarGms(-9.9747306, 'latitude')).toBe('9°58\'29,03" S');
    expect(formatarGms(-69.949, 'longitude')).toBe('69°56\'56,4" O');
  });

  it('não formata valores ausentes ou fora do intervalo', () => {
    expect(formatarGms(null, 'latitude')).toBeNull();
    expect(formatarGms(95, 'latitude')).toBeNull();
  });
});
