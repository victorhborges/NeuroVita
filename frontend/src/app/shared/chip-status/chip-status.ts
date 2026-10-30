import { Component, input } from '@angular/core';

import { StatusConsulta } from '../clinica/modelos';
import { ROTULO_CHIP } from '../clinica/status';

/** Etiqueta com o status da consulta (Confirmado, Pendente...). */
@Component({
  selector: 'app-chip-status',
  template: `<span class="chip-st" [class]="'chip-st-' + status()">{{ rotulo[status()] }}</span>`,
})
export class ChipStatus {
  readonly status = input.required<StatusConsulta>();
  protected readonly rotulo = ROTULO_CHIP;
}
