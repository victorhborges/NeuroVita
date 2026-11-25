import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProfissionalApi {
    id: string;
    nome: string;
    email: string;
    emailContato: string | null;
    dataNascimento: string | null;
    cpf: string | null;
    telefone: string | null;
    cep: string | null;
    cidade: string | null;
    endereco: string | null;
    numero: string | null;
    complemento: string | null;
    bairro: string | null;
    admissao: string | null;
    encerramento: string | null;
    categoria: string;
    formacaoAcademica: string | null;
    numeroRegistro: string | null;
    anoConclusao: number | null;
    atendeOnline: boolean | null;
    atendePresencial: boolean;
    ativo: boolean | null;
}

export interface ProfissionalRequestApi extends Omit<ProfissionalApi, 'id'> {
    senhaProvisoria?: string;
    enviarEmailBoasVindas?: boolean;
}

export interface DisponibilidadeApi {
    id: string;
    diaSemana: string;
    horaInicio: string;
    horaFim: string;
    ativo: boolean | null;
}

export interface DisponibilidadeRequestApi {
    diaSemana: string;
    horaInicio: string;
    horaFim: string;
    ativo: boolean;
}

@Injectable({
    providedIn: 'root',
})
export class ProfissionaisApiService {
    private readonly http = inject(HttpClient);
    private readonly api = '/api/profissionais';

    listarTodos(): Observable<ProfissionalApi[]> {
        return this.http.get<ProfissionalApi[]>(this.api);
    }

    buscarPorId(id: string): Observable<ProfissionalApi> {
        return this.http.get<ProfissionalApi>(
            `${this.api}/${encodeURIComponent(id)}`
        );
    }

    listarDisponibilidades(
        profissionalId: string
    ): Observable<DisponibilidadeApi[]> {
        return this.http.get<DisponibilidadeApi[]>(
            `${this.api}/${encodeURIComponent(profissionalId)}/disponibilidades`
        );
    }

    criar(dados: ProfissionalRequestApi): Observable<{
        profissional: ProfissionalApi;
        senhaProvisoria: string | null;
        emailBoasVindasEnviado: boolean;
    }> {
        return this.http.post<{
            profissional: ProfissionalApi;
            senhaProvisoria: string | null;
            emailBoasVindasEnviado: boolean;
        }>(this.api, dados);
    }

    atualizar(
        id: string,
        dados: ProfissionalRequestApi
    ): Observable<ProfissionalApi> {
        return this.http.put<ProfissionalApi>(
            `${this.api}/${encodeURIComponent(id)}`,
            dados
        );
    }

    excluir(id: string): Observable<void> {
        return this.http.delete<void>(
            `${this.api}/${encodeURIComponent(id)}`
        );
    }

    adicionarDisponibilidade(
        profissionalId: string,
        disponibilidadeId: string
    ): Observable<ProfissionalApi> {
        return this.http.post<ProfissionalApi>(
            `${this.api}/${encodeURIComponent(profissionalId)}/disponibilidades/${encodeURIComponent(disponibilidadeId)}`,
            {}
        );
    }

    removerDisponibilidade(
        profissionalId: string,
        disponibilidadeId: string
    ): Observable<void> {
        return this.http.delete<void>(
            `${this.api}/${encodeURIComponent(profissionalId)}/disponibilidades/${encodeURIComponent(disponibilidadeId)}`
        );
    }
}