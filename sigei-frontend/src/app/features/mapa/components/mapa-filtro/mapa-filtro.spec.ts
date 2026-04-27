import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapaFiltro } from './mapa-filtro';

describe('MapaFiltro', () => {
  let component: MapaFiltro;
  let fixture: ComponentFixture<MapaFiltro>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapaFiltro],
    }).compileComponents();

    fixture = TestBed.createComponent(MapaFiltro);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
