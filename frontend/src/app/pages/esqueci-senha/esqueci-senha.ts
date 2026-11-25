import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthCard } from '../../shared/auth-card/auth-card';
import { RecuperacaoSenhaApiService } from '../../shared/clinica/recuperacao-senha-api.service';

/** Solicita código real de recuperação por e-mail. */
@Component({
  selector: 'app-esqueci-senha',
  imports: [AuthCard, RouterLink],
  templateUrl: './esqueci-senha.html',
})
export class EsqueciSenha {
  private readonly router = inject(Router);
  private readonly api = inject(RecuperacaoSenhaApiService);
  protected readonly erro = signal('');
  protected readonly enviando = signal(false);

  protected enviar(evento: Event, email: string): void {
    evento.preventDefault();
    if (this.enviando()) return;
    this.erro.set('');
    this.enviando.set(true);
    const endereco = email.trim();
    this.api.solicitar(endereco).pipe(finalize(() => this.enviando.set(false))).subscribe({
      next: () => {
        sessionStorage.setItem('neurovita_recuperacao_email', endereco);
        this.router.navigate(['/codigo']);
      },
      error: (erro) => this.erro.set(erro?.error?.mensagem ?? erro?.error?.message
        ?? 'Não foi possível enviar o código. Tente novamente mais tarde.'),
    });
  }
}
