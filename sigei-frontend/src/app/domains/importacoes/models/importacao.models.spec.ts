import { ImportacaoMatriculasResultado, OPCOES_IMPORTACAO, indicadoresResultado } from './importacao.models';

describe('importacao', () => {
  it('lista as importações na ordem de carga de um ano novo', () => {
    expect(OPCOES_IMPORTACAO.map(opcao => opcao.tipo)).toEqual(['infraestrutura', 'matriculas', 'professores']);
    expect(OPCOES_IMPORTACAO.filter(opcao => opcao.criaAno).map(opcao => opcao.tipo)).toEqual(['infraestrutura']);
  });

  it('resume os contadores de cada tipo de importação', () => {
    const matriculas: ImportacaoMatriculasResultado = {
      linhasLidas: 176,
      escolasEncontradas: 175,
      registrosAnuaisEncontrados: 175,
      niveisInseridos: 1225,
      seriesInseridas: 2100,
      registrosAnuaisAtualizados: 175,
      linhasIgnoradas: 1,
      erros: ['Linha 178: Nenhuma escola encontrada para o código 12004065']
    };

    expect(indicadoresResultado('matriculas', matriculas)).toEqual([
      { rotulo: 'Linhas lidas', valor: 176 },
      { rotulo: 'Escolas encontradas', valor: 175 },
      { rotulo: 'Registros atualizados', valor: 175 }
    ]);
  });
});
