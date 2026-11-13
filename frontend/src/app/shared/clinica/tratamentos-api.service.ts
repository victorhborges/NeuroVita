import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TratamentoApi {
  id: string;
  pacienteId: string;
  profissionalId: string;
  descricao: string;
  objetivo?: string | null;
  frequencia?: string | null;
  duracao?: string | null;
  observacoes?: string | null;
  ativo: boolean;
  arquivadoEm?: string | null;
}

export interface TratamentoRequestApi {
  pacienteId: string;
  profissionalId: string;
  descricao: string;
  objetivo?: string;
  frequencia?: string;
  duracao?: string;
  observacoes?: string;
}

@Injectable({ providedIn: 'root' })
export class TratamentosApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/tratamentos';

  listarTodos(): Observable<TratamentoApi[]> {
    return this.http.get<TratamentoApi[]>(this.api);
  }

  listarPorPaciente(pacienteId: string): Observable<TratamentoApi[]> {
    return this.http.get<TratamentoApi[]>(`${this.api}/paciente/${encodeURIComponent(pacienteId)}`);
  }

  buscar(id: string): Observable<TratamentoApi> {
    return this.http.get<TratamentoApi>(`${this.api}/${encodeURIComponent(id)}`);
  }

  criar(dados: TratamentoRequestApi): Observable<TratamentoApi> {
    return this.http.post<TratamentoApi>(this.api, dados);
  }

  atualizar(id: string, dados: TratamentoRequestApi): Observable<TratamentoApi> {
    return this.http.put<TratamentoApi>(`${this.api}/${encodeURIComponent(id)}`, dados);
  }

  arquivar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${encodeURIComponent(id)}`);
  }
}
