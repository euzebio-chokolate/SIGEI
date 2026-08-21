import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">Visão Geral</h2>
        <p class="text-sm text-gray-500 mt-1">Resumo dos dados registados no sistema SIGEI.</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div
          class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow relative overflow-hidden"
        >
          <i
            class="fa-solid fa-city absolute -right-4 -bottom-4 text-7xl text-gray-50 opacity-50"
          ></i>
          <h3 class="text-sm font-bold text-gray-500 uppercase tracking-wider mb-2 relative z-10">
            Municípios
          </h3>
          <p class="text-4xl font-extrabold text-gray-800 relative z-10">22</p>
        </div>

        <div
          class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow relative overflow-hidden"
        >
          <i
            class="fa-solid fa-tents absolute -right-4 -bottom-4 text-7xl text-blue-50 opacity-50"
          ></i>
          <h3 class="text-sm font-bold text-gray-500 uppercase tracking-wider mb-2 relative z-10">
            Aldeias
          </h3>
          <p class="text-4xl font-extrabold text-blue-600 relative z-10">145</p>
        </div>

        <div
          class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow relative overflow-hidden"
        >
          <i
            class="fa-solid fa-school-flag absolute -right-4 -bottom-4 text-7xl text-green-50 opacity-50"
          ></i>
          <h3 class="text-sm font-bold text-gray-500 uppercase tracking-wider mb-2 relative z-10">
            Escolas
          </h3>
          <p class="text-4xl font-extrabold text-green-600 relative z-10">89</p>
        </div>
      </div>

      <div
        class="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 min-h-[300px] flex flex-col items-center justify-center border-dashed"
      >
        <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4">
          <i class="fa-solid fa-chart-column text-3xl text-gray-300"></i>
        </div>
        <p class="text-gray-500 font-medium">Os gráficos de desempenho serão renderizados aqui.</p>
        <p class="text-xs text-gray-400 mt-1">Conecte a API para visualizar estatísticas reais.</p>
      </div>
    </div>
  `,
})
export class DashboardComponent {}
