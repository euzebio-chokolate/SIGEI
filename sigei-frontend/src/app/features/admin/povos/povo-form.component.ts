import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-povo-form',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="max-w-4xl mx-auto space-y-5 font-sans">
      
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i> Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <a routerLink="/admin/povos" class="hover:text-emerald-600 transition-colors">
          Povos Indígenas
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">Cadastrar Povo</span>
      </nav>

      <div class="flex items-center gap-4">
        <a routerLink="/admin/povos" 
           class="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-all shadow-sm">
          <i class="fa-solid fa-arrow-left"></i>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Cadastrar Povo (Etnia)</h1>
          <p class="text-sm text-slate-500 mt-0.5">Informe as características culturais e linguísticas do povo.</p>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        
        <form class="p-8 space-y-8">
          
          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100 flex items-center gap-2">
              <i class="fa-solid fa-fingerprint text-slate-300"></i> Identidade e Nomenclatura
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Nome Conhecido / Etnia <span class="text-rose-500">*</span>
                </label>
                <input type="text" placeholder="Ex: Kaxinawá" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Autodenominação <span class="text-rose-500">*</span>
                </label>
                <input type="text" placeholder="Ex: Huni Kuin" 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
                <p class="text-[10px] text-slate-400 mt-1">Como o povo se chama em sua própria língua.</p>
              </div>
            </div>
          </div>

          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100 flex items-center gap-2">
              <i class="fa-solid fa-comments text-slate-300"></i> Classificação Linguística
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Tronco Linguístico
                </label>
                <select class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
                  <option value="">Selecione...</option>
                  <option value="pano">Pano</option>
                  <option value="arawak">Arawak (Aruak)</option>
                  <option value="tupi">Tupi</option>
                  <option value="macro-je">Macro-Jê</option>
                  <option value="isolada">Língua Isolada</option>
                </select>
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Família Linguística
                </label>
                <input type="text" placeholder="Ex: Pano, Aruak..." 
                       class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm">
              </div>

              <div class="md:col-span-2">
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">Histórico Breve ou Notas Culturais</label>
                <textarea rows="3" placeholder="Informações relevantes sobre a cultura, costumes ou contato..." 
                          class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm resize-none"></textarea>
              </div>
            </div>
          </div>

          <div class="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <a routerLink="/admin/povos" 
               class="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 transition-all">
              Cancelar
            </a>
            <button type="button" 
                    class="bg-emerald-600 hover:bg-emerald-700 text-white px-6 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all">
              <i class="fa-solid fa-check text-xs"></i>
              Salvar Povo
            </button>
          </div>

        </form>

      </div>
    </div>
  `
})
export class PovoFormComponent {}