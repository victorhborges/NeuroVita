import { Component, ElementRef, computed, inject, input, model, signal } from '@angular/core';

import { Icone } from '../icone/icone';
import { normalizar } from '../util';

export interface OpcaoLista {
  valor: string;
  rotulo: string;
}

/**
 * Campo de escolha com a lista aberta embaixo (como no protótipo).
 * Uso: <app-lista-suspensa titulo="Pacientes" [opcoes]="..." [(valor)]="id" />
 */
@Component({
  selector: 'app-lista-suspensa',
  imports: [Icone],
  host: { '(document:click)': 'fecharSeFora($event)', '(document:keydown.escape)': 'aberta.set(false)' },
  template: `
    <div class="lista-suspensa">
      <button
        type="button" class="lista-campo" [class.lista-campo-erro]="erro()" aria-haspopup="listbox"
        [attr.aria-expanded]="aberta()" [attr.aria-label]="titulo()" (click)="aberta.set(!aberta())"
      >
        <span [class.lista-placeholder]="!selecionada()">{{ selecionada()?.rotulo ?? placeholder() }}</span>
        <app-icone nome="seta-baixo" />
      </button>

      @if (aberta()) {
        <div class="lista-painel" role="listbox" [attr.aria-label]="titulo()">
          <p class="lista-titulo">{{ titulo() }}</p>
          @if (comBusca()) {
            <input type="search" class="lista-busca" placeholder="Buscar..." aria-label="Buscar na lista" [value]="busca()" (input)="busca.set($any($event.target).value)" />
          }
          <div class="lista-itens">
            @for (o of filtradas(); track o.valor) {
              <button type="button" role="option" class="lista-opcao" [attr.aria-selected]="o.valor === valor()" (click)="escolher(o)">{{ o.rotulo }}</button>
            } @empty {
              <p class="lista-vazia">Nada encontrado.</p>
            }
          </div>
        </div>
      }
    </div>
  `,
})
export class ListaSuspensa {
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly titulo = input.required<string>();
  readonly opcoes = input.required<OpcaoLista[]>();
  readonly placeholder = input('Selecione');
  readonly comBusca = input(false);
  readonly erro = input(false);
  readonly valor = model('');

  protected readonly aberta = signal(false);
  protected readonly busca = signal('');

  protected readonly selecionada = computed(() => this.opcoes().find((o) => o.valor === this.valor()));
  protected readonly filtradas = computed(() => {
    const termo = normalizar(this.busca());
    return this.opcoes().filter((o) => !termo || normalizar(o.rotulo).includes(termo));
  });

  protected escolher(opcao: OpcaoLista): void {
    this.valor.set(opcao.valor);
    this.aberta.set(false);
    this.busca.set('');
  }

  protected fecharSeFora(evento: Event): void {
    if (this.aberta() && !this.elemento.nativeElement.contains(evento.target as Node)) this.aberta.set(false);
  }
}
