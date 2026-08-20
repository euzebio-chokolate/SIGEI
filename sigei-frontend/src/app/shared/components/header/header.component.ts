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
        class="bg-verde-escuro backdrop-blur-md px-5 py-3 rounded-xl shadow-lg flex items-center gap-3 pointer-events-auto shrink-0"
      >
        <img src="assets/logo/Logo-SIGEI.svg" alt="Logo SIGEI" class="w-8 h-8 object-contain" />
        <div>
          <h1 class="text-xl font-bold tracking-wide text-white leading-tight">SIGEI</h1>
          <p class="text-[10px] text-gray-300 uppercase tracking-wider">Gestão Escolar Indígena</p>
        </div>
      </div>

      <!-- BOTÃO ADMINISTRATIVO -->
      <a
        routerLink="/admin"
        class="pointer-events-auto flex shrink-0 items-center gap-2 rounded-xl border border-verde-claro bg-verde-claro px-4 py-3 text-sm font-semibold text-white shadow-lg backdrop-blur-md transition-all hover:bg-verde-escuro group"
      >
        <i class="fas fa-lock text-white transition-transform group-hover:scale-110"></i>
        Área Administrativa
      </a>
    </header>
  `,
})
export class HeaderComponent {}
