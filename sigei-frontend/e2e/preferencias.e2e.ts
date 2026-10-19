import { ANOS, DASHBOARD } from './apoio/dados';
import { USUARIOS, definirPreferencias, entrarComo, expect, test } from './apoio/api-simulada';

test.describe('tela de configurações', () => {
  test('mostra "Sobre o sistema" no lugar da identificação e não oferece confirmar exclusões', async ({ page, api }) => {
    await entrarComo(page, USUARIOS.admin);

    await page.goto('/admin/configuracoes');

    await expect(page.getByRole('heading', { name: 'Sobre o sistema' })).toBeVisible();
    await expect(page.getByText('Ana Administradora')).toHaveCount(2); // menu lateral e "Sobre o sistema"
    await expect(page.getByText('Identificação institucional')).toHaveCount(0);
    await expect(page.getByLabel('Nome da instituição')).toHaveCount(0);
    await expect(page.getByText('Confirmar exclusões')).toHaveCount(0);
    await expect(page.getByText('Lembrete de ano letivo')).toBeVisible();
    expect(api.naoSimuladas).toEqual([]);
  });

  test('a página inicial escolhida vale no próximo login', async ({ page, api }) => {
    await entrarComo(page, USUARIOS.admin);
    api
      .em('POST', '/auth/login', { json: USUARIOS.admin })
      .em('GET', '/escolas', { json: [] });

    await page.goto('/admin/configuracoes');
    await page.getByLabel('Página inicial após o acesso').selectOption({ label: 'Escolas' });
    await page.getByRole('button', { name: 'Salvar alterações' }).click();
    await expect(page.getByText('Preferências salvas e aplicadas neste navegador.')).toBeVisible();

    await page.getByRole('button', { name: 'Sair' }).click();
    await expect(page).toHaveURL(/\/login$/);
    await page.getByLabel('E-mail').fill('admin@sigei.com');
    await page.getByLabel('Senha', { exact: true }).fill('senha-correta');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/\/admin\/escolas$/);
  });

  test('o menu compacto mostra só os ícones, com o nome no título', async ({ page, api }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await entrarComo(page, USUARIOS.admin);
    await definirPreferencias(page, { menuCompacto: true, avisarAtualizacaoAnual: false });
    api.em('GET', '/dashboard', { json: DASHBOARD });

    await page.goto('/admin/dashboard');

    const menu = page.locator('aside');
    expect((await menu.boundingBox())!.width).toBeLessThan(100);
    await expect(menu.locator('.nav-rotulo').first()).toBeHidden();
    await expect(page.getByRole('link', { name: 'Municípios' })).toBeVisible();
  });

  test('reduzir animações vale para toda a aplicação', async ({ page, api }) => {
    await entrarComo(page, USUARIOS.admin);
    await definirPreferencias(page, { reduzirAnimacoes: true, avisarAtualizacaoAnual: false });
    api.em('GET', '/dashboard', { json: DASHBOARD });

    await page.goto('/admin/dashboard');

    await expect(page.locator('html')).toHaveClass(/reduzir-animacoes/);
  });
});

test.describe('menu lateral', () => {
  test('o atalho do mapa público fica à vista mesmo numa página longa', async ({ page, api }) => {
    await page.setViewportSize({ width: 1366, height: 768 });
    await entrarComo(page, USUARIOS.admin);
    api.em('GET', '/dashboard', { json: DASHBOARD });

    await page.goto('/admin/dashboard');
    await expect(page.getByRole('heading', { name: 'Visão geral' })).toBeVisible();
    // Uma página bem mais alta que a janela
    await page.locator('.admin-content > div').evaluate(tela => {
      const espaco = document.createElement('div');
      espaco.style.height = '4000px';
      tela.appendChild(espaco);
    });

    const atalho = page.getByRole('link', { name: 'Visualizar mapa público' });
    await expect(atalho).toBeInViewport();

    await page.mouse.wheel(0, 2500);
    await expect(atalho).toBeInViewport();
  });
});

test.describe('lembrete de ano letivo', () => {
  test.beforeEach(async ({ page, api }) => {
    await page.clock.setFixedTime(new Date('2026-03-10T12:00:00-05:00'));
    await definirPreferencias(page, { avisarAtualizacaoAnual: true });
    api
      .em('GET', '/anos-referencia', { json: ANOS }) // o mais recente é 2025
      .em('GET', '/dashboard', { json: DASHBOARD });
  });

  test('o administrador abre o ano atual a partir do aviso', async ({ page, api }) => {
    await entrarComo(page, USUARIOS.admin);
    api.em('POST', '/anos-referencia/proximo', { status: 201, json: { id: 3, ano: 2026 } });

    await page.goto('/admin/dashboard');
    await expect(page.getByText('O ano letivo de 2026 ainda não foi aberto')).toBeVisible();

    await page.getByRole('button', { name: 'Abrir 2026' }).click();
    const confirmacao = page.locator('app-confirm-modal');
    await expect(confirmacao).toContainText('Abrir o ano letivo de 2026?');
    expect(api.chamadas('POST', '/anos-referencia/proximo')).toHaveLength(0);

    await confirmacao.getByRole('button', { name: 'Abrir 2026' }).click();

    await expect(page.getByText('O ano letivo de 2026 foi aberto com os dados de 2025.')).toBeVisible();
    await expect(page.getByText('O ano letivo de 2026 ainda não foi aberto')).toBeHidden();
    expect(api.chamadas('POST', '/anos-referencia/proximo')).toHaveLength(1);
  });

  test('o perfil de consulta só é avisado', async ({ page }) => {
    await entrarComo(page, USUARIOS.usuario);

    await page.goto('/admin/dashboard');

    await expect(page.getByText('Peça a um administrador para abrir o ano.')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Abrir 2026' })).toHaveCount(0);
  });

  test('dispensado, não volta ao trocar de tela', async ({ page, api }) => {
    await entrarComo(page, USUARIOS.admin);
    api.em('GET', '/escolas', { json: [] });

    await page.goto('/admin/dashboard');
    await page.getByRole('button', { name: 'Dispensar' }).click();
    await expect(page.getByText('O ano letivo de 2026 ainda não foi aberto')).toBeHidden();

    await page.getByRole('link', { name: 'Escolas', exact: true }).click();
    await expect(page).toHaveURL(/\/admin\/escolas$/);
    await expect(page.getByText('O ano letivo de 2026 ainda não foi aberto')).toHaveCount(0);
  });
});
