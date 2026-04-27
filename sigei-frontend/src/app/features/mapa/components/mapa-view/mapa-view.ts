import {
  Component,
  AfterViewInit,
  ApplicationRef,
  EnvironmentInjector,
  createComponent
} from '@angular/core';
import * as L from 'leaflet';
import { MapaPopup } from '../mapa-popup/mapa-popup';

@Component({
  selector: 'app-mapa-view',
  standalone: true,
  templateUrl: './mapa-view.html',
  styleUrls: ['./mapa-view.css']
})
export class MapaView implements AfterViewInit {

  private map: any;

  constructor(
    private appRef: ApplicationRef,
    private injector: EnvironmentInjector
  ) {}

  ngAfterViewInit(): void {
    this.initMap();
  }

  private initMap() {
    this.map = L.map('map').setView([-0.1303, -67.0892], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
      .addTo(this.map);

    this.addMarcadores();
  }

  private addMarcadores() {

    const escola = {
      nome: "Escola Baniwa",
      cidade: "São Gabriel da Cachoeira - AM",
      alunos: 95,
      professores: 1,
      ano: 2023,
      lat: -0.13,
      lng: -67.09
    };

    const marker = L.marker([escola.lat, escola.lng]).addTo(this.map);

    //cria componente Angular dinamicamente
    const componentRef = createComponent(MapaPopup, {
      environmentInjector: this.injector
    });

    componentRef.instance.escola = escola;

    this.appRef.attachView(componentRef.hostView);

    const element = (componentRef.hostView as any).rootNodes[0] as HTMLElement;

    marker.bindPopup(element);
  }
}