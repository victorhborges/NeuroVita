import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ChipStatus } from '../chip-status/chip-status';
import { Icone } from '../icone/icone';
import { TagDiagnostico } from '../tag-diagnostico/tag-diagnostico';
import { ItemAgenda } from '../dados-demo';

/** Cartão com os atendimentos do dia. */
@Component({
  selector: 'app-agenda-do-dia',
  imports: [ChipStatus, Icone, RouterLink, TagDiagnostico],
  templateUrl: './agenda-do-dia.html',
})
export class AgendaDoDia {
  readonly itens = input.required<ItemAgenda[]>();
  readonly data = input.required<string>();
  /** Mostra os 3 pontinhos em cada linha. */
  readonly menuAcoes = input(false);
  /** Endereço da tela de Agenda (se já existir para o perfil). */
  readonly rota = input<string | null>(null);
}
