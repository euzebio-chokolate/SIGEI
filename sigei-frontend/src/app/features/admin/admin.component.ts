import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="flex h-screen bg-verde-fundo font-sans overflow-hidden">
      <aside class="w-80 bg-verde-escuro border-r border-gray-200 flex flex-col shadow-sm z-20">
        <div
          class="h-20 flex items-center px-6 border-b border-verde-claro bg-verde-escuro backdrop-blur-md shrink-0"
        >
          <img
            src="assets/logo/Logo-SIGEI.svg"
            alt="Logo SIGEI"
            class="w-13 h-13 object-contain mr-3"
          />

          <div class="flex flex-col">
            <h1 class="text-2xl font-bold tracking-wide text-white leading-none">SIGEI</h1>
            <p class="text-xs text-gray-300 uppercase tracking-wider mt-0.5">
              Gestão Escolar Indígena
            </p>
          </div>
        </div>

        <nav class="flex-1 overflow-y-auto py-6 px-4 space-y-1">
          <p class="px-3 text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">
            Menu Principal
          </p>

          <a
            routerLink="/admin/dashboard"
            routerLinkActive="bg-verde-claro text-white/100"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-white/50 hover:bg-verde-fundo/20 hover:text-white transition-colors font-medium"
          >
            <i class="fa-solid fa-chart-line text-lg w-5 text-center"></i>
            Visão Geral
          </a>

          <a
            routerLink="/admin/municipios"
            routerLinkActive="bg-verde-claro text-white/100"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-white/50 hover:bg-verde-fundo/20 hover:text-white transition-colors font-medium mt-1"
          >
            <i class="fa-solid fa-map-location-dot text-lg w-5 text-center"></i>
            Municípios
          </a>

          <a
            routerLink="/admin/escolas"
            routerLinkActive="bg-green-50 text-green-700"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-white/50 hover:bg-verde-fundo/20 hover:text-white transition-colors font-medium mt-1"
          >
            <i class="fa-solid fa-school text-lg w-5 text-center"></i>
            Escolas
          </a>

          <a
            routerLink="/admin/aldeias"
            routerLinkActive="bg-green-50 text-green-700"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-white/50 hover:bg-verde-fundo/20 hover:text-white transition-colors font-medium mt-1"
          >
            <i class="fa-solid fa-tents text-lg w-5 text-center"></i>
            Aldeias
          </a>
        </nav>

        <div class="p-4 border-t border-gray-100">
          <a
            routerLink="/"
            class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-red-600 hover:bg-red-50 transition-colors font-medium"
          >
            <i class="fa-solid fa-arrow-right-from-bracket text-lg w-5 text-center"></i>
            Voltar ao Mapa
          </a>
        </div>
      </aside>

      <main class="flex-1 flex flex-col min-w-0 overflow-hidden">
        <header
          class="h-20 bg-verde-fundo border-b border-gray-200 flex items-center justify-end px-8 shadow-sm z-10"
        >
          <div class="flex items-center gap-4">
            <div class="text-right hidden md:block">
              <p class="text-sm font-bold text-gray-700">Administrador</p>
              <p class="text-xs text-gray-500">admin&#64;sigei.gov.br</p>
            </div>
            <div
              class="w-10 h-10 rounded-full bg-green-100 border-2 border-green-500 flex items-center justify-center text-green-700 font-bold"
            >
              <i class="fa-solid fa-user"></i>
            </div>
          </div>
        </header>

        <div class="flex-1 overflow-y-auto p-8">
          <router-outlet></router-outlet>
        </div>
      </main>
    </div>
  `,
})
export class AdminComponent {}
