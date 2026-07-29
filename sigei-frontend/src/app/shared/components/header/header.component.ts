import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  template: `
    <header
      class="absolute top-4 left-4 right-4 z-[1010] pointer-events-none flex items-center justify-between gap-4"
    >
      <!-- LOGO -->
      <div
        class="bg-verde-escuro backdrop-blur-md px-5 py-3 rounded-xl shadow-lg border border-gray-100 flex items-center gap-3 pointer-events-auto shrink-0"
      >
        <img src="assets/logo/Logo-SIGEI.svg" alt="Logo SIGEI" class="w-8 h-8 object-contain" />
        <div>
          <h1 class="text-xl font-bold tracking-wide text-white leading-tight">SIGEI</h1>
          <p class="text-[10px] text-gray-300 uppercase tracking-wider">Gestão Escolar Indígena</p>
        </div>
      </div>

      <!-- BARRA DE PESQUISA CENTRAL -->
      <div class="pointer-events-auto flex-1 max-w-md mx-4 hidden md:block">
        <div class="relative w-full flex items-center">
          <div
            class="absolute left-4 flex items-center justify-center pointer-events-none z-10 text-gray-400"
          >
            <i class="fa-solid fa-magnifying-glass text-base"></i>
          </div>
          <input
            type="text"
            placeholder="Pesquisar escolas..."
            class="w-full bg-white/95 backdrop-blur-md text-sm text-gray-700 pl-11 pr-4 py-3 rounded-xl shadow-lg border border-gray-100 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all z-0"
          />
        </div>
      </div>

      <!-- BOTÃO ADMINISTRATIVO -->
      <a
        routerLink="/admin"
        class="pointer-events-auto flex shrink-0 items-center gap-2 rounded-xl border border-gray-100 bg-verde-escuro px-4 py-3 text-sm font-semibold text-white shadow-lg backdrop-blur-md transition-all hover:bg-verde-claro group"
      >
        <i class="fas fa-lock text-white transition-transform group-hover:scale-110"></i>
        Área Administrativa
      </a>
    </header>
  `,
})
export class HeaderComponent {}
