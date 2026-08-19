import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { MapaFiltrosComponent } from './components/mapa-filtros.component';
import * as L from 'leaflet';

@Component({
  selector: 'app-mapa-publico',
  standalone: true,
  imports: [HeaderComponent, MapaFiltrosComponent],
  template: `
    <div class="relative w-screen h-screen overflow-hidden bg-gray-200">
      
      <app-header></app-header>
      <app-mapa-filtros></app-mapa-filtros>

      <!-- Botão de Camadas -->
      <div class="absolute bottom-6 left-4 z-[1010] flex flex-col gap-2">
        <div class="bg-white/95 backdrop-blur-md p-2 rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.15)] border border-gray-100 flex flex-col gap-1 transition-all duration-300 origin-bottom-left"
             [class.opacity-0]="!menuCamadasAberto"
             [class.scale-0]="!menuCamadasAberto"
             [class.pointer-events-none]="!menuCamadasAberto">
          
          <button (click)="mudarCamada('padrao')" [class.bg-green-50]="camadaAtual === 'padrao'" [class.text-green-700]="camadaAtual === 'padrao'" class="text-left px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 rounded-lg font-semibold transition-colors">Padrão</button>
          <button (click)="mudarCamada('satelite')" [class.bg-green-50]="camadaAtual === 'satelite'" [class.text-green-700]="camadaAtual === 'satelite'" class="text-left px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 rounded-lg font-semibold transition-colors">Satélite</button>
          <button (click)="mudarCamada('terreno')" [class.bg-green-50]="camadaAtual === 'terreno'" [class.text-green-700]="camadaAtual === 'terreno'" class="text-left px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 rounded-lg font-semibold transition-colors">Terreno</button>
          <button (click)="mudarCamada('escuro')" [class.bg-green-50]="camadaAtual === 'escuro'" [class.text-green-700]="camadaAtual === 'escuro'" class="text-left px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 rounded-lg font-semibold transition-colors">Escuro</button>
        </div>

        <button (click)="menuCamadasAberto = !menuCamadasAberto" class="h-12 px-4 bg-white/95 backdrop-blur-md rounded-xl shadow-lg border border-gray-100 flex items-center gap-2.5 text-gray-700 hover:bg-gray-50 transition-all self-start pointer-events-auto">
          <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M2 17l10 5 10-5M2 12l10 5 10-5M12 2L2 7l10 5 10-5-10-5z" />
          </svg>
          <span class="text-base font-medium text-gray-700">{{ nomesCamadas[camadaAtual] }}</span>
        </button>
      </div>
      
      <!-- Div do Mapa -->
      <div id="map" class="absolute inset-0 z-0"></div>
    </div>
  `
})
export class MapaPublicoComponent implements OnInit, OnDestroy {
  private map!: L.Map;
  private tileLayerAtual!: L.TileLayer;
  
  // Usando injeção de dependência moderna do Angular para chamadas HTTP
  private http = inject(HttpClient);
  
  menuCamadasAberto = false;
  camadaAtual = 'padrao';

  nomesCamadas: { [key: string]: string } = {
    padrao: 'Padrão',
    satelite: 'Satélite',
    terreno: 'Terreno',
    escuro: 'Escuro'
  };

  private camadas: { [key: string]: L.TileLayer } = {
    padrao: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; OpenStreetMap' }),
    satelite: L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', { maxZoom: 17, attribution: '&copy; Esri World Imagery' }),
    terreno: L.tileLayer('https://{s}.opentopomap.org/{z}/{x}/{y}.png', { maxZoom: 17, attribution: '&copy; OpenTopoMap' }),
    escuro: L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', { maxZoom: 19, attribution: '&copy; CartoDB' })
  };

  // O array agora começa vazio e será preenchido pela API
  dadosEscolas: any[] = [];

  ngOnInit(): void {
    this.iniciarMapa();
    this.buscarDadosDaApi(); // Chama a sua API em Java
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  private iniciarMapa(): void {
    this.map = L.map('map', {
      zoomControl: false 
    }).setView([-8.5, -72.0], 7); 
    
    L.control.zoom({ position: 'bottomright' }).addTo(this.map);

    this.tileLayerAtual = this.camadas['padrao'];
    this.tileLayerAtual.addTo(this.map);
  }

  // NOVA FUNÇÃO: Bate no endpoint e busca o JSON
  private buscarDadosDaApi(): void {
    this.http.get<any[]>('http://localhost:8080/infraestrutura').subscribe({
      next: (dadosRecebidos) => {
        this.dadosEscolas = dadosRecebidos;
        // Só tenta adicionar os pins DEPOIS que os dados chegarem com sucesso!
        this.adicionarPinsEscolas(); 
      },
      error: (erro) => {
        console.error('Erro ao comunicar com a API do SIGEI:', erro);
        // Opcional: Adicionar um Toast/Aviso visual de erro aqui futuramente
      }
    });
  }

  // Função original adaptada (Não mudou quase nada, apenas lê de this.dadosEscolas dinâmico)
 private adicionarPinsEscolas(): void {
    const iconeSigei = L.icon({
      iconUrl: 'assets/markers/escola-marker.svg', 
      iconSize: [50, 50], 
      
      // O "bico" do pin que vai tocar exatamente na coordenada no mapa
      // Geralmente é a (Metade da Largura, Altura Total)
      iconAnchor: [16, 38], 
      popupAnchor: [0, -38] 
    });

    this.dadosEscolas.forEach(item => {
      
      // VALIDAÇÃO CRÍTICA: Só desenha se houver coordenadas válidas!
      if (item.latitude && item.longitude) {
        
        const marcador = L.marker([item.latitude, item.longitude], { icon: iconeSigei });

        const popupConteudo = `
          <div class="font-sans min-w-[220px]">
            <h3 class="font-bold text-gray-800 text-base leading-tight mb-1">${item.escola.nome}</h3>
            <p class="text-xs font-mono text-gray-500 mb-3 bg-gray-100 inline-block px-1.5 py-0.5 rounded border border-gray-200">
              INEP: ${item.escola.codigo || 'Sem INEP'}
            </p>
            
            <div class="grid grid-cols-2 gap-y-2 gap-x-4 text-[13px] border-t border-gray-100 pt-2">
              <div>
                <span class="block text-[10px] uppercase font-bold text-gray-400">Situação</span>
                <span class="font-semibold text-green-600">${item.situacao || 'N/A'}</span>
              </div>
              <div>
                <span class="block text-[10px] uppercase font-bold text-gray-400">Acesso</span>
                <span class="font-medium text-gray-700">${item.tipoAcesso || 'N/A'}</span>
              </div>
              <div>
                <span class="block text-[10px] uppercase font-bold text-gray-400">Salas</span>
                <span class="font-medium text-gray-700">${item.numSalas || 0}</span>
              </div>
              <div>
                <span class="block text-[10px] uppercase font-bold text-gray-400">Energia</span>
                <span class="font-medium text-gray-700">${item.tipoEnergia || 'N/A'}</span>
              </div>
            </div>
            
            <div class="mt-3 pt-2 border-t border-gray-100 flex gap-2">
              <span class="px-2 py-0.5 rounded text-[10px] font-bold ${item.temInternet ? 'bg-sky-50 text-sky-600 border border-sky-100' : 'bg-gray-100 text-gray-400'}">Internet</span>
              <span class="px-2 py-0.5 rounded text-[10px] font-bold ${item.temPredio ? 'bg-emerald-50 text-emerald-600 border border-emerald-100' : 'bg-gray-100 text-gray-400'}">Prédio</span>
            </div>
          </div>
        `;

        marcador.bindPopup(popupConteudo).addTo(this.map);
        
      } else {
        console.warn(`Aviso: A escola ${item.escola?.nome} não possui coordenadas e foi ignorada no mapa.`);
      }
      
    });
  }

  mudarCamada(novaCamada: string): void {
    if (this.camadaAtual === novaCamada) {
      this.menuCamadasAberto = false;
      return;
    }
    this.map.removeLayer(this.tileLayerAtual);
    this.camadaAtual = novaCamada;
    this.tileLayerAtual = this.camadas[novaCamada];
    this.tileLayerAtual.addTo(this.map);
    this.menuCamadasAberto = false;
    setTimeout(() => {
      this.map.invalidateSize();
    }, 50);
  }
}