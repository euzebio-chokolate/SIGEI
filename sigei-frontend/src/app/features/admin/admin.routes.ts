import { Routes } from '@angular/router';
import { adminGuard } from '@core/auth/auth.guard';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./admin-shell/admin.component').then(module => module.AdminComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/pages/dashboard/dashboard.component').then(module => module.DashboardComponent),
        title: 'SIGEI - Dashboard'
      },
      {
        path: 'municipios',
        loadComponent: () => import('./municipios/pages/municipios-lista/municipios-lista.component').then(module => module.MunicipiosListaComponent),
        title: 'SIGEI - Municípios'
      },
      {
        path: 'terras-indigenas',
        loadComponent: () => import('./terras-indigenas/pages/terras-indigenas-lista/terras-indigenas-lista.component').then(module => module.TerrasIndigenasListaComponent),
        title: 'SIGEI - Terras Indígenas'
      },
      {
        path: 'terras-indigenas/novo',
        canActivate: [adminGuard],
        loadComponent: () => import('./terras-indigenas/pages/terra-indigena-form/terra-indigena-form.component').then(module => module.TerraIndigenaFormComponent),
        title: 'SIGEI - Nova T.I.'
      },
      {
        path: 'terras-indigenas/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./terras-indigenas/pages/terra-indigena-form/terra-indigena-form.component').then(module => module.TerraIndigenaFormComponent),
        title: 'SIGEI - Editar T.I.'
      },
      {
        path: 'aldeias',
        loadComponent: () => import('./aldeias/pages/aldeias-lista/aldeias-lista.component').then(module => module.AldeiasListaComponent),
        title: 'SIGEI - Aldeias'
      },
      {
        path: 'aldeias/novo',
        canActivate: [adminGuard],
        loadComponent: () => import('./aldeias/pages/aldeia-form/aldeia-form.component').then(module => module.AldeiaFormComponent),
        title: 'SIGEI - Nova Aldeia'
      },
      {
        path: 'aldeias/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./aldeias/pages/aldeia-form/aldeia-form.component').then(module => module.AldeiaFormComponent),
        title: 'SIGEI - Editar Aldeia'
      },
      {
        path: 'povos',
        loadComponent: () => import('./povos/pages/povos-lista/povos-lista.component').then(module => module.PovosListaComponent),
        title: 'SIGEI - Povos Indígenas'
      },
      {
        path: 'povos/novo',
        canActivate: [adminGuard],
        loadComponent: () => import('./povos/pages/povo-form/povo-form.component').then(module => module.PovoFormComponent),
        title: 'SIGEI - Novo Povo'
      },
      {
        path: 'povos/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./povos/pages/povo-form/povo-form.component').then(module => module.PovoFormComponent),
        title: 'SIGEI - Editar Povo'
      },
      {
        path: 'escolas',
        loadComponent: () => import('./escolas/pages/escolas-lista/escolas-lista.component').then(module => module.EscolasListaComponent),
        title: 'SIGEI - Escolas'
      },
      {
        path: 'escolas/novo',
        canActivate: [adminGuard],
        loadComponent: () => import('./escolas/pages/escola-form/escola-form.component').then(module => module.EscolaFormComponent),
        title: 'SIGEI - Nova Escola'
      },
      {
        path: 'escolas/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./escolas/pages/escola-form/escola-form.component').then(module => module.EscolaFormComponent),
        title: 'SIGEI - Editar Escola'
      },
      {
        path: 'professores',
        loadComponent: () => import('./professores/pages/professores-lista/professores-lista.component').then(module => module.ProfessoresListaComponent),
        title: 'SIGEI - Professores'
      },
      {
        path: 'professores/novo',
        canActivate: [adminGuard],
        loadComponent: () => import('./professores/pages/professor-form/professor-form.component').then(module => module.ProfessorFormComponent),
        title: 'SIGEI - Novo Professor'
      },
      {
        path: 'professores/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./professores/pages/professor-form/professor-form.component').then(module => module.ProfessorFormComponent),
        title: 'SIGEI - Editar Professor'
      },
      {
        path: 'professores/lotacoes/novo',
        canActivate: [adminGuard],
        loadComponent: () => import('./professores/pages/lotacao-form/lotacao-form.component').then(module => module.LotacaoFormComponent),
        title: 'SIGEI - Nova Lotação'
      },
      {
        path: 'professores/lotacoes/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./professores/pages/lotacao-form/lotacao-form.component').then(module => module.LotacaoFormComponent),
        title: 'SIGEI - Editar Lotação'
      },
      {
        path: 'historico-escolas',
        loadComponent: () => import('./historico-escolas/pages/historico-escolas/historico-escolas.component').then(module => module.HistoricoEscolasComponent),
        title: 'SIGEI - Histórico de Escolas'
      },
      {
        path: 'usuarios',
        canActivate: [adminGuard],
        loadComponent: () => import('./usuarios/pages/usuarios-lista/usuarios-lista.component').then(module => module.UsuariosListaComponent),
        title: 'SIGEI - Usuários'
      },
      {
        path: 'usuarios/novo',
        canActivate: [adminGuard],
        loadComponent: () => import('./usuarios/pages/usuario-form/usuario-form.component').then(module => module.UsuarioFormComponent),
        title: 'SIGEI - Novo Usuário'
      },
      {
        path: 'usuarios/editar/:id',
        canActivate: [adminGuard],
        loadComponent: () => import('./usuarios/pages/usuario-form/usuario-form.component').then(module => module.UsuarioFormComponent),
        title: 'SIGEI - Editar Usuário'
      },
      {
        path: 'auditoria',
        canActivate: [adminGuard],
        loadComponent: () => import('./auditoria/pages/auditoria/auditoria.component').then(module => module.AuditoriaComponent),
        title: 'SIGEI - Auditoria'
      },
      {
        path: 'configuracoes',
        loadComponent: () => import('./configuracoes/pages/configuracoes/configuracoes.component').then(module => module.ConfiguracoesComponent),
        title: 'SIGEI - Configurações'
      }
    ]
  }
];
