import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '@environments/environment';

import { botao, campo, digitar, texto } from '@testing/dom';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let raiz: HTMLElement;
  let http: HttpTestingController;
  let navegar: ReturnType<typeof vi.spyOn>;

  async function renderizar(): Promise<void> {
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function entrar(email: string, senha: string): Promise<void> {
    digitar(campo(raiz, '#email'), email);
    digitar(campo(raiz, '#senha'), senha);
    botao(raiz, 'Entrar').click();
    await renderizar();
  }

  beforeEach(async () => {
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });
    http = TestBed.inject(HttpTestingController);
    navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(LoginComponent);
    raiz = fixture.nativeElement;
    await renderizar();
  });

  afterEach(() => http.verify());

  it('não envia o formulário vazio e explica o que falta', async () => {
    botao(raiz, 'Entrar').click();
    await renderizar();

    expect(texto(raiz)).toContain('Informe o e-mail.');
    expect(texto(raiz)).toContain('Informe sua senha.');
    http.expectNone(`${environment.apiUrl}/auth/login`);
  });

  it('recusa e-mail mal formado sem chamar a API', async () => {
    await entrar('admin', 'qualquer');

    expect(texto(raiz)).toContain('Informe um e-mail válido.');
    http.expectNone(`${environment.apiUrl}/auth/login`);
  });

  it('entra e abre o painel', async () => {
    await entrar('admin@sigei.com', 'senha-correta');

    const requisicao = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(requisicao.request.body).toEqual({ email: 'admin@sigei.com', senha: 'senha-correta' });
    requisicao.flush({ id: 1, nome: 'Admin', email: 'admin@sigei.com', perfil: 'ADMIN', alterarSenha: false });
    await renderizar();

    expect(navegar).toHaveBeenCalledWith(['/admin/dashboard']);
    expect(JSON.parse(localStorage.getItem('sigei_usuario')!).perfil).toBe('ADMIN');
  });

  it('leva à troca de senha quando ela é obrigatória', async () => {
    await entrar('novo@sigei.com', 'temporaria');

    http.expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ id: 2, nome: 'Novo', email: 'novo@sigei.com', perfil: 'USUARIO', alterarSenha: true });
    await renderizar();

    expect(navegar).toHaveBeenCalledWith(['/alterar-senha']);
  });

  it('mostra a mensagem da API quando as credenciais são recusadas', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    await entrar('admin@sigei.com', 'errada');

    http.expectOne(`${environment.apiUrl}/auth/login`).flush(
      { status: 401, title: 'Não autenticado', detail: 'E-mail ou senha inválidos.' },
      { status: 401, statusText: 'Unauthorized' }
    );
    await renderizar();

    expect(texto(raiz)).toContain('E-mail ou senha inválidos.');
    expect(navegar).not.toHaveBeenCalled();
    expect(botao(raiz, 'Entrar').disabled).toBe(false);
  });
});
