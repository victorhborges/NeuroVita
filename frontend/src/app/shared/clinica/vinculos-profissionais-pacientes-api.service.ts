import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface VinculoProfissionalPacienteApi {
  id: string;
  pacienteId: string;
  profissionalId: string;
  iniciadoEm: string;
  iniciadoPor: string;
  encerradoEm?: string | null;
  encerradoPor?: string | null;
  ativo: boolean;
}

export interface AbrirVinculoRequestApi {
  pacienteId: string;
  profissionalId: string;
}

@Injectable({ providedIn: 'root' })
export class VinculosProfissionaisPacientesApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/vinculos-profissionais-pacientes';

  listarPorPaciente(pacienteId: string): Observable<VinculoProfissionalPacienteApi[]> {
    return this.http.get<VinculoProfissionalPacienteApi[]>(
      `${this.api}/paciente/${encodeURIComponent(pacienteId)}`,
    );
  }

  abrir(dados: AbrirVinculoRequestApi): Observable<VinculoProfissionalPacienteApi> {
    return this.http.post<VinculoProfissionalPacienteApi>(this.api, dados);
  }

  encerrar(id: string): Observable<VinculoProfissionalPacienteApi> {
    return this.http.patch<VinculoProfissionalPacienteApi>(
      `${this.api}/${encodeURIComponent(id)}/encerrar`,
      {},
    );
  }
}
