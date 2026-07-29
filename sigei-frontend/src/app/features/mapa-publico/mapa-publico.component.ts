import { Component, OnInit, OnDestroy } from '@angular/core';
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

      <div class="absolute bottom-6 left-4 z-[1010] flex flex-col gap-2">
        
        <!-- Menu de Camadas -->
        <div class="bg-white/95 backdrop-blur-md p-2 rounded-xl shadow-[0_4px_20px_rgba(0,0,0,0.15)] border border-gray-100 flex flex-col gap-1 transition-all duration-300 origin-bottom-left"
             [class.opacity-0]="!menuCamadasAberto"
             [class.scale-0]="!menuCamadasAberto"
             [class.pointer-events-none]="!menuCamadasAberto">
          
          <button (click)="mudarCamada('padrao')" 
                  [class.bg-green-50]="camadaAtual === 'padrao'" 
                  [class.text-green-700]="camadaAtual === 'padrao'"
                  class="text-left px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 rounded-lg font-semibold transition-colors">
            Padrão
          </button>
          
          <button (click)="mudarCamada('satelite')" 
                  [class.bg-green-50]="camadaAtual === 'satelite'" 
                  [class.text-green-700]="camadaAtual === 'satelite'"
                  class="text-left px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 rounded-lg font-semibold transition-colors">
            Satélite
          </button>
          
          <button (click)="mudarCamada('terreno')" 
                  [class.bg-green-50]="camadaAtual === 'terreno'" 
                  [class.text-green-700]="camadaAtual === 'terreno'"
                  class="text-left px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 rounded-lg font-semibold transition-colors">
            Terreno
          </button>
          
          <button (click)="mudarCamada('escuro')" 
                  [class.bg-green-50]="camadaAtual === 'escuro'" 
                  [class.text-green-700]="camadaAtual === 'escuro'"
                  class="text-left px-4 py-2 text-sm text-gray-600 hover:bg-gray-50 rounded-lg font-semibold transition-colors">
            Escuro
          </button>
        </div>

        <!-- Botão de Camadas -->
        <button (click)="menuCamadasAberto = !menuCamadasAberto" 
                class="h-12 px-4 bg-white/95 backdrop-blur-md rounded-xl shadow-lg border border-gray-100 flex items-center gap-2.5 text-gray-700 hover:bg-gray-50 transition-all self-start pointer-events-auto">
                
          <svg class="w-5 h-5 text-green-600" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M2 17l10 5 10-5M2 12l10 5 10-5M12 2L2 7l10 5 10-5-10-5z" />
          </svg>
          <span class="text-base font-medium text-gray-700">
            {{ nomesCamadas[camadaAtual] }}
          </span>
        </button>

      </div>
      
      <div id="map" class="absolute inset-0 z-0"></div>
      
    </div>
  `
})
export class MapaPublicoComponent implements OnInit, OnDestroy {
  private map!: L.Map;
  private tileLayerAtual!: L.TileLayer;
  
  menuCamadasAberto = false;
  camadaAtual = 'padrao';

  nomesCamadas: { [key: string]: string } = {
    padrao: 'Padrão',
    satelite: 'Satélite',
    terreno: 'Terreno',
    escuro: 'Escuro'
  };

  private camadas: { [key: string]: L.TileLayer } = {
    padrao: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { 
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap'
    }),
    satelite: L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', { 
      maxZoom: 17,
      attribution: '&copy; Esri World Imagery'
    }),
    terreno: L.tileLayer('https://{s}.opentopomap.org/{z}/{x}/{y}.png', { 
      maxZoom: 17,
      attribution: '&copy; OpenTopoMap'
    }),
    escuro: L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', { 
      maxZoom: 19,
      attribution: '&copy; CartoDB'
    })
  };

  ngOnInit(): void {
    this.iniciarMapa();
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  private iniciarMapa(): void {
    this.map = L.map('map', {
      zoomControl: false 
    }).setView([-9.97499, -67.8243], 13); 
    
    L.control.zoom({ position: 'bottomright' }).addTo(this.map);

    this.tileLayerAtual = this.camadas['padrao'];
    this.tileLayerAtual.addTo(this.map);
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
