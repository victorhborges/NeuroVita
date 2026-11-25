import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SecretariaApi {
    id: string;
    nome: string;
    email: string;
    telefone: string | null;
    emailContato?: string | null;
    dataNascimento?: string | null;
    cpf?: string | null;
    endereco?: string | null;
    cep?: string | null;
    cidade?: string | null;
    numero?: string | null;
    complemento?: string | null;
    bairro?: string | null;
    cargo?: string | null;
    admissao?: string | null;
    encerramento?: string | null;
    cbo?: string | null;
    cargaHoraria?: string | null;
    ativo?: boolean | null;
}

export interface SecretariaRequestApi extends Omit<SecretariaApi, 'id'> {
    senhaProvisoria?: string;
    enviarEmailBoasVindas?: boolean;
}

@Injectable({ providedIn: 'root' })
export class SecretariasApiService {
    private readonly http = inject(HttpClient);
    private readonly api = '/api/secretarias';

    listarTodos(): Observable<SecretariaApi[]> {
        return this.http.get<SecretariaApi[]>(this.api);
    }

    buscarPorId(id: string): Observable<SecretariaApi> {
        return this.http.get<SecretariaApi>(`${this.api}/${id}`);
    }

    criar(dados: SecretariaRequestApi): Observable<{
        secretaria: SecretariaApi;
        senhaProvisoria: string | null;
        emailBoasVindasEnviado: boolean;
    }> {
        return this.http.post<{
            secretaria: SecretariaApi;
            senhaProvisoria: string | null;
            emailBoasVindasEnviado: boolean;
        }>(this.api, dados);
    }

    atualizar(id: string, dados: SecretariaRequestApi): Observable<SecretariaApi> {
        return this.http.put<SecretariaApi>(`${this.api}/${id}`, dados);
    }

    excluir(id: string): Observable<void> {
        return this.http.delete<void>(`${this.api}/${id}`);
    }
}