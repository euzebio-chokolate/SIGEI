import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-aldeia-form',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="max-w-4xl mx-auto space-y-5 font-sans">
      
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i> Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <a routerLink="/admin/aldeias" class="hover:text-emerald-600 transition-colors">
          Aldeias
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">Cadastrar Aldeia</span>
      </nav>

      <div class="flex items-center gap-4">
        <a routerLink="/admin/aldeias" 
           class="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-all shadow-sm">
          <i class="fa-solid fa-arrow-left"></i>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Cadastrar Aldeia</h1>
          <p class="text-sm text-slate-500 mt-0.5">Preenche as informações da comunidade e a sua vinculação territorial.</p>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        
        <form class="p-8 space-y-8">
          
          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100 flex items-center gap-2">
              <i class="fa-solid fa-users-viewfinder text-slate-300"></i> Identificação e Liderança
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Nome da Aldeia <span class="text-rose-500">*</span>
                </label>
                <input type="text" placeholder="Ex: Aldeia Mutum" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Etnia Maioritária
                </label>
                <input type="text" placeholder="Ex: Yawanawá" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Nome do Cacique / Liderança Principal
                </label>
                <input type="text" placeholder="Ex: Bira Yawanawá" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Número Estimado de Famílias
                </label>
                <input type="number" placeholder="Ex: 45" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>
            </div>
          </div>

          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100 flex items-center gap-2">
              <i class="fa-solid fa-map-pin text-slate-300"></i> Localização Geográfica
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Terra Indígena (T.I.) Vinculada <span class="text-rose-500">*</span>
                </label>
                <select class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
                  <option value="">Selecione a T.I....</option>
                  <option value="1">T.I. Rio Gregório</option>
                  <option value="2">T.I. Mamoadate</option>
                  <option value="3">T.I. Campinas/Katukina</option>
                </select>
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Município <span class="text-rose-500">*</span>
                </label>
                <select class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
                  <option value="">Selecione o município...</option>
                  <option value="1">Tarauacá</option>
                  <option value="2">Sena Madureira</option>
                  <option value="3">Cruzeiro do Sul</option>
                </select>
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">Latitude (S)</label>
                <input type="text" placeholder="Ex: -8.2435" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">Longitude (W)</label>
                <input type="text" placeholder="Ex: -71.5542" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>
            </div>
          </div>

          <div class="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <a routerLink="/admin/aldeias" 
               class="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 transition-all">
              Cancelar
            </a>
            <button type="submit" 
                    class="bg-emerald-600 hover:bg-emerald-700 text-white px-6 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all">
              <i class="fa-solid fa-check text-xs"></i>
              Guardar Aldeia
            </button>
          </div>

        </form>

      </div>
    </div>
  `
})
export class AldeiaFormComponent {}