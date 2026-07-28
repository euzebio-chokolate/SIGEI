import { Routes } from '@angular/router';
import { MapaPublicoComponent } from './features/mapa-publico/mapa-publico.component';

export const routes: Routes = [
  { 
    path: '', 
    component: MapaPublicoComponent,
    title: 'SIGEI - Mapa Público'
  },
  {
    path: 'admin',
    // Futuramente faremos o lazy load da área administrativa aqui
    loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
    title: 'SIGEI - Administração'
  },
  { 
    path: '**', 
    redirectTo: '' 
  }
];