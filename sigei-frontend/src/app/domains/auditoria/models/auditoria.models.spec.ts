import { AlteracaoHistorico, agruparPorRevisao, campoVisivel, formatarValor, rotuloCampo, rotuloEntidade } from './auditoria.models';

const alteracao = (revisao: number, entidade: string): AlteracaoHistorico => ({
  revisao,
  dataHora: '2026-10-09T15:00:00Z',
  usuarioId: 1,
  usuarioNome: 'Admin',
  usuarioEmail: 'admin@sigei.com',
  entidade,
  entidadeId: revisao * 10,
  ano: entidade === 'Escola' ? null : 2026,
  operacao: 'ALTERACAO',
  campos: []
});

describe('auditoria', () => {
  it('agrupa as alterações gravadas na mesma revisão, mantendo a ordem do backend', () => {
    const grupos = agruparPorRevisao([
      alteracao(9, 'Escola'),
      alteracao(9, 'InfraestruturaEscola'),
      alteracao(7, 'RegistroAnual')
    ]);
    expect(grupos.map(grupo => [grupo.revisao, grupo.alteracoes.length])).toEqual([[9, 2], [7, 1]]);
  });

  it('formata os valores para leitura', () => {
    expect(formatarValor('temInternet', true)).toBe('Sim');
    expect(formatarValor('temInternet', false)).toBe('Não');
    expect(formatarValor('nome', null)).toBe('—');
    expect(formatarValor('totalAlunos', 1234)).toBe('1.234');
    expect(formatarValor('fotoUrl', 'https://res.cloudinary.com/x.jpg')).toBe('Enviada');
  });

  it('traduz entidades e campos, com alternativa para nomes desconhecidos', () => {
    expect(rotuloEntidade('InfraestruturaEscola')).toBe('Infraestrutura');
    expect(rotuloCampo('numSalas')).toBe('Número de salas');
    expect(rotuloCampo('temAreaLazer')).toBe('Tem Area Lazer');
  });

  it('oculta campos internos', () => {
    expect(campoVisivel({ campo: 'fotoPublicId', valorAnterior: null, valorNovo: 'abc' })).toBe(false);
    expect(campoVisivel({ campo: 'nome', valorAnterior: 'A', valorNovo: 'B' })).toBe(true);
  });
});
