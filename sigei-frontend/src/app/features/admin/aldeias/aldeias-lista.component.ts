import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-aldeias-lista',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <div class="space-y-5 font-sans">
      
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i> Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">Aldeias</span>
      </nav>

      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Gestão de Aldeias</h1>
          <p class="text-sm text-slate-500 mt-1">Gere as comunidades, as suas lideranças e infraestruturas escolares.</p>
        </div>
        
        <a routerLink="/admin/aldeias/novo" 
           class="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all">
          <i class="fa-solid fa-plus text-xs"></i>
          Nova Aldeia
        </a>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total de Aldeias</span>
            <p class="text-2xl font-bold text-slate-800 mt-1">145</p>
          </div>
          <div class="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600 text-lg">
            <i class="fa-solid fa-tents"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Com Escola</span>
            <p class="text-2xl font-bold text-slate-800 mt-1">89</p>
          </div>
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 text-lg">
            <i class="fa-solid fa-school-circle-check"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Sem Escola</span>
            <p class="text-2xl font-bold text-slate-800 mt-1">56</p>
          </div>
          <div class="w-12 h-12 bg-rose-50 rounded-xl flex items-center justify-center text-rose-600 text-lg">
            <i class="fa-solid fa-school-circle-xmark"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Famílias (Aprox.)</span>
            <p class="text-2xl font-bold text-slate-800 mt-1">3.2k</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 text-lg">
            <i class="fa-solid fa-people-roof"></i>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        
        <div class="p-5 border-b border-slate-100 bg-slate-50/50 flex flex-col md:flex-row gap-3 justify-between items-center">
          <div class="relative w-full md:w-96">
            <i class="fa-solid fa-search absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm"></i>
            <input type="text" placeholder="Buscar por nome da aldeia ou cacique..." 
                   class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all text-slate-700 placeholder:text-slate-400 shadow-sm">
          </div>

          <div class="flex items-center gap-2 w-full md:w-auto">
            <select class="px-3.5 py-2.5 bg-white border border-slate-200 rounded-xl text-sm text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 shadow-sm">
              <option value="">Filtro: Município</option>
              <option value="rio-branco">Rio Branco</option>
              <option value="cruzeiro-sul">Cruzeiro do Sul</option>
            </select>
            
            <select class="px-3.5 py-2.5 bg-white border border-slate-200 rounded-xl text-sm text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 shadow-sm">
              <option value="">Filtro: T.I.</option>
              <option value="mamoadate">T.I. Mamoadate</option>
              <option value="alto-purus">T.I. Alto Rio Purus</option>
            </select>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/80 text-slate-400 text-[11px] font-bold uppercase tracking-wider border-b border-slate-100">
                <th class="px-6 py-3.5">Nome da Aldeia</th>
                <th class="px-6 py-3.5">Cacique / Liderança</th>
                <th class="px-6 py-3.5">Localização (T.I. / Mun)</th>
                <th class="px-6 py-3.5 text-center">Possui Escola?</th>
                <th class="px-6 py-3.5 text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 text-sm text-slate-700">
              <tr *ngFor="let aldeia of dummyAldeias" class="hover:bg-slate-50/60 transition-colors group">
                <td class="px-6 py-4">
                  <div class="font-semibold text-slate-800">{{ aldeia.nome }}</div>
                  <span class="text-xs text-slate-400">{{ aldeia.etnia }}</span>
                </td>
                <td class="px-6 py-4">
                  <div class="flex items-center gap-2">
                    <div class="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-slate-500 text-[10px]">
                      <i class="fa-solid fa-user"></i>
                    </div>
                    <span class="font-medium text-slate-600">{{ aldeia.cacique }}</span>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span class="block font-medium text-slate-700 truncate max-wxs" title="{{ aldeia.ti }}">
                    {{ aldeia.ti }}
                  </span>
                  <span class="text-xs text-slate-400"><i class="fa-solid fa-location-dot mr-1 text-slate-300"></i>{{ aldeia.municipio }}</span>
                </td>
                <td class="px-6 py-4 text-center">
                  <span *ngIf="aldeia.temEscola" class="inline-flex items-center justify-center w-8 h-8 rounded-full bg-emerald-50 text-emerald-600 border border-emerald-100" title="Possui Escola">
                    <i class="fa-solid fa-check"></i>
                  </span>
                  <span *ngIf="!aldeia.temEscola" class="inline-flex items-center justify-center w-8 h-8 rounded-full bg-rose-50 text-rose-500 border border-rose-100" title="Não Possui Escola">
                    <i class="fa-solid fa-xmark"></i>
                  </span>
                </td>
                <td class="px-6 py-4 text-right">
                  <div class="flex items-center justify-end gap-1">
                    <a [routerLink]="['/admin/aldeias/editar', aldeia.id]" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-all" title="Editar">
                      <i class="fa-solid fa-pen-to-square text-xs"></i>
                    </a>
                    <button class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-all" title="Excluir">
                      <i class="fa-solid fa-trash-can text-xs"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="px-6 py-4 border-t border-slate-100 bg-slate-50/30 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <span>Mostrando <strong>1</strong> a <strong>4</strong> de <strong>145</strong> registos</span>
          <div class="flex items-center gap-1">
            <button class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 disabled:opacity-50 transition-colors">
              <i class="fa-solid fa-chevron-left mr-1"></i> Anterior
            </button>
            <button class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 transition-colors">
              Próximo <i class="fa-solid fa-chevron-right ml-1"></i>
            </button>
          </div>
        </div>

      </div>
    </div>
  `
})
export class AldeiasListaComponent {
  dummyAldeias = [
    { id: 1, nome: 'Aldeia Mutum', etnia: 'Yawanawá', cacique: 'Bira Yawanawá', ti: 'T.I. Rio Gregório', municipio: 'Tarauacá', temEscola: true },
    { id: 2, nome: 'Aldeia Nova Esperança', etnia: 'Huni Kuin', cacique: 'Nixiwaka', ti: 'T.I. Kaxinawá do Rio Jordão', municipio: 'Jordão', temEscola: true },
    { id: 3, nome: 'Aldeia Extrema', etnia: 'Manchineri', cacique: 'Lucas Manchineri', ti: 'T.I. Mamoadate', municipio: 'Sena Madureira', temEscola: false },
    { id: 4, nome: 'Aldeia Morada Nova', etnia: 'Noke Koí', cacique: 'Fernando Katukina', ti: 'T.I. Campinas/Katukina', municipio: 'Cruzeiro do Sul', temEscola: true }
  ];
}