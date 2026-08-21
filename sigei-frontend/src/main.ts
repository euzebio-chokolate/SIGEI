import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config'; // Arquivo de configuração que inclui as rotas
import { AppComponent } from './app/app'; // Corrigido de App para AppComponent

bootstrapApplication(AppComponent, appConfig).catch((err) => console.error(err));
