import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-alterar-senha',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './alterar-senha.component.html'
})
export class AlterarSenhaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  carregando = false;
  erroMensagem = '';
  mostrarSenhas = false;
  trocaObrigatoria = this.authService.deveAlterarSenha();

  form = this.fb.nonNullable.group({
    senhaAtual: [this.authService.consumirSenhaTemporaria(), Validators.required],
    novaSenha: ['', [Validators.required, Validators.minLength(8)]],
    confirmacaoSenha: ['', Validators.required]
  });

  alterarSenha(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dados = this.form.getRawValue();
    if (dados.novaSenha !== dados.confirmacaoSenha) {
      this.erroMensagem = 'A confirmação não corresponde à nova senha.';
      return;
    }
    if (this.carregando) return;

    this.carregando = true;
    this.erroMensagem = '';
    this.authService.alterarSenha(dados).subscribe({
      next: () => {
        // A resposta é 204 e não traz um token com as novas autoridades.
        this.authService.logout();
        this.router.navigate(['/login'], { state: { senhaAlterada: true } });
      },
      error: erro => {
        this.carregando = false;
        if (erro?.status === 400) this.erroMensagem = typeof erro?.error === 'string' ? erro.error : erro?.error?.message ?? 'Confira a senha atual e os requisitos da nova senha.';
        else if (erro?.status === 401) this.erroMensagem = 'O token não foi aceito pelo servidor. Entre novamente.';
        else if (erro?.status === 403) this.erroMensagem = 'O servidor bloqueou esta troca de senha (403). Verifique se o backend em execução contém a liberação de /auth/alterar-senha para usuários autenticados.';
        else if (erro?.status === 0) this.erroMensagem = 'Não foi possível conectar ao servidor.';
        else this.erroMensagem = 'Não foi possível alterar a senha.';
        this.cdr.markForCheck();
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
