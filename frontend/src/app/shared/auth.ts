import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

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
}

interface UsuarioSessao {
  nome: string;
  email: string;
  perfil: Perfil;
}

export type ResultadoLogin =
  | { ok: true; rota: string }
  | { ok: false; motivo: 'perfil' | 'paciente' };

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly http = inject(HttpClient);

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
        };

        sessionStorage.setItem(this.chaveToken, resposta.token);
        sessionStorage.setItem(
          this.chaveUsuario,
          JSON.stringify(usuario),
        );

        return {
          ok: true,
          rota: CONFIG_PERFIS[perfilFrontend].rota,
        } as const;
      }),
    );
  }

  obterToken(): string | null {
    return sessionStorage.getItem(this.chaveToken);
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

  sair(): void {
    sessionStorage.removeItem(this.chaveToken);
    sessionStorage.removeItem(this.chaveUsuario);
  }

  private normalizar(valor: string): string {
    return valor
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toUpperCase();
  }
}