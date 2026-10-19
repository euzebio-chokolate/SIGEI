import { defineConfig, devices } from '@playwright/test';

/**
 * Testes de ponta a ponta das telas (e2e/*.e2e.ts). O Angular roda de verdade no
 * Chromium; a API é simulada pelo próprio Playwright (e2e/apoio/api-simulada.ts),
 * então os testes não precisam do backend nem de banco. A porta é outra que a do
 * `ng serve` de desenvolvimento, para os dois poderem rodar juntos.
 */
const PORTA = 4300;
const CI = !!process.env['CI'];

export default defineConfig({
  testDir: './e2e',
  testMatch: '**/*.e2e.ts',
  fullyParallel: true,
  forbidOnly: CI,
  retries: CI ? 2 : 0,
  reporter: CI ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: `http://localhost:${PORTA}`,
    locale: 'pt-BR',
    timezoneId: 'America/Rio_Branco',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } }
  ],
  webServer: {
    command: `npx ng serve --port ${PORTA}`,
    url: `http://localhost:${PORTA}`,
    reuseExistingServer: !CI,
    timeout: 180_000
  }
});
