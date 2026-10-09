import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { interpretarErro, mensagemErroCarregamento } from '@core/http/erro-http';
import { mensagemErroFormulario } from '@core/http/erros-formulario';
import { ToastService } from '@core/notificacao/toast.service';
import { ProfessorRequest, ProfessorService } from '@domains/professores/data-access/professor.service';
import {
  LotacaoProfessorResponse,
  LotacaoProfessorService
} from '@domains/professores/data-access/lotacao-professor.service';
import {
  OPCOES_SEXO,
  Sexo,
  cpfValido,
  formatarCpf,
  formatarData,
  rotuloTipoContrato,
  somenteDigitos
} from '@domains/professores/models/professor.models';
import { ErroCampoComponent } from '@shared/ui/erro-campo/erro-campo.component';
import { SucessoModalComponent } from '@shared/ui/sucesso-modal/sucesso-modal.component';

function validarCpf(controle: AbstractControl): ValidationErrors | null {
  const valor = controle.value as string;
  return !valor || cpfValido(valor) ? null : { cpf: true };
}

@Component({
  selector: 'app-professor-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, SucessoModalComponent, ErroCampoComponent],
  templateUrl: './professor-form.component.html'
})
export class ProfessorFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly professorService = inject(ProfessorService);
  private readonly lotacaoService = inject(LotacaoProfessorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);

  readonly opcoesSexo = OPCOES_SEXO;
  readonly rotuloTipoContrato = rotuloTipoContrato;
  readonly formatarData = formatarData;

  editando = false;
  professorId: number | null = null;
  carregando = false;
  salvando = false;
  erroMensagem = '';
  lotacoes: LotacaoProfessorResponse[] = [];

  modalSucessoAberto = false;
  tituloSucesso = '';
  mensagemSucesso = '';

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(255)]],
    etnia: ['', [Validators.maxLength(255)]],
    sexo: ['' as Sexo | ''],
    formacao: ['', [Validators.maxLength(255)]],
    cpf: ['', [validarCpf]],
    matricula: ['', [Validators.maxLength(30)]]
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (id) {
      this.editando = true;
      this.professorId = id;
      this.carregar(id);
    }
  }

  carregar(id: number): void {
    this.carregando = true;

    this.professorService.buscarPorId(id).subscribe({
      next: professor => {
        this.form.patchValue({
          nome: professor.nome,
          etnia: professor.etnia ?? '',
          sexo: professor.sexo ?? '',
          formacao: professor.formacao ?? '',
          cpf: formatarCpf(professor.cpf),
          matricula: professor.matricula ?? ''
        });
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar professor:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar o professor.', 'Professor não encontrado.');
        this.carregando = false;
        this.cdr.markForCheck();
      }
    });

    this.lotacaoService.listarPorProfessor(id).subscribe({
      next: lotacoes => {
        this.lotacoes = lotacoes ?? [];
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar lotações do professor:', erro);
        const falha = interpretarErro(erro);
        if (!falha.global) this.toast.aviso('Não foi possível carregar as lotações deste professor.');
      }
    });
  }

  /** Aplica a máscara 000.000.000-00 enquanto o CPF é digitado. */
  formatarCampoCpf(): void {
    const controle = this.form.controls.cpf;
    const formatado = formatarCpf(controle.value);

    if (formatado !== controle.value) {
      controle.setValue(formatado, { emitEvent: false });
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.salvando) return;

    this.salvando = true;
    this.erroMensagem = '';

    const valor = this.form.getRawValue();
    const dados: ProfessorRequest = {
      nome: valor.nome.trim(),
      etnia: valor.etnia.trim() || null,
      sexo: valor.sexo || null,
      formacao: valor.formacao.trim() || null,
      cpf: somenteDigitos(valor.cpf) || null,
      matricula: valor.matricula.trim() || null
    };

    if (this.editando && this.professorId) {
      dados.id = this.professorId;
    }

    const requisicao = this.editando
      ? this.professorService.editar(dados)
      : this.professorService.salvar(dados);

    requisicao.subscribe({
      next: professor => {
        this.salvando = false;
        this.professorId = professor.id;
        this.tituloSucesso = this.editando ? 'Professor atualizado' : 'Professor cadastrado';
        this.mensagemSucesso = this.editando
          ? 'Os dados do professor foram atualizados com sucesso.'
          : 'Cadastro concluído. Em seguida, registre a lotação no ano.';
        this.modalSucessoAberto = true;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao salvar professor:', erro);
        this.erroMensagem = mensagemErroFormulario(this.form, erro, 'Não foi possível salvar o professor.', [
          { campo: 'cpf', padrao: /CPF/ }
        ]);
        this.salvando = false;
        this.cdr.markForCheck();
      }
    });
  }

  fecharModalSucesso(): void {
    this.modalSucessoAberto = false;

    if (!this.editando && this.professorId) {
      this.router.navigate(['/admin/professores/lotacoes/novo'], { queryParams: { professorId: this.professorId } });
      return;
    }

    this.router.navigate(['/admin/professores']);
  }
}
