import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AnexoClinicoApi { nome: string; conteudoBase64: string; tipoMime: string; }
export interface DocumentoClinicoApi {
  id: string; pacienteId: string; profissionalId: string; secao: string; titulo: string;
  dataSessao: string; tipoAtendimento: string; conteudo: string; atualizadoEm: string; anexos: AnexoClinicoApi[];
}
export type DocumentoClinicoRequestApi = Omit<DocumentoClinicoApi, 'id' | 'profissionalId' | 'atualizadoEm'>;

@Injectable({ providedIn: 'root' })
export class DocumentosApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/documentos-clinicos';
  listarPorPaciente(pacienteId: string): Observable<DocumentoClinicoApi[]> {
    return this.http.get<DocumentoClinicoApi[]>(`${this.api}/paciente/${pacienteId}`);
  }
  criar(dados: DocumentoClinicoRequestApi): Observable<DocumentoClinicoApi> {
    return this.http.post<DocumentoClinicoApi>(this.api, dados);
  }
  atualizar(id: string, dados: DocumentoClinicoRequestApi): Observable<DocumentoClinicoApi> {
    return this.http.put<DocumentoClinicoApi>(`${this.api}/${id}`, dados);
  }
  excluir(id: string): Observable<void> { return this.http.delete<void>(`${this.api}/${id}`); }
}
