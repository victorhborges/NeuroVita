import { Injectable, inject, signal } from '@angular/core';
import {
    EMPTY,
    Observable,
    catchError,
    finalize,
    mergeMap,
    map,
    of,
    shareReplay,
    throwError,
    timeout,
} from 'rxjs';

import { Auth } from '../auth';
import {
    PreferenciasApiService,
    PreferenciasUsuarioApi,
    PreferenciasUsuarioPatchApi,
} from './preferencias-api.service';

const PADRAO: PreferenciasUsuarioApi = {
    lembretesConsultaEmail: false,
    alertasConsultasPendentes: false,
    notificacoesSistema: false,
    tema: 'CLARO',
    idioma: 'PORTUGUES',
    formatoData: 'DD/MM/AAAA',
    duracaoPadraoConsulta: 50,
    inicioSemana: 'SEGUNDA-FEIRA',
    atualizadaEm: null,
};

const TEMPO_MAXIMO_CARGA_MS = 4_000;

interface IdentidadeSessao {
    readonly geracao: number;
    readonly token: string;
}

interface OperacaoCarga {
    readonly identidade: IdentidadeSessao;
    readonly fluxo: Observable<PreferenciasUsuarioApi>;
}

/** Estado global de preferências, isolado por geração de sessão autenticada. */
@Injectable({ providedIn: 'root' })
export class PreferenciasGlobaisService {
    private readonly api = inject(PreferenciasApiService);
    private readonly auth = inject(Auth);

    readonly preferencias = signal<PreferenciasUsuarioApi>(PADRAO);
    readonly erroCarregamento = signal(false);

    private geracaoSessao = 0;
    private identidade: IdentidadeSessao | null = null;
    private carregadaNaGeracao: number | null = null;
    private tentadaNaGeracao: number | null = null;
    private operacaoCarga?: OperacaoCarga;
    private geracaoSalvamento = 0;

    constructor() {
        this.auth.sessaoAlterada$.subscribe((autenticada) => {
            if (autenticada) {
                const token = this.auth.obterToken();

                if (token) {
                    this.iniciarSessao(token);
                } else {
                    this.limparSessao();
                }
            } else {
                this.limparSessao();
            }
        });
    }

    inicializar(): Observable<void> {
        return this.carregar().pipe(map(() => void 0));
    }

    carregar(forcar = false): Observable<PreferenciasUsuarioApi> {
        const identidade = this.obterIdentidadeAtual();

        if (!identidade) {
            return of(PADRAO);
        }

        if (this.operacaoCarga?.identidade === identidade) {
            return this.operacaoCarga.fluxo;
        }

        if (
            !forcar &&
            this.carregadaNaGeracao === identidade.geracao
        ) {
            return of(this.preferencias());
        }

        if (
            !forcar &&
            this.tentadaNaGeracao === identidade.geracao
        ) {
            return of(this.preferencias());
        }

        this.erroCarregamento.set(false);
        this.tentadaNaGeracao = identidade.geracao;

        let operacao!: OperacaoCarga;

        const fluxo = this.api.buscar().pipe(
            timeout({ first: TEMPO_MAXIMO_CARGA_MS }),

            mergeMap((resposta) => {
                if (!this.ehSessaoAtual(identidade)) {
                    return EMPTY;
                }

                this.carregadaNaGeracao = identidade.geracao;
                this.aplicar(resposta);

                return of(resposta);
            }),

            catchError(() => {
                if (!this.ehSessaoAtual(identidade)) {
                    return EMPTY;
                }

                this.erroCarregamento.set(true);
                this.aplicar(PADRAO);

                return of(PADRAO);
            }),

            finalize(() => {
                if (this.operacaoCarga === operacao) {
                    this.operacaoCarga = undefined;
                }
            }),

            shareReplay({ bufferSize: 1, refCount: true }),
        );

        operacao = {
            identidade,
            fluxo,
        };

        this.operacaoCarga = operacao;

        return fluxo;
    }

    salvar(
        patch: PreferenciasUsuarioPatchApi,
    ): Observable<PreferenciasUsuarioApi> {
        const identidade = this.obterIdentidadeAtual();

        if (!identidade) {
            return throwError(
                () =>
                    new Error(
                        'É necessário estar autenticado para salvar preferências.',
                    ),
            );
        }

        const geracaoSalvamento = ++this.geracaoSalvamento;

        return this.api.salvar(patch).pipe(
            timeout({ first: 10_000 }),

            mergeMap((resposta) => {
                if (
                    !this.ehSessaoAtual(identidade) ||
                    geracaoSalvamento !== this.geracaoSalvamento
                ) {
                    return EMPTY;
                }

                this.carregadaNaGeracao = identidade.geracao;
                this.erroCarregamento.set(false);
                this.aplicar(resposta);

                return of(resposta);
            }),

            catchError((erro: unknown) => {
                if (
                    !this.ehSessaoAtual(identidade) ||
                    geracaoSalvamento !== this.geracaoSalvamento
                ) {
                    return EMPTY;
                }

                return throwError(() => erro);
            }),
        );
    }

    aplicarRascunho(patch: PreferenciasUsuarioPatchApi): void {
        this.aplicar({
            ...this.preferencias(),
            ...patch,
        });
    }

    usarPadrao(): void {
        this.erroCarregamento.set(false);
        this.aplicar(PADRAO);
    }

    private obterIdentidadeAtual(): IdentidadeSessao | null {
        const token = this.auth.obterToken();

        if (!token) {
            if (this.identidade) {
                this.limparSessao();
            }

            return null;
        }

        if (!this.identidade || this.identidade.token !== token) {
            return this.iniciarSessao(token);
        }

        return this.identidade;
    }

    private iniciarSessao(token: string): IdentidadeSessao {
        const identidade: IdentidadeSessao = {
            geracao: ++this.geracaoSessao,
            token,
        };

        this.identidade = identidade;
        this.carregadaNaGeracao = null;
        this.tentadaNaGeracao = null;
        this.operacaoCarga = undefined;
        this.geracaoSalvamento++;

        this.erroCarregamento.set(false);
        this.aplicar(PADRAO);

        return identidade;
    }

    private limparSessao(): void {
        this.geracaoSessao++;
        this.identidade = null;
        this.carregadaNaGeracao = null;
        this.tentadaNaGeracao = null;
        this.operacaoCarga = undefined;
        this.geracaoSalvamento++;

        this.erroCarregamento.set(false);
        this.aplicar(PADRAO);
    }

    private ehSessaoAtual(
        identidade: IdentidadeSessao,
    ): boolean {
        return (
            this.identidade === identidade &&
            this.auth.obterToken() === identidade.token
        );
    }

    private aplicar(
        preferencias: PreferenciasUsuarioApi,
    ): void {
        const normalizadas = {
            ...PADRAO,
            ...preferencias,
        };

        this.preferencias.set(normalizadas);

        if (typeof document === 'undefined') {
            return;
        }

        const raiz = document.documentElement;

        raiz.dataset['tema'] =
            normalizadas.tema === 'ESCURO' ? 'escuro' : 'claro';

        // A interface ainda não tem catálogo integral em inglês.
        raiz.dataset['idioma'] = 'pt';
        raiz.dataset['formatoData'] =
            normalizadas.formatoData ?? 'DD/MM/AAAA';

        raiz.lang = 'pt-BR';
    }
}