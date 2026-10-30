import { Component, input, model, signal } from '@angular/core';

/**
 * Campo de senha com ícone de cadeado e botão de mostrar/ocultar.
 * Uso: <app-campo-senha placeholder="Senha" [(valor)]="minhaSenha" />
 */
@Component({
  selector: 'app-campo-senha',
  templateUrl: './campo-senha.html',
})
export class CampoSenha {
  /** Texto exibido dentro do campo vazio. */
  readonly placeholder = input('Senha');
  /** Atributo name do campo. */
  readonly nome = input('');
  /** Cor do ícone de cadeado: 'brand' (azul) ou 'muted' (cinza). */
  readonly corIcone = input<'brand' | 'muted'>('brand');
  /** Mostra uma linha separando o botão do olho. */
  readonly separador = input(false);
  /** Valor digitado (ligação de duas vias com [(valor)]). */
  readonly valor = model('');

  protected readonly visivel = signal(false);

  protected alternarVisibilidade(): void {
    this.visivel.update((v) => !v);
  }

  protected aoDigitar(evento: Event): void {
    this.valor.set((evento.target as HTMLInputElement).value);
  }
}
