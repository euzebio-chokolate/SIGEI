import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  template: `
    <header class="pointer-events-none absolute left-3 right-3 top-3 z-[1010] flex items-start justify-between gap-3 sm:left-5 sm:right-5 sm:top-5">
      <div class="pointer-events-auto flex h-14 shrink-0 items-center gap-3 rounded-2xl border border-white/10 bg-verde-escuro/95 px-3.5 text-white shadow-2xl backdrop-blur-xl sm:h-16 sm:px-4">
        <div class="grid h-9 w-9 place-items-center rounded-xl bg-white/10 ring-1 ring-white/10 sm:h-10 sm:w-10">
          <img src="assets/logo/Logo-SIGEI.svg" alt="Logo SIGEI" class="h-8 w-8 object-contain" />
        </div>
        <div class="hidden sm:block">
          <div class="flex items-center gap-2"><h1 class="text-lg font-extrabold tracking-wide leading-none">SIGEI</h1><span class="rounded-full bg-emerald-400/15 px-2 py-0.5 text-[8px] font-bold uppercase tracking-wider text-emerald-200">Mapa público</span></div>
          <p class="mt-1 text-[9px] font-medium uppercase tracking-[0.14em] text-white/50">Gestão Escolar Indígena</p>
        </div>
      </div>

      <a routerLink="/admin" class="group pointer-events-auto flex h-12 shrink-0 items-center gap-2 rounded-xl border border-white/70 bg-white/95 px-3.5 text-xs font-bold text-slate-700 shadow-xl backdrop-blur-xl transition hover:border-emerald-200 hover:text-emerald-700 sm:h-14 sm:px-4 sm:text-sm">
        <span class="grid h-7 w-7 place-items-center rounded-lg bg-emerald-50 text-emerald-600 transition group-hover:bg-emerald-100"><i class="fa-solid fa-lock text-[10px]"></i></span>
        <span class="hidden sm:inline">Área administrativa</span>
      </a>
    </header>
  `,
})
export class HeaderComponent {}
