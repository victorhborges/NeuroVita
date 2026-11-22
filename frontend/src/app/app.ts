import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { AvisoTela } from './shared/aviso/aviso';
import { Abertura, AberturaEstado } from './shared/abertura/abertura';
import { PreferenciasGlobaisService } from './shared/clinica/preferencias-globais.service';

/** Componente raiz: exibe a página da rota atual e os avisos rápidos. */
@Component({
  selector: 'app-root',
  imports: [Abertura, AvisoTela, RouterOutlet],
  template: `
    <div class="app-conteudo" [attr.inert]="abertura.ativa() ? '' : null"><router-outlet /></div>
    @if (preferencias.erroCarregamento()) {
      <div class="preferencias-aviso" role="alert">
        Não foi possível carregar suas preferências. O sistema abriu com os valores padrão.
        <button type="button" (click)="recarregarPreferencias()">Tentar novamente</button>
      </div>
    }
    <app-aviso />
    <app-abertura />
  `,
})
export class App {
  protected readonly abertura = inject(AberturaEstado);
  protected readonly preferencias = inject(PreferenciasGlobaisService);

  protected recarregarPreferencias(): void {
    this.preferencias.carregar(true).subscribe();
  }
}
