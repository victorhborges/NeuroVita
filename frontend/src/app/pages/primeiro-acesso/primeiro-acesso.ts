import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../shared/auth';
import { AuthCard } from '../../shared/auth-card/auth-card';
import { CampoSenha } from '../../shared/campo-senha/campo-senha';

@Component({
  selector: 'app-primeiro-acesso',
  imports: [AuthCard, CampoSenha, RouterLink],
  templateUrl: './primeiro-acesso.html',
})
export class PrimeiroAcesso {
  protected readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected readonly novaSenha = signal('');
  protected readonly confirmarSenha = signal('');
  protected readonly erro = signal('');
  protected readonly salvando = signal(false);

  protected alterar(evento: Event): void {
    evento.preventDefault();

    const novaSenha = this.novaSenha();
    const confirmarSenha = this.confirmarSenha();

    if (novaSenha.length < 8 || novaSenha.length > 72) {
      this.erro.set('A senha deve ter entre 8 e 72 caracteres.');
      return;
    }

    if (!confirmarSenha.trim()) {
      this.erro.set('Confirme a nova senha.');
      return;
    }

    if (novaSenha !== confirmarSenha) {
      this.erro.set('As senhas não coincidem.');
      return;
    }

    if (this.salvando()) return;

    this.salvando.set(true);
    this.erro.set('');

    this.auth
      .alterarSenhaPrimeiroAcesso(novaSenha, confirmarSenha)
      .subscribe({
        next: () => {
          this.auth.sair();
          this.router.navigate(['/'], {
            queryParams: { sucesso: 'senha-alterada' },
          });
        },
        error: (erro: HttpErrorResponse) => {
          this.salvando.set(false);
          this.erro.set(
            erro.error?.message ||
              erro.error?.mensagem ||
              'Não foi possível alterar a senha. Entre novamente e tente de novo.',
          );

          if (erro.status === 401 || erro.status === 403) {
            this.auth.sair();
          }
        },
      });
  }
}
