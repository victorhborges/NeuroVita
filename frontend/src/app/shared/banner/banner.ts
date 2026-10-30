import { Component, input } from '@angular/core';

/** Faixa colorida com frase motivacional e ilustração. */
@Component({
  selector: 'app-banner',
  templateUrl: './banner.html',
})
export class Banner {
  readonly variante = input<'azul' | 'roxo'>('azul');
  readonly titulo = input.required<string>();
  readonly texto = input<string>();
}
