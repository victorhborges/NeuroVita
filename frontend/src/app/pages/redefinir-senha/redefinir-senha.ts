import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthCard } from '../../shared/auth-card/auth-card';
import { CampoSenha } from '../../shared/campo-senha/campo-senha';
import { validarNovaSenha } from '../../shared/validacao-senha';
import { RecuperacaoSenhaApiService } from '../../shared/clinica/recuperacao-senha-api.service';

/** Salva a nova senha após validar o código de e-mail. */
@Component({
  selector: 'app-redefinir-senha',
  imports: [AuthCard, CampoSenha, RouterLink],
  templateUrl: './redefinir-senha.html',
})
export class RedefinirSenha {
  private readonly router = inject(Router);
  private readonly api = inject(RecuperacaoSenhaApiService);

  protected readonly novaSenha = signal('');
  protected readonly confirmarSenha = signal('');
  protected readonly erro = signal<string | null>(null);

  protected redefinir(evento: Event): void {
    evento.preventDefault();
    const mensagem = validarNovaSenha(this.novaSenha(), this.confirmarSenha());
    this.erro.set(mensagem);
    if (mensagem) return;

    const token = sessionStorage.getItem('neurovita_recuperacao_token');
    if (!token) { this.erro.set('O processo expirou. Solicite um novo código.'); return; }
    this.api.redefinir(token, this.novaSenha(), this.confirmarSenha()).subscribe({
      next: () => {
        sessionStorage.removeItem('neurovita_recuperacao_email');
        sessionStorage.removeItem('neurovita_recuperacao_token');
        this.router.navigate(['/'], { queryParams: { sucesso: 'true' } });
      },
      error: (erro) => this.erro.set(erro?.error?.mensagem ?? 'Não foi possível redefinir a senha. Solicite um novo código.'),
    });
  }
}
