import { Component, input } from '@angular/core';

import { Icone } from '../icone/icone';
import { DIAS_DA_SEMANA, DiaCalendario } from '../dados-demo';

/**
 * Calendário mensal (por enquanto só visual, igual ao protótipo).
 * TODO: navegar entre os meses (setas) e buscar as consultas de cada dia no back-end.
 */
@Component({
  selector: 'app-calendario',
  imports: [Icone],
  templateUrl: './calendario.html',
})
export class Calendario {
  readonly mes = input.required<string>();
  readonly semanas = input.required<DiaCalendario[][]>();

  protected readonly diasDaSemana = DIAS_DA_SEMANA;
}
