import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AdministradorApi {
  id: string;
  nome: string;
  email: string;
  perfil: string;
  cpf?: string | null;
  telefone?: string | null;
  criadoEm?: string | null;
}

export interface AdministradorRequestApi {
  nome: string;
  email: string;
  /** Obrigatória ao criar; opcional ao atualizar para preservar o hash existente. */
  senha?: string;
  perfil?: string;
  cpf?: string | null;
  telefone?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AdministradoresApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/administradores';

  listarTodos(): Observable<AdministradorApi[]> {
    return this.http.get<AdministradorApi[]>(this.api);
  }

  buscarPorId(id: string): Observable<AdministradorApi> {
    return this.http.get<AdministradorApi>(`${this.api}/${encodeURIComponent(id)}`);
  }

  criar(dados: AdministradorRequestApi & { senha: string }): Observable<AdministradorApi> {
    return this.http.post<AdministradorApi>(this.api, dados);
  }

  atualizar(id: string, dados: AdministradorRequestApi): Observable<AdministradorApi> {
    return this.http.put<AdministradorApi>(`${this.api}/${encodeURIComponent(id)}`, dados);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${encodeURIComponent(id)}`);
  }
}
