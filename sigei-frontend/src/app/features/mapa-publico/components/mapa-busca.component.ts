import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-mapa-busca',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="absolute top-4 left-1/2 -translate-x-1/2 z-[1010] w-full max-w-md px-4 pointer-events-auto">
      
      <div class="relative bg-white/95 backdrop-blur-md shadow-xl rounded-2xl border border-gray-100 p-2">
        <div class="relative flex items-center">
          <i class="fa-solid fa-magnifying-glass absolute left-3.5 text-slate-400 text-sm"></i>
          
          <input type="text" 
                 [(ngModel)]="termoBusca" 
                 (ngModelChange)="aoDigitar()"
                 (focus)="mostrarSugestoes = true"
                 placeholder="Buscar por nome da escola ou código INEP..." 
                 class="w-full pl-10 pr-9 py-2 bg-transparent text-sm text-slate-800 placeholder:text-slate-400 outline-none">

          @if (termoBusca) {
            <button (click)="limpar()" class="absolute right-2.5 text-slate-400 hover:text-slate-600 p-1">
              <i class="fa-solid fa-xmark text-xs"></i>
            </button>
          }
        </div>

        @if (mostrarSugestoes && sugestoes.length > 0) {
          <ul class="absolute left-0 right-0 top-full mt-2 bg-white/98 backdrop-blur-md border border-gray-200/80 rounded-2xl shadow-2xl max-h-60 overflow-y-auto z-[1020] divide-y divide-slate-100">
            @for (item of sugestoes; track item.id) {
              <li (click)="selecionar(item)"
                  class="p-3 hover:bg-emerald-50/80 cursor-pointer transition-colors group">
                
                <div class="font-bold text-xs transition-colors"
                     [class.text-red-600]="isParalisada(item.situacao)"
                     [class.text-slate-800]="!isParalisada(item.situacao)">
                  {{ item.escola?.nome || 'Escola sem Nome' }}
                </div>

                <div class="flex items-center gap-2 mt-1 text-[10px] text-slate-500">
                  <span class="font-mono bg-slate-100 px-1.5 py-0.5 rounded border border-slate-200">
                    INEP: {{ item.escola?.codigo || 'N/A' }}
                  </span>
                  
                  @if (item.situacao) {
                    <span class="font-semibold"
                          [class.text-red-600]="isParalisada(item.situacao)"
                          [class.text-emerald-600]="!isParalisada(item.situacao)">
                      • {{ item.situacao }}
                    </span>
                  }
                </div>
              </li>
            }
          </ul>
        }
      </div>

    </div>
  `
})
export class MapaBuscaComponent {
  @Input() dadosEscolas: any[] = [];
  @Output() buscaAlterada = new EventEmitter<string>();
  @Output() escolaSelecionada = new EventEmitter<any>();

  termoBusca = '';
  sugestoes: any[] = [];
  mostrarSugestoes = false;

  // Método auxiliar que valida se a situação é Paralisada / Paralizada
  isParalisada(situacao?: string): boolean {
    const norm = situacao?.toUpperCase() || '';
    return norm === 'PARALISADA' || norm === 'PARALIZADA';
  }

  aoDigitar() {
    const termo = this.termoBusca.trim().toLowerCase();
    
    if (termo.length > 0) {
      this.sugestoes = this.dadosEscolas.filter(item => {
        const nome = item.escola?.nome?.toLowerCase() || '';
        const codigo = item.escola?.codigo?.toLowerCase() || '';
        return nome.includes(termo) || codigo.includes(termo);
      }).slice(0, 25);
      
      this.mostrarSugestoes = true;
    } else {
      this.sugestoes = [];
      this.mostrarSugestoes = false;
    }

    this.buscaAlterada.emit(this.termoBusca);
  }

  selecionar(item: any) {
    this.termoBusca = item.escola?.nome || item.escola?.codigo || '';
    this.mostrarSugestoes = false;
    this.sugestoes = [];
    
    this.buscaAlterada.emit(this.termoBusca);
    this.escolaSelecionada.emit(item);
  }

  limpar() {
    this.termoBusca = '';
    this.sugestoes = [];
    this.mostrarSugestoes = false;
    this.buscaAlterada.emit('');
  }
}