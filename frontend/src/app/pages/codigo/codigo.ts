import { Component, ElementRef, inject, signal, viewChildren } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthCard } from '../../shared/auth-card/auth-card';
import { RecuperacaoSenhaApiService } from '../../shared/clinica/recuperacao-senha-api.service';

/** Valida o código de recuperação enviado por e-mail. */
@Component({
  selector: 'app-codigo',
  imports: [AuthCard, RouterLink],
  templateUrl: './codigo.html',
})
export class Codigo {
  private readonly router = inject(Router);
  private readonly api = inject(RecuperacaoSenhaApiService);
  protected readonly erro = signal('');
  protected readonly reenviado = signal(false);

  /** Posições dos 6 dígitos (usado no @for do template). */
  protected readonly posicoes = [0, 1, 2, 3, 4, 5];

  private readonly digitos = viewChildren<ElementRef<HTMLInputElement>>('digito');

  /** Mantém só o último número digitado e pula para o próximo campo. */
  protected aoDigitar(indice: number): void {
    const campos = this.digitos();
    const campo = campos[indice].nativeElement;
    campo.value = campo.value.replace(/\D/g, '').slice(-1);
    if (campo.value && indice < campos.length - 1) {
      campos[indice + 1].nativeElement.focus();
    }
  }

  /** Backspace num campo vazio volta para o campo anterior. */
  protected aoPressionarTecla(evento: KeyboardEvent, indice: number): void {
    const campos = this.digitos();
    if (evento.key === 'Backspace' && !campos[indice].nativeElement.value && indice > 0) {
      campos[indice - 1].nativeElement.focus();
    }
  }

  protected reenviar(): void {
    const email = sessionStorage.getItem('neurovita_recuperacao_email');
    if (!email) { this.router.navigate(['/esqueci-senha']); return; }
    this.api.solicitar(email).subscribe({
      next: () => { this.reenviado.set(true); this.erro.set(''); },
      error: (erro) => this.erro.set(erro?.error?.mensagem ?? 'Não foi possível reenviar o código.'),
    });
  }

  protected verificar(evento: Event): void {
    evento.preventDefault();
    const email = sessionStorage.getItem('neurovita_recuperacao_email');
    const codigo = this.digitos().map((ref) => ref.nativeElement.value.replace(/\D/g, '')).join('');
    if (!email || codigo.length !== 6) { this.erro.set('Informe os seis dígitos do código.'); return; }
    this.api.verificar(email, codigo).subscribe({
      next: ({ token }) => { sessionStorage.setItem('neurovita_recuperacao_token', token); this.router.navigate(['/redefinir-senha']); },
      error: (erro) => this.erro.set(erro?.error?.mensagem ?? 'Código inválido ou expirado.'),
    });
  }
}
