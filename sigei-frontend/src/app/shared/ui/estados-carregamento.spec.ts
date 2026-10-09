import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { EstadoErroComponent } from './estado-erro/estado-erro.component';
import { SkeletonLinhaComponent } from './skeleton/skeleton-linha.component';

@Component({
  standalone: true,
  imports: [SkeletonLinhaComponent, EstadoErroComponent],
  template: `
    <table><tbody><tr appSkeletonLinha [colunas]="colunas()"></tr></tbody></table>
    <app-estado-erro mensagem="Não foi possível carregar." (tentarNovamente)="tentativas = tentativas + 1" />
  `
})
class HostComponent {
  colunas = signal(4);
  tentativas = 0;
}

describe('estados de carregamento', () => {
  it('a linha skeleton tem uma célula por coluna e fica oculta para leitores de tela', () => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    const linha: HTMLElement = fixture.nativeElement.querySelector('tr');
    expect(linha.querySelectorAll('td').length).toBe(4);
    expect(linha.getAttribute('aria-hidden')).toBe('true');
  });

  it('o estado de erro mostra a mensagem e repete a carga ao clicar', () => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    const elemento: HTMLElement = fixture.nativeElement.querySelector('app-estado-erro');
    expect(elemento.textContent).toContain('Não foi possível carregar.');
    elemento.querySelector('button')!.click();
    expect(fixture.componentInstance.tentativas).toBe(1);
  });
});
