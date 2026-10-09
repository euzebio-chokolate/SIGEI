import { ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, filter, Observable, of, switchMap } from 'rxjs';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { CadastroEscolaRequest, EscolaResponse, EscolaService } from '@domains/escolas/data-access/escola.service';
import { AldeiaResponse, AldeiaService } from '@domains/territorios/data-access/aldeia.service';
import { AnoReferencia, AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { RegistroAnualResponse, RegistroAnualService } from '@domains/escolas/data-access/registro-anual.service';
import { RegistroAnualDetalhe } from '@domains/escolas/models/escola.models';

import { EscolaEtapaComponent } from '@features/admin/escolas/ui/form-steps/escola-etapa.component';
import { RegistroAnualEtapaComponent, StatusAnoRegistro } from '@features/admin/escolas/ui/form-steps/registro-anual-etapa.component';
import { EquipeEtapaComponent } from '@features/admin/escolas/ui/form-steps/equipe-etapa.component';
import { InfraestruturaEtapaComponent } from '@features/admin/escolas/ui/form-steps/infraestrutura-etapa.component';
import { MatriculasNivelEtapaComponent } from '@features/admin/escolas/ui/form-steps/matriculas-nivel-etapa.component';
import { MatriculasSerieEtapaComponent } from '@features/admin/escolas/ui/form-steps/matriculas-serie-etapa.component';
import { RevisaoEtapaComponent } from '@features/admin/escolas/ui/form-steps/revisao-etapa.component';
import { NIVEIS_MATRICULA, SERIES_MATRICULA } from './escola-form.config';

import { interpretarErro, mensagemErroCarregamento } from '@core/http/erro-http';
import { aplicarErrosServidor } from '@core/http/erros-formulario';
import { ToastService } from '@core/notificacao/toast.service';
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
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toast = inject(ToastService);

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
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar os dados da escola.', 'Escola não encontrada.');
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

  /** Limpa os dados do ano, mantendo escola, ano, rede e local selecionados. */
  private limparDadosAnuais(): void {
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

    this.registroForm.patchValue({
      anoReferenciaId: registro.anoReferencia?.id ?? 0,
      rede: registro.rede ?? '',
      localFuncionamento: registro.localFuncionamento ?? '',
      totalAlunos: registro.totalAlunos ?? null
    }, { emitEvent: false });

    if (registro.equipe) {
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

    for (const grupo of this.matriculasNivel.controls) {
      const nivel = grupo.controls.nivel.value;
      const encontrada = registro.matriculasNivel?.find(item => item.nivel === nivel);

      if (encontrada) {
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
        const falha = interpretarErro(erro, 'Não foi possível adicionar o ano.');
        this.erroNovoAno = falha.status === 409
          ? 'Este ano já está cadastrado. Atualize a lista e selecione-o.'
          : falha.mensagem;
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
        this.erroMensagem = mensagemErroCarregamento(erro, 'Não foi possível carregar as aldeias.');
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
        this.erroAnos = mensagemErroCarregamento(erro, 'Não foi possível carregar os anos de referência.');
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
        if (!interpretarErro(erro).global) {
          this.toast.aviso('Não foi possível carregar a lista de escolas sede.');
        }
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

    const dados: CadastroEscolaRequest = {
      escola: {
        ...(this.editando && this.escolaId ? { id: this.escolaId } : {}),
        codigo: this.form.controls.codigo.value,
        nome: this.form.controls.nome.value,
        tipoUnidade: this.form.controls.tipoUnidade.value,
        escolaSedeId: this.form.controls.escolaSedeId.value,
        aldeiaId: this.form.controls.aldeiaId.value
      },
      anoReferenciaId: this.registroForm.controls.anoReferenciaId.value,
      rede: this.registroForm.controls.rede.value,
      localFuncionamento: this.registroForm.controls.localFuncionamento.value,
      totalAlunos: this.obterTotalAlunosRegistro(),
      equipe: this.equipeForm.getRawValue(),
      infraestrutura: this.infraestruturaForm.getRawValue(),
      matriculasNivel: this.montarMatriculasNivel(),
      matriculasSerie: this.montarMatriculasSerie(),
      removerFoto: !this.fotoEscola && this.fotoAtualRemovida
    };

    // Uma única requisição: o backend grava todo o cadastro em uma transação
    const cadastro$ = this.editando
      ? this.escolaService.editarCompleto(dados, this.fotoEscola)
      : this.escolaService.salvarCompleto(dados, this.fotoEscola);

    cadastro$.subscribe({
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
        this.tratarErroCadastro(erro);
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

  /**
   * Distribui os erros do cadastro completo entre as etapas do formulário e
   * volta para a primeira etapa com problema. Os campos chegam com o caminho do
   * CadastroEscolaRequestDTO (ex.: "escola.nome", "matriculasNivel[2].qtdFeminino").
   */
  private tratarErroCadastro(erro: unknown): void {
    const falha = interpretarErro(erro, 'Não foi possível concluir o cadastro da escola.');
    const destinos = [
      { etapa: 1, form: this.form as FormGroup, campos: {} as Record<string, string[]>, regras: [
        { campo: 'tipoUnidade', padrao: /tipo de unidade|SEDE não pode ter/i },
        { campo: 'escolaSedeId', padrao: /sede/i }
      ] },
      { etapa: 2, form: this.registroForm as FormGroup, campos: {}, regras: [
        { campo: 'anoReferenciaId', padrao: /registro anual para essa escola neste ano/i }
      ] },
      { etapa: 3, form: this.equipeForm as FormGroup, campos: {}, regras: [] },
      { etapa: 4, form: this.infraestruturaForm as FormGroup, campos: {}, regras: [] },
      { etapa: 5, form: this.matriculasNivelForm as FormGroup, campos: {}, regras: [] },
      { etapa: 6, form: this.matriculasSerieForm as FormGroup, campos: {}, regras: [] }
    ];

    for (const [caminho, mensagens] of Object.entries(falha.campos)) {
      const nivel = /^matriculasNivel\[(\d+)\]\.(\w+)$/.exec(caminho);
      const serie = /^matriculasSerie\[(\d+)\]\.(\w+)$/.exec(caminho);
      const [, grupo, resto] = /^(escola|equipe|infraestrutura)\.(.+)$/.exec(caminho) ?? [];

      if (nivel) {
        // A primeira linha enviada é o TOTAL_GERAL, que não existe no formulário
        destinos[4].campos[`matriculas.${Number(nivel[1]) - 1}.${nivel[2]}`] = mensagens;
      } else if (serie) {
        destinos[5].campos[`matriculas.${serie[1]}.${serie[2]}`] = mensagens;
      } else if (grupo) {
        destinos[{ escola: 0, equipe: 2, infraestrutura: 3 }[grupo]!].campos[resto] = mensagens;
      } else {
        destinos[1].campos[caminho] = mensagens;
      }
    }

    const comErro = destinos.filter(({ form, campos, regras }) =>
      aplicarErrosServidor(form, { ...falha, campos }, regras)
    );

    if (comErro.length) {
      this.etapaAtual = Math.min(...comErro.map(destino => destino.etapa));
      this.erroMensagem = 'Corrija os campos destacados e tente novamente.';
      return;
    }

    this.erroMensagem = falha.global ? '' : falha.mensagem;
  }
}
