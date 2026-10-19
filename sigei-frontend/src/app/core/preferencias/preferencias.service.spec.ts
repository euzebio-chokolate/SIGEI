import { TestBed } from '@angular/core/testing';

import { PREFERENCIAS_PADRAO, PreferenciasService } from './preferencias.service';

describe('PreferenciasService', () => {
  function criar(salvo?: unknown): PreferenciasService {
    localStorage.clear();
    if (salvo !== undefined) {
      localStorage.setItem('sigei-preferencias', typeof salvo === 'string' ? salvo : JSON.stringify(salvo));
    }
    TestBed.resetTestingModule();
    return TestBed.inject(PreferenciasService);
  }

  afterEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('reduzir-animacoes');
  });

  it('sem nada salvo, usa o padrão', () => {
    expect(criar().preferencias()).toEqual(PREFERENCIAS_PADRAO);
  });

  it('valores fora das opções voltam ao padrão, e o restante é mantido', () => {
    const servico = criar({ paginaInicial: 'usuarios', camadaMapa: 'satelite', menuCompacto: 'sim', reduzirAnimacoes: true });

    expect(servico.preferencias()).toEqual({
      ...PREFERENCIAS_PADRAO,
      camadaMapa: 'satelite',
      reduzirAnimacoes: true
    });
  });

  it('JSON corrompido não quebra a aplicação', () => {
    expect(criar('{nao-e-json').preferencias()).toEqual(PREFERENCIAS_PADRAO);
  });

  it('descarta as preferências retiradas ao salvar', () => {
    const servico = criar({ nomeInstituicao: 'Outra', siglaInstituicao: 'X', confirmarExclusoes: false, menuCompacto: true });

    servico.salvar(servico.preferencias());

    const gravado = JSON.parse(localStorage.getItem('sigei-preferencias')!);
    expect(gravado).toEqual({ ...PREFERENCIAS_PADRAO, menuCompacto: true });
    expect(gravado).not.toHaveProperty('nomeInstituicao');
    expect(gravado).not.toHaveProperty('confirmarExclusoes');
  });

  it('reduzir animações marca o documento, e desmarca ao restaurar', () => {
    const servico = criar();

    servico.salvar({ ...PREFERENCIAS_PADRAO, reduzirAnimacoes: true });
    TestBed.tick();
    expect(document.documentElement.classList.contains('reduzir-animacoes')).toBe(true);

    servico.restaurarPadrao();
    TestBed.tick();
    expect(document.documentElement.classList.contains('reduzir-animacoes')).toBe(false);
    expect(localStorage.getItem('sigei-preferencias')).toBeNull();
  });

  it('a rota inicial segue a preferência', () => {
    const servico = criar({ paginaInicial: 'historico-escolas' });

    expect(servico.rotaInicial()).toEqual(['/admin', 'historico-escolas']);
  });
});
