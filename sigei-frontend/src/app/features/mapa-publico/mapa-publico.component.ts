import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { MapaFiltrosComponent } from './components/mapa-filtros.component';
import { MapaBuscaComponent } from './components/mapa-busca.component';
import * as L from 'leaflet';

@Component({
  selector: 'app-mapa-publico',
  standalone: true,
  imports: [HeaderComponent, MapaFiltrosComponent, MapaBuscaComponent],
  template: `
    <div class="relative w-screen h-screen overflow-hidden bg-gray-200">
      
      <app-header></app-header>

      <app-mapa-busca 
        [dadosEscolas]="dadosEscolasOriginal"
        (buscaAlterada)="aoMudarBusca($event)"
        (escolaSelecionada)="focarEscola($event)">
      </app-mapa-busca>

      <app-mapa-filtros 
        (filtrosAlterados)="aoMudarFiltros($event)">
      </app-mapa-filtros>

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
          <i class="fa-solid fa-layer-group text-emerald-600"></i>
          <span class="text-base font-medium text-gray-700">{{ nomesCamadas[camadaAtual] }}</span>
        </button>
      </div>
      
      <div id="map" class="absolute inset-0 z-0"></div>
    </div>
  `
})
export class MapaPublicoComponent implements OnInit, OnDestroy {
  private map!: L.Map;
  private tileLayerAtual!: L.TileLayer;
  private http = inject(HttpClient);
  
  private grupoMarcadores = L.layerGroup(); 
  private marcadoresPorId: { [key: string]: L.Marker } = {};

  menuCamadasAberto = false;
  camadaAtual = 'padrao';

  nomesCamadas: { [key: string]: string } = {
    padrao: 'Padrão',
    satelite: 'Satélite',
    terreno: 'Terreno',
    escuro: 'Escuro'
  };

  private camadas: { [key: string]: L.TileLayer } = {
    padrao: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }),
    satelite: L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', { maxZoom: 17 }),
    terreno: L.tileLayer('https://{s}.opentopomap.org/{z}/{x}/{y}.png', { maxZoom: 17 }),
    escuro: L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', { maxZoom: 19 })
  };

  dadosEscolasOriginal: any[] = [];
  dadosEscolasFiltrados: any[] = [];

  termoBuscaTexto = '';
  filtrosAvancados = {
    situacao: '',
    tipoAcesso: '',
    apenasComInternet: false,
    apenasComPredio: false
  };

  ngOnInit(): void {
    this.iniciarMapa();
    this.buscarDadosDaApi();
  }

  ngOnDestroy(): void {
    if (this.map) this.map.remove();
  }

  private iniciarMapa(): void {
    this.map = L.map('map', { zoomControl: false }).setView([-8.5, -72.0], 7); 
    L.control.zoom({ position: 'bottomright' }).addTo(this.map);
    
    this.tileLayerAtual = this.camadas['padrao'];
    this.tileLayerAtual.addTo(this.map);

    this.grupoMarcadores.addTo(this.map);
  }

  private buscarDadosDaApi(): void {
    this.http.get<any[]>('http://localhost:8080/infraestrutura').subscribe({
      next: (dadosRecebidos) => {
        this.dadosEscolasOriginal = dadosRecebidos;
        this.dadosEscolasFiltrados = [...dadosRecebidos];
        this.adicionarPinsEscolas(); 
      },
      error: (erro) => console.error('Erro API:', erro)
    });
  }

  aoMudarBusca(termo: string) {
    this.termoBuscaTexto = termo;
    this.processarFiltros();
  }

  aoMudarFiltros(filtros: any) {
    this.filtrosAvancados = filtros;
    this.processarFiltros();
  }

  private processarFiltros(): void {
    this.dadosEscolasFiltrados = this.dadosEscolasOriginal.filter(item => {
      const termo = this.termoBuscaTexto.toLowerCase();
      const nomeEscola = item.escola?.nome?.toLowerCase() || '';
      const codigoInep = item.escola?.codigo?.toLowerCase() || '';
      const passaBusca = nomeEscola.includes(termo) || codigoInep.includes(termo);

      const passaSituacao = this.filtrosAvancados.situacao ? item.situacao === this.filtrosAvancados.situacao : true;
      const passaAcesso = this.filtrosAvancados.tipoAcesso ? item.tipoAcesso === this.filtrosAvancados.tipoAcesso : true;
      const passaInternet = this.filtrosAvancados.apenasComInternet ? item.temInternet === true : true;
      const passaPredio = this.filtrosAvancados.apenasComPredio ? item.temPredio === true : true;

      return passaBusca && passaSituacao && passaAcesso && passaInternet && passaPredio;
    });

    this.adicionarPinsEscolas();
  }

  focarEscola(item: any): void {
    if (item.latitude && item.longitude) {
      this.map.setView([item.latitude, item.longitude], 11);
      
      const marcador = this.marcadoresPorId[item.id];
      if (marcador) {
        marcador.openPopup();
      }
    }
  }

  private adicionarPinsEscolas(): void {
    this.grupoMarcadores.clearLayers();
    this.marcadoresPorId = {};

    // 1. Ícone Padrão (Verde)
    const iconePadrao = L.icon({
      iconUrl: 'assets/markers/escola-marker-green.svg', 
      iconSize: [50, 50],
      iconAnchor: [25, 50],
      popupAnchor: [0, -50]
    });

    // 2. Ícone para Escolas Paralisadas (Vermelho)
    const iconeVermelho = L.icon({
      iconUrl: 'assets/markers/escola-marker-red.svg', 
      iconSize: [50, 50],
      iconAnchor: [25, 50],
      popupAnchor: [0, -50]
    });

    this.dadosEscolasFiltrados.forEach(item => {
      if (item.latitude && item.longitude) {
        
        // Verifica se a situação é PARALISADA ou PARALIZADA (ignora maiúsculas/minúsculas)
        const situacaoNorm = item.situacao?.toUpperCase() || '';
        const isParalisada = situacaoNorm === 'PARALISADA' || situacaoNorm === 'PARALIZADA';

        const iconeUsado = isParalisada ? iconeVermelho : iconePadrao;

        const marcador = L.marker([item.latitude, item.longitude], { icon: iconeUsado });

        const popupConteudo = `
          <div class="font-sans min-w-[220px]">
            <h3 class="font-bold text-gray-800 text-base leading-tight mb-1">${item.escola?.nome || 'Escola sem Nome'}</h3>
            <p class="text-xs font-mono text-gray-500 mb-2 bg-gray-100 inline-block px-1.5 py-0.5 rounded border border-gray-200">
              INEP: ${item.escola?.codigo || 'Sem INEP'}
            </p>
            
            <div class="grid grid-cols-2 gap-y-2 gap-x-4 text-[13px] border-t border-gray-100 pt-2 mb-2">
              <div>
                <span class="block text-[10px] uppercase font-bold text-gray-400">Situação</span>
                <span class="font-semibold ${isParalisada ? 'text-red-600' : 'text-green-600'}">${item.situacao || 'N/A'}</span>
              </div>
              <div>
                <span class="block text-[10px] uppercase font-bold text-gray-400">Acesso</span>
                <span class="font-medium text-gray-700">${item.tipoAcesso || 'N/A'}</span>
              </div>
            </div>

            <div class="flex gap-2 pt-2 border-t border-gray-100 mb-3">
              <span class="px-2 py-0.5 rounded text-[10px] font-bold ${item.temInternet ? 'bg-sky-50 text-sky-600 border border-sky-100' : 'bg-gray-100 text-gray-400'}">Internet</span>
              <span class="px-2 py-0.5 rounded text-[10px] font-bold ${item.temPredio ? 'bg-emerald-50 text-emerald-600 border border-emerald-100' : 'bg-gray-100 text-gray-400'}">Prédio</span>
            </div>

            <button onclick="window.navegarDetalhesEscola(${item.escola?.id})"
                    class="w-full bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold py-2 px-3 rounded-xl transition-all shadow-sm flex items-center justify-center gap-1.5 active:scale-95">
              <span>Ver detalhes completos</span>
              <i class="fa-solid fa-arrow-right text-[10px]"></i>
            </button>
          </div>
        `;

        marcador.bindPopup(popupConteudo);
        this.grupoMarcadores.addLayer(marcador);
        this.marcadoresPorId[item.id] = marcador;
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