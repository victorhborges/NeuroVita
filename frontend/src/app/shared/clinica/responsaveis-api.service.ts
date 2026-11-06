import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ResponsavelApi {
    id: string;
    nome: string;
    cpf: string;
    email: string;
    telefone: string;
    dataNascimento?: string;
    endereco?: string;
}

export type ResponsavelRequestApi = Omit<ResponsavelApi, 'id'>;

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
}
