import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

import type { MunicipioResumo, PovoResumo, TerraIndigenaResumo, AldeiaResumo, EscolaResumo, EscolaResponse, AnoReferenciaResumo, EquipeEscola, InfraestruturaEscolaDetalhe, MatriculaNivel, MatriculaSerie, RegistroAnualDetalhe, EscolaDetalheResponse } from '../models/escola.models';
export type { MunicipioResumo, PovoResumo, TerraIndigenaResumo, AldeiaResumo, EscolaResumo, EscolaResponse, AnoReferenciaResumo, EquipeEscola, InfraestruturaEscolaDetalhe, MatriculaNivel, MatriculaSerie, RegistroAnualDetalhe, EscolaDetalheResponse } from '../models/escola.models';
import type { RegistroAnualResponse } from '../models/registro-anual.models';
import type { AlteracaoHistorico } from '@domains/auditoria/models/auditoria.models';

export interface CadastroEscolaRequest {
  escola: {
    id?: number;
    codigo: string;
    nome: string;
    tipoUnidade: string;
    escolaSedeId: number | null;
    aldeiaId: number;
  };
  anoReferenciaId: number;
  rede: string;
  localFuncionamento: string;
  totalAlunos: number | null;
  equipe: {
    temProfessorResponsavel: boolean;
    temEquipeGestora: boolean;
    temMerendeira: boolean;
    temZeladora: boolean;
    temBarqueiro: boolean;
    necessitaTransporte: boolean;
  };
  infraestrutura: {
    anoConstrucao: number | null;
    numSalas: number | null;
    latitude: number | null;
    longitude: number | null;
    situacao: string;
    tipoAcesso: string;
    temPredio: boolean;
    tipoAgua: string;
    temInternet: boolean;
    tipoEnergia: string;
    temRegistroFotografico: boolean;
  };
  matriculasNivel: { nivel: string; qtdMasculino: number | null; qtdFeminino: number | null; total: number; }[];
  matriculasSerie: { denominacao: string; etapaEnsino: string; qtdMasculino: number | null; qtdFeminino: number | null; total: number; }[];
  removerFoto: boolean;
}

export interface CadastroEscolaResponse {
  escola: EscolaResponse;
  registro: RegistroAnualResponse;
}

@Injectable({ providedIn: 'root' })
export class EscolaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/escola`;

  salvar(
    dados: {
      codigo: string;
      nome: string;
      tipoUnidade: string;
      escolaSedeId: number | null;
      aldeiaId: number;
    }
  ): Observable<EscolaResponse> {
    return this.http.post<EscolaResponse>(`${this.apiUrl}/salvar`, dados);
  }

  listar(): Observable<EscolaResponse[]> {
    return this.http.get<EscolaResponse[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<EscolaDetalheResponse> {
    return this.http.get<EscolaDetalheResponse>(`${this.apiUrl}/${id}`);
  }

  /** Trilha de auditoria (Envers) da escola e de seus registros anuais. Somente ADMIN. */
  buscarAuditoria(id: number): Observable<AlteracaoHistorico[]> {
    return this.http.get<AlteracaoHistorico[]>(`${this.apiUrl}/${id}/historico`);
  }

  editar(
    dados: {
      id?: number;
      codigo: string;
      nome: string;
      tipoUnidade: string;
      escolaSedeId: number | null;
      aldeiaId: number;
    }
    ): Observable<EscolaResponse> {
        return this.http.put<EscolaResponse>(`${this.apiUrl}/editar`,dados);
  }

  /**
   * Grava escola, registro anual, equipe, infraestrutura, matrículas e foto
   * em uma única transação no backend: se algo falhar, nada é gravado.
   */
  salvarCompleto(dados: CadastroEscolaRequest, foto: File | null): Observable<CadastroEscolaResponse> {
    return this.http.post<CadastroEscolaResponse>(`${this.apiUrl}/salvarCompleto`, this.montarCadastro(dados, foto));
  }

  editarCompleto(dados: CadastroEscolaRequest, foto: File | null): Observable<CadastroEscolaResponse> {
    return this.http.put<CadastroEscolaResponse>(`${this.apiUrl}/editarCompleto`, this.montarCadastro(dados, foto));
  }

  private montarCadastro(dados: CadastroEscolaRequest, foto: File | null): FormData {
    const formData = new FormData();
    formData.append('dados', new Blob([JSON.stringify(dados)], { type: 'application/json' }));
    if (foto) {
      formData.append('foto', foto);
    }
    return formData;
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/excluir/${id}`);
  }
}