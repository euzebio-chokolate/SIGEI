import { Routes } from '@angular/router';
import { MapaPublicoComponent } from './features/mapa-publico/mapa-publico.component';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/mapa-publico/mapa-publico.component').then((m) => m.MapaPublicoComponent),
    title: 'SIGEI - Mapa Público',
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin.component').then((m) => m.AdminComponent),
    children: [
      //Rotas Dashboard
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent,
          ),
        title: 'SIGEI - Dashboard',
      },

      //Rotas Municipio
      {
        path: 'municipios',
        loadComponent: () =>
          import('./features/admin/municipios/municipios-lista.component').then(
            (m) => m.MunicipiosListaComponent,
          ),
        title: 'SIGEI - Municípios',
      },

      //Rotas Terras Indigenas
      {
        path: 'terras-indigenas',
        loadComponent: () =>
          import('./features/admin/terras-indigenas/terras-indigenas-lista.component').then(
            (m) => m.TerrasIndigenasListaComponent,
          ),
        title: 'SIGEI - Terras Indígenas',
      },
      {
        path: 'terras-indigenas/novo',
        loadComponent: () =>
          import('./features/admin/terras-indigenas/terra-indigena-form.component').then(
            (m) => m.TerraIndigenaFormComponent,
          ),
        title: 'SIGEI - Nova T.I.',
      },
      {
        path: 'terras-indigenas/editar/:id',
        loadComponent: () =>
          import('./features/admin/terras-indigenas/terra-indigena-form.component').then(
            (m) => m.TerraIndigenaFormComponent,
          ),
        title: 'SIGEI - Editar T.I.',
      },

      //Rotas Aldeias
      {
        path: 'aldeias',
        loadComponent: () =>
          import('./features/admin/aldeias/aldeias-lista.component').then(
            (m) => m.AldeiasListaComponent,
          ),
        title: 'SIGEI - Aldeias',
      },
      {
        path: 'aldeias/novo',
        loadComponent: () =>
          import('./features/admin/aldeias/aldeia-form.component').then(
            (m) => m.AldeiaFormComponent,
          ),
        title: 'SIGEI - Nova Aldeia',
      },
      {
        path: 'aldeias/editar/:id',
        loadComponent: () =>
          import('./features/admin/aldeias/aldeia-form.component').then(
            (m) => m.AldeiaFormComponent,
          ),
        title: 'SIGEI - Editar Aldeia',
      },

      //Rotas Povos
      {
        path: 'povos',
        loadComponent: () =>
          import('./features/admin/povos/povos-lista.component').then((m) => m.PovosListaComponent),
        title: 'SIGEI - Povos Indígenas',
      },
      {
        path: 'povos/novo',
        loadComponent: () =>
          import('./features/admin/povos/povo-form.component').then((m) => m.PovoFormComponent),
        title: 'SIGEI - Novo Povo',
      },
      {
        path: 'povos/editar/:id',
        loadComponent: () =>
          import('./features/admin/povos/povo-form.component').then((m) => m.PovoFormComponent),
        title: 'SIGEI - Editar Povo',
      },

      //Rota Escolas
      {
        path: 'escolas',
        loadComponent: () =>
          import('./features/admin/escolas/escolas-lista.component').then(
            (m) => m.EscolasListaComponent,
          ),
        title: 'SIGEI - Escolas',
      },
      {
        path: 'escolas/novo',
        loadComponent: () =>
          import('./features/admin/escolas/escola-form.component').then(
            (m) => m.EscolaFormComponent,
          ),
        title: 'SIGEI - Nova Escola',
      },
      {
        path: 'escolas/editar/:id',
        loadComponent: () =>
          import('./features/admin/escolas/escola-form.component').then(
            (m) => m.EscolaFormComponent,
          ),
        title: 'SIGEI - Editar Escola',
      },

      // No futuro, adicionamos aqui: 'municipios', 'escolas', etc.
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
