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
      
      <div id="map" class="absolute inset-0 z-0"></div>
      
    </div>
  `
})
export class MapaPublicoComponent implements OnInit, OnDestroy {
  private map!: L.Map;

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
      zoomControl: false // Ocultamos o padrão
    }).setView([-9.97499, -67.8243], 13); 

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);
    
    // Controlo de zoom reposicionado para não interferir
    L.control.zoom({ position: 'bottomright' }).addTo(this.map);
  }
}