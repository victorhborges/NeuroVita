import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MedicamentoPrescritoApi {
  medicamento: string;
  dosagem?: string | null;
  posologia?: string | null;
  frequencia?: string | null;
  duracao?: string | null;
}

export interface PrescricaoApi {
  id: string;
  pacienteId: string;
  profissionalId: string;
  conteudo: string;
  medicamentos: MedicamentoPrescritoApi[];
  criadaEm: string;
  atualizadaEm: string;
  historico: unknown[];
}

export interface PrescricaoRequestApi {
  pacienteId: string;
  profissionalId?: string;
  conteudo: string;
  medicamentos?: MedicamentoPrescritoApi[];
}

@Injectable({ providedIn: 'root' })
export class PrescricoesApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/prescricoes';

  listarPorPaciente(pacienteId: string): Observable<PrescricaoApi[]> {
    return this.http.get<PrescricaoApi[]>(`${this.api}/paciente/${encodeURIComponent(pacienteId)}`);
  }

  buscar(id: string): Observable<PrescricaoApi> {
    return this.http.get<PrescricaoApi>(`${this.api}/${encodeURIComponent(id)}`);
  }

  criar(dados: PrescricaoRequestApi): Observable<PrescricaoApi> {
    return this.http.post<PrescricaoApi>(this.api, dados);
  }

  atualizar(id: string, dados: PrescricaoRequestApi): Observable<PrescricaoApi> {
    return this.http.put<PrescricaoApi>(`${this.api}/${encodeURIComponent(id)}`, dados);
  }
}
