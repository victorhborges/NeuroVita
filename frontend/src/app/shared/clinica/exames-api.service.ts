import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AnexoExameApi { nome: string; conteudoBase64: string; tipoMime: string; }
export interface ExameApi {
  id: string;
  pacienteId: string;
  profissionalId: string;
  nome: string;
  tipo?: string | null;
  descricaoSolicitacao?: string | null;
  dataRealizacao?: string | null;
  resultado?: string | null;
  observacoes?: string | null;
  solicitadoEm: string;
  criadoEm: string;
  resultadoAtualizadoEm?: string | null;
  anexosResultado: AnexoExameApi[];
  historicoResultados: unknown[];
  ativo: boolean;
  arquivadoEm?: string | null;
}

export interface ExameRequestApi {
  pacienteId: string;
  profissionalId: string;
  nome: string;
  tipo?: string;
  descricaoSolicitacao?: string;
}

export interface ExameResultadoRequestApi {
  dataRealizacao: string;
  resultado?: string;
  observacoes?: string;
  anexos?: AnexoExameApi[];
}

@Injectable({ providedIn: 'root' })
export class ExamesApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/exames';

  listarPorPaciente(pacienteId: string): Observable<ExameApi[]> {
    return this.http.get<ExameApi[]>(`${this.api}/paciente/${encodeURIComponent(pacienteId)}`);
  }

  buscar(id: string): Observable<ExameApi> {
    return this.http.get<ExameApi>(`${this.api}/${encodeURIComponent(id)}`);
  }

  criar(dados: ExameRequestApi): Observable<ExameApi> {
    return this.http.post<ExameApi>(this.api, dados);
  }

  atualizar(id: string, dados: ExameRequestApi): Observable<ExameApi> {
    return this.http.put<ExameApi>(`${this.api}/${encodeURIComponent(id)}`, dados);
  }

  registrarResultado(id: string, dados: ExameResultadoRequestApi): Observable<ExameApi> {
    return this.http.put<ExameApi>(`${this.api}/${encodeURIComponent(id)}/resultado`, dados);
  }

  arquivar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${encodeURIComponent(id)}`);
  }
}
