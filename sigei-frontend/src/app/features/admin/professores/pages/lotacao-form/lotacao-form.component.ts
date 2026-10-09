import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { AnoReferencia, AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { EscolaResponse, EscolaService } from '@domains/escolas/data-access/escola.service';
import { MunicipioEstatistica, MunicipioService } from '@domains/territorios/data-access/municipio.service';
import { ProfessorResponse, ProfessorService } from '@domains/professores/data-access/professor.service';
import {
  LotacaoProfessorRequest,
  LotacaoProfessorService
} from '@domains/professores/data-access/lotacao-professor.service';
import {
  ANO_QUADRO_PROFESSORES,
  OPCOES_TIPO_CONTRATO,
  TipoContrato
} from '@domains/professores/models/professor.models';
import { mensagemErroCarregamento } from '@core/http/erro-http';
import { mensagemErroFormulario } from '@core/http/erros-formulario';
import { ErroCampoComponent } from '@shared/ui/erro-campo/erro-campo.component';
import { SkeletonFormularioComponent } from '@shared/ui/skeleton/skeleton-formulario.component';
import { SucessoModalComponent } from '@shared/ui/sucesso-modal/sucesso-modal.component';

type Local = 'escola' | 'nucleo';

@Component({
  selector: 'app-lotacao-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, SucessoModalComponent, ErroCampoComponent, SkeletonFormularioComponent],
  templateUrl: './lotacao-form.component.html'
})
export class LotacaoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly lotacaoService = inject(LotacaoProfessorService);
  private readonly professorService = inject(ProfessorService);
  private readonly escolaService = inject(EscolaService);
  private readonly municipioService = inject(MunicipioService);
  private readonly anoReferenciaService = inject(AnoReferenciaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly opcoesTipoContrato = OPCOES_TIPO_CONTRATO;

  editando = false;
  lotacaoId: number | null = null;
  carregando = true;
  salvando = false;
  erroMensagem = '';

  anos: AnoReferencia[] = [];
  professores: ProfessorResponse[] = [];
  escolas: EscolaResponse[] = [];
  municipios: MunicipioEstatistica[] = [];
  buscaProfessor = '';

  modalSucessoAberto = false;
  tituloSucesso = '';
  mensagemSucesso = '';

  form = this.fb.group({
    anoReferenciaId: this.fb.control<number | null>(null, Validators.required),
    professorId: this.fb.control<number | null>(null),
    local: this.fb.nonNullable.control<Local>('escola'),
    municipioId: this.fb.control<number | null>(null, Validators.required),
    escolaId: this.fb.control<number | null>(null),
    cargo: this.fb.nonNullable.control('', Validators.maxLength(255)),
    segmento: this.fb.nonNullable.control('', Validators.maxLength(255)),
    tipoContrato: this.fb.control<TipoContrato | null>(null),
    inicioContrato: this.fb.control<string | null>(null),
    amparoLegal: this.fb.nonNullable.control('', Validators.maxLength(255)),
    numeroContrato: this.fb.nonNullable.control('', Validators.maxLength(30))
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    const professorId = Number(this.route.snapshot.queryParamMap.get('professorId'));

    if (id) {
      this.editando = true;
      this.lotacaoId = id;
    } else if (professorId) {
      this.form.controls.professorId.setValue(professorId);
    }

    this.form.controls.local.valueChanges.subscribe(() => this.atualizarValidacaoEscola());
    this.form.controls.municipioId.valueChanges.subscribe(() => {
      const escolaId = this.form.controls.escolaId.value;
      if (escolaId && !this.escolasDoMunicipio.some(escola => escola.id === escolaId)) {
        this.form.controls.escolaId.setValue(null);
      }
    });
    this.atualizarValidacaoEscola();

    forkJoin({
      anos: this.anoReferenciaService.listar(),
      professores: this.professorService.listar(),
      escolas: this.escolaService.listar(),
      municipios: this.municipioService.listarEstatisticas()
    }).subscribe({
      next: dados => {
        this.anos = [...dados.anos].sort((a, b) => b.ano - a.ano);
        this.professores = dados.professores;
        this.escolas = [...dados.escolas].sort((a, b) => a.nome.localeCompare(b.nome));
        this.municipios = [...dados.municipios].sort((a, b) => a.nome.localeCompare(b.nome));

        if (this.editando && this.lotacaoId) {
          this.carregarLotacao(this.lotacaoId);
        } else {
          const padrao = this.anos.find(item => item.ano === ANO_QUADRO_PROFESSORES) ?? this.anos[0];
          this.form.controls.anoReferenciaId.setValue(padrao?.id ?? null);
          this.carregando = false;
          this.cdr.markForCheck();
        }
      },
      error: erro => {
        console.error('Erro ao carregar dados do formulário de lotação:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os dados do formulário.');
        this.carregando = false;
        this.cdr.markForCheck();
      }
    });
  }

  private carregarLotacao(id: number): void {
    this.lotacaoService.buscarPorId(id).subscribe({
      next: lotacao => {
        this.form.patchValue({
          anoReferenciaId: lotacao.anoReferencia.id,
          professorId: lotacao.professor?.id ?? null,
          local: lotacao.escola ? 'escola' : 'nucleo',
          municipioId: lotacao.municipio.id,
          escolaId: lotacao.escola?.id ?? null,
          cargo: lotacao.cargo ?? '',
          segmento: lotacao.segmento ?? '',
          tipoContrato: lotacao.tipoContrato,
          inicioContrato: lotacao.inicioContrato,
          amparoLegal: lotacao.amparoLegal ?? '',
          numeroContrato: lotacao.numeroContrato ?? ''
        });
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar lotação:', erro);
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar a lotação.', 'Lotação não encontrada.');
        this.carregando = false;
        this.cdr.markForCheck();
      }
    });
  }

  /** Professores que correspondem à busca; o selecionado continua na lista. */
  get professoresFiltrados(): ProfessorResponse[] {
    const termo = this.normalizar(this.buscaProfessor);
    const selecionado = this.form.controls.professorId.value;

    return this.professores
      .filter(professor => !termo || professor.id === selecionado || this.normalizar(professor.nome).includes(termo));
  }

  get escolasDoMunicipio(): EscolaResponse[] {
    const municipioId = this.form.controls.municipioId.value;
    if (!municipioId) return [];
    return this.escolas.filter(escola => escola.aldeia?.terraIndigena?.municipio?.id === municipioId);
  }

  get lotacaoNoNucleo(): boolean {
    return this.form.controls.local.value === 'nucleo';
  }

  atualizarBuscaProfessor(evento: Event): void {
    this.buscaProfessor = (evento.target as HTMLInputElement).value;
  }

  private atualizarValidacaoEscola(): void {
    const escola = this.form.controls.escolaId;

    if (this.lotacaoNoNucleo) {
      escola.clearValidators();
      escola.setValue(null);
    } else {
      escola.setValidators(Validators.required);
    }

    escola.updateValueAndValidity();
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
    const dados: LotacaoProfessorRequest = {
      professorId: valor.professorId,
      anoReferenciaId: valor.anoReferenciaId!,
      escolaId: this.lotacaoNoNucleo ? null : valor.escolaId,
      municipioId: valor.municipioId,
      cargo: valor.cargo.trim() || null,
      segmento: valor.segmento.trim() || null,
      tipoContrato: valor.tipoContrato || null,
      inicioContrato: valor.inicioContrato || null,
      amparoLegal: valor.amparoLegal.trim() || null,
      numeroContrato: valor.numeroContrato.trim() || null
    };

    if (this.editando && this.lotacaoId) {
      dados.id = this.lotacaoId;
    }

    const requisicao = this.editando ? this.lotacaoService.editar(dados) : this.lotacaoService.salvar(dados);

    requisicao.subscribe({
      next: () => {
        this.salvando = false;
        this.tituloSucesso = this.editando ? 'Lotação atualizada' : 'Lotação cadastrada';
        this.mensagemSucesso = this.editando
          ? 'Os dados da lotação foram atualizados com sucesso.'
          : 'A lotação foi cadastrada com sucesso.';
        this.modalSucessoAberto = true;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao salvar lotação:', erro);
        this.erroMensagem = mensagemErroFormulario(this.form, erro, 'Não foi possível salvar a lotação.', [
          { campo: 'anoReferenciaId', padrao: /ano/i },
          { campo: 'escolaId', padrao: /escola/i },
          { campo: 'professorId', padrao: /professor/i }
        ]);
        this.salvando = false;
        this.cdr.markForCheck();
      }
    });
  }

  fecharModalSucesso(): void {
    this.modalSucessoAberto = false;
    this.router.navigate(['/admin/professores']);
  }

  private normalizar(valor: string | null | undefined): string {
    return (valor ?? '').normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();
  }
}
