import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-escolas-lista',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <div class="space-y-5 font-sans pb-10">
      
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i> Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">Escolas</span>
      </nav>

      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">Gestão de Escolas</h1>
          <p class="text-sm text-slate-500 mt-1">Administre as unidades, sua infraestrutura e equipe lotada.</p>
        </div>
        
        <a routerLink="/admin/escolas/novo" 
           class="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all">
          <i class="fa-solid fa-plus text-xs"></i>
          Nova Escola
        </a>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total de Unidades</span>
            <p class="text-2xl font-bold text-slate-800 mt-1">89</p>
          </div>
          <div class="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600 text-lg">
            <i class="fa-solid fa-school"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Com Internet</span>
            <p class="text-2xl font-bold text-slate-800 mt-1">42</p>
          </div>
          <div class="w-12 h-12 bg-sky-50 rounded-xl flex items-center justify-center text-sky-600 text-lg">
            <i class="fa-solid fa-wifi"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">S/ Prédio Próprio</span>
            <p class="text-2xl font-bold text-slate-800 mt-1">15</p>
          </div>
          <div class="w-12 h-12 bg-amber-50 rounded-xl flex items-center justify-center text-amber-600 text-lg">
            <i class="fa-solid fa-tents"></i>
          </div>
        </div>

        <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm flex items-center justify-between">
          <div>
            <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">Requer Transporte</span>
            <p class="text-2xl font-bold text-slate-800 mt-1">28</p>
          </div>
          <div class="w-12 h-12 bg-rose-50 rounded-xl flex items-center justify-center text-rose-600 text-lg">
            <i class="fa-solid fa-van-shuttle"></i>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        
        <div class="p-5 border-b border-slate-100 bg-slate-50/50 flex flex-col md:flex-row gap-3 justify-between items-center">
          <div class="relative w-full md:w-96">
            <i class="fa-solid fa-search absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 text-sm"></i>
            <input type="text" placeholder="Buscar por nome ou código INEP..." 
                   class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 transition-all text-slate-700 placeholder:text-slate-400 shadow-sm">
          </div>

          <div class="flex items-center gap-2 w-full md:w-auto">
            <select class="px-3.5 py-2.5 bg-white border border-slate-200 rounded-xl text-sm text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 shadow-sm">
              <option value="">Tipo: Todos</option>
              <option value="sede">Escola Sede</option>
              <option value="anexo">Anexo / Extensão</option>
            </select>
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse">
            <thead>
              <tr class="bg-slate-50/80 text-slate-400 text-[11px] font-bold uppercase tracking-wider border-b border-slate-100">
                <th class="px-6 py-3.5">Escola e Código</th>
                <th class="px-6 py-3.5">Localização</th>
                <th class="px-6 py-3.5">Tipo</th>
                <th class="px-6 py-3.5">Infra e Logística</th>
                <th class="px-6 py-3.5 text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100 text-sm text-slate-700">
              
              <tr *ngFor="let escola of dummyEscolas" class="hover:bg-slate-50/60 transition-colors group">
                
                <td class="px-6 py-4">
                  <div class="font-bold text-slate-800">{{ escola.nome }}</div>
                  <div class="mt-1 flex items-center gap-2">
                    <span *ngIf="escola.inep" class="font-mono text-[11px] font-semibold text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded">
                      INEP: {{ escola.inep }}
                    </span>
                    <span *ngIf="!escola.inep" class="text-rose-400 text-[10px] font-bold uppercase tracking-wider bg-rose-50 px-1.5 py-0.5 rounded border border-rose-100">
                      Sem INEP
                    </span>
                  </div>
                </td>
                
                <td class="px-6 py-4">
                  <span class="font-medium text-slate-600 block">
                    <i class="fa-solid fa-tents text-slate-300 mr-1.5"></i> {{ escola.aldeia }}
                  </span>
                  <span *ngIf="escola.escolaSede" class="text-[10px] text-slate-400 flex items-center gap-1 mt-1">
                    <i class="fa-solid fa-link text-slate-300"></i> Vinculada a: {{ escola.escolaSede }}
                  </span>
                </td>

                <td class="px-6 py-4">
                  <span *ngIf="escola.tipo === 'Sede'" class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-100">
                    <i class="fa-solid fa-building-flag text-[10px]"></i> Sede
                  </span>
                  <span *ngIf="escola.tipo === 'Anexo'" class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-bold bg-amber-50 text-amber-700 border border-amber-100">
                    <i class="fa-solid fa-house-chimney text-[10px]"></i> Anexo
                  </span>
                </td>

                <td class="px-6 py-4">
                  <div class="flex flex-col gap-1.5">
                    
                    <div class="flex gap-1.5" *ngIf="escola.infraestrutura">
                      
                      <span [title]="escola.infraestrutura.temInternet ? 'Possui Internet' : 'Sem Internet'"
                            [class]="escola.infraestrutura.temInternet ? 'bg-sky-50 text-sky-600 border border-sky-100' : 'bg-slate-50 text-slate-300 border border-slate-100'"
                            class="w-6 h-6 rounded-md flex items-center justify-center text-[10px] transition-colors">
                        <i class="fa-solid fa-wifi"></i>
                      </span>
                      
                      <span [title]="escola.infraestrutura.temEnergia ? 'Possui Energia Elétrica' : 'Sem Energia'"
                            [class]="escola.infraestrutura.temEnergia ? 'bg-amber-50 text-amber-500 border border-amber-100' : 'bg-slate-50 text-slate-300 border border-slate-100'"
                            class="w-6 h-6 rounded-md flex items-center justify-center text-[10px] transition-colors">
                        <i class="fa-solid fa-bolt"></i>
                      </span>

                      <span [title]="escola.infraestrutura.temAgua ? 'Possui Água Potável' : 'Sem Água'"
                            [class]="escola.infraestrutura.temAgua ? 'bg-blue-50 text-blue-500 border border-blue-100' : 'bg-slate-50 text-slate-300 border border-slate-100'"
                            class="w-6 h-6 rounded-md flex items-center justify-center text-[10px] transition-colors">
                        <i class="fa-solid fa-faucet-drip"></i>
                      </span>
                    </div>

                    <div *ngIf="escola.equipe?.necessitaTransporte">
                      <span class="inline-flex items-center gap-1 text-[9px] font-bold uppercase tracking-wider text-rose-600 bg-rose-50 px-1.5 py-0.5 rounded border border-rose-100">
                        <i class="fa-solid fa-van-shuttle"></i> Necessita Transporte
                      </span>
                    </div>

                  </div>
                </td>

                <td class="px-6 py-4 text-right">
                  <div class="flex items-center justify-end gap-1">
                    <a [routerLink]="['/admin/escolas/editar', escola.id]" class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-all" title="Editar Unidade">
                      <i class="fa-solid fa-pen-to-square text-xs"></i>
                    </a>
                    <button class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-rose-600 hover:bg-rose-50 transition-all" title="Excluir Unidade">
                      <i class="fa-solid fa-trash-can text-xs"></i>
                    </button>
                  </div>
                </td>

              </tr>

            </tbody>
          </table>
        </div>

        <div class="px-6 py-4 border-t border-slate-100 bg-slate-50/30 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <span>Mostrando <strong>1</strong> a <strong>4</strong> de <strong>89</strong> registos</span>
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
export class EscolasListaComponent {
  
  // Agora usamos a tipagem enriquecida com equipe e infra
  dummyEscolas: Escola[] = [
    { 
      id: 1, 
      inep: '12003456', 
      nome: 'Escola Indígena Ixã', 
      tipo: 'Sede', 
      aldeia: 'Aldeia Mutum',
      infraestrutura: { temInternet: true, temEnergia: true, temAgua: true, situacao: 'boa' },
      equipe: { temProfessor: true, necessitaTransporte: false }
    },
    { 
      id: 2, 
      inep: null, 
      nome: 'Sala Anexa Xinã', 
      tipo: 'Anexo', 
      aldeia: 'Aldeia Nova Esperança', 
      escolaSede: 'Escola Indígena Ixã',
      infraestrutura: { temInternet: false, temEnergia: false, temAgua: true, situacao: 'regular' },
      equipe: { temProfessor: true, necessitaTransporte: true } // Destaca o transporte
    },
    { 
      id: 3, 
      inep: '12009876', 
      nome: 'Escola Yawanawá', 
      tipo: 'Sede', 
      aldeia: 'Aldeia Amparo',
      infraestrutura: { temInternet: false, temEnergia: true, temAgua: true, situacao: 'boa' },
      equipe: { temProfessor: true, necessitaTransporte: false }
    },
    { 
      id: 4, 
      inep: '12005555', 
      nome: 'Escola Kaxinawá', 
      tipo: 'Sede', 
      aldeia: 'Aldeia Caucho',
      infraestrutura: { temInternet: false, temEnergia: false, temAgua: false, situacao: 'ruim' },
      equipe: { temProfessor: false, necessitaTransporte: true }
    }
  ];
}

// CONTRATO DE TIPAGEM ATUALIZADO
interface Escola {
  id: number;
  inep: string | null;
  nome: string;
  tipo: 'Sede' | 'Anexo';
  aldeia: string;
  escolaSede?: string; 
  // Adição das novas tabelas relacionadas:
  infraestrutura?: {
    temInternet: boolean;
    temEnergia: boolean;
    temAgua: boolean;
    situacao: string;
  };
  equipe?: {
    temProfessor: boolean;
    necessitaTransporte: boolean;
  };
}