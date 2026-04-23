import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { EscolaService } from '../../services/escola.service';

@Component({
  selector: 'app-mapa',
  standalone: true,
  templateUrl: './mapa.html',
})
export class MapaComponent implements OnInit {

  platformId = inject(PLATFORM_ID);

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.initMap();
    }
  }

  constructor(private escolaService: EscolaService) {}

  async initMap() {
  const L = await import('leaflet');

  const map = L.map('map').setView([-9.93, -67.81], 12);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
    .addTo(map);

  //BUSCA NO BACKEND
  
  this.escolaService.listar().subscribe(escolas => {

    escolas.forEach(e => {
      L.marker([e.latitude, e.longitude])
        .addTo(map)
        .bindPopup(`
          <b>${e.nome}</b><br>
          <b>${"Alunos: " + e.qtdAlunos}</b><br>

          `);
    });

  });
}
}