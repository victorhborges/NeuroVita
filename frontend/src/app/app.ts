import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { Abertura, AberturaEstado } from './shared/abertura/abertura';
import { AvisoTela } from './shared/aviso/aviso';

/** Componente raiz: exibe a página da rota atual, os avisos rápidos e a abertura animada. */
@Component({
  selector: 'app-root',
  imports: [Abertura, AvisoTela, RouterOutlet],
  template: `
    <!-- Enquanto a abertura toca, o resto da página fica inativo (sem foco nem cliques). -->
    <div class="app-conteudo" [attr.inert]="abertura.ativa() ? '' : null"><router-outlet /></div>
    <app-aviso />
    <app-abertura />
  `,
})
export class App {
  protected readonly abertura = inject(AberturaEstado);
}
