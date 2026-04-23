import { Component } from '@angular/core';
import { MapaComponent } from './pages/mapa/mapa';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [MapaComponent],
  templateUrl: './app.html',
})
export class App {}