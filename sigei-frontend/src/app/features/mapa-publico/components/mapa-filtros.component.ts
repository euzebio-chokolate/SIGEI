import { Component, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-mapa-filtros',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="absolute top-24 z-[1010] transition-all duration-300 ease-in-out flex"
         [class.left-4]="isAberto"
         [class.-left-80]="!isAberto">

      <aside class="w-80 bg-white/95 backdrop-blur-md shadow-xl rounded-2xl border border-gray-100 flex flex-col overflow-hidden max-h-[calc(100vh-8rem)]">
        
        <div class="p-5 bg-gray-50/50 border-b border-gray-100 flex justify-between items-center">
          <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">
            <i class="fa-solid fa-sliders text-emerald-600"></i>
            Filtros Avançados
          </h2>
          
          <button (click)="toggleGaveta()" class="text-gray-400 hover:text-gray-700 transition-colors p-1 rounded-md hover:bg-gray-200">
            <i class="fa-solid fa-xmark text-lg"></i>
          </button>
        </div>

        <div class="p-5 flex-1 overflow-y-auto space-y-4">
          
          <div>
            <label class="block text-[11px] font-bold text-gray-500 mb-1 uppercase tracking-wider">Situação</label>
            <select [(ngModel)]="filtros.situacao" (ngModelChange)="emitirFiltros()"
                    class="w-full px-3 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none shadow-sm text-slate-700">
              <option value="">Todas as situações</option>
              <option value="FUNCIONANDO">Funcionando</option>
              <option value="PARALISADA">Paralisada</option>
              <option value="EXTINTA">Extinta</option>
            </select>
          </div>

          <div>
            <label class="block text-[11px] font-bold text-gray-500 mb-1 uppercase tracking-wider">Tipo de Acesso</label>
            <select [(ngModel)]="filtros.tipoAcesso" (ngModelChange)="emitirFiltros()"
                    class="w-full px-3 py-2.5 bg-white border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none shadow-sm text-slate-700">
              <option value="">Todos os acessos</option>
              <option value="TERRESTRE">Terrestre</option>
              <option value="FLUVIAL">Fluvial</option>
              <option value="MISTO">Misto</option>
            </select>
          </div>

          <div class="pt-3 space-y-2 border-t border-slate-100">
            <label class="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
              <input type="checkbox" [(ngModel)]="filtros.apenasComInternet" (ngModelChange)="emitirFiltros()" class="w-4 h-4 text-emerald-600 rounded border-gray-300 focus:ring-emerald-500">
              Apenas com Internet
            </label>

            <label class="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
              <input type="checkbox" [(ngModel)]="filtros.apenasComPredio" (ngModelChange)="emitirFiltros()" class="w-4 h-4 text-emerald-600 rounded border-gray-300 focus:ring-emerald-500">
              Apenas com Prédio Próprio
            </label>
          </div>
          
        </div>
      </aside>

      <button (click)="toggleGaveta()"
              class="absolute top-6 -right-10 w-10 h-12 bg-white/95 backdrop-blur-md shadow-[4px_0_10px_rgba(0,0,0,0.1)] rounded-r-xl border-y border-r border-gray-200 text-gray-600 hover:text-emerald-600 flex items-center justify-center transition-all duration-300"
              [class.opacity-0]="isAberto"
              [class.pointer-events-none]="isAberto">
        <i class="fa-solid fa-sliders"></i>
      </button>

    </div>
  `
})
export class MapaFiltrosComponent {
  isAberto = true;

  @Output() filtrosAlterados = new EventEmitter<any>();

  filtros = {
    situacao: '',
    tipoAcesso: '',
    apenasComInternet: false,
    apenasComPredio: false
  };

  toggleGaveta() {
    this.isAberto = !this.isAberto;
  }

  emitirFiltros() {
    this.filtrosAlterados.emit(this.filtros);
  }
}