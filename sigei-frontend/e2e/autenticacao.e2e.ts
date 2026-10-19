import { DASHBOARD } from './apoio/dados';
import { USUARIOS, entrarComo, expect, problema, test } from './apoio/api-simulada';

test.describe('autenticação e perfis', () => {
  test('credenciais recusadas mostram a mensagem da API', async ({ page, api }) => {
    api.em('POST', '/auth/login', { status: 401, json: problema(401, 'E-mail ou senha inválidos.') });

    await page.goto('/login');
    await page.getByLabel('E-mail').fill('admin@sigei.com');
    await page.getByLabel('Senha', { exact: true }).fill('errada');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page.getByText('E-mail ou senha inválidos.')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('o administrador entra e vê o painel', async ({ page, api }) => {
    api
      .em('POST', '/auth/login', { json: USUARIOS.admin })
      .em('GET', '/dashboard', { json: DASHBOARD });

    await page.goto('/login');
    await page.getByLabel('E-mail').fill('admin@sigei.com');
    await page.getByLabel('Senha', { exact: true }).fill('senha-correta');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/\/admin\/dashboard$/);
    await expect(page.getByRole('heading', { name: 'Visão geral' })).toBeVisible();
    await expect(page.getByText('9.876').first()).toBeVisible();
    expect(api.chamadas('POST', '/auth/login')[0].postDataJSON())
      .toEqual({ email: 'admin@sigei.com', senha: 'senha-correta' });
  });

  test('com senha temporária, o login leva à troca de senha', async ({ page, api }) => {
    api.em('POST', '/auth/login', { json: USUARIOS.senhaTemporaria });

    await page.goto('/login');
    await page.getByLabel('E-mail').fill('novo@sigei.com');
    await page.getByLabel('Senha', { exact: true }).fill('temporaria');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/\/alterar-senha$/);
  });

  test('sem sessão, a área administrativa leva ao login', async ({ page }) => {
    await page.goto('/admin/escolas');

    await expect(page).toHaveURL(/\/login$/);
  });

  test('o perfil de consulta não abre telas só de administrador', async ({ page, api }) => {
    await entrarComo(page, USUARIOS.usuario);
    api.em('GET', '/dashboard', { json: DASHBOARD });

    await page.goto('/admin/importacoes');

    await expect(page).toHaveURL(/\/admin\/dashboard$/);
    await expect(page.getByText('Esta área é restrita a administradores.')).toBeVisible();
  });

  test('sessão expirada que não renova volta ao login', async ({ page, api }) => {
    await entrarComo(page, USUARIOS.admin);
    api
      .em('GET', '/dashboard', { status: 401, json: problema(401, 'Autenticação necessária.') })
      .em('POST', '/auth/refresh', { status: 401, json: problema(401, 'Sessão expirada.') });

    await page.goto('/admin/dashboard');

    await expect(page).toHaveURL(/\/login/);
    expect(api.chamadas('POST', '/auth/refresh')).toHaveLength(1);
    expect(await page.evaluate(() => localStorage.getItem('sigei_usuario'))).toBeNull();
  });
});
