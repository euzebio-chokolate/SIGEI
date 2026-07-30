import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-terra-indigena-form',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="max-w-4xl mx-auto space-y-5 font-sans">
      
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i> Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <a routerLink="/admin/terras-indigenas" class="hover:text-emerald-600 transition-colors">
          Terras Indígenas
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">Cadastrar T.I.</span>
      </nav>

      <div class="flex items-center gap-4">
        <a routerLink="/admin/terras-indigenas" 
           class="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-all shadow-sm">
          <i class="fa-solid fa-arrow-left"></i>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Cadastrar Terra Indígena</h1>
          <p class="text-sm text-slate-500 mt-0.5">Informe os dados oficiais de demarcação e localização do território.</p>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        
        <form class="p-8 space-y-8">
          
          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100 flex items-center gap-2">
              <i class="fa-solid fa-id-card text-slate-300"></i> Identificação Principal
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div class="md:col-span-2">
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Nome da Terra Indígena <span class="text-rose-500">*</span>
                </label>
                <input type="text" placeholder="Ex: Mamoadate" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Povos / Etnias Predominantes <span class="text-rose-500">*</span>
                </label>
                <input type="text" placeholder="Ex: Manchineri, Jaminawa" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Situação Jurídica (Fase) <span class="text-rose-500">*</span>
                </label>
                <select class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
                  <option value="">Selecione a fase...</option>
                  <option value="estudo">Em Estudo</option>
                  <option value="delimitada">Delimitada</option>
                  <option value="declarada">Declarada</option>
                  <option value="homologada">Homologada</option>
                  <option value="regularizada">Regularizada</option>
                </select>
              </div>
            </div>
          </div>

          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100 flex items-center gap-2">
              <i class="fa-solid fa-map-location-dot text-slate-300"></i> Dados Territoriais
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Área Total (Hectares)
                </label>
                <input type="number" placeholder="Ex: 313647" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Municípios Abrangidos
                </label>
                <input type="text" placeholder="Ex: Sena Madureira, Assis Brasil" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>
              
              <div class="md:col-span-2">
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">Observações ou Marco Legal</label>
                <textarea rows="3" placeholder="Decreto de homologação, número da portaria, etc." 
                          class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm resize-none"></textarea>
              </div>
            </div>
          </div>

          <div class="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <a routerLink="/admin/terras-indigenas" 
               class="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 transition-all">
              Cancelar
            </a>
            <button type="submit" 
                    class="bg-emerald-600 hover:bg-emerald-700 text-white px-6 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all">
              <i class="fa-solid fa-check text-xs"></i>
              Salvar Terra Indígena
            </button>
          </div>

        </form>

      </div>
    </div>
  `
})
export class TerraIndigenaFormComponent {}