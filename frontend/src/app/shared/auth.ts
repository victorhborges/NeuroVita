import { Injectable } from '@angular/core';

import { CONFIG_PERFIS, Perfil } from './perfil';

/**
 * LOGIN DE TESTE (sem back-end).
 * TODO (back-end): apagar as contas abaixo e chamar a API de login. O servidor deve
 * devolver o perfil do usuário (psicologo | psiquiatra | secretaria | admin) e o
 * redirecionamento continua o mesmo: CONFIG_PERFIS[perfil].rota.
 */
export const SENHA_DE_TESTE = '123456';

export interface ContaDeTeste {
  perfil: Perfil;
  rotulo: string;
  email: string;
}

export const CONTAS_DE_TESTE: ContaDeTeste[] = [
  { perfil: 'psicologo', rotulo: 'Psicólogo', email: 'psicologo@neurovita.com' },
  { perfil: 'psiquiatra', rotulo: 'Psiquiatra', email: 'psiquiatra@neurovita.com' },
  { perfil: 'secretaria', rotulo: 'Secretária', email: 'secretaria@neurovita.com' },
  { perfil: 'admin', rotulo: 'Administrador', email: 'admin@neurovita.com' },
];

export type ResultadoLogin =
  | { ok: true; rota: string }
  | { ok: false; motivo: 'vazio' | 'email' | 'senha' };

@Injectable({ providedIn: 'root' })
export class Auth {
  entrar(email: string, senha: string): ResultadoLogin {
    const emailLimpo = email.trim().toLowerCase();
    if (!emailLimpo || !senha) {
      return { ok: false, motivo: 'vazio' };
    }

    const conta = CONTAS_DE_TESTE.find((c) => c.email === emailLimpo);
    if (!conta) {
      return { ok: false, motivo: 'email' };
    }
    if (senha !== SENHA_DE_TESTE) {
      return { ok: false, motivo: 'senha' };
    }
    return { ok: true, rota: CONFIG_PERFIS[conta.perfil].rota };
  }
}
