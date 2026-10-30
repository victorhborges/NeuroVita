import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Icone } from '../icone/icone';

/** Título da página, subtítulo e (opcional) seta de voltar. Botões e campos vão dentro das tags. */
@Component({
  selector: 'app-titulo-pagina',
  imports: [Icone, RouterLink],
  template: `
    <header class="pg-topo">
      <div>
        <h1 class="pg-titulo">
          @if (voltarPara()) {
            <a class="pg-voltar" [routerLink]="voltarPara()" aria-label="Voltar" (click)="aoVoltar($event)"><app-icone nome="voltar" /></a>
          }
          {{ titulo() }}
        </h1>
        <p class="pg-subtitulo">{{ subtitulo() }}</p>
      </div>
      <div class="pg-acoes"><ng-content /></div>
    </header>
  `,
})
export class TituloPagina {
  readonly titulo = input.required<string>();
  readonly subtitulo = input('');
  readonly voltarPara = input<string | null>(null);
  /** Pergunta antes de sair (use quando há texto não salvo). */
  readonly bloquearSaida = input(false);

  protected aoVoltar(evento: Event): void {
    if (this.bloquearSaida() && !window.confirm('Há alterações não salvas. Deseja sair mesmo assim?')) {
      evento.preventDefault();
    }
  }
}
