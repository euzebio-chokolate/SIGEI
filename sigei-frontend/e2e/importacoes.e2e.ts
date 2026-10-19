import { ANOS, LAYOUTS, RESULTADO_IMPORTACAO_ESCOLAS } from './apoio/dados';
import { USUARIOS, entrarComo, expect, test } from './apoio/api-simulada';

const PLANILHA = {
  name: 'escolas-2025.xlsx',
  mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  buffer: Buffer.from('planilha de teste')
};

test.describe('importação de planilhas', () => {
  test.beforeEach(async ({ page, api }) => {
    await entrarComo(page, USUARIOS.admin);
    api
      .em('GET', '/importacoes/layouts', { json: LAYOUTS })
      .em('GET', '/anos-referencia', { json: ANOS })
      .em('POST', '/importacoes/infraestrutura', { json: RESULTADO_IMPORTACAO_ESCOLAS });
  });

  test('pré-visualiza sem gravar e só grava depois de confirmar', async ({ page, api }) => {
    await page.goto('/admin/importacoes');
    await page.locator('input[type="file"]').setInputFiles(PLANILHA);

    await page.getByRole('button', { name: /Pré-visualizar/ }).click();

    await expect(page.getByText('Pré-visualização: nada foi gravado ainda')).toBeVisible();
    await expect(page.getByText('Linha 4: Município não cadastrado: Xapuri Velho')).toBeVisible();
    const envios = () => api.chamadas('POST', '/importacoes/infraestrutura');
    expect(envios()).toHaveLength(1);
    expect(new URL(envios()[0].url()).searchParams.get('simulacao')).toBe('true');

    await page.getByRole('button', { name: 'Confirmar e gravar' }).click();

    await expect(page.getByText('Importação concluída')).toBeVisible();
    expect(envios()).toHaveLength(2);
    const gravacao = new URL(envios()[1].url()).searchParams;
    expect(gravacao.get('simulacao')).toBe('false');
    expect(gravacao.get('ano')).toBe('2025');
    expect(envios()[1].postData()).toContain('escolas-2025.xlsx');
  });

  test('cancelar a prévia não grava nada', async ({ page, api }) => {
    await page.goto('/admin/importacoes');
    await page.locator('input[type="file"]').setInputFiles(PLANILHA);
    await page.getByRole('button', { name: /Pré-visualizar/ }).click();
    await expect(page.getByText('Pré-visualização: nada foi gravado ainda')).toBeVisible();

    await page.getByRole('button', { name: 'Cancelar' }).click();

    await expect(page.getByText('Pré-visualização: nada foi gravado ainda')).toBeHidden();
    const envios = api.chamadas('POST', '/importacoes/infraestrutura');
    expect(envios.map(req => new URL(req.url()).searchParams.get('simulacao'))).toEqual(['true']);
  });
});
