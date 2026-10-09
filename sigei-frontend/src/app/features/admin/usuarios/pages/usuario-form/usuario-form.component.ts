import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { mensagemErroCarregamento } from '@core/http/erro-http';
import { mensagemErroFormulario } from '@core/http/erros-formulario';
import { PerfilUsuario, UsuarioRequest, UsuarioService } from '@domains/usuarios/data-access/usuario.service';
import { ErroCampoComponent } from '@shared/ui/erro-campo/erro-campo.component';
import { SucessoModalComponent } from '@shared/ui/sucesso-modal/sucesso-modal.component';

@Component({
  selector: 'app-usuario-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, SucessoModalComponent, ErroCampoComponent],
  templateUrl: './usuario-form.component.html'
})
export class UsuarioFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  editando = false;
  usuarioId: number | null = null;
  carregando = false;
  salvando = false;
  erroMensagem = '';
  mostrarSenha = false;
  modalSucessoAberto = false;
  tituloSucesso = '';
  mensagemSucesso = '';

  perfis: PerfilUsuario[] = ['ADMIN', 'USUARIO'];

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.minLength(4)]],
    perfil: ['USUARIO' as PerfilUsuario, [Validators.required]],
    ativo: [true]
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (id) {
      this.editando = true;
      this.usuarioId = id;
      this.carregarUsuario(id);
    } else {
      this.form.controls.senha.addValidators(Validators.required);
      this.form.controls.senha.updateValueAndValidity();
    }
  }

  carregarUsuario(id: number): void {
    this.carregando = true;
    this.erroMensagem = '';

    this.usuarioService.buscarPorId(id).subscribe({
      next: usuario => {
        this.form.patchValue({
          nome: usuario.nome,
          email: usuario.email,
          perfil: usuario.perfil,
          ativo: usuario.ativo
        });

        this.form.controls.senha.setValue('');
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar usuário:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar o usuário.', 'Usuário não encontrado.');
        this.carregando = false;
        this.cdr.markForCheck();
      }
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.salvando) {
      return;
    }

    this.salvando = true;
    this.erroMensagem = '';

    const valor = this.form.getRawValue();
    const dados: UsuarioRequest = {
      nome: valor.nome.trim(),
      email: valor.email.trim(),
      senha: valor.senha,
      perfil: valor.perfil,
      ativo: valor.ativo
    };

    if (this.editando && this.usuarioId) {
      dados.id = this.usuarioId;
    }

    const requisicao = this.editando
      ? this.usuarioService.editar(dados)
      : this.usuarioService.salvar(dados);

    requisicao.subscribe({
      next: () => {
        this.salvando = false;

        if (this.editando) {
          this.tituloSucesso = 'Usuário atualizado';
          this.mensagemSucesso = 'Os dados do usuário foram atualizados com sucesso.';
        } else {
          this.tituloSucesso = 'Usuário cadastrado';
          this.mensagemSucesso = 'O novo usuário foi cadastrado com sucesso.';
        }

        this.modalSucessoAberto = true;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao salvar usuário:', erro);
        this.erroMensagem = mensagemErroFormulario(this.form, erro, 'Não foi possível salvar o usuário.', [
          { campo: 'email', padrao: /e-?mail/i },
          { campo: 'senha', padrao: /senha/i }
        ]);
        this.salvando = false;
        this.cdr.markForCheck();
      }
    });
  }

  alternarSenha(): void {
    this.mostrarSenha = !this.mostrarSenha;
  }

  fecharModalSucesso(): void {
    this.modalSucessoAberto = false;
    this.router.navigate(['/admin/usuarios']);
  }
}