import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DiagnosticoApi {
  id: string;
  pacienteId: string;
  profissionalId: string;
  descricao: string;
  cid?: string | null;
  ativo?: boolean;
}

@Injectable({ providedIn: 'root' })
export class DiagnosticosApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/diagnosticos';
  listarPorPaciente(pacienteId: string): Observable<DiagnosticoApi[]> {
    return this.http.get<DiagnosticoApi[]>(`${this.api}/paciente/${pacienteId}`);
  }
}
