import { Component, ElementRef, computed, inject, input, model, signal } from '@angular/core';

import { Icone, NomeIcone } from '../icone/icone';
import { normalizar } from '../util';

export interface OpcaoLista {
  valor: string;
  rotulo: string;
  /** Imagem pequena ao lado do texto (ex.: bandeira do idioma). */
  imagem?: string;
  desabilitada?: boolean;
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
    <div class="lista-suspensa" [class.lista-compacta]="compacta()">
      <button
        type="button" class="lista-campo" [class.lista-campo-erro]="erro()" aria-haspopup="listbox"
        [attr.aria-expanded]="aberta()" [attr.aria-label]="titulo()" (click)="aberta.set(!aberta())"
      >
        <span class="lista-valor" [class.lista-placeholder]="!selecionada()">
          @if (icone(); as ic) { <app-icone [nome]="ic" /> }
          @if (selecionada()?.imagem; as img) { <img class="lista-imagem" [src]="img" alt="" /> }
          {{ selecionada()?.rotulo ?? placeholder() }}
        </span>
        <app-icone nome="seta-baixo" />
      </button>

      @if (aberta()) {
        <div class="lista-painel" role="listbox" [attr.aria-label]="titulo()">
          @if (mostrarTitulo()) { <p class="lista-titulo">{{ titulo() }}</p> }
          @if (comBusca()) {
            <input type="search" class="lista-busca" placeholder="Buscar..." aria-label="Buscar na lista" [value]="busca()" (input)="busca.set($any($event.target).value)" />
          }
          <div class="lista-itens">
            @for (o of filtradas(); track o.valor) {
              <button type="button" role="option" class="lista-opcao" [disabled]="o.desabilitada ?? false" [attr.aria-disabled]="o.desabilitada ?? false" [attr.aria-selected]="o.valor === valor()" (click)="escolher(o)">
                @if (iconeOpcao(); as ic) { <app-icone [nome]="ic" /> }
                @if (o.imagem) { <img class="lista-imagem" [src]="o.imagem" alt="" /> }
                {{ o.rotulo }}
              </button>
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
  /** Escreve o título dentro da lista aberta (desligue em listas simples, como as das Configurações). */
  readonly mostrarTitulo = input(true);
  readonly erro = input(false);
  /** Ícone no começo do campo fechado. */
  readonly icone = input<NomeIcone | null>(null);
  /** Ícone ao lado de cada opção da lista aberta. */
  readonly iconeOpcao = input<NomeIcone | null>(null);
  /** Campo e lista menores, para janelas (ex.: Editar consulta). */
  readonly compacta = input(false);
  readonly valor = model('');

  protected readonly aberta = signal(false);
  protected readonly busca = signal('');

  protected readonly selecionada = computed(() => this.opcoes().find((o) => o.valor === this.valor()));
  protected readonly filtradas = computed(() => {
    const termo = normalizar(this.busca());
    return this.opcoes().filter((o) => !termo || normalizar(o.rotulo).includes(termo));
  });

  protected escolher(opcao: OpcaoLista): void {
    if (opcao.desabilitada) return;
    this.valor.set(opcao.valor);
    this.aberta.set(false);
    this.busca.set('');
  }

  protected fecharSeFora(evento: Event): void {
    if (this.aberta() && !this.elemento.nativeElement.contains(evento.target as Node)) this.aberta.set(false);
  }
}
