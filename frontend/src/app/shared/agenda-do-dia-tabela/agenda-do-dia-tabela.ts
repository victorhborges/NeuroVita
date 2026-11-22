import { Component, input } from '@angular/core';

import { Avatar } from '../avatar/avatar';
import { StatusConsulta } from '../clinica/modelos';
import { ROTULO_STATUS } from '../clinica/status';
import { Icone, NomeIcone } from '../icone/icone';

export interface LinhaAgendaClinica {
  hora: string;
  paciente: string;
  telefone: string;
  profissional: string;
  status: StatusConsulta;
  foto?: string;
}

@Component({
  selector: 'app-agenda-do-dia-tabela',
  imports: [Avatar, Icone],
  templateUrl: './agenda-do-dia-tabela.html',
})
export class AgendaDoDiaTabela {
  readonly linhas = input.required<LinhaAgendaClinica[]>();
  protected readonly rotulo = ROTULO_STATUS;

  protected icone(status: StatusConsulta): NomeIcone {
    return status === 'confirmado' ? 'check' : 'relogio';
  }
}
