import { Component } from '@angular/core';

@Component({
  selector: 'app-mapa-filtros',
  standalone: true,
  template: `
    <aside class="absolute top-24 left-4 z-[1010] w-80 bg-white/95 backdrop-blur-md shadow-xl rounded-2xl border border-gray-100 flex flex-col overflow-hidden max-h-[calc(100vh-8rem)]">
      
      <div class="p-5 bg-gray-50/50 border-b border-gray-100">
        <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">
          <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
          </svg>
          Pesquisa Avançada
        </h2>
      </div>

      <div class="p-5 flex-1 overflow-y-auto space-y-4">
        
        <div>
          <label class="block text-[11px] font-bold text-gray-500 mb-1 uppercase tracking-wider">Município</label>
          <select class="w-full px-3 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition-all shadow-sm">
            <option value="">Selecione...</option>
            <option value="rio-branco">Rio Branco</option>
            <option value="cruzeiro-do-sul">Cruzeiro do Sul</option>
          </select>
        </div>

        <div>
          <label class="block text-[11px] font-bold text-gray-500 mb-1 uppercase tracking-wider">Aldeia</label>
          <input type="text" placeholder="Nome da aldeia..." 
                 class="w-full px-3 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition-all shadow-sm">
        </div>

        <div>
          <label class="block text-[11px] font-bold text-gray-500 mb-1 uppercase tracking-wider">Escola</label>
          <input type="text" placeholder="Nome da escola..." 
                 class="w-full px-3 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-green-500 focus:border-green-500 outline-none transition-all shadow-sm">
        </div>

      </div>

      <div class="p-4 border-t border-gray-100 bg-gray-50/50">
        <button class="w-full bg-green-600 hover:bg-green-700 text-white py-2.5 rounded-lg text-sm font-bold shadow-md transition-all flex items-center justify-center gap-2">
          Buscar no Mapa
        </button>
      </div>

    </aside>
  `
})
export class MapaFiltrosComponent {}