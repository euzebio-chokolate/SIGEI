import { Routes } from '@angular/router';
import { MapaPublicoComponent } from './features/mapa-publico/mapa-publico.component';

export const routes: Routes = [
  { 
    path: '', 
    loadComponent: () => import('./features/mapa-publico/mapa-publico.component').then(m => m.MapaPublicoComponent),
    title: 'SIGEI - Mapa Público'
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
    children: [
      { 
        path: '', 
        redirectTo: 'dashboard', 
        pathMatch: 'full' 
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'SIGEI - Dashboard'
      },
      {
        path: 'municipios',
        loadComponent: () => import('./features/admin/municipios/municipios-lista.component').then(m => m.MunicipiosListaComponent),
        title: 'SIGEI - Municípios'
      },
      {
        path: 'municipios/novo',
        loadComponent: () => import('./features/admin/municipios/municipio-form.component').then(m => m.MunicipioFormComponent),
        title: 'SIGEI - Novo Município'
      },
      {
        path: 'municipios/editar/:id',
        loadComponent: () => import('./features/admin/municipios/municipio-form.component').then(m => m.MunicipioFormComponent),
        title: 'SIGEI - Editar Município'
      }
      // No futuro, adicionamos aqui: 'municipios', 'escolas', etc.
    ]
  },
  { 
    path: '**', 
    redirectTo: '' 
  }
];