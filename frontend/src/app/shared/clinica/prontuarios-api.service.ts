import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProntuarioApi {
  id: string;
  pacienteId: string;
  profissionalId: string;
  consultaId?: string | null;
  queixaPrincipal?: string | null;
  avaliacao?: string | null;
  planoTratamento?: string | null;
  prescricao?: string | null;
  criadoEm?: string | null;
  atualizadoEm?: string | null;
}

export interface ProntuarioRequestApi {
  pacienteId: string;
  profissionalId: string;
  consultaId?: string;
  queixaPrincipal?: string;
  avaliacao?: string;
  planoTratamento?: string;
  prescricao?: string;
}

@Injectable({ providedIn: 'root' })
export class ProntuariosApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/prontuarios';

  buscarPorPaciente(pacienteId: string): Observable<ProntuarioApi> {
    return this.http.get<ProntuarioApi>(
      `${this.api}/paciente/${encodeURIComponent(pacienteId)}`,
    );
  }

  buscarPorId(id: string): Observable<ProntuarioApi> {
    return this.http.get<ProntuarioApi>(`${this.api}/${encodeURIComponent(id)}`);
  }

  criar(dados: ProntuarioRequestApi): Observable<ProntuarioApi> {
    return this.http.post<ProntuarioApi>(this.api, dados);
  }

  atualizar(id: string, dados: ProntuarioRequestApi): Observable<ProntuarioApi> {
    return this.http.put<ProntuarioApi>(`${this.api}/${encodeURIComponent(id)}`, dados);
  }
}
