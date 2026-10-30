import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Avatar } from '../avatar/avatar';
import { ChipStatus } from '../chip-status/chip-status';
import { Icone } from '../icone/icone';
import { TagDiagnostico } from '../tag-diagnostico/tag-diagnostico';
import { ConsultaProxima } from '../dados-demo';

/** Cartão com as próximas consultas marcadas. */
@Component({
  selector: 'app-proximas-consultas',
  imports: [Avatar, ChipStatus, Icone, RouterLink, TagDiagnostico],
  templateUrl: './proximas-consultas.html',
})
export class ProximasConsultas {
  readonly consultas = input.required<ConsultaProxima[]>();
  /** Mostra os 3 pontinhos em cada linha. */
  readonly menuAcoes = input(false);
  /** Endereço da tela de Agenda (se já existir para o perfil). */
  readonly rota = input<string | null>(null);
}
