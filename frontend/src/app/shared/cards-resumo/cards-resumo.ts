import { Component, input } from '@angular/core';

import { Icone } from '../icone/icone';
import { CardResumo } from '../dados-demo';

/** Os 4 cards de números do topo (pacientes, consultas...). */
@Component({
  selector: 'app-cards-resumo',
  imports: [Icone],
  template: `
    <section class="resumo" aria-label="Resumo da clínica">
      @for (card of cards(); track card.rotulo) {
        <div class="cartao resumo-card">
          <span class="resumo-icone"><app-icone [nome]="card.icone" /></span>
          <div class="resumo-dados">
            <span class="resumo-valor">{{ card.valor }}</span>
            <span class="resumo-rotulo">{{ card.rotulo }}</span>
          </div>
        </div>
      }
    </section>
  `,
})
export class CardsResumo {
  readonly cards = input.required<CardResumo[]>();
}
