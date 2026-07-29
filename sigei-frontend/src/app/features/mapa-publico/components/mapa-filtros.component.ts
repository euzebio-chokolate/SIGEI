import { Component } from '@angular/core';

@Component({
  selector: 'app-mapa-filtros',
  standalone: true,
  template: `
    <div class="absolute top-24 z-[1010] transition-all duration-300 ease-in-out flex"
         [class.left-4]="isAberto"
         [class.-left-80]="!isAberto">

      <aside class="w-80 bg-verde-fundo backdrop-blur-md shadow-xl rounded-2xl border border-gray-100 flex flex-col overflow-hidden max-h-[calc(100vh-8rem)]">
        
        <div class="p-5 bg-gray-verde-fundo border-b border-gray-100 flex justify-between items-center">
          <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">
            <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            Filtros
          </h2>
          
          <button (click)="toggleGaveta()" class="text-gray-400 hover:text-gray-700 transition-colors p-1 rounded-md hover:bg-gray-200">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>

        <div class="p-5 flex-1 overflow-y-auto space-y-4">
          <div>
            <label class="block text-[11px] font-bold text-gray-500 mb-1 uppercase tracking-wider">Município</label>
            <select class="w-full px-3 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-green-500 outline-none shadow-sm">
              <option value="">Selecione...</option>
              <option value="rio-branco">Rio Branco</option>
              <option value="cruzeiro-do-sul">Cruzeiro do Sul</option>
            </select>
          </div>

          <div>
            <label class="block text-[11px] font-bold text-gray-500 mb-1 uppercase tracking-wider">Aldeia</label>
            <input type="text" placeholder="Nome da aldeia..." class="w-full px-3 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-green-500 outline-none shadow-sm">
          </div>

          <div>
            <label class="block text-[11px] font-bold text-gray-500 mb-1 uppercase tracking-wider">Escola</label>
            <input type="text" placeholder="Nome da escola..." class="w-full px-3 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-green-500 outline-none shadow-sm">
          </div>
        </div>

        <div class="p-4 border-t border-gray-100 bg-gray-50/50">
          <button class="w-full bg-verde-claro hover:bg-verde-escuro text-white py-2.5 rounded-lg text-sm font-bold shadow-md transition-all flex items-center justify-center gap-2">
            Buscar no Mapa
          </button>
        </div>
      </aside>

      <button (click)="toggleGaveta()"
              class="absolute top-6 -right-10 w-10 h-12 bg-white/95 backdrop-blur-md shadow-[4px_0_10px_rgba(0,0,0,0.1)] rounded-r-xl border-y border-r border-gray-200 text-gray-600 hover:text-green-600 flex items-center justify-center transition-all duration-300"
              [class.opacity-0]="isAberto"
              [class.pointer-events-none]="isAberto">
        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
        </svg>
      </button>

    </div>
  `
})
export class MapaFiltrosComponent {
  // Variável que controla se a gaveta está aberta ou fechada
  isAberto = true;

  // Função disparada pelos botões para inverter o estado
  toggleGaveta() {
    this.isAberto = !this.isAberto;
  }
}