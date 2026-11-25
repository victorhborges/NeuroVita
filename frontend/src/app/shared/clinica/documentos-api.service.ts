import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AnexoClinicoApi {
    nome: string;
    conteudoBase64: string;
    tipoMime: string;
}

export interface DocumentoClinicoApi {
    id: string;
    pacienteId: string;
    profissionalId: string;
    secao: string;
    titulo: string;
    grupo?: string | null;
    dataSessao: string | null;
    tipoAtendimento: string;
    conteudo: string;
    criadoEm: string | null;
    atualizadoEm: string | null;
    anexos: AnexoClinicoApi[];
    ativo: boolean;
    arquivadoEm: string | null;
}

export interface DocumentoClinicoRequestApi {
    pacienteId: string;
    secao: string;
    titulo: string;
    grupo?: string | null;
    dataSessao: string;
    tipoAtendimento: string;
    conteudo: string;
    anexos?: AnexoClinicoApi[];
}

@Injectable({ providedIn: 'root' })
export class DocumentosApiService {
    private readonly http = inject(HttpClient);
    private readonly api = '/api/documentos-clinicos';

    listarPorPaciente(pacienteId: string): Observable<DocumentoClinicoApi[]> {
        return this.http.get<DocumentoClinicoApi[]>(
            `${this.api}/paciente/${encodeURIComponent(pacienteId)}`
        );
    }

    criar(dados: DocumentoClinicoRequestApi): Observable<DocumentoClinicoApi> {
        return this.http.post<DocumentoClinicoApi>(this.api, dados);
    }

    atualizar(
        id: string,
        dados: DocumentoClinicoRequestApi
    ): Observable<DocumentoClinicoApi> {
        return this.http.put<DocumentoClinicoApi>(
            `${this.api}/${encodeURIComponent(id)}`,
            dados
        );
    }

    excluir(id: string): Observable<void> {
        return this.http.delete<void>(
            `${this.api}/${encodeURIComponent(id)}`
        );
    }
}