import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-escola-form',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <div class="max-w-4xl mx-auto space-y-5 font-sans pb-10">
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a
          routerLink="/admin/dashboard"
          class="hover:text-emerald-600 transition-colors flex items-center gap-1.5"
        >
          <i class="fa-solid fa-house text-slate-400"></i> Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <a routerLink="/admin/escolas" class="hover:text-emerald-600 transition-colors">
          Escolas
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">Cadastrar Escola</span>
      </nav>

      <div class="flex items-center gap-4">
        <a
          routerLink="/admin/escolas"
          class="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-all shadow-sm"
        >
          <i class="fa-solid fa-arrow-left"></i>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">
            Cadastrar Escola ou Anexo
          </h1>
          <p class="text-sm text-slate-500 mt-0.5">
            Introduza os dados da unidade escolar, equipe e infraestrutura.
          </p>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <form class="p-8 space-y-8">
          <div>
            <h3
              class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100 flex items-center gap-2"
            >
              <i class="fa-solid fa-school text-slate-300"></i> Dados da Instituição
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div class="md:col-span-2">
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Nome da Escola / Sala Anexa <span class="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  placeholder="Ex: Escola Indígena Ixã"
                  class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">Código INEP</label>
                <input
                  type="text"
                  placeholder="Ex: 12003456"
                  class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Tipo de Unidade <span class="text-rose-500">*</span>
                </label>
                <div class="flex gap-4 mt-2">
                  <label class="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
                    <input
                      type="radio"
                      name="tipoEscola"
                      value="sede"
                      (change)="isAnexo = false"
                      checked
                      class="w-4 h-4 text-emerald-600 focus:ring-emerald-500"
                    />
                    Escola Sede
                  </label>
                  <label class="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
                    <input
                      type="radio"
                      name="tipoEscola"
                      value="anexo"
                      (change)="isAnexo = true"
                      class="w-4 h-4 text-emerald-600 focus:ring-emerald-500"
                    />
                    Sala Anexa / Extensão
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div *ngIf="isAnexo" class="bg-amber-50/50 p-5 rounded-xl border border-amber-100">
            <label class="block text-xs font-bold text-amber-800 mb-1.5 flex items-center gap-2">
              <i class="fa-solid fa-link"></i> Escola Sede Vinculada
              <span class="text-rose-500">*</span>
            </label>
            <select
              class="w-full px-4 py-2.5 bg-white border border-amber-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-amber-500/20 focus:border-amber-500 text-slate-800 transition-all shadow-sm"
            >
              <option value="">Buscar escola sede...</option>
              <option value="1">Escola Indígena Ixã</option>
            </select>
          </div>

          <div>
            <h3
              class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100 flex items-center gap-2"
            >
              <i class="fa-solid fa-tents text-slate-300"></i> Vinculação
            </h3>
            <div>
              <label class="block text-xs font-semibold text-slate-700 mb-1.5">
                Aldeia onde a escola está situada <span class="text-rose-500">*</span>
              </label>
              <select
                class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
              >
                <option value="">Selecione a aldeia...</option>
                <option value="1">Aldeia Mutum</option>
              </select>
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-4 pb-2 border-b border-slate-100">
              <h3
                class="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-2"
              >
                <i class="fa-solid fa-users text-slate-300"></i> Equipe Escolar e Logística
              </h3>
              <span
                class="text-[9px] bg-indigo-50 text-indigo-500 px-2 py-0.5 rounded border border-indigo-100 font-bold uppercase tracking-wider"
              >
                Gestão de Pessoal
              </span>
            </div>

            <div class="bg-slate-50/70 p-6 rounded-xl border border-slate-100">
              <div class="mb-6 w-full sm:w-48">
                <label class="block text-xs font-semibold text-slate-700 mb-1.5"
                  >Ano de Referência <span class="text-rose-500">*</span></label
                >
                <input
                  type="number"
                  placeholder="2024"
                  value="2024"
                  class="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                />
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-y-4 gap-x-6">
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Professor Responsável</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Equipe Gestora</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Merendeira</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Zeladora</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-emerald-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Barqueiro</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-indigo-500"
                  ></div>
                  <span class="ml-3 text-sm font-bold text-indigo-700">Necessita Transporte</span>
                </label>
              </div>
            </div>
          </div>

          <div>
            <div class="flex items-center justify-between mb-4 pb-2 border-b border-slate-100">
              <h3
                class="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-2"
              >
                <i class="fa-solid fa-building text-slate-300"></i> Infraestrutura e Instalações
              </h3>
              <span
                class="text-[9px] bg-sky-50 text-sky-600 px-2 py-0.5 rounded border border-sky-100 font-bold uppercase tracking-wider"
              >
                Gestão Predial
              </span>
            </div>

            <div class="bg-slate-50/70 p-6 rounded-xl border border-slate-100 space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1.5"
                    >Ano de Referência <span class="text-rose-500">*</span></label
                  >
                  <input
                    type="number"
                    placeholder="2024"
                    value="2024"
                    class="w-full px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 text-slate-800 shadow-sm"
                  />
                </div>
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1.5"
                    >Ano de Construção</label
                  >
                  <input
                    type="number"
                    placeholder="Ex: 2015"
                    class="w-full px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 text-slate-800 shadow-sm"
                  />
                </div>
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1.5"
                    >Número de Salas</label
                  >
                  <input
                    type="number"
                    placeholder="Ex: 4"
                    class="w-full px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 text-slate-800 shadow-sm"
                  />
                </div>

                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1.5"
                    >Situação do Prédio</label
                  >
                  <select
                    class="w-full px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 text-slate-800 shadow-sm"
                  >
                    <option value="">Selecione...</option>
                    <option value="boa">Boa</option>
                    <option value="regular">Regular</option>
                    <option value="ruim">Ruim (Precisa de Reforma)</option>
                    <option value="inativa">Inativa / Interditada</option>
                  </select>
                </div>
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1.5"
                    >Tipo de Acesso</label
                  >
                  <select
                    class="w-full px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 text-slate-800 shadow-sm"
                  >
                    <option value="">Selecione...</option>
                    <option value="terrestre">Terrestre (Estrada/Ramal)</option>
                    <option value="fluvial">Fluvial (Barco)</option>
                    <option value="misto">Misto (Terrestre + Fluvial)</option>
                    <option value="aereo">Aéreo</option>
                  </select>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-5 pt-4 border-t border-slate-200/60">
                <div>
                  <label
                    class="block text-xs font-semibold text-slate-700 mb-1.5 flex items-center gap-1"
                    ><i class="fa-solid fa-location-crosshairs text-sky-500"></i> Latitude
                    (S)</label
                  >
                  <input
                    type="text"
                    placeholder="Ex: -8.2435"
                    class="w-full px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 text-slate-800 shadow-sm"
                  />
                </div>
                <div>
                  <label
                    class="block text-xs font-semibold text-slate-700 mb-1.5 flex items-center gap-1"
                    ><i class="fa-solid fa-location-crosshairs text-sky-500"></i> Longitude
                    (W)</label
                  >
                  <input
                    type="text"
                    placeholder="Ex: -71.5542"
                    class="w-full px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500/20 focus:border-sky-500 text-slate-800 shadow-sm"
                  />
                </div>
              </div>

              <div
                class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-y-4 gap-x-6 pt-4 border-t border-slate-200/60"
              >
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-300 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-sky-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Tem Prédio Próprio?</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-300 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-sky-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Tem Água Potável?</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-300 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-sky-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Tem Internet?</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-300 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-sky-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Tem Energia Elétrica?</span
                  >
                </label>
                <label class="relative flex items-center cursor-pointer group">
                  <input type="checkbox" class="sr-only peer" />
                  <div
                    class="w-10 h-5 bg-slate-300 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-sky-500"
                  ></div>
                  <span class="ml-3 text-sm font-medium text-slate-600 group-hover:text-slate-900"
                    >Tem Registro Fotográfico?</span
                  >
                </label>
              </div>
            </div>
          </div>

          <div
            class="pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-4"
          >
            <p class="text-[11px] text-slate-400 italic">
              <i class="fa-solid fa-layer-group mr-1"></i>
              Ao salvar, a Escola, a Equipe e a Infraestrutura serão registrados de forma integrada.
            </p>

            <div class="flex items-center gap-3">
              <a
                routerLink="/admin/escolas"
                class="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 transition-all"
              >
                Cancelar
              </a>
              <button
                type="submit"
                class="bg-emerald-600 hover:bg-emerald-700 text-white px-6 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all"
              >
                <i class="fa-solid fa-check-double text-xs"></i>
                Salvar Cadastro Completo
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class EscolaFormComponent {
  isAnexo = false;

  /* ========================================================================
  DICA DE ARQUITETURA PARA O FUTURO (Integração com a API Java)
  ========================================================================
  Como você tem 1 tabela principal (Escola) e 2 filhas (Equipe e Infra),
  você deverá usar o forkJoin do RxJS para salvar as filhas simultaneamente
  após a escola ser gerada.

  salvar() {
    this.escolaService.salvarEscola(dadosEscola).pipe(
      switchMap((escolaSalva) => {
        // Atribui a Chave Estrangeira
        dadosEquipe.escola_id = escolaSalva.id;
        dadosInfra.escola_id = escolaSalva.id;

        // Dispara as duas requisições ao mesmo tempo
        return forkJoin([
          this.equipeService.salvarEquipe(dadosEquipe),
          this.infraService.salvarInfraestrutura(dadosInfra)
        ]);
      })
    ).subscribe({
      next: (resultados) => {
        console.log('Escola, Equipe e Infraestrutura salvos!', resultados);
        this.router.navigate(['/admin/escolas']);
      },
      error: (err) => console.error('Falha no cadastro múltiplo:', err)
    });
  }
  */
}
