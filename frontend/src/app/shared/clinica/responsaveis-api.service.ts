import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ResponsavelApi {
    id: string;
    nome: string;
    cpf: string | null;
    email: string | null;
    telefone: string | null;
    dataNascimento?: string | null;
    endereco?: string | null;
}

export interface ResponsavelRequestApi {
    nome: string;
    cpf?: string | null;
    email?: string | null;
    telefone?: string | null;
    dataNascimento?: string | null;
    endereco?: string | null;
}

@Injectable({
    providedIn: 'root',
})
export class ResponsaveisApiService {
    private readonly http = inject(HttpClient);
    private readonly api = '/api/responsaveis';

    listarTodos(): Observable<ResponsavelApi[]> {
        return this.http.get<ResponsavelApi[]>(this.api);
    }

    buscarPorId(id: string): Observable<ResponsavelApi> {
        return this.http.get<ResponsavelApi>(`${this.api}/${id}`);
    }

    criar(dados: ResponsavelRequestApi): Observable<ResponsavelApi> {
        return this.http.post<ResponsavelApi>(this.api, dados);
    }

    atualizar(id: string, dados: ResponsavelRequestApi): Observable<ResponsavelApi> {
        return this.http.put<ResponsavelApi>(`${this.api}/${id}`, dados);
    }

    excluir(id: string): Observable<void> {
        return this.http.delete<void>(`${this.api}/${id}`);
    }
}