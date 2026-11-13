import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PreferenciasUsuarioApi {
  lembretesConsultaEmail: boolean | null;
  alertasConsultasPendentes: boolean | null;
  notificacoesSistema: boolean | null;
  tema: 'CLARO' | 'ESCURO' | null;
  idioma: 'PORTUGUES' | 'INGLES' | null;
  formatoData: 'DD/MM/AAAA' | 'MM/DD/AAAA' | 'AAAA/MM/DD' | null;
  duracaoPadraoConsulta: number | null;
  inicioSemana: 'SEGUNDA-FEIRA' | 'DOMINGO' | null;
  atualizadaEm: string | null;
}

export type PreferenciasUsuarioAtualizacaoApi = Pick<PreferenciasUsuarioApi,
  | 'lembretesConsultaEmail'
  | 'alertasConsultasPendentes'
  | 'notificacoesSistema'
  | 'tema'
  | 'idioma'
  | 'formatoData'
  | 'duracaoPadraoConsulta'
  | 'inicioSemana'
>;

/** Campos omitidos ou null são preservados pelo backend; boolean false é uma atualização válida. */
export type PreferenciasUsuarioPatchApi = Partial<PreferenciasUsuarioAtualizacaoApi>;

@Injectable({ providedIn: 'root' })
export class PreferenciasApiService {
  private readonly http = inject(HttpClient);

  buscar(): Observable<PreferenciasUsuarioApi> {
    return this.http.get<PreferenciasUsuarioApi>('/api/preferencias');
  }

  salvar(dados: PreferenciasUsuarioPatchApi): Observable<PreferenciasUsuarioApi> {
    return this.http.put<PreferenciasUsuarioApi>('/api/preferencias', dados);
  }
}
