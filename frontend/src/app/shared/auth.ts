import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, map, tap } from 'rxjs';

import { Perfil, CONFIG_PERFIS } from './perfil';

interface LoginRequest {
    email: string;
    senha: string;
}

interface LoginResponse {
    token: string;
    nome: string;
    email: string;
    perfil: string;
    categoriaProfissional?: string | null;
    entidadeId?: string | null;
}

interface UsuarioSessao {
    nome: string;
    email: string;
    perfil: Perfil;
    entidadeId?: string | null;
}

export type ResultadoLogin =
    | { ok: true; rota: string; primeiroAcesso: boolean }
    | { ok: false; motivo: 'perfil' | 'paciente' };

@Injectable({
    providedIn: 'root',
})
export class Auth {
    private readonly http = inject(HttpClient);
    private readonly eventoSessao = new Subject<boolean>();
    readonly sessaoAlterada$ = this.eventoSessao.asObservable();

    private readonly chaveToken = 'neurovita_token';
    private readonly chaveUsuario = 'neurovita_usuario';

    entrar(email: string, senha: string): Observable<ResultadoLogin> {
        const dados: LoginRequest = {
            email: email.trim(),
            senha,
        };

        return this.http.post<LoginResponse>('/api/auth/login', dados).pipe(
            map((resposta) => {
                const perfilBackend = this.normalizar(resposta.perfil);

                let perfilFrontend: Perfil | null = null;

                if (
                    perfilBackend === 'ADMINISTRADOR' ||
                    perfilBackend === 'ADMIN'
                ) {
                    perfilFrontend = 'admin';
                } else if (perfilBackend === 'SECRETARIA') {
                    perfilFrontend = 'secretaria';
                } else if (perfilBackend === 'PROFISSIONAL') {
                    const categoria = this.normalizar(
                        resposta.categoriaProfissional ?? '',
                    );

                    if (categoria === 'PSICOLOGO') {
                        perfilFrontend = 'psicologo';
                    } else if (categoria === 'PSIQUIATRA') {
                        perfilFrontend = 'psiquiatra';
                    }
                }

                if (perfilBackend === 'PACIENTE') {
                    return { ok: false, motivo: 'paciente' } as const;
                }

                if (!perfilFrontend) {
                    return { ok: false, motivo: 'perfil' } as const;
                }

                const usuario: UsuarioSessao = {
                    nome: resposta.nome,
                    email: resposta.email,
                    perfil: perfilFrontend,
                    entidadeId: resposta.entidadeId ?? undefined,
                };

                sessionStorage.setItem(this.chaveToken, resposta.token);
                sessionStorage.setItem(
                    this.chaveUsuario,
                    JSON.stringify(usuario),
                );
                this.eventoSessao.next(true);

                return {
                    ok: true,
                    rota: CONFIG_PERFIS[perfilFrontend].rota,
                    primeiroAcesso: this.ehPrimeiroAcesso(resposta.token),
                } as const;
            }),
        );
    }

    alterarSenhaPrimeiroAcesso(
        novaSenha: string,
        confirmarSenha: string,
    ): Observable<void> {
        return this.http.post<void>(
            '/api/auth/primeiro-acesso/alterar-senha',
            { novaSenha, confirmarSenha },
        );
    }

    alterarSenhaAutenticada(
        senhaAtual: string,
        novaSenha: string,
        confirmarSenha: string,
    ): Observable<void> {
        return this.http.post<void>('/api/auth/alterar-senha', {
            senhaAtual,
            novaSenha,
            confirmarSenha,
        }).pipe(tap(() => this.sair()));
    }

    atualizarPerfil(nome: string, email: string): Observable<void> {
        return this.http.put<LoginResponse>('/api/auth/perfil', { nome, email }).pipe(
            tap((resposta) => {
                const perfil = this.obterPerfil();
                if (!perfil) return;
                sessionStorage.setItem(this.chaveToken, resposta.token);
                sessionStorage.setItem(this.chaveUsuario, JSON.stringify({
                    nome: resposta.nome,
                    email: resposta.email,
                    entidadeId: resposta.entidadeId ?? undefined,
                    perfil,
                } satisfies UsuarioSessao));
                this.eventoSessao.next(true);
            }),
            map(() => void 0),
        );
    }

    obterToken(): string | null {
        return sessionStorage.getItem(this.chaveToken);
    }

    /** Destino da sessão restaurada, incluindo a troca de senha obrigatória. */
    rotaInicialAutenticada(): string | null {
        const token = this.obterToken();
        const perfil = this.obterPerfil();
        if (!token || !perfil) return null;
        return this.ehPrimeiroAcesso(token)
            ? '/primeiro-acesso'
            : CONFIG_PERFIS[perfil].rota;
    }

    obterPerfil(): Perfil | null {
        const dados = sessionStorage.getItem(this.chaveUsuario);

        if (!dados) {
            return null;
        }

        try {
            const usuario = JSON.parse(dados) as UsuarioSessao;

            if (
                usuario.perfil === 'admin' ||
                usuario.perfil === 'secretaria' ||
                usuario.perfil === 'psicologo' ||
                usuario.perfil === 'psiquiatra'
            ) {
                return usuario.perfil;
            }

            return null;
        } catch {
            return null;
        }
    }

    obterNome(): string | null {
        const dados = sessionStorage.getItem(this.chaveUsuario);

        if (!dados) {
            return null;
        }

        try {
            const usuario = JSON.parse(dados) as UsuarioSessao;
            return usuario.nome || null;
        } catch {
            return null;
        }
    }

    obterEmail(): string | null {
        const dados = sessionStorage.getItem(this.chaveUsuario);
        if (!dados) return null;
        try {
            const usuario = JSON.parse(dados) as UsuarioSessao;
            return usuario.email || null;
        } catch {
            return null;
        }
    }

    obterEntidadeId(): string | null {
        const dados = sessionStorage.getItem(this.chaveUsuario);

        if (!dados) return null;

        try {
            const usuario = JSON.parse(dados) as UsuarioSessao;
            return usuario.entidadeId || null;
        } catch {
            return null;
        }
    }

    sair(): void {
        sessionStorage.removeItem(this.chaveToken);
        sessionStorage.removeItem(this.chaveUsuario);
        this.eventoSessao.next(false);
    }

    private normalizar(valor: string): string {
        return valor
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .trim()
            .toUpperCase();
    }

    private ehPrimeiroAcesso(token: string): boolean {
        try {
            const payload = token.split('.')[1];
            const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
            const json = atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '='));

            return JSON.parse(json).primeiroAcesso === true;
        } catch {
            return false;
        }
    }
}
