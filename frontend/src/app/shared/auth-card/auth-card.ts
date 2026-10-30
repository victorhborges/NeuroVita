import { Component } from '@angular/core';

/**
 * Moldura das telas de autenticação: fundo ilustrado, cartão branco e logo.
 * O conteúdo de cada tela entra no lugar de <ng-content />.
 */
@Component({
  selector: 'app-auth-card',
  template: `
    <main class="auth-main">
      <div class="auth-card">
        <img src="img/logo.png" alt="NeuroVita" width="90" height="90" class="auth-logo" />
        <ng-content />
      </div>
    </main>
  `,
})
export class AuthCard {}
