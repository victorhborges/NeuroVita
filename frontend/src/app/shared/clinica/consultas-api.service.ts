import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StatusConsultaApi =
    | 'PENDENTE'
    | 'CONFIRMADA'
    | 'REMARCADA'
    | 'CANCELADA';

export interface ConsultaApi {
    id: string;
    pacienteId: string;
    profissionalId: string;
    dataHora: string;
    tipoAtendimento: string;
    local: string;
    modalidade: string;
    duracao: number;
    status: StatusConsultaApi;
}

export interface ConsultaRequestApi {
    pacienteId: string;
    profissionalId: string;
    dataHora: string;
    tipoAtendimento: string;
    local: string;
    modalidade: string;
    duracao: number;
}

@Injectable({
    providedIn: 'root',
})
export class ConsultasApiService {
    private readonly http = inject(HttpClient);
    private readonly api = '/api/consultas';

    listarTodas(): Observable<ConsultaApi[]> {
        return this.http.get<ConsultaApi[]>(this.api);
    }

    listarPorPaciente(pacienteId: string): Observable<ConsultaApi[]> {
        return this.http.get<ConsultaApi[]>(
            `${this.api}/paciente/${pacienteId}`,
        );
    }

    listarPorProfissional(profissionalId: string): Observable<ConsultaApi[]> {
        return this.http.get<ConsultaApi[]>(
            `${this.api}/profissional/${profissionalId}`,
        );
    }

    listarPorProfissionalNoPeriodo(
        profissionalId: string,
        dataInicio: string,
        dataFim: string,
    ): Observable<ConsultaApi[]> {
        return this.http.get<ConsultaApi[]>(
            `${this.api}/profissional/${profissionalId}`,
            { params: { dataInicio, dataFim } },
        );
    }

    listarPorPacienteNoPeriodo(
        pacienteId: string,
        dataInicio: string,
        dataFim: string,
    ): Observable<ConsultaApi[]> {
        return this.http.get<ConsultaApi[]>(
            `${this.api}/paciente/${pacienteId}`,
            { params: { dataInicio, dataFim } },
        );
    }

    buscarPorId(id: string): Observable<ConsultaApi> {
        return this.http.get<ConsultaApi>(`${this.api}/${id}`);
    }

    criar(dados: ConsultaRequestApi): Observable<ConsultaApi> {
        return this.http.post<ConsultaApi>(this.api, dados);
    }

    atualizar(
        id: string,
        dados: ConsultaRequestApi,
    ): Observable<ConsultaApi> {
        return this.http.put<ConsultaApi>(`${this.api}/${id}`, dados);
    }

    confirmar(id: string): Observable<ConsultaApi> {
        return this.http.patch<ConsultaApi>(
            `${this.api}/${id}/confirmar`,
            {},
        );
    }

    cancelar(id: string): Observable<ConsultaApi> {
        return this.http.patch<ConsultaApi>(
            `${this.api}/${id}/cancelar`,
            {},
        );
    }

    excluir(id: string): Observable<void> {
        return this.http.delete<void>(`${this.api}/${id}`);
    }
}
