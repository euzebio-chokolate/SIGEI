import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  carregando = false;
  erroMensagem = '';
  mostrarSenha = false;

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    // O tamanho mínimo vale só ao definir a senha; aqui basta não estar vazia, como no LoginRequestDTO
    senha: ['', Validators.required]
  });

  login(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.carregando) return;

    this.carregando = true;
    this.erroMensagem = '';
    const dados = this.form.getRawValue();

    this.authService.login(dados).subscribe({
      next: usuario => {
        this.carregando = false;
        this.cdr.markForCheck();
        if (this.authService.deveAlterarSenha()) {
          // Mantida somente até a tela seguinte e removida assim que for lida.
          this.authService.guardarSenhaTemporaria(dados.senha);
          this.router.navigate(['/alterar-senha']);
        } else {
          this.authService.consumirSenhaTemporaria();
          this.router.navigate(['/admin/dashboard']);
        }
      },
      error: erro => {
        console.error('Erro no login:', erro);
        this.carregando = false;

        if (erro?.status === 401) {
          this.erroMensagem = 'E-mail ou senha inválidos.';
        } else if (erro?.status === 403) {
          // A API só informa a desativação a quem acertou a senha
          this.erroMensagem = 'Usuário desativado. Procure o administrador do sistema.';
        } else if (erro?.status === 0) {
          this.erroMensagem = 'Não foi possível conectar ao servidor.';
        } else if (erro?.status >= 500) {
          this.erroMensagem = 'O servidor está com problemas. Tente novamente em instantes.';
        } else {
          this.erroMensagem = 'Não foi possível realizar o login.';
        }

        this.cdr.markForCheck();
      }
    });
  }

  alternarSenha(): void {
    this.mostrarSenha = !this.mostrarSenha;
  }
}
