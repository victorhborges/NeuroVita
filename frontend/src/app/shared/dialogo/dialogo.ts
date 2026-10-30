import { Component, input, output } from '@angular/core';

/** Janela de confirmação (ex.: "Salvar Edição?"). O conteúdo entre as tags aparece abaixo do título. */
@Component({
  selector: 'app-dialogo',
  host: { '(document:keydown.escape)': 'aberto() && cancelar.emit()' },
  template: `
    @if (aberto()) {
      <div class="dialogo-fundo" (mousedown)="fundo($event)">
        <div class="dialogo" role="dialog" aria-modal="true" [attr.aria-label]="titulo()">
          <h2 class="dialogo-titulo">{{ titulo() }}</h2>
          <ng-content />
          <div class="dialogo-botoes">
            <button type="button" class="botao-claro" (click)="cancelar.emit()">{{ textoCancelar() }}</button>
            <button type="button" class="botao-escuro" (click)="confirmar.emit()">{{ textoConfirmar() }}</button>
          </div>
        </div>
      </div>
    }
  `,
})
export class Dialogo {
  readonly aberto = input(false);
  readonly titulo = input.required<string>();
  readonly textoCancelar = input('Cancelar');
  readonly textoConfirmar = input('Confirmar');
  readonly cancelar = output<void>();
  readonly confirmar = output<void>();

  protected fundo(evento: MouseEvent): void {
    if (evento.target === evento.currentTarget) this.cancelar.emit();
  }
}
