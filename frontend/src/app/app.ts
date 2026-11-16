import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { AvisoTela } from './shared/aviso/aviso';
import { Abertura, AberturaEstado } from './shared/abertura/abertura';

/** Componente raiz: exibe a página da rota atual e os avisos rápidos. */
@Component({
  selector: 'app-root',
  imports: [Abertura, AvisoTela, RouterOutlet],
  template: `
    <div class="app-conteudo" [attr.inert]="abertura.ativa() ? '' : null"><router-outlet /></div>
    <app-aviso />
    <app-abertura />
  `,
})
export class App {
  protected readonly abertura = inject(AberturaEstado);
}
