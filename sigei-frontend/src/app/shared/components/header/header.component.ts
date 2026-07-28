import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  template: `
    <header class="absolute top-4 left-4 right-4 z-[1010] pointer-events-none flex items-center justify-between gap-4">
      
      <!-- LOGO -->
      <div class="bg-white/95 backdrop-blur-md px-5 py-3 rounded-xl shadow-lg border border-gray-100 flex items-center gap-3 pointer-events-auto shrink-0">
        <div class="w-8 h-8 bg-green-500 rounded flex items-center justify-center font-bold text-white shadow-sm">
          S
        </div>
        <div>
          <h1 class="text-xl font-bold tracking-wide text-gray-800 leading-tight">SIGEI</h1>
          <p class="text-[10px] text-gray-500 uppercase tracking-wider">Gestão Escolar Indígena</p>
        </div>
      </div>

      <!-- BARRA DE PESQUISA CENTRAL -->
      <div class="pointer-events-auto flex-1 max-w-md mx-4 hidden md:block">
  <div class="relative w-full flex items-center">
    
    <!-- ÍCONE DE LUPA (FORÇADO COM ALTURA E LARGURA NO CONTÊINER) -->
    <div class="absolute left-3 flex items-center justify-center w-5 h-5 pointer-events-none z-10">
      <svg 
        class="w-full h-full text-gray-500" 
        fill="none" 
        stroke="currentColor" 
        stroke-width="2.5" 
        viewBox="0 0 24 24" 
        xmlns="http://w3.org"
      >
        <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
      </svg>
    </div>
    
    <!-- INPUT COM ESPAÇAMENTO REFORÇADO (pl-10) E Z-INDEX AJUSTADO -->
    <input 
      type="text" 
      placeholder="Pesquisar escolas, turmas ou alunos..." 
      class="w-full bg-white/95 backdrop-blur-md text-sm text-gray-700 pl-10 pr-4 py-3 rounded-xl shadow-lg border border-gray-100 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all z-0"
    />
  </div>
</div>

      <!-- BOTÃO ADMINISTRATIVO -->
      <a routerLink="/admin" 
         class="pointer-events-auto bg-white/95 backdrop-blur-md hover:bg-gray-50 border border-gray-100 text-gray-700 px-4 py-3 rounded-xl shadow-lg text-sm font-semibold transition-all flex items-center gap-2 group shrink-0">
        <svg class="w-4 h-4 text-green-600 group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
        </svg>
        Área Administrativa
      </a>

    </header>
  `
})
export class HeaderComponent { }