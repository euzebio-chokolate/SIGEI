import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TerraIndigenaRequest, TerraIndigenaService } from '@domains/territorios/data-access/terra-indigena.service';
import { MunicipioEstatistica, MunicipioService } from '@domains/territorios/data-access/municipio.service';
import { mensagemErroCarregamento } from '@core/http/erro-http';
import { mensagemErroFormulario } from '@core/http/erros-formulario';
import { ToastService } from '@core/notificacao/toast.service';
import { ErroCampoComponent } from '@shared/ui/erro-campo/erro-campo.component';

@Component({
  selector: 'app-terra-indigena-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ErroCampoComponent],
  template: `
    <div class="max-w-3xl mx-auto space-y-5 font-sans pb-10">
      <nav class="flex items-center gap-2 text-xs font-semibold text-slate-500">
        <a routerLink="/admin/dashboard" class="hover:text-emerald-600 transition-colors flex items-center gap-1.5">
          <i class="fa-solid fa-house text-slate-400"></i>
          Início
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <a routerLink="/admin/terras-indigenas" class="hover:text-emerald-600 transition-colors">
          Terras Indígenas
        </a>
        <i class="fa-solid fa-chevron-right text-[9px] text-slate-300"></i>
        <span class="text-slate-800">
          {{ editando ? 'Editar Terra Indígena' : 'Nova Terra Indígena' }}
        </span>
      </nav>

      <div class="flex items-center gap-4">
        <a routerLink="/admin/terras-indigenas" class="w-10 h-10 rounded-xl bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-all shadow-sm">
          <i class="fa-solid fa-arrow-left"></i>
        </a>
        <div>
          <h1 class="text-2xl font-bold text-slate-800 tracking-tight">
            {{ editando ? 'Editar Terra Indígena' : 'Cadastrar Terra Indígena' }}
          </h1>
          <p class="text-sm text-slate-500 mt-0.5">
            @if (editando) {
              Altere as informações da terra indígena.
            } @else {
              Informe o nome da terra indígena e o município ao qual ela pertence.
            }
          </p>
        </div>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <form [formGroup]="form" (ngSubmit)="salvar()" class="p-8 space-y-6">
          <div>
            <h3 class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4 pb-2 border-b border-slate-100">
              Dados da Terra Indígena
            </h3>

            <div class="grid grid-cols-1 gap-5">
              <div>
                <label for="nome" class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Nome da Terra Indígena
                  <span class="text-rose-500">*</span>
                </label>

                <input
                  id="nome"
                  type="text"
                  formControlName="nome"
                  placeholder="Ex: Cabeceira do Rio Acre"
                  class="w-full px-4 py-2.5 bg-white border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                  [class.border-rose-400]="form.controls.nome.invalid && form.controls.nome.touched"
                  [class.border-slate-200]="!form.controls.nome.invalid || !form.controls.nome.touched"
                />

                <app-erro-campo [controle]="form.controls.nome" [mensagens]="{ required: 'Informe o nome da terra indígena.' }" />
              </div>

              <div>
                <label for="municipioId" class="block text-xs font-semibold text-slate-700 mb-1.5">
                  Município
                  <span class="text-rose-500">*</span>
                </label>

                <select
                  id="municipioId"
                  formControlName="municipioId"
                  class="w-full px-4 py-2.5 bg-white border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 text-slate-800 transition-all shadow-sm"
                  [class.border-rose-400]="form.controls.municipioId.invalid && form.controls.municipioId.touched"
                  [class.border-slate-200]="!form.controls.municipioId.invalid || !form.controls.municipioId.touched"
                >
                  <option [ngValue]="0">Selecione um município...</option>
                  @for (municipio of municipios; track municipio.id) {
                    <option [ngValue]="municipio.id">
                      {{ municipio.nome }}
                    </option>
                  }
                </select>

                @if (carregandoMunicipios) {
                  <p class="text-xs text-slate-400 mt-1.5">
                    <i class="fa-solid fa-circle-notch animate-spin mr-1"></i>
                    Carregando municípios...
                  </p>
                }

                <app-erro-campo
                  [controle]="form.controls.municipioId"
                  [mensagens]="{ required: 'Selecione um município.', min: 'Selecione um município.' }"
                />
              </div>
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
            <a routerLink="/admin/terras-indigenas" class="px-5 py-2.5 rounded-xl text-sm font-semibold text-slate-600 hover:text-slate-800 hover:bg-slate-100 transition-all">
              Cancelar
            </a>

            <button
              type="submit"
              [disabled]="salvando || carregandoMunicipios"
              class="bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-400 disabled:cursor-not-allowed text-white px-6 py-2.5 rounded-xl text-sm font-semibold shadow-sm hover:shadow flex items-center gap-2 transition-all"
            >
              @if (salvando) {
                <i class="fa-solid fa-circle-notch animate-spin text-xs"></i>
                {{ editando ? 'Salvando alterações...' : 'Salvando...' }}
              } @else {
                <i class="fa-solid fa-check text-xs"></i>
                {{ editando ? 'Salvar Alterações' : 'Salvar Terra Indígena' }}
              }
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
})
export class TerraIndigenaFormComponent implements OnInit {
  // --- Injeções de Dependência ---
  private fb = inject(FormBuilder);
  private terraIndigenaService = inject(TerraIndigenaService);
  private municipioService = inject(MunicipioService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);
  private toast = inject(ToastService);

  // --- Propriedades de Estado ---
  editando = false;
  terraId: number | null = null;
  salvando = false;
  carregandoMunicipios = false;
  erroMensagem = '';
  municipios: MunicipioEstatistica[] = [];

  // --- Formulário ---
  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2)]],
    municipioId: [0, [Validators.required, Validators.min(1)]],
  });

  // --- Ciclo de Vida ---
  ngOnInit(): void {
    this.carregarMunicipios();
    
    const id = this.route.snapshot.paramMap.get('id');
    
    if (id) {
      const terraId = Number(id);
      
      if (!Number.isNaN(terraId)) {
        this.editando = true;
        this.terraId = terraId;
        this.carregarTerra();
      }
    }
  }

  // --- Métodos de Negócio ---
  carregarMunicipios(): void {
    this.carregandoMunicipios = true;

    this.municipioService.listarEstatisticas().subscribe({
      next: (dados) => {
        this.municipios = [...(dados ?? [])].sort((a, b) => a.nome.localeCompare(b.nome));
        this.carregandoMunicipios = false;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar municípios:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os municípios.');
        this.carregandoMunicipios = false;
        this.cdr.markForCheck();
      },
    });
  }

  carregarTerra(): void {
    if (this.terraId === null) {
      return;
    }

    this.terraIndigenaService.buscarPorId(this.terraId).subscribe({
      next: (terra) => {
        this.form.patchValue({
          nome: terra.nome,
          municipioId: terra.municipio.id,
        });
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar terra indígena:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os dados da terra indígena.', 'Terra indígena não encontrada.');
        this.cdr.markForCheck();
      },
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando = true;
    this.erroMensagem = '';

    const valores = this.form.getRawValue();
    const dados: TerraIndigenaRequest = {
      nome: valores.nome.trim(),
      municipioId: valores.municipioId,
    };

    // Fluxo de Edição
    if (this.editando && this.terraId !== null) {
      const dadosEdicao: TerraIndigenaRequest = {
        id: this.terraId,
        nome: valores.nome.trim(),
        municipioId: valores.municipioId,
      };

      this.terraIndigenaService.editar(dadosEdicao).subscribe({
        next: () => {
          this.salvando = false;
          this.toast.sucesso('Os dados da terra indígena foram atualizados.', 'Terra indígena atualizada');
          this.router.navigate(['/admin/terras-indigenas']);
        },
        error: (erro) => {
          console.error('Erro ao editar terra indígena:', erro);
          this.erroMensagem = mensagemErroFormulario(this.form, erro, 'Não foi possível salvar as alterações.');
          this.salvando = false;
          this.cdr.markForCheck();
        },
      });
      return;
    }

    // Fluxo de Cadastro
    this.terraIndigenaService.salvar(dados).subscribe({
      next: () => {
        this.salvando = false;
        this.toast.sucesso('A nova terra indígena foi cadastrada.', 'Terra indígena cadastrada');
        this.router.navigate(['/admin/terras-indigenas']);
      },
      error: (erro) => {
        console.error('Erro ao cadastrar terra indígena:', erro);
        this.erroMensagem = mensagemErroFormulario(this.form, erro, 'Não foi possível cadastrar a terra indígena.');
        this.salvando = false;
        this.cdr.markForCheck();
      },
    });
  }
}