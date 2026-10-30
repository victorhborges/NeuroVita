import { Component, computed, input } from '@angular/core';

const CORES = ['#7ba4d9', '#8fbc8b', '#ff9a8b', '#9b87c7', '#e0b36a', '#6fb7b3'];

/**
 * Foto redonda da pessoa. Sem foto, mostra a inicial do nome (ou um círculo cinza liso).
 * O tamanho vem da variável CSS --avatar (padrão 2.5rem).
 */
@Component({
  selector: 'app-avatar',
  template: `
    @if (foto()) {
      <img class="avatar-img" [src]="foto()" [alt]="nome()" />
    } @else if (!neutro()) {
      <span class="avatar-inicial" [style.background]="cor()" aria-hidden="true">{{ inicial() }}</span>
    }
  `,
})
export class Avatar {
  readonly nome = input.required<string>();
  readonly foto = input<string>();
  /** Círculo cinza liso, sem inicial. */
  readonly neutro = input(false);

  protected readonly inicial = computed(() => {
    // Ignora o tratamento (Dr., Dra.) para pegar a inicial do nome de verdade.
    const limpo = this.nome().replace(/^(dra?\.)\s*/i, '').trim();
    return (limpo.charAt(0) || '?').toUpperCase();
  });

  protected readonly cor = computed(() => {
    let soma = 0;
    for (const letra of this.nome()) soma += letra.charCodeAt(0);
    return CORES[soma % CORES.length];
  });
}
