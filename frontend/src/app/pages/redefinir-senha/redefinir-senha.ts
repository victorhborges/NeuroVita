import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthCard } from '../../shared/auth-card/auth-card';
import { CampoSenha } from '../../shared/campo-senha/campo-senha';
import { validarNovaSenha } from '../../shared/validacao-senha';

/** Redefinir senha (demonstrativo: não salva a senha). */
@Component({
  selector: 'app-redefinir-senha',
  imports: [AuthCard, CampoSenha, RouterLink],
  templateUrl: './redefinir-senha.html',
})
export class RedefinirSenha {
  private readonly router = inject(Router);

  protected readonly novaSenha = signal('');
  protected readonly confirmarSenha = signal('');
  protected readonly erro = signal<string | null>(null);

  protected redefinir(evento: Event): void {
    evento.preventDefault();
    const mensagem = validarNovaSenha(this.novaSenha(), this.confirmarSenha());
    this.erro.set(mensagem);
    if (mensagem) return;

    // TODO: salvar aqui a nova senha de verdade.
    this.router.navigate(['/'], { queryParams: { sucesso: 'true' } });
  }
}
