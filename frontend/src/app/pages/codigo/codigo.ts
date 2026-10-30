import { Component, ElementRef, inject, viewChildren } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthCard } from '../../shared/auth-card/auth-card';

/** Código de verificação de 6 dígitos (demonstrativo). */
@Component({
  selector: 'app-codigo',
  imports: [AuthCard, RouterLink],
  templateUrl: './codigo.html',
})
export class Codigo {
  private readonly router = inject(Router);

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
    // TODO: chamar aqui o reenvio real do código.
  }

  protected verificar(evento: Event): void {
    evento.preventDefault();
    // TODO: validar o código de verdade antes de seguir.
    this.router.navigate(['/redefinir-senha']);
  }
}
