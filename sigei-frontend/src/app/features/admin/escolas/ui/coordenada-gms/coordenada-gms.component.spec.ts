import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, Validators } from '@angular/forms';

import { campo, digitar, escolher, texto } from '@testing/dom';
import { CoordenadaGmsComponent } from './coordenada-gms.component';

@Component({
  standalone: true,
  imports: [CoordenadaGmsComponent],
  template: `
    @if (visivel()) {
      <app-coordenada-gms [controle]="latitude" tipo="latitude" rotulo="Latitude" />
    }
  `
})
class HostComponent {
  readonly visivel = signal(true);
  readonly latitude = new FormControl<number | null>(null, [Validators.min(-90), Validators.max(90)]);
}

describe('CoordenadaGmsComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let host: HostComponent;
  let raiz: HTMLElement;

  async function renderizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  const graus = () => campo(raiz, 'input[aria-label="Latitude: graus"]');
  const minutos = () => campo(raiz, 'input[aria-label="Latitude: minutos"]');
  const segundos = () => campo(raiz, 'input[aria-label="Latitude: segundos"]');
  const direcao = () => campo<HTMLSelectElement>(raiz, 'select');

  beforeEach(async () => {
    fixture = TestBed.createComponent(HostComponent);
    host = fixture.componentInstance;
    raiz = fixture.nativeElement;
    await renderizar();
  });

  it('grava no controle o valor decimal, com sul negativo', async () => {
    digitar(graus(), '9');
    digitar(minutos(), '58');
    digitar(segundos(), '29');
    await renderizar();

    expect(host.latitude.value).toBe(-9.9747222);
    expect(host.latitude.valid).toBe(true);
    expect(texto(raiz)).toContain('Decimal: -9.9747222');
  });

  it('aceita vírgula nos segundos e troca o sinal pela direção', async () => {
    digitar(graus(), '10');
    digitar(minutos(), '30');
    digitar(segundos(), '36,5');
    escolher(direcao(), 'N');
    await renderizar();

    expect(host.latitude.value).toBe(10.5101389);
  });

  it('mostra o erro e invalida o controle com minutos fora de 0 a 59', async () => {
    digitar(graus(), '9');
    digitar(minutos(), '75');
    await renderizar();

    expect(host.latitude.value).toBeNull();
    expect(host.latitude.hasError('gms')).toBe(true);
    expect(texto(raiz)).toContain('Os minutos devem ser um número inteiro de 0 a 59.');
  });

  it('preenche os campos a partir de um valor carregado depois (edição)', async () => {
    host.latitude.setValue(-8.1585);
    await renderizar();

    expect(graus().value).toBe('8');
    expect(minutos().value).toBe('9');
    expect(segundos().value).toBe('30,6');
    expect(direcao().value).toBe('S');
  });

  it('ao sair do modo G°M\'S", deixa de invalidar o controle', async () => {
    digitar(graus(), '200');
    await renderizar();
    expect(host.latitude.invalid).toBe(true);

    host.visivel.set(false);
    await renderizar();

    expect(host.latitude.hasError('gms')).toBe(false);
  });
});
