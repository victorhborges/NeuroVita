import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PacienteApi {
    id: string;
    nome: string;
    dataNascimento: string;
    email: string;
    cpf: string;
    telefone: string;
    endereco?: string;
    ativo?: boolean;
    responsaveis: {
        responsavelId: string;
        grauParentesco: string;
    }[];
    profissionais?: {
        profissionalId: string;
        ativo: boolean;
    }[];
    possuiPlanoSaude: boolean;
    planoSaude: string;
    numeroCarteirinha: string;
    validadePlano: string | null;
}

export interface PacienteRequestApi extends Omit<PacienteApi, 'id'> {}

@Injectable({
    providedIn: 'root',
})
export class PacientesApiService {
    private readonly http = inject(HttpClient);
    private readonly api = '/api/pacientes';

    listarTodos(): Observable<PacienteApi[]> {
        return this.http.get<PacienteApi[]>(this.api);
    }

    buscarPorId(id: string): Observable<PacienteApi> {
        return this.http.get<PacienteApi>(`${this.api}/${id}`);
    }

    criar(dados: PacienteRequestApi): Observable<{ paciente: PacienteApi; senhaProvisoria: string }> {
        return this.http.post<{ paciente: PacienteApi; senhaProvisoria: string }>(this.api, dados);
    }

    atualizar(id: string, dados: PacienteRequestApi): Observable<PacienteApi> {
        return this.http.put<PacienteApi>(`${this.api}/${id}`, dados);
    }

    excluir(id: string): Observable<void> {
        return this.http.delete<void>(`${this.api}/${id}`);
    }
}
