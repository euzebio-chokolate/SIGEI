import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';

import { ToastService } from '@core/notificacao/toast.service';
import { adminGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { SeAdminDirective } from './se-admin.directive';

@Component({
  standalone: true,
  imports: [SeAdminDirective],
  template: `<button *seAdmin>Excluir</button><span>Sempre</span>`
})
class HostComponent {}

function configurar(admin: boolean) {
  const auth = {
    estaLogado: () => true,
    deveAlterarSenha: () => false,
    ehAdmin: () => admin
  };
  TestBed.configureTestingModule({
    providers: [provideRouter([]), { provide: AuthService, useValue: auth }]
  });
}

describe('SeAdminDirective', () => {
  it('mostra a ação para administradores', () => {
    configurar(true);
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button')).not.toBeNull();
  });

  it('remove a ação do DOM para o perfil de leitura', () => {
    configurar(false);
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Sempre');
  });
});

describe('adminGuard', () => {
  const executar = () => TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

  it('libera o administrador', () => {
    configurar(true);
    expect(executar()).toBe(true);
  });

  it('redireciona o perfil de leitura ao dashboard e avisa o motivo', () => {
    configurar(false);
    const resultado = executar() as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/admin/dashboard');
    expect(TestBed.inject(ToastService).toasts()[0]?.titulo).toBe('Acesso restrito');
  });
});
