import { Component } from '@angular/core';
import { MapaView } from '../../components/mapa-view/mapa-view';
import { MapaFiltro } from '../../components/mapa-filtro/mapa-filtro';

@Component({
  selector: 'app-mapa-home',
  standalone: true,
  imports: [MapaView, MapaFiltro],
  templateUrl: './mapa-home.html',
  styleUrls: ['./mapa-home.css']
})
export class MapaHome {}