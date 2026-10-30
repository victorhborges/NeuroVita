import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthCard } from '../../shared/auth-card/auth-card';

/** Esqueci minha senha (demonstrativo: não envia e-mails). */
@Component({
  selector: 'app-esqueci-senha',
  imports: [AuthCard, RouterLink],
  templateUrl: './esqueci-senha.html',
})
export class EsqueciSenha {
  private readonly router = inject(Router);

  protected enviar(evento: Event): void {
    evento.preventDefault();
    // TODO: chamar aqui o envio real do código por e-mail.
    this.router.navigate(['/codigo']);
  }
}
