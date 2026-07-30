import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-municipios-lista',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="space-y-5 font-sans">
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a
          routerLink="/admin/dashboard"
          class="hover:text-emerald-600 transition-colors flex items-center gap-1.5"
        >
          <i class="fa-solid fa-house text-slate-400"></i> Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">Municípios</span>
      </nav>

      <div>
        <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Gestão de Municípios</h1>
        <p class="text-sm text-slate-500 mt-1">
          Gerencie os municípios cadastrados e a vinculação de escolas indígenas.
        </p>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div
          class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between"
        >
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Total de Municípios
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">22</p>
          </div>
          <div
            class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 text-lg"
          >
            <i class="fa-solid fa-city"></i>
          </div>
        </div>

        <div
          class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between"
        >
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Terras Indígenas
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">20</p>
          </div>
          <div
            class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 text-lg"
          >
            <i class="fa-solid fa-circle-check"></i>
          </div>
        </div>

        <div
          class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between"
        >
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Escolas Atendidas
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">89</p>
          </div>
          <div
            class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center text-amber-600 text-lg"
          >
            <i class="fa-solid fa-school"></i>
          </div>
        </div>

        <div
          class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between"
        >
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Aldeias Vinculadas
            </span>
            <p class="text-2xl font-bold text-slate-800 mt-1">145</p>
          </div>
          <div
            class="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600 text-lg"
          >
            <i class="fa-solid fa-tents"></i>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr
                class="bg-slate-50/80 text-slate-400 text-[11px] font-bold uppercase tracking-wider border-b border-slate-100"
              >
                <th class="px-6 py-3.5">Município</th>
                <th class="px-6 py-3.5">Terras Indígenas</th>
                <th class="px-6 py-3.5">Aldeias</th>
                <th class="px-6 py-3.5">Escolas</th>
              </tr>
            </thead>

            <tbody class="divide-y divide-slate-100 text-sm text-slate-700">
              <tr
                *ngFor="let mun of dummyMunicipios"
                class="hover:bg-slate-50/60 transition-colors"
              >
                <td class="px-6 py-4">
                  <div class="font-semibold text-slate-800">
                    {{ mun.nome }}
                  </div>
                  <span class="text-xs text-slate-400"> Região de cobertura SIGEI </span>
                </td>

                <td class="px-6 py-4 font-medium text-slate-600">
                  <i class="fa-solid fa-mountain-sun text-emerald-500 mr-1.5"></i>
                  {{ mun.totalTerras }}
                </td>

                <td class="px-6 py-4 font-medium text-slate-600">
                  <i class="fa-solid fa-tents text-amber-500 mr-1.5"></i>
                  {{ mun.totalAldeias }}
                </td>

                <td class="px-6 py-4 font-medium text-slate-600">
                  <i class="fa-solid fa-school text-slate-300 mr-1.5"></i>
                  {{ mun.totalEscolas }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div
          class="px-6 py-4 border-t border-slate-100 bg-slate-50/30 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500"
        ></div>
      </div>
    </div>
  `,
})
export class MunicipiosListaComponent {
  dummyMunicipios = [
    {
      id: 1,
      nome: 'Rio Branco',
      estado: 'AC',
      totalTerras: 5,
      totalAldeias: 28,
      totalEscolas: 14,
    },
    {
      id: 2,
      nome: 'Cruzeiro do Sul',
      estado: 'AC',
      totalTerras: 8,
      totalAldeias: 41,
      totalEscolas: 22,
    },
    {
      id: 3,
      nome: 'Sena Madureira',
      estado: 'AC',
      totalTerras: 3,
      totalAldeias: 17,
      totalEscolas: 9,
    },
    {
      id: 4,
      nome: 'Tarauacá',
      estado: 'AC',
      totalTerras: 6,
      totalAldeias: 32,
      totalEscolas: 18,
    },
    {
      id: 5,
      nome: 'Feijó',
      estado: 'AC',
      totalTerras: 4,
      totalAldeias: 27,
      totalEscolas: 12,
    },
  ];
}
