import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { botao, campo, digitar, texto } from '@testing/dom';
import { InfraestruturaEtapaComponent, InfraestruturaFormControls } from './infraestrutura-etapa.component';

/** O mesmo grupo que o formulário de escola monta para esta etapa. */
function formulario(): FormGroup<InfraestruturaFormControls> {
  return new FormGroup({
    anoConstrucao: new FormControl<number | null>(null),
    numSalas: new FormControl<number | null>(null, Validators.min(0)),
    latitude: new FormControl<number | null>(null, [Validators.min(-90), Validators.max(90)]),
    longitude: new FormControl<number | null>(null, [Validators.min(-180), Validators.max(180)]),
    situacao: new FormControl('', { nonNullable: true }),
    tipoAcesso: new FormControl('', { nonNullable: true }),
    temPredio: new FormControl(false, { nonNullable: true }),
    tipoAgua: new FormControl('', { nonNullable: true }),
    temInternet: new FormControl(false, { nonNullable: true }),
    tipoEnergia: new FormControl('', { nonNullable: true }),
    temRegistroFotografico: new FormControl(false, { nonNullable: true })
  });
}

describe('InfraestruturaEtapaComponent: coordenadas', () => {
  let fixture: ComponentFixture<InfraestruturaEtapaComponent>;
  let raiz: HTMLElement;
  let form: FormGroup<InfraestruturaFormControls>;

  async function renderizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  beforeEach(async () => {
    form = formulario();
    fixture = TestBed.createComponent(InfraestruturaEtapaComponent);
    fixture.componentRef.setInput('form', form);
    raiz = fixture.nativeElement;
    await renderizar();
  });

  it('começa em decimal e mostra o equivalente em G°M\'S"', async () => {
    digitar(campo(raiz, '#latitude'), '-9.9747222');
    await renderizar();

    expect(form.controls.latitude.value).toBe(-9.9747222);
    expect(texto(raiz)).toContain('9°58\'29" S');
  });

  it('em G°M\'S", grava latitude e longitude em decimal no formulário', async () => {
    botao(raiz, 'G° M\' S"').click();
    await renderizar();
    expect(raiz.querySelector('#latitude')).toBeNull();

    digitar(campo(raiz, 'input[aria-label="Latitude: graus"]'), '8');
    digitar(campo(raiz, 'input[aria-label="Latitude: minutos"]'), '9');
    digitar(campo(raiz, 'input[aria-label="Latitude: segundos"]'), '30,6');
    digitar(campo(raiz, 'input[aria-label="Longitude: graus"]'), '70');
    digitar(campo(raiz, 'input[aria-label="Longitude: minutos"]'), '21');
    digitar(campo(raiz, 'input[aria-label="Longitude: segundos"]'), '7,56');
    await renderizar();

    expect(form.controls.latitude.value).toBe(-8.1585);
    expect(form.controls.longitude.value).toBe(-70.3521);
    expect(form.valid).toBe(true);
  });

  it('volta ao decimal com o valor digitado em G°M\'S"', async () => {
    botao(raiz, 'G° M\' S"').click();
    await renderizar();
    digitar(campo(raiz, 'input[aria-label="Latitude: graus"]'), '10');
    digitar(campo(raiz, 'input[aria-label="Latitude: minutos"]'), '30');
    await renderizar();

    botao(raiz, 'Decimal').click();
    await renderizar();

    expect(campo(raiz, '#latitude').value).toBe('-10.5');
  });

  it('um G°M\'S" inválido impede avançar a etapa', async () => {
    botao(raiz, 'G° M\' S"').click();
    await renderizar();
    digitar(campo(raiz, 'input[aria-label="Longitude: graus"]'), '70');
    digitar(campo(raiz, 'input[aria-label="Longitude: segundos"]'), '61');
    await renderizar();

    expect(form.invalid).toBe(true);
    expect(texto(raiz)).toContain('Os segundos devem estar entre 0 e 59,999.');
  });
});
