import { Component, input } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { MenuLateral } from '../../shared/menu-lateral/menu-lateral';
import { Perfil } from '../../shared/perfil';

/** Moldura das telas logadas: menu escuro à esquerda e a tela do perfil à direita. */
@Component({
  selector: 'app-painel-shell',
  imports: [MenuLateral, RouterOutlet],
  template: `
    <div class="painel">
      <app-menu-lateral [perfil]="perfil()" />
      <main class="painel-conteudo"><router-outlet /></main>
    </div>
  `,
})
export class PainelShell {
  /** Vem do `data` da rota (ver app.routes.ts). */
  readonly perfil = input.required<Perfil>();
}
