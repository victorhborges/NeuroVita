import { Component, input, signal } from '@angular/core';

import { Icone } from '../icone/icone';

/**
 * Imagem da central de ajuda. Se o arquivo ainda não existe, mostra um espaço reservado
 * (ou nada, com `ocultarSeFalhar`) em vez do ícone de imagem quebrada.
 */
@Component({
  selector: 'app-imagem-ajuda',
  imports: [Icone],
  template: `
    @if (!falhou()) {
      <img class="ajuda-img" [src]="src()" [alt]="alt()" (error)="falhou.set(true)" />
    } @else if (!ocultarSeFalhar()) {
      <div class="ajuda-img-vazia" role="img" [attr.aria-label]="alt()">
        <app-icone nome="imagem" />
        <span>{{ alt() }}</span>
      </div>
    }
  `,
})
export class ImagemAjuda {
  readonly src = input.required<string>();
  readonly alt = input.required<string>();
  readonly ocultarSeFalhar = input(false);

  protected readonly falhou = signal(false);
}
