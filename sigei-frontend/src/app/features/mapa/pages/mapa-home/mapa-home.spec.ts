import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapaHome } from './mapa-home';

describe('MapaHome', () => {
  let component: MapaHome;
  let fixture: ComponentFixture<MapaHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapaHome],
    }).compileComponents();

    fixture = TestBed.createComponent(MapaHome);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
