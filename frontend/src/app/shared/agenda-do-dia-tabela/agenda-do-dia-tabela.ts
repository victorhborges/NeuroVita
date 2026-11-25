import { Component, input } from '@angular/core';

import { Avatar } from '../avatar/avatar';
import { StatusConsulta } from '../clinica/modelos';
import { ROTULO_STATUS } from '../clinica/status';
import { Icone, NomeIcone } from '../icone/icone';

/** Uma linha da agenda do dia em formato de tabela (início do psicólogo). */
export interface LinhaAgendaClinica {
  hora: string;
  paciente: string;
  /** Caminho da foto do paciente. Sem foto, aparece um círculo cinza liso (como no Figma). */
  foto?: string;
  telefone: string;
  profissional: string;
  /** 'Presencial' ou 'Online'. */
  tipo?: string;
  status: StatusConsulta;
}

/** Cartão "Agenda do dia" em tabela: horário, paciente, telefone, profissional e status. */
@Component({
  selector: 'app-agenda-do-dia-tabela',
  imports: [Avatar, Icone],
  templateUrl: './agenda-do-dia-tabela.html',
})
export class AgendaDoDiaTabela {
  readonly linhas = input.required<LinhaAgendaClinica[]>();
  /** Mostra a coluna "Tipo" (Presencial/Online). */
  readonly mostrarTipo = input(false);

  protected readonly rotulo = ROTULO_STATUS;

  protected icone(status: StatusConsulta): NomeIcone {
    return status === 'confirmado' ? 'check' : 'relogio';
  }
}
