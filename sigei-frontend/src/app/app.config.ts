import { ApplicationConfig, LOCALE_ID, inject, provideAppInitializer, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from '@core/auth/auth.interceptor';
import { erroInterceptor } from '@core/http/erro.interceptor';
import { PreferenciasService } from '@core/preferencias/preferencias.service';

// Datas e números dos pipes (date, number) no formato brasileiro: 09/10/2026, 1.234,5
registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([erroInterceptor, authInterceptor])),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    // Aplica as preferências do navegador (como reduzir animações) desde o carregamento
    provideAppInitializer(() => { inject(PreferenciasService); })
  ]
};
