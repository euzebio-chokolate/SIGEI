import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { UsuarioResponse, UsuarioService } from '@domains/usuarios/data-access/usuario.service';
import { mensagemErroCarregamento, mensagemErroExclusao } from '@core/http/erro-http';
import { ToastService } from '@core/notificacao/toast.service';
import { ConfirmModalComponent } from '@shared/ui/confirm-modal/confirm-modal.component';
import { SucessoModalComponent } from '@shared/ui/sucesso-modal/sucesso-modal.component';
import { SkeletonLinhaComponent } from '@shared/ui/skeleton/skeleton-linha.component';
import { EstadoErroComponent } from '@shared/ui/estado-erro/estado-erro.component';

@Component({
  selector: 'app-usuarios-lista',
  standalone: true,
  imports: [FormsModule, RouterLink, ConfirmModalComponent, SucessoModalComponent, SkeletonLinhaComponent, EstadoErroComponent],
  templateUrl: './usuarios-lista.component.html'
})
export class UsuariosListaComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);

  usuarios: UsuarioResponse[] = [];
  usuariosFiltrados: UsuarioResponse[] = [];

  filtroTexto = '';
  paginaAtual = 1;
  itensPorPagina = 10;

  carregando = false;
  erroMensagem = '';

  usuarioParaExcluir: UsuarioResponse | null = null;
  excluindoId: number | null = null;

  modalSucessoAberto = false;
  tituloSucesso = '';
  mensagemSucesso = '';

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  carregarUsuarios(): void {
    this.carregando = true;
    this.erroMensagem = '';

    this.usuarioService.listar().subscribe({
      next: dados => {
        this.usuarios = [...(dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome));
        this.usuariosFiltrados = [...this.usuarios];
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar usuários:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os usuários.');
        this.carregando = false;
        this.cdr.markForCheck();
      }
    });
  }

  filtrarUsuarios(): void {
    const termo = this.filtroTexto.trim().toLowerCase();
    this.paginaAtual = 1;

    if (!termo) {
      this.usuariosFiltrados = [...this.usuarios];
      return;
    }

    this.usuariosFiltrados = this.usuarios.filter(usuario => {
      return (
        usuario.nome.toLowerCase().includes(termo) ||
        usuario.email.toLowerCase().includes(termo) ||
        usuario.perfil.toLowerCase().includes(termo)
      );
    });
  }

  get usuariosPaginados(): UsuarioResponse[] {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    return this.usuariosFiltrados.slice(inicio, inicio + this.itensPorPagina);
  }

  get totalPaginas(): number {
    return Math.ceil(this.usuariosFiltrados.length / this.itensPorPagina);
  }

  paginaAnterior(): void {
    if (this.paginaAtual > 1) {
      this.paginaAtual--;
    }
  }

  proximaPagina(): void {
    if (this.paginaAtual < this.totalPaginas) {
      this.paginaAtual++;
    }
  }

  abrirModalExcluir(usuario: UsuarioResponse): void {
    this.usuarioParaExcluir = usuario;
  }

  cancelarExclusao(): void {
    if (this.excluindoId !== null) {
      return;
    }
    this.usuarioParaExcluir = null;
  }

  confirmarExclusao(): void {
    if (!this.usuarioParaExcluir) {
      return;
    }

    const usuario = this.usuarioParaExcluir;
    this.excluindoId = usuario.id;

    this.usuarioService.excluir(usuario.id).subscribe({
      next: () => {
        this.usuarios = this.usuarios.filter(item => item.id !== usuario.id);
        this.usuariosFiltrados = this.usuariosFiltrados.filter(item => item.id !== usuario.id);

        this.usuarioParaExcluir = null;
        this.excluindoId = null;

        this.tituloSucesso = 'Usuário excluído';
        this.mensagemSucesso = `O usuário ${usuario.nome} foi excluído com sucesso.`;
        this.modalSucessoAberto = true;

        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao excluir usuário:', erro);
        const mensagem = mensagemErroExclusao(
          erro,
          'Não foi possível excluir o usuário.',
          // As sessões (refresh_token) do usuário impedem a exclusão no backend
          'Não é possível excluir este usuário porque ele já acessou o sistema. Desative-o em vez de excluir.'
        );
        if (mensagem) this.toast.erro(mensagem);
        this.usuarioParaExcluir = null;
        this.excluindoId = null;
        this.cdr.markForCheck();
      }
    });
  }

  fecharModalSucesso(): void {
    this.modalSucessoAberto = false;
  }
}