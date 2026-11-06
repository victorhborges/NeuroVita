import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProfissionalApi {
    id: string;
    nome: string;
    email: string;
    emailContato?: string;
    dataNascimento?: string;
    cpf: string;
    telefone: string;
    categoria: string;
    formacaoAcademica: string;
    numeroRegistro: string;
    anoConclusao: number | null;
    atendeOnline: boolean;
    ativo: boolean;
}

export interface ProfissionalRequestApi extends Omit<ProfissionalApi, 'id'> { senhaProvisoria?: string; }

export interface DisponibilidadeApi {
    id: string;
    diaSemana: string;
    horaInicio: string;
    horaFim: string;
    ativo: boolean;
}

export interface DisponibilidadeRequestApi { diaSemana: string; horaInicio: string; horaFim: string; ativo: boolean; }

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
        return this.http.get<ProfissionalApi>(`${this.api}/${id}`);
    }

    listarDisponibilidades(
        profissionalId: string
    ): Observable<DisponibilidadeApi[]> {
        return this.http.get<DisponibilidadeApi[]>(
            `${this.api}/${profissionalId}/disponibilidades`
        );
    }

    criar(dados: ProfissionalRequestApi): Observable<{ profissional: ProfissionalApi; senhaProvisoria: string }> {
        return this.http.post<{ profissional: ProfissionalApi; senhaProvisoria: string }>(this.api, dados);
    }

    atualizar(id: string, dados: ProfissionalRequestApi): Observable<ProfissionalApi> {
        return this.http.put<ProfissionalApi>(`${this.api}/${id}`, dados);
    }

    excluir(id: string): Observable<void> {
        return this.http.delete<void>(`${this.api}/${id}`);
    }

    adicionarDisponibilidade(profissionalId: string, disponibilidadeId: string): Observable<ProfissionalApi> {
        return this.http.post<ProfissionalApi>(`${this.api}/${profissionalId}/disponibilidades/${disponibilidadeId}`, {});
    }
    removerDisponibilidade(profissionalId: string, disponibilidadeId: string): Observable<void> {
        return this.http.delete<void>(`${this.api}/${profissionalId}/disponibilidades/${disponibilidadeId}`);
    }
}
