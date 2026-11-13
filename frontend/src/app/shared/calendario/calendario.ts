import { Component, input, output } from '@angular/core';

import { Icone } from '../icone/icone';
import { DIAS_DA_SEMANA, DiaCalendario } from '../dados-demo';

/**
 * Calendário mensal do painel; os dados dos dias vêm das consultas carregadas pela tela.
 */
@Component({
  selector: 'app-calendario',
  imports: [Icone],
  templateUrl: './calendario.html',
})
export class Calendario {
  readonly mes = input.required<string>();
  readonly semanas = input.required<DiaCalendario[][]>();
  readonly navegarMes = output<number>();

  protected readonly diasDaSemana = DIAS_DA_SEMANA;
}
