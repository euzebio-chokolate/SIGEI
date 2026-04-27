import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapaPopup } from './mapa-popup';

describe('MapaPopup', () => {
  let component: MapaPopup;
  let fixture: ComponentFixture<MapaPopup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapaPopup],
    }).compileComponents();

    fixture = TestBed.createComponent(MapaPopup);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
