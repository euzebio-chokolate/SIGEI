import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-mapa-popup',
  standalone: true,
  templateUrl: './mapa-popup.html',
  styleUrls: ['./mapa-popup.css']
})
export class MapaPopup {
  @Input() escola: any;
}