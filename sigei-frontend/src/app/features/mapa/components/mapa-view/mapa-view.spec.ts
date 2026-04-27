import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MapaView } from './mapa-view';

describe('MapaView', () => {
  let component: MapaView;
  let fixture: ComponentFixture<MapaView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MapaView],
    }).compileComponents();

    fixture = TestBed.createComponent(MapaView);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
