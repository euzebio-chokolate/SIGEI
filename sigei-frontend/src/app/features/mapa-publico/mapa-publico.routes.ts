import { Routes } from '@angular/router';

export const MAPA_PUBLICO_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/mapa-publico/mapa-publico.component').then(
        module => module.MapaPublicoComponent
      ),
    title: 'SIGEI - Mapa Público'
  }
];
