import { Component, Injectable, inject, signal } from '@angular/core';

/** Mensagem rápida que aparece embaixo da tela (ex.: "Laudo salvo"). */
@Injectable({ providedIn: 'root' })
export class Aviso {
  readonly mensagem = signal('');
  private temporizador?: ReturnType<typeof setTimeout>;

  mostrar(texto: string, duracaoMs = 3200): void {
    this.mensagem.set(texto);
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.mensagem.set(''), duracaoMs);
  }
}

@Component({
  selector: 'app-aviso',
  template: `
    @if (aviso.mensagem()) {
      <div class="aviso" role="status">{{ aviso.mensagem() }}</div>
    }
  `,
})
export class AvisoTela {
  protected readonly aviso = inject(Aviso);
}
