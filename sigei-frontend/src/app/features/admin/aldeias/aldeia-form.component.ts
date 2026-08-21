import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AldeiaRequest, AldeiaService } from '../../../core/services/aldeia.service';
import { TerraIndigenaEstatistica, TerraIndigenaService } from '../../../core/services/terra-indigena.service';
import { PovoResponse, PovoService } from '../../../core/services/povo.service';

@Component({
  selector: 'app-aldeia-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="max-w-3xl mx-auto space-y-5 font-sans pb-10">
      <!-- Breadcrumb -->
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i>
          Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <a routerLink="/admin/aldeias" class="hover:text-emerald-600 transition-colors">
          Aldeias
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">
          {{ editando ? 'Editar Aldeia' : 'Nova Aldeia' }}
        </span>
      </nav>

      <!-- Cabeçalho -->
      <div class="flex items-center gap-4">
        <a routerLink="/admin/aldeias" class="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-all shadow-sm">
          <i class="fa-solid fa-arrow-left"></i>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">
            {{ editando ? 'Editar Aldeia' : 'Cadastrar Aldeia' }}
          </h1>
          <p class="text-sm text-slate-500 mt-0.5">
            Informe o nome da aldeia, terra indígena e povo.
          </p>
        </div>
      </div>

      <!-- Card -->
      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <form [formGroup]="form" (ngSubmit)="salvar()" class="p-8 space-y-6">
          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100">
              Dados da Aldeia
            </h3>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
              <!-- Nome -->
              <div class="md:col-span-2">
                <label for="nome" class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Nome da Aldeia
                  <span class="text-rose-500">*</span>
                </label>
                <input
                  id="nome"
                  type="text"
                  formControlName="nome"
                  placeholder="Ex: Liberdade"
                  class="w-full px-4 py-2.5 bg-white border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                  [class.border-rose-400]="form.controls.nome.invalid && form.controls.nome.touched"
                  [class.border-slate-200]="!form.controls.nome.invalid || !form.controls.nome.touched"
                />
                @if (form.controls.nome.invalid && form.controls.nome.touched) {
                  <p class="text-xs text-rose-500 mt-1.5">Informe o nome da aldeia.</p>
                }
              </div>

              <!-- Terra Indígena -->
              <div>
                <label for="terraIndigenaId" class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Terra Indígena
                  <span class="text-rose-500">*</span>
                </label>
                <select
                  id="terraIndigenaId"
                  formControlName="terraIndigenaId"
                  class="w-full px-4 py-2.5 bg-white border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                  [class.border-rose-400]="form.controls.terraIndigenaId.invalid && form.controls.terraIndigenaId.touched"
                  [class.border-slate-200]="!form.controls.terraIndigenaId.invalid || !form.controls.terraIndigenaId.touched"
                >
                  <option [ngValue]="0">Selecione uma terra indígena...</option>
                  @for (terra of terras; track terra.id) {
                    <option [ngValue]="terra.id">
                      {{ terra.nome }} — {{ terra.municipio.nome }}
                    </option>
                  }
                </select>
                @if (form.controls.terraIndigenaId.invalid && form.controls.terraIndigenaId.touched) {
                  <p class="text-xs text-rose-500 mt-1.5">Selecione uma terra indígena.</p>
                }
              </div>

              <!-- Povo -->
              <div>
                <label for="povoId" class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Povo
                  <span class="text-rose-500">*</span>
                </label>
                <select
                  id="povoId"
                  formControlName="povoId"
                  class="w-full px-4 py-2.5 bg-white border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                  [class.border-rose-400]="form.controls.povoId.invalid && form.controls.povoId.touched"
                  [class.border-slate-200]="!form.controls.povoId.invalid || !form.controls.povoId.touched"
                >
                  <option [ngValue]="0">Selecione um povo...</option>
                  @for (povo of povos; track povo.id) {
                    <option [ngValue]="povo.id">
                      {{ povo.nome }}
                    </option>
                  }
                </select>
                @if (form.controls.povoId.invalid && form.controls.povoId.touched) {
                  <p class="text-xs text-rose-500 mt-1.5">Selecione um povo.</p>
                }
              </div>
            </div>
          </div>

          <!-- Loading combinados -->
          @if (carregandoDados) {
            <div class="text-sm text-slate-400 flex items-center gap-2">
              <i class="fa-solid fa-circle-notch animate-spin"></i>
              Carregando terras indígenas e povos...
            </div>
          }

          <!-- Mensagem de erro -->
          @if (erroMensagem) {
            <div class="flex items-start gap-3 p-4 rounded-xl bg-rose-50 border border-rose-100 text-rose-700 text-sm">
              <i class="fa-solid fa-circle-exclamation mt-0.5"></i>
              <span>
                {{ erroMensagem }}
              </span>
            </div>
          }

          <!-- Botões -->
          <div class="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <a routerLink="/admin/aldeias" class="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 transition-all">
              Cancelar
            </a>
            <button
              type="submit"
              [disabled]="salvando || carregandoDados"
              class="bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-400 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all"
            >
              @if (salvando) {
                <i class="fa-solid fa-circle-notch animate-spin text-xs"></i>
                {{ editando ? 'Salvando alterações...' : 'Salvando...' }}
              } @else {
                <i class="fa-solid fa-check text-xs"></i>
                {{ editando ? 'Salvar Alterações' : 'Salvar Aldeia' }}
              }
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class AldeiaFormComponent implements OnInit {
  // --- Injeções de Dependência ---
  private readonly fb = inject(FormBuilder);
  private readonly aldeiaService = inject(AldeiaService);
  private readonly terraIndigenaService = inject(TerraIndigenaService);
  private readonly povoService = inject(PovoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly cdr = inject(ChangeDetectorRef);

  // --- Propriedades de Estado ---
  terras: TerraIndigenaEstatistica[] = [];
  povos: PovoResponse[] = [];
  
  editando = false;
  aldeiaId: number | null = null;
  
  carregandoTerras = false;
  carregandoPovos = false;
  salvando = false;
  erroMensagem = '';

  // --- Formulário ---
  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2)]],
    terraIndigenaId: [0, [Validators.required, Validators.min(1)]],
    povoId: [0, [Validators.required, Validators.min(1)]],
  });

  // --- Getters ---
  get carregandoDados(): boolean {
    return this.carregandoTerras || this.carregandoPovos;
  }

  // --- Ciclo de Vida ---
  ngOnInit(): void {
    this.carregarTerras();
    this.carregarPovos();

    const id = this.route.snapshot.paramMap.get('id');

    if (!id) {
      return;
    }

    const aldeiaId = Number(id);

    if (Number.isNaN(aldeiaId)) {
      this.erroMensagem = 'Identificador da aldeia inválido.';
      return;
    }

    this.editando = true;
    this.aldeiaId = aldeiaId;
    this.carregarAldeia();
  }

  // --- Métodos de Negócio (Carregamento de Dados) ---
  carregarAldeia(): void {
    if (this.aldeiaId === null) {
      return;
    }

    this.aldeiaService.buscarPorId(this.aldeiaId).subscribe({
      next: (aldeia) => {
        this.form.patchValue({
          nome: aldeia.nome,
          terraIndigenaId: aldeia.terraIndigena.id,
          povoId: aldeia.povo.id
        });
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar aldeia:', erro);
        this.erroMensagem = 'Não foi possível carregar os dados da aldeia.';
        this.cdr.markForCheck();
      }
    });
  }

  carregarTerras(): void {
    this.carregandoTerras = true;

    this.terraIndigenaService.listarEstatisticas().subscribe({
      next: (dados) => {
        this.terras = [...(dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome));
        this.carregandoTerras = false;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar terras indígenas:', erro);
        this.erroMensagem = 'Não foi possível carregar as terras indígenas.';
        this.carregandoTerras = false;
        this.cdr.markForCheck();
      },
    });
  }

  carregarPovos(): void {
    this.carregandoPovos = true;

    this.povoService.listar().subscribe({
      next: (dados) => {
        this.povos = [...(dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome));
        this.carregandoPovos = false;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar povos:', erro);
        this.erroMensagem = 'Não foi possível carregar os povos.';
        this.carregandoPovos = false;
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

    const valores = this.form.getRawValue();

    const dados: AldeiaRequest = {
      nome: valores.nome.trim(),
      terraIndigenaId: valores.terraIndigenaId,
      povoId: valores.povoId
    };

    // Fluxo de Edição
    if (this.editando && this.aldeiaId !== null) {
      const dadosEdicao: AldeiaRequest = {
        id: this.aldeiaId,
        nome: valores.nome.trim(),
        terraIndigenaId: valores.terraIndigenaId,
        povoId: valores.povoId
      };

      this.aldeiaService.editar(dadosEdicao).subscribe({
        next: () => {
          this.salvando = false;
          this.router.navigate(['/admin/aldeias']);
        },
        error: (erro) => {
          console.error('Erro ao editar aldeia:', erro);
          this.erroMensagem = 'Não foi possível editar a aldeia.';
          this.salvando = false;
          this.cdr.markForCheck();
        }
      });
      return;
    }

    // Fluxo de Cadastro
    this.aldeiaService.salvar(dados).subscribe({
      next: () => {
        this.salvando = false;
        this.router.navigate(['/admin/aldeias']);
      },
      error: (erro) => {
        console.error('Erro ao cadastrar aldeia:', erro);
        this.erroMensagem = 'Não foi possível cadastrar a aldeia.';
        this.salvando = false;
        this.cdr.markForCheck();
      }
    });
  }
}