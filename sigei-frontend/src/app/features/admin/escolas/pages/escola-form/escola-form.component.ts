import { ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, filter, forkJoin, map, Observable, of, switchMap, tap } from 'rxjs';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { EscolaResponse, EscolaService } from '@domains/escolas/data-access/escola.service';
import { AldeiaResponse, AldeiaService } from '@domains/territorios/data-access/aldeia.service';
import { AnoReferencia, AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { RegistroAnualResponse, RegistroAnualService } from '@domains/escolas/data-access/registro-anual.service';
import { RegistroAnualDetalhe } from '@domains/escolas/models/escola.models';
import { EquipeEscolaService } from '@domains/escolas/data-access/equipe-escola.service';
import { InfraestruturaService } from '@domains/escolas/data-access/infraestrutura.service';
import { MatriculaNivelService } from '@domains/escolas/data-access/matricula-nivel.service';
import { MatriculaSerieService } from '@domains/escolas/data-access/matricula-serie.service';

import { EscolaEtapaComponent } from '@features/admin/escolas/ui/form-steps/escola-etapa.component';
import { RegistroAnualEtapaComponent, StatusAnoRegistro } from '@features/admin/escolas/ui/form-steps/registro-anual-etapa.component';
import { EquipeEtapaComponent } from '@features/admin/escolas/ui/form-steps/equipe-etapa.component';
import { InfraestruturaEtapaComponent } from '@features/admin/escolas/ui/form-steps/infraestrutura-etapa.component';
import { MatriculasNivelEtapaComponent } from '@features/admin/escolas/ui/form-steps/matriculas-nivel-etapa.component';
import { MatriculasSerieEtapaComponent } from '@features/admin/escolas/ui/form-steps/matriculas-serie-etapa.component';
import { RevisaoEtapaComponent } from '@features/admin/escolas/ui/form-steps/revisao-etapa.component';
import { NIVEIS_MATRICULA, SERIES_MATRICULA } from './escola-form.config';

import { SucessoModalComponent } from '@shared/ui/sucesso-modal/sucesso-modal.component';

@Component({
  selector: 'app-escola-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    EscolaEtapaComponent,
    RegistroAnualEtapaComponent,
    EquipeEtapaComponent,
    InfraestruturaEtapaComponent,
    MatriculasNivelEtapaComponent,
    MatriculasSerieEtapaComponent,
    RevisaoEtapaComponent,
    SucessoModalComponent
  ],
  templateUrl: './escola-form.component.html'
})
export class EscolaFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly escolaService = inject(EscolaService);
  private readonly aldeiaService = inject(AldeiaService);
  private readonly anoReferenciaService = inject(AnoReferenciaService);
  private readonly registroAnualService = inject(RegistroAnualService);
  private readonly equipeEscolaService = inject(EquipeEscolaService);
  private readonly infraestruturaService = inject(InfraestruturaService);
  private readonly matriculaNivelService = inject(MatriculaNivelService);
  private readonly matriculaSerieService = inject(MatriculaSerieService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  etapaAtual = 1;
  salvando = false;
  carregandoAldeias = false;
  carregandoAnos = false;

  editando = false;
  escolaId: number | null = null;
  carregandoEdicao = false;

  // Estado do ano selecionado na edição: permite cadastrar/editar anos anteriores
  statusAno: StatusAnoRegistro = null;
  salvandoAno = false;
  erroNovoAno = '';
  private escolaCarregada = false;
  private anoPendente: number | null = null;
  private voltarParaHistorico = false;

  registroAnualId: number | null = null;
  equipeId: number | null = null;
  infraestruturaId: number | null = null;

  matriculasNivelIds = new Map<string, number>();
  matriculasSerieIds = new Map<string, number>();

  erroMensagem = '';
  erroAnos = '';
  fotoEscola: File | null = null;
  fotoUrlAtual: string | null = null;
  fotoAtualRemovida = false;

  aldeias: AldeiaResponse[] = [];
  private aldeiasCadastradas: AldeiaResponse[] = [];
  private escolasCadastradas: EscolaResponse[] = [];
  anosReferencia: AnoReferencia[] = [];
  escolasSede: { id: number; codigo: string; nome: string; }[] = [];

  modalSucessoAberto = false;
  tituloSucesso = '';
  mensagemSucesso = '';

  form = this.fb.group({
    codigo: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    nome: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(2)]),
    tipoUnidade: this.fb.nonNullable.control('', Validators.required),
    aldeiaId: this.fb.nonNullable.control(0, [Validators.required, Validators.min(1)]),
    escolaSedeId: this.fb.control<number | null>(null)
  });

  registroForm = this.fb.nonNullable.group({
    anoReferenciaId: [0, [Validators.required, Validators.min(1)]],
    rede: ['', Validators.required],
    localFuncionamento: ['', Validators.required],
    totalAlunos: this.fb.control<number | null>(null, Validators.min(0))
  });

  equipeForm = this.fb.nonNullable.group({
    temProfessorResponsavel: [false],
    temEquipeGestora: [false],
    temMerendeira: [false],
    temZeladora: [false],
    temBarqueiro: [false],
    necessitaTransporte: [false]
  });

  infraestruturaForm = this.fb.group({
    anoConstrucao: this.fb.control<number | null>(null),
    numSalas: this.fb.control<number | null>(null),
    latitude: this.fb.control<number | null>(null),
    longitude: this.fb.control<number | null>(null),
    situacao: this.fb.nonNullable.control('', Validators.required),
    tipoAcesso: this.fb.nonNullable.control('', Validators.required),
    temPredio: this.fb.nonNullable.control(false),
    tipoAgua: this.fb.nonNullable.control('', Validators.required),
    temInternet: this.fb.nonNullable.control(false),
    tipoEnergia: this.fb.nonNullable.control('', Validators.required),
    temRegistroFotografico: this.fb.nonNullable.control(false)
  });

  readonly niveisMatricula = [...NIVEIS_MATRICULA];

  matriculasNivelForm = this.fb.group({
    matriculas: this.fb.array(
      this.niveisMatricula.map(item =>
        this.fb.group({
          nivel: this.fb.nonNullable.control(item.nivel),
          qtdMasculino: this.fb.control<number | null>(null, [Validators.min(0)]),
          qtdFeminino: this.fb.control<number | null>(null, [Validators.min(0)])
        })
      )
    )
  });

  readonly seriesMatricula = [...SERIES_MATRICULA];

  matriculasSerieForm = this.fb.group({
    matriculas: this.fb.array(
      this.seriesMatricula.map(item =>
        this.fb.group({
          denominacao: this.fb.nonNullable.control(item.denominacao),
          etapaEnsino: this.fb.nonNullable.control(item.etapaEnsino),
          qtdMasculino: this.fb.control<number | null>(null, [Validators.min(0)]),
          qtdFeminino: this.fb.control<number | null>(null, [Validators.min(0)])
        })
      )
    )
  });

  ngOnInit(): void {
    this.carregarAldeias();
    this.carregarAnosReferencia();
    this.carregarEscolasSede();

    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      const id = Number(idParam);
      if (!Number.isNaN(id)) {
        // ?ano=1990 abre o registro daquele ano; ?novoAno=true inicia o cadastro de um ano anterior
        const query = this.route.snapshot.queryParamMap;
        const anoParam = Number(query.get('ano')) || null;
        const novoAno = query.get('novoAno') === 'true';

        this.editando = true;
        this.escolaId = id;
        this.anoPendente = anoParam;
        this.voltarParaHistorico = !!anoParam || novoAno;

        if (this.voltarParaHistorico) {
          this.etapaAtual = 2;
        }

        this.observarTrocaDeAno();
        this.carregarEscolaParaEdicao(id, novoAno);
      }
    }
  }

  get matriculasNivel(): FormArray<FormGroup<{
    nivel: FormControl<string>;
    qtdMasculino: FormControl<number | null>;
    qtdFeminino: FormControl<number | null>;
  }>> {
    return this.matriculasNivelForm.controls.matriculas;
  }

  get matriculasSerie(): FormArray<FormGroup<{
    denominacao: FormControl<string>;
    etapaEnsino: FormControl<string>;
    qtdMasculino: FormControl<number | null>;
    qtdFeminino: FormControl<number | null>;
  }>> {
    return this.matriculasSerieForm.controls.matriculas;
  }

  carregarEscolaParaEdicao(id: number, novoAno = false): void {
    this.carregandoEdicao = true;
    this.erroMensagem = '';

    this.escolaService.buscarPorId(id).subscribe({
      next: escola => {
        this.form.patchValue({
          codigo: escola.codigo,
          nome: escola.nome,
          tipoUnidade: escola.tipoUnidade,
          aldeiaId: escola.aldeia?.id ?? 0,
          escolaSedeId: escola.escolaSede?.id ?? null
        });

        this.aoAlterarTipoUnidade();

        const registro = escola.registroAnual;

        if (registro && (novoAno || this.anoPendente)) {
          // Outro ano será carregado/criado: reaproveita apenas rede e local como sugestão
          this.registroForm.patchValue({
            rede: registro.rede ?? '',
            localFuncionamento: registro.localFuncionamento ?? ''
          }, { emitEvent: false });
        } else if (registro) {
          this.aplicarRegistro(registro);
          this.statusAno = 'existente';
        }

        this.escolaCarregada = true;
        this.selecionarAnoPendente();
        this.carregandoEdicao = false;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao carregar escola para edição:', erro);
        this.erroMensagem = 'Não foi possível carregar os dados da escola.';
        this.carregandoEdicao = false;
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * Na edição, trocar o ano carrega o registro daquele ano (ou prepara um novo),
   * em vez de mover o registro atual para outro ano.
   */
  private observarTrocaDeAno(): void {
    this.registroForm.controls.anoReferenciaId.valueChanges.pipe(
      filter(() => this.escolaId !== null),
      tap(() => this.limparIdsAnuais()),
      switchMap(anoReferenciaId => this.buscarRegistroDoAno(anoReferenciaId)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(registro => {
      if (registro) {
        this.aplicarRegistro(registro);
        this.statusAno = 'existente';
      } else if (this.statusAno === 'carregando') {
        this.limparDadosAnuais();
        this.statusAno = 'novo';
      }
      this.cdr.markForCheck();
    });
  }

  private buscarRegistroDoAno(anoReferenciaId: number): Observable<RegistroAnualResponse | null> {
    const ano = this.anosReferencia.find(item => item.id === anoReferenciaId)?.ano;

    if (!ano || this.escolaId === null) {
      this.limparDadosAnuais();
      this.statusAno = null;
      return EMPTY;
    }

    this.statusAno = 'carregando';
    this.erroAnos = '';
    this.cdr.markForCheck();

    return this.registroAnualService.buscarPorEscolaEAno(this.escolaId, ano).pipe(
      catchError(erro => {
        if (erro?.status === 404) return of(null);

        console.error('Erro ao carregar registro do ano:', erro);
        // Sem saber se o ano já tem registro, não é seguro salvar: força nova seleção
        this.limparDadosAnuais();
        this.statusAno = null;
        this.registroForm.controls.anoReferenciaId.setValue(0, { emitEvent: false });
        this.erroAnos = `Não foi possível carregar os dados de ${ano}. Tente novamente.`;
        this.cdr.markForCheck();
        return EMPTY;
      })
    );
  }

  private selecionarAnoPendente(): void {
    if (!this.anoPendente || !this.escolaCarregada || this.anosReferencia.length === 0) return;

    const anoReferencia = this.anosReferencia.find(item => item.ano === this.anoPendente);
    this.anoPendente = null;

    if (anoReferencia) {
      this.registroForm.controls.anoReferenciaId.setValue(anoReferencia.id);
    }
  }

  private limparIdsAnuais(): void {
    this.registroAnualId = null;
    this.equipeId = null;
    this.infraestruturaId = null;
    this.matriculasNivelIds.clear();
    this.matriculasSerieIds.clear();
  }

  /** Limpa os dados do ano, mantendo escola, ano, rede e local selecionados. */
  private limparDadosAnuais(): void {
    this.limparIdsAnuais();

    this.registroForm.controls.totalAlunos.reset(null);
    this.equipeForm.reset();
    this.infraestruturaForm.reset();

    for (const grupo of this.matriculasNivel.controls) {
      grupo.patchValue({ qtdMasculino: null, qtdFeminino: null });
    }

    for (const grupo of this.matriculasSerie.controls) {
      grupo.patchValue({ qtdMasculino: null, qtdFeminino: null });
    }

    this.fotoEscola = null;
    this.fotoUrlAtual = null;
    this.fotoAtualRemovida = false;
  }

  private aplicarRegistro(registro: RegistroAnualDetalhe | RegistroAnualResponse): void {
    this.limparDadosAnuais();
    this.registroAnualId = registro.id;

    this.registroForm.patchValue({
      anoReferenciaId: registro.anoReferencia?.id ?? 0,
      rede: registro.rede ?? '',
      localFuncionamento: registro.localFuncionamento ?? '',
      totalAlunos: registro.totalAlunos ?? null
    }, { emitEvent: false });

    if (registro.equipe) {
      this.equipeId = registro.equipe.id;
      this.equipeForm.patchValue({
        temProfessorResponsavel: registro.equipe.temProfessorResponsavel,
        temEquipeGestora: registro.equipe.temEquipeGestora,
        temMerendeira: registro.equipe.temMerendeira,
        temZeladora: registro.equipe.temZeladora,
        temBarqueiro: registro.equipe.temBarqueiro,
        necessitaTransporte: registro.equipe.necessitaTransporte
      });
    }

    if (registro.infraestrutura) {
      this.fotoUrlAtual = registro.infraestrutura.fotoUrl ?? null;
      this.infraestruturaId = registro.infraestrutura.id;

      this.infraestruturaForm.patchValue({
        anoConstrucao: registro.infraestrutura.anoConstrucao,
        numSalas: registro.infraestrutura.numSalas,
        latitude: registro.infraestrutura.latitude,
        longitude: registro.infraestrutura.longitude,
        situacao: registro.infraestrutura.situacao ?? '',
        tipoAcesso: registro.infraestrutura.tipoAcesso ?? '',
        temPredio: registro.infraestrutura.temPredio,
        tipoAgua: registro.infraestrutura.tipoAgua ?? '',
        temInternet: registro.infraestrutura.temInternet,
        tipoEnergia: registro.infraestrutura.tipoEnergia ?? '',
        temRegistroFotografico: registro.infraestrutura.temRegistroFotografico
      });
    }

    const totalGeralExistente = registro.matriculasNivel?.find(item => item.nivel === 'TOTAL_GERAL');

    if (totalGeralExistente) {
      this.matriculasNivelIds.set('TOTAL_GERAL', totalGeralExistente.id);
    }

    for (const grupo of this.matriculasNivel.controls) {
      const nivel = grupo.controls.nivel.value;
      const encontrada = registro.matriculasNivel?.find(item => item.nivel === nivel);

      if (encontrada) {
        this.matriculasNivelIds.set(nivel, encontrada.id);
        grupo.patchValue({
          qtdMasculino: encontrada.qtdMasculino,
          qtdFeminino: encontrada.qtdFeminino
        });
      }
    }

    for (const grupo of this.matriculasSerie.controls) {
      const denominacao = grupo.controls.denominacao.value;
      const etapaEnsino = grupo.controls.etapaEnsino.value;
      const encontrada = registro.matriculasSerie?.find(
        item => item.denominacao === denominacao && item.etapaEnsino === etapaEnsino
      );

      if (encontrada) {
        const chave = `${denominacao}|${etapaEnsino}`;
        this.matriculasSerieIds.set(chave, encontrada.id);
        grupo.patchValue({
          qtdMasculino: encontrada.qtdMasculino,
          qtdFeminino: encontrada.qtdFeminino
        });
      }
    }
  }

  adicionarAnoReferencia(ano: number): void {
    const anoAtual = new Date().getFullYear();
    this.erroNovoAno = '';

    if (!Number.isInteger(ano) || ano < 1900 || ano > anoAtual) {
      this.erroNovoAno = `Informe um ano entre 1900 e ${anoAtual}.`;
      return;
    }

    const existente = this.anosReferencia.find(item => item.ano === ano);
    if (existente) {
      this.selecionarAnoReferencia(existente.id);
      return;
    }

    this.salvandoAno = true;

    this.anoReferenciaService.salvar(ano).subscribe({
      next: novo => {
        this.anosReferencia = [...this.anosReferencia, novo].sort((a, b) => b.ano - a.ano);
        this.salvandoAno = false;
        this.selecionarAnoReferencia(novo.id);
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error('Erro ao adicionar ano de referência:', erro);
        this.salvandoAno = false;
        this.erroNovoAno = erro?.status === 409
          ? 'Este ano já está cadastrado. Atualize a lista e selecione-o.'
          : 'Não foi possível adicionar o ano.';
        this.cdr.markForCheck();
      }
    });
  }

  private selecionarAnoReferencia(id: number): void {
    const control = this.registroForm.controls.anoReferenciaId;
    control.setValue(id);
    control.markAsTouched();
  }

  carregarAldeias(): void {
    this.carregandoAldeias = true;

    this.aldeiaService.listar().subscribe({
      next: (dados) => {
        this.aldeiasCadastradas = dados ?? [];
        this.atualizarAldeiasDisponiveis();
        this.carregandoAldeias = false;
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar aldeias:', erro);
        this.erroMensagem = 'Não foi possível carregar as aldeias.';
        this.carregandoAldeias = false;
        this.cdr.markForCheck();
      }
    });
  }

  carregarAnosReferencia(): void {
    this.carregandoAnos = true;
    this.erroAnos = '';

    this.anoReferenciaService.listar().subscribe({
      next: (dados) => {
        this.anosReferencia = [...(dados ?? [])].sort((a, b) => b.ano - a.ano);
        this.carregandoAnos = false;
        this.selecionarAnoPendente();
        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar anos de referência:', erro);
        this.erroAnos = 'Não foi possível carregar os anos de referência.';
        this.carregandoAnos = false;
        this.cdr.markForCheck();
      }
    });
  }

  carregarEscolasSede(): void {
    this.escolaService.listar().subscribe({
      next: (dados) => {
        this.escolasCadastradas = dados ?? [];
        this.atualizarAldeiasDisponiveis();
        this.escolasSede = this.escolasCadastradas
          .filter(escola => escola.tipoUnidade === 'SEDE')
          .map(escola => ({
            id: escola.id,
            codigo: escola.codigo,
            nome: escola.nome
          }));

        this.cdr.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar escolas sede:', erro);
      }
    });
  }

  private atualizarAldeiasDisponiveis(): void {
    const aldeiasVinculadas = new Set(
      this.escolasCadastradas
        .filter(escola => escola.id !== this.escolaId)
        .map(escola => escola.aldeia?.id)
        .filter((id): id is number => id !== undefined && id !== null)
    );

    this.aldeias = this.aldeiasCadastradas
      .filter(aldeia => !aldeiasVinculadas.has(aldeia.id))
      .sort((a, b) => a.nome.localeCompare(b.nome));
  }

  aoAlterarTipoUnidade(): void {
    const tipo = this.form.controls.tipoUnidade.value;
    const escolaSedeControl = this.form.controls.escolaSedeId;

    if (tipo === 'ANEXO') {
      escolaSedeControl.setValidators(Validators.required);
    } else {
      escolaSedeControl.clearValidators();
      escolaSedeControl.setValue(null);
    }

    escolaSedeControl.updateValueAndValidity();
  }

  aoAlterarFoto(foto: File | null): void {
    if (!foto && this.fotoUrlAtual) {
      this.fotoAtualRemovida = true;
      this.fotoUrlAtual = null;
    } else if (foto) {
      this.fotoAtualRemovida = false;
    }

    this.fotoEscola = foto;
  }

  avancar(): void {
    this.erroMensagem = '';

    if (this.etapaAtual === 1) {
      if (this.form.invalid) {
        this.form.markAllAsTouched();
        return;
      }
      this.etapaAtual = 2;
      this.cdr.markForCheck();
      return;
    }

    if (this.etapaAtual === 2) {
      if (this.statusAno === 'carregando') return;

      if (this.registroForm.invalid) {
        this.registroForm.markAllAsTouched();
        return;
      }
      this.etapaAtual = 3;
      this.cdr.markForCheck();
      return;
    }

    if (this.etapaAtual === 3) {
      this.etapaAtual = 4;
      this.cdr.markForCheck();
      return;
    }

    if (this.etapaAtual === 4) {
      if (this.infraestruturaForm.invalid) {
        this.infraestruturaForm.markAllAsTouched();
        return;
      }

      const temRegistroFotografico =
  this.infraestruturaForm.controls
    .temRegistroFotografico.value;

const possuiFoto =
  !!this.fotoEscola ||
  !!this.fotoUrlAtual;

if (
  temRegistroFotografico &&
  !possuiFoto
) {

  this.erroMensagem =
    'Selecione a foto do registro fotográfico.';

  this.cdr.markForCheck();

  return;
}

      this.etapaAtual = 5;
      this.cdr.markForCheck();
      return;
    }

    if (this.etapaAtual === 5) {
      if (this.matriculasNivelForm.invalid) {
        this.matriculasNivelForm.markAllAsTouched();
        return;
      }
      this.etapaAtual = 6;
      this.cdr.markForCheck();
      return;
    }

    if (this.etapaAtual === 6) {
      if (this.matriculasSerieForm.invalid) {
        this.matriculasSerieForm.markAllAsTouched();
        return;
      }
      this.etapaAtual = 7;
      this.cdr.markForCheck();
      return;
    }
  }

  voltarEtapa(): void {
    if (this.etapaAtual > 1) {
      this.etapaAtual--;
    }

    this.erroMensagem = '';
    this.cdr.markForCheck();
  }

  calcularTotalNivel(index: number): number {
    const grupo = this.matriculasNivel.at(index);
    const masculino = Number(grupo.controls.qtdMasculino.value ?? 0);
    const feminino = Number(grupo.controls.qtdFeminino.value ?? 0);
    return masculino + feminino;
  }

  calcularTotalMasculino(): number {
    return this.matriculasNivel.controls.reduce(
      (total, grupo) => total + Number(grupo.controls.qtdMasculino.value ?? 0),
      0
    );
  }

  calcularTotalFeminino(): number {
    return this.matriculasNivel.controls.reduce(
      (total, grupo) => total + Number(grupo.controls.qtdFeminino.value ?? 0),
      0
    );
  }

  calcularTotalGeral(): number {
    return this.calcularTotalMasculino() + this.calcularTotalFeminino();
  }

  montarMatriculasNivel() {
    const matriculas = this.matriculasNivel.controls.map((grupo, index) => ({
      nivel: grupo.controls.nivel.value,
      qtdMasculino: grupo.controls.qtdMasculino.value,
      qtdFeminino: grupo.controls.qtdFeminino.value,
      total: this.calcularTotalNivel(index)
    }));

    return [
      {
        nivel: 'TOTAL_GERAL',
        qtdMasculino: this.calcularTotalMasculino(),
        qtdFeminino: this.calcularTotalFeminino(),
        total: this.calcularTotalGeral()
      },
      ...matriculas
    ];
  }

  calcularTotalSerie(index: number): number {
    const grupo = this.matriculasSerie.at(index);
    const masculino = Number(grupo.controls.qtdMasculino.value ?? 0);
    const feminino = Number(grupo.controls.qtdFeminino.value ?? 0);
    return masculino + feminino;
  }

  calcularTotalSerieMasculino(): number {
    return this.matriculasSerie.controls.reduce(
      (total, grupo) => total + Number(grupo.controls.qtdMasculino.value ?? 0),
      0
    );
  }

  calcularTotalSerieFeminino(): number {
    return this.matriculasSerie.controls.reduce(
      (total, grupo) => total + Number(grupo.controls.qtdFeminino.value ?? 0),
      0
    );
  }

  calcularTotalSeries(): number {
    return this.calcularTotalSerieMasculino() + this.calcularTotalSerieFeminino();
  }

  montarMatriculasSerie() {
    return this.matriculasSerie.controls.map((grupo, index) => ({
      denominacao: grupo.controls.denominacao.value,
      etapaEnsino: grupo.controls.etapaEnsino.value,
      qtdMasculino: grupo.controls.qtdMasculino.value,
      qtdFeminino: grupo.controls.qtdFeminino.value,
      total: this.calcularTotalSerie(index)
    }));
  }

  get nomeAldeiaSelecionada(): string {
    const aldeiaId = this.form.controls.aldeiaId.value;
    return this.aldeias.find(aldeia => aldeia.id === aldeiaId)?.nome ?? '-';
  }

  get nomeEscolaSedeSelecionada(): string {
    const escolaSedeId = this.form.controls.escolaSedeId.value;
    if (!escolaSedeId) return '-';
    return this.escolasSede.find(escola => escola.id === escolaSedeId)?.nome ?? '-';
  }

  get anoReferenciaSelecionado(): string {
    const id = this.registroForm.controls.anoReferenciaId.value;
    const ano = this.anosReferencia.find(item => item.id === id);
    return ano ? String(ano.ano) : '-';
  }

  salvarCadastro(): void {
    if (this.salvando) return;

    // Ao abrir direto na etapa do ano (vindo do histórico), a etapa 1 não foi validada
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroMensagem = 'Confira os dados da escola na etapa 1 antes de salvar.';
      return;
    }

    this.salvando = true;
    this.erroMensagem = '';

    const dadosEscola = {
      ...(this.editando && this.escolaId ? { id: this.escolaId } : {}),
      codigo: this.form.controls.codigo.value,
      nome: this.form.controls.nome.value,
      tipoUnidade: this.form.controls.tipoUnidade.value,
      escolaSedeId: this.form.controls.escolaSedeId.value,
      aldeiaId: this.form.controls.aldeiaId.value
    };

    const escola$ = this.editando
      ? this.escolaService.editar(dadosEscola)
      : this.escolaService.salvar(dadosEscola);

    escola$.pipe(
      switchMap(escolaSalva => {
        const dadosRegistro = {
          ...(this.editando && this.registroAnualId ? { id: this.registroAnualId } : {}),
          escolaId: escolaSalva.id,
          anoReferenciaId: this.registroForm.controls.anoReferenciaId.value,
          rede: this.registroForm.controls.rede.value,
          localFuncionamento: this.registroForm.controls.localFuncionamento.value,
          totalAlunos: this.obterTotalAlunosRegistro()
        };

        const registro$ = this.editando && this.registroAnualId
          ? this.registroAnualService.editar(dadosRegistro)
          : this.registroAnualService.salvar(dadosRegistro);

        return registro$.pipe(
          map(registroSalvo => ({
            escola: escolaSalva,
            registro: registroSalvo
          }))
        );
      }),
      switchMap(({ escola, registro }) => {
        const registroId = registro.id;

        const dadosEquipe = {
          ...(this.editando && this.equipeId ? { id: this.equipeId } : {}),
          registroAnualId: registroId,
          temProfessorResponsavel: this.equipeForm.controls.temProfessorResponsavel.value,
          temEquipeGestora: this.equipeForm.controls.temEquipeGestora.value,
          temMerendeira: this.equipeForm.controls.temMerendeira.value,
          temZeladora: this.equipeForm.controls.temZeladora.value,
          temBarqueiro: this.equipeForm.controls.temBarqueiro.value,
          necessitaTransporte: this.equipeForm.controls.necessitaTransporte.value
        };

        const equipe$ = this.editando && this.equipeId
          ? this.equipeEscolaService.editar(dadosEquipe)
          : this.equipeEscolaService.salvar(dadosEquipe);

        const dadosInfraestrutura = {
          ...(this.editando && this.infraestruturaId ? { id: this.infraestruturaId } : {}),
          registroAnualId: registroId,
          anoConstrucao: this.infraestruturaForm.controls.anoConstrucao.value,
          numSalas: this.infraestruturaForm.controls.numSalas.value,
          latitude: this.infraestruturaForm.controls.latitude.value,
          longitude: this.infraestruturaForm.controls.longitude.value,
          situacao: this.infraestruturaForm.controls.situacao.value,
          tipoAcesso: this.infraestruturaForm.controls.tipoAcesso.value,
          temPredio: this.infraestruturaForm.controls.temPredio.value,
          tipoAgua: this.infraestruturaForm.controls.tipoAgua.value,
          temInternet: this.infraestruturaForm.controls.temInternet.value,
          tipoEnergia: this.infraestruturaForm.controls.tipoEnergia.value,
          temRegistroFotografico: this.infraestruturaForm.controls.temRegistroFotografico.value
        };

        const salvarInfraestrutura$ = this.editando && this.infraestruturaId
          ? this.infraestruturaService.editar(dadosInfraestrutura)
          : this.infraestruturaService.salvar(dadosInfraestrutura);

        const infraestrutura$ = salvarInfraestrutura$.pipe(
          switchMap(infraestruturaSalva => {
            if (this.fotoEscola) {
              return this.infraestruturaService.adicionarFoto(
                infraestruturaSalva.id,
                this.fotoEscola
              );
            }

            if (this.fotoAtualRemovida) {
              return this.infraestruturaService.excluirFoto(
                infraestruturaSalva.id
              ).pipe(map(() => infraestruturaSalva));
            }

            return of(infraestruturaSalva);
          })
        );

        const chamadasNivel = this.montarMatriculasNivel().map(matricula => {
          const idExistente = this.matriculasNivelIds.get(matricula.nivel);
          const dados = {
            ...(idExistente ? { id: idExistente } : {}),
            registroId,
            nivel: matricula.nivel,
            qtdMasculino: matricula.qtdMasculino,
            qtdFeminino: matricula.qtdFeminino,
            total: matricula.total
          };

          if (this.editando && idExistente) {
            return this.matriculaNivelService.editar(dados);
          }
          return this.matriculaNivelService.salvar(dados);
        });

        const matriculasNivel$ = forkJoin(chamadasNivel);

        const chamadasSerie = this.montarMatriculasSerie().map(matricula => {
          const chave = `${matricula.denominacao}|${matricula.etapaEnsino}`;
          const idExistente = this.matriculasSerieIds.get(chave);
          const dados = {
            ...(idExistente ? { id: idExistente } : {}),
            registroId,
            denominacao: matricula.denominacao,
            etapaEnsino: matricula.etapaEnsino,
            qtdMasculino: matricula.qtdMasculino,
            qtdFeminino: matricula.qtdFeminino,
            total: matricula.total
          };

          if (this.editando && idExistente) {
            return this.matriculaSerieService.editar(dados);
          }
          return this.matriculaSerieService.salvar(dados);
        });

        const matriculasSerie$ = forkJoin(chamadasSerie);

        return forkJoin({
          equipe: equipe$,
          infraestrutura: infraestrutura$,
          matriculasNivel: matriculasNivel$,
          matriculasSerie: matriculasSerie$
        }).pipe(
          map(resultado => ({
            escola,
            registro,
            ...resultado
          }))
        );
      })
    ).subscribe({
      next: resultado => {
        console.log(this.editando ? 'Escola atualizada com sucesso:' : 'Escola cadastrada com sucesso:', resultado);
        this.salvando = false;

        if (this.voltarParaHistorico) {
          this.tituloSucesso = `Registro de ${resultado.registro.anoReferencia.ano} salvo`;
          this.mensagemSucesso = 'Os dados do ano foram salvos. Os registros dos demais anos foram preservados.';
        } else {
          this.tituloSucesso = this.editando ? 'Escola atualizada' : 'Escola cadastrada';
          this.mensagemSucesso = this.editando
            ? 'Os dados da escola foram atualizados com sucesso.'
            : 'A nova escola foi cadastrada com sucesso.';
        }

        this.modalSucessoAberto = true;
        this.cdr.markForCheck();
      },
      error: erro => {
        console.error(
          this.editando ? 'Erro ao editar escola:' : 'Erro ao cadastrar escola:',
          erro
        );
        this.salvando = false;
        this.erroMensagem = this.obterMensagemErroCadastro(erro);
        this.cdr.markForCheck();
      }
    });
  }

  fecharModalSucesso(): void {
    this.modalSucessoAberto = false;

    if (this.voltarParaHistorico && this.escolaId) {
      this.router.navigate(['/admin/historico-escolas'], { queryParams: { escolaId: this.escolaId } });
      return;
    }

    this.router.navigate(['/admin/escolas']);
  }

  /** Total informado manualmente; na falta dele, o total das matrículas por nível (se houver). */
  obterTotalAlunosRegistro(): number | null {
    const informado = this.registroForm.controls.totalAlunos.value;
    if (informado !== null && informado !== undefined && String(informado) !== '') {
      return Number(informado);
    }

    const totalMatriculas = this.calcularTotalGeral();
    return totalMatriculas > 0 ? totalMatriculas : null;
  }

  private obterMensagemErroCadastro(erro: any): string {
    if (erro?.error?.message) return erro.error.message;
    if (typeof erro?.error === 'string') return erro.error;
    if (erro?.status === 0) return 'Não foi possível conectar ao servidor.';
    if (erro?.status === 400) return 'Algum dado informado é inválido. Confira o cadastro e tente novamente.';
    if (erro?.status === 404) return 'Um dos registros relacionados não foi encontrado.';
    if (erro?.status >= 500) return 'Ocorreu um erro no servidor ao realizar o cadastro.';
    return 'Não foi possível concluir o cadastro da escola.';
  }
}
