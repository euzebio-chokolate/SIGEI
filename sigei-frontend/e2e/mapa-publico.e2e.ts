import { ESCOLAS_MAPA } from './apoio/dados';
import { expect, problema, test } from './apoio/api-simulada';

test.describe('mapa público', () => {
  test('mostra um marcador por escola com coordenadas, sem login', async ({ page, api }) => {
    api.em('GET', '/infraestruturas/mais-recentes', { json: ESCOLAS_MAPA });

    await page.goto('/');

    const marcadores = page.locator('.leaflet-marker-icon');
    await expect(marcadores).toHaveCount(2);
    await expect(page.getByTitle('Escola Indígena Alto Bonito')).toBeVisible();
    await expect(page.getByTitle('Escola Indígena Sem Coordenadas')).toHaveCount(0);
  });

  test('a busca filtra os marcadores por nome', async ({ page, api }) => {
    api.em('GET', '/infraestruturas/mais-recentes', { json: ESCOLAS_MAPA });

    await page.goto('/');
    await expect(page.locator('.leaflet-marker-icon')).toHaveCount(2);

    await page.getByLabel('Buscar escola no mapa').fill('coqueiro');

    await expect(page.locator('.leaflet-marker-icon')).toHaveCount(1);
    await expect(page.getByTitle('Escola Indígena Coqueiro')).toBeVisible();
  });

  test('o popup da escola mostra código e situação', async ({ page, api }) => {
    api.em('GET', '/infraestruturas/mais-recentes', { json: ESCOLAS_MAPA });

    await page.goto('/');
    await page.getByTitle('Escola Indígena Coqueiro').click();

    const popup = page.locator('.leaflet-popup');
    await expect(popup).toContainText('Escola Indígena Coqueiro');
    await expect(popup).toContainText('12000002');
  });

  test('uma falha da API mostra o erro e permite tentar de novo', async ({ page, api }) => {
    let falhar = true;
    api.em('GET', '/infraestruturas/mais-recentes', () =>
      falhar ? { status: 500, json: problema(500, 'Erro interno.') } : { json: ESCOLAS_MAPA });

    await page.goto('/');
    await expect(page.getByText('Não foi possível carregar as escolas no mapa.')).toBeVisible();

    falhar = false;
    await page.getByRole('button', { name: /tentar/i }).click();

    await expect(page.locator('.leaflet-marker-icon')).toHaveCount(2);
  });
});
