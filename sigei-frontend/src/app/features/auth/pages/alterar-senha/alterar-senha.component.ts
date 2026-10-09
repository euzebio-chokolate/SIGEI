import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '@core/auth/auth.service';
import { mensagemErroFormulario } from '@core/http/erros-formulario';
import { ErroCampoComponent } from '@shared/ui/erro-campo/erro-campo.component';

@Component({
  selector: 'app-alterar-senha',
  standalone: true,
  imports: [ReactiveFormsModule, ErroCampoComponent],
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
      this.form.controls.confirmacaoSenha.setErrors({ confirmacao: true });
      return;
    }
    if (dados.novaSenha === dados.senhaAtual) {
      this.form.controls.novaSenha.setErrors({ igualAtual: true });
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
        // Mensagens do AuthService do backend; a sessão expirada (401) é tratada pelo authInterceptor
        this.erroMensagem = mensagemErroFormulario(this.form, erro, 'Não foi possível alterar a senha.', [
          { campo: 'senhaAtual', padrao: /senha atual inválida/i },
          { campo: 'confirmacaoSenha', padrao: /não coincidem/i },
          { campo: 'novaSenha', padrao: /nova senha/i }
        ]);
        this.cdr.markForCheck();
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
