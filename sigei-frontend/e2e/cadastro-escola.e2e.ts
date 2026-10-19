import { Request } from '@playwright/test';

import { ALDEIAS, ANOS } from './apoio/dados';
import { USUARIOS, entrarComo, expect, test } from './apoio/api-simulada';

/** A parte "dados" (JSON) do multipart enviado ao cadastro completo. */
function dadosDoCadastro(requisicao: Request): any {
  const corpo = requisicao.postData() ?? '';
  const parte = corpo.split(/\r?\n--/).find(trecho => trecho.includes('name="dados"'));
  if (!parte) throw new Error('Parte "dados" ausente no envio.');
  return JSON.parse(parte.slice(parte.indexOf('{'), parte.lastIndexOf('}') + 1));
}

test.describe('cadastro completo de escola', () => {
  test('percorre as etapas com coordenadas em G°M\'S" e envia o decimal', async ({ page, api }) => {
    await entrarComo(page, USUARIOS.admin);
    api
      .em('GET', '/aldeias', { json: ALDEIAS })
      .em('GET', '/anos-referencia', { json: ANOS })
      .em('GET', '/escolas', { json: [] })
      .em('POST', '/escolas/cadastro-completo', { status: 201, json: {
        escola: { id: 900, codigo: '12999901', nome: 'Escola Indígena Nova Esperança' },
        registro: { id: 901, anoReferencia: ANOS[0] }
      } });

    await page.goto('/admin/escolas/novo');

    // 1. Escola
    await page.locator('#codigo').fill('12999901');
    await page.locator('#nome').fill('Escola Indígena Nova Esperança');
    await page.locator('#tipoUnidade').selectOption('SEDE');
    await page.locator('#aldeiaId').selectOption({ label: 'Alto Bonito — Alto Rio Envira — Feijó' });
    await page.getByRole('button', { name: 'Próximo' }).click();

    // 2. Registro do ano
    await page.locator('#anoReferenciaId').selectOption({ label: '2025' });
    await page.locator('#rede').selectOption('ESTADUAL');
    await page.locator('#localFuncionamento').selectOption('RURAL');
    await page.locator('#totalAlunos').fill('30');
    await page.getByRole('button', { name: 'Próximo' }).click();

    // 3. Equipe
    await page.getByRole('button', { name: 'Próximo' }).click();

    // 4. Infraestrutura, com as coordenadas em graus, minutos e segundos
    await page.getByRole('button', { name: 'G° M\' S"' }).click();
    await page.getByLabel('Latitude: graus').fill('8');
    await page.getByLabel('Latitude: minutos').fill('9');
    await page.getByLabel('Latitude: segundos').fill('30,6');
    await page.getByLabel('Longitude: graus').fill('70');
    await page.getByLabel('Longitude: minutos').fill('21');
    await page.getByLabel('Longitude: segundos').fill('7,56');
    await expect(page.getByText('Decimal: -8.1585')).toBeVisible();
    await page.locator('#situacao').selectOption({ label: 'Funcionando' });
    await page.locator('#tipoAcesso').selectOption({ label: 'Fluvial' });
    await page.locator('#tipoAgua').selectOption({ label: 'Rio' });
    await page.locator('#tipoEnergia').selectOption({ label: 'Solar' });
    await page.getByRole('button', { name: 'Próximo' }).click();

    // 5 e 6. Matrículas
    await page.getByRole('button', { name: 'Próximo' }).click();
    await page.getByRole('button', { name: 'Revisar Cadastro' }).click();

    // 7. Revisão: o decimal e o equivalente em G°M'S"
    await expect(page.getByText('8°9\'30,6" S')).toBeVisible();
    await expect(page.getByText('70°21\'7,56" O')).toBeVisible();
    await page.getByRole('button', { name: 'Salvar Cadastro Completo' }).click();

    await expect(page.getByText('A nova escola foi cadastrada com sucesso.')).toBeVisible();

    const [envio] = api.chamadas('POST', '/escolas/cadastro-completo');
    const dados = dadosDoCadastro(envio);
    expect(dados.escola).toMatchObject({ codigo: '12999901', tipoUnidade: 'SEDE', aldeiaId: 21 });
    expect(dados.infraestrutura).toMatchObject({
      latitude: -8.1585,
      longitude: -70.3521,
      situacao: 'FUNCIONANDO',
      tipoAcesso: 'FLUVIAL'
    });
  });
});
