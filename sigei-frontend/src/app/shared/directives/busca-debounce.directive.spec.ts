import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { BuscaDebounceDirective } from './busca-debounce.directive';

@Component({
  standalone: true,
  imports: [BuscaDebounceDirective],
  template: `<input type="text" (buscaDebounce)="buscas = buscas + 1" />`
})
class HostComponent {
  buscas = 0;
}

describe('BuscaDebounceDirective', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  const montar = () => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
    return { host: fixture.componentInstance, input };
  };

  it('filtra uma vez depois que o usuário para de digitar', () => {
    const { host, input } = montar();
    for (let i = 0; i < 5; i++) input.dispatchEvent(new Event('input'));
    expect(host.buscas).toBe(0);
    vi.advanceTimersByTime(250);
    expect(host.buscas).toBe(1);
  });

  it('Enter aplica a busca na hora', () => {
    const { host, input } = montar();
    input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));
    expect(host.buscas).toBe(1);
    vi.advanceTimersByTime(250);
    expect(host.buscas).toBe(1);
  });
});
