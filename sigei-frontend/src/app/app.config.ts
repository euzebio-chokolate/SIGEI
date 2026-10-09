import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from '@core/auth/auth.interceptor';
import { erroInterceptor } from '@core/http/erro.interceptor';

// Datas e números dos pipes (date, number) no formato brasileiro: 09/10/2026, 1.234,5
registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([erroInterceptor, authInterceptor])),
    { provide: LOCALE_ID, useValue: 'pt-BR' }
  ]
};
