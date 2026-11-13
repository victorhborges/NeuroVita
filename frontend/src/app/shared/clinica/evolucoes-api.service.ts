import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EvolucaoApi {
  id: string;
  pacienteId: string;
  profissionalId: string;
  consultaId?: string | null;
  prontuarioId?: string | null;
  tipoEvolucao: string;
  areaAtendimento?: string | null;
  titulo: string;
  dataAtendimento: string;
  conteudo: string;
  objetivosTrabalhados: string[];
  observacoes?: string | null;
  conduta?: string | null;
  encaminhamentos: string[];
  status: string;
  criadoEm: string;
  atualizadoEm: string;
  versao: number;
}

export interface EvolucaoRequestApi {
  pacienteId: string;
  profissionalId?: string;
  consultaId?: string;
  prontuarioId?: string;
  tipoEvolucao: string;
  areaAtendimento?: string;
  titulo: string;
  dataAtendimento: string;
  conteudo: string;
  objetivosTrabalhados?: string[];
  observacoes?: string;
  conduta?: string;
  encaminhamentos?: string[];
}

@Injectable({ providedIn: 'root' })
export class EvolucoesApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/evolucoes';

  listarPorPaciente(pacienteId: string): Observable<EvolucaoApi[]> {
    return this.http.get<EvolucaoApi[]>(`${this.api}/paciente/${encodeURIComponent(pacienteId)}`);
  }

  criar(dados: EvolucaoRequestApi): Observable<EvolucaoApi> {
    return this.http.post<EvolucaoApi>(this.api, dados);
  }
}
