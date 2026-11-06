import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

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
    this.enviando.set(true); this.erro.set('');
    this.api.solicitar(email.trim()).subscribe({
      next: () => { sessionStorage.setItem('neurovita_recuperacao_email', email.trim()); this.router.navigate(['/codigo']); },
      error: (erro) => { this.erro.set(erro?.error?.mensagem ?? 'Não foi possível enviar o código. Tente novamente mais tarde.'); this.enviando.set(false); },
    });
  }
}
