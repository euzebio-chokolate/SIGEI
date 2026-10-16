import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { PovoRequest, PovoService } from '@domains/territorios/data-access/povo.service';
import { mensagemErroCarregamento } from '@core/http/erro-http';
import { mensagemErroFormulario } from '@core/http/erros-formulario';
import { TAMANHO_TEXTO, textoObrigatorio } from '@core/forms/validadores';
import { ToastService } from '@core/notificacao/toast.service';
import { ErroCampoComponent } from '@shared/ui/erro-campo/erro-campo.component';
import { SkeletonFormularioComponent } from '@shared/ui/skeleton/skeleton-formulario.component';

@Component({
  selector: 'app-povo-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ErroCampoComponent, SkeletonFormularioComponent],
  template: `
    <div class="max-w-3xl mx-auto space-y-5 font-sans pb-10">
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i>
          Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <a routerLink="/admin/povos" class="hover:text-emerald-600 transition-colors">
          Povos
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">
          {{ editando ? 'Editar Povo' : 'Novo Povo' }}
        </span>
      </nav>

      <div class="flex items-center gap-4">
        <a routerLink="/admin/povos" class="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-all shadow-sm">
          <i class="fa-solid fa-arrow-left"></i>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">
            {{ editando ? 'Editar Povo' : 'Cadastrar Povo' }}
          </h1>
          <p class="text-sm text-slate-500 mt-0.5">
            @if (editando) {
              Altere o nome do povo indígena.
            } @else {
              Informe o nome do povo indígena.
            }
          </p>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        @if (carregandoRegistro) {
          <div class="p-8">
            <app-skeleton-formulario [campos]="2" [contorno]="false" rotulo="Carregando povo..." />
          </div>
        } @else {
        <form [formGroup]="form" (ngSubmit)="salvar()" class="p-8 space-y-6">
          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100">
              Dados do Povo
            </h3>

            <div>
              <label for="nome" class="block text-xs font-semibold text-slate-700 mb-1.5">
                Nome do Povo
                <span class="text-rose-500">*</span>
              </label>

              <input
                id="nome"
                type="text"
                formControlName="nome"
                placeholder="Ex: Huni Kuin"
                class="w-full px-4 py-2.5 bg-white border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                [class.border-rose-400]="form.controls.nome.invalid && form.controls.nome.touched"
                [class.border-slate-200]="!form.controls.nome.invalid || !form.controls.nome.touched"
              />

              <app-erro-campo [controle]="form.controls.nome" [mensagens]="{ required: 'Informe o nome do povo.' }" />
            </div>
          </div>

          @if (erroMensagem) {
            <div class="flex items-start gap-3 p-4 rounded-xl bg-rose-50 border border-rose-100 text-rose-700 text-sm">
              <i class="fa-solid fa-circle-exclamation mt-0.5"></i>
              <span>
                {{ erroMensagem }}
              </span>
            </div>
          }

          <div class="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <a routerLink="/admin/povos" class="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 transition-all">
              Cancelar
            </a>

            <button
              type="submit"
              [disabled]="salvando"
              class="bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-400 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all"
            >
              @if (salvando) {
                <i class="fa-solid fa-circle-notch animate-spin text-xs"></i>
                {{ editando ? 'Salvando alterações...' : 'Salvando...' }}
              } @else {
                <i class="fa-solid fa-check text-xs"></i>
                {{ editando ? 'Salvar Alterações' : 'Salvar Povo' }}
              }
            </button>
          </div>
        </form>
        }
      </div>
    </div>
  `,
})
export class PovoFormComponent implements OnInit {
  // --- Injeções de Dependência ---
  private readonly fb = inject(FormBuilder);
  private readonly povoService = inject(PovoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);

  // --- Propriedades de Estado ---
  editando = false;
  povoId: number | null = null;
  salvando = false;
  erroMensagem = '';
  carregandoRegistro = false;

  // --- Formulário ---
  form = this.fb.nonNullable.group({
    nome: ['', [textoObrigatorio, Validators.minLength(2), Validators.maxLength(TAMANHO_TEXTO)]],
  });

  // --- Ciclo de Vida ---
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) {
      return;
    }

    const povoId = Number(id);

    if (Number.isNaN(povoId)) {
      this.erroMensagem = 'Identificador do povo inválido.';
      return;
    }

    this.editando = true;
    this.povoId = povoId;

    this.carregarPovo();
  }

  // --- Métodos de Negócio (Carregamento de Dados) ---
  carregarPovo(): void {
    if (this.povoId === null) {
      return;
    }

    this.carregandoRegistro = true;

    this.povoService.buscarPorId(this.povoId).subscribe({
      next: (povo) => {
        this.carregandoRegistro = false;
        this.form.patchValue({
          nome: povo.nome,
        });

        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar povo:', erro);
        this.carregandoRegistro = false;
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os dados do povo.', 'Povo não encontrado.');
        this.cdr.markForCheck();
      },
    });
  }

  // --- Métodos de Negócio (Salvar/Editar) ---
  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando = true;
    this.erroMensagem = '';

    const dados: PovoRequest = {
      nome: this.form.getRawValue().nome.trim(),
    };

    // Fluxo de edição
    if (this.editando && this.povoId !== null) {
      this.povoService.editar(this.povoId, dados).subscribe({
        next: () => {
          this.salvando = false;
          this.toast.sucesso('Os dados do povo foram atualizados.', 'Povo atualizado');
          this.router.navigate(['/admin/povos']);
        },
        error: (erro) => {
          console.error('Erro ao editar povo:', erro);
          this.erroMensagem = mensagemErroFormulario(this.form, erro, 'Não foi possível editar o povo.');
          this.salvando = false;
          this.cdr.markForCheck();
        },
      });
      return;
    }

    // Fluxo de Cadastro
    this.povoService.salvar(dados).subscribe({
      next: () => {
        this.salvando = false;
        this.toast.sucesso('O novo povo foi cadastrado.', 'Povo cadastrado');
        this.router.navigate(['/admin/povos']);
      },
      error: (erro) => {
        console.error('Erro ao cadastrar povo:', erro);
        this.erroMensagem = mensagemErroFormulario(this.form, erro, 'Não foi possível cadastrar o povo.');
        this.salvando = false;
        this.cdr.markForCheck();
      },
    });
  }
}