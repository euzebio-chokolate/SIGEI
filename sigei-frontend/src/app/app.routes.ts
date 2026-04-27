import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () =>
      import('./features/mapa/mapa.routes').then(m => m.MAPA_ROUTES)
  }
];