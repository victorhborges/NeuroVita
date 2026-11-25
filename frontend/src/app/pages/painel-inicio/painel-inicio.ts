import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AgendaDoDiaTabela, LinhaAgendaClinica } from '../../shared/agenda-do-dia-tabela/agenda-do-dia-tabela';
import { Banner } from '../../shared/banner/banner';
import { Cabecalho } from '../../shared/cabecalho/cabecalho';
import { CardsResumo } from '../../shared/cards-resumo/cards-resumo';
import { Clinica } from '../../shared/clinica/clinica';
import { StatusConsulta } from '../../shared/clinica/modelos';
import { minutosDe } from '../../shared/clinica/status';
import { CardResumo } from '../../shared/dados-demo';
import { Icone } from '../../shared/icone/icone';
import { CONFIG_PERFIS, Perfil } from '../../shared/perfil';
import { paraISO } from '../../shared/util';

const HOJE_DEMO = paraISO(new Date());

/**
 * Tela inicial dos 4 perfis (psicólogo, psiquiatra, secretária e administrador).
 * É a mesma tela: o que muda (cards, botões, banner...) vem de CONFIG_PERFIS.
 * O perfil chega pela rota (o `data` do pai, ver app.routes.ts) e o menu lateral vem do PainelShell.
 */
@Component({
  selector: 'app-painel-inicio',
  imports: [AgendaDoDiaTabela, Banner, Cabecalho, CardsResumo, Icone, RouterLink],
  templateUrl: './painel-inicio.html',
})
export class PainelInicio {
  readonly perfil = input.required<Perfil>();

  protected readonly config = computed(() => CONFIG_PERFIS[this.perfil()]);

  private readonly clinica = inject(Clinica);

  /** Consultas de hoje, em ordem de horário. */
  private readonly hoje = computed(() =>
    this.clinica.agendamentos()
      .filter((a) => a.data === HOJE_DEMO)
      .sort((a, b) => minutosDe(a.inicio) - minutosDe(b.inicio)),
  );

  /** Números dos cards (secretária e administrador), calculados a partir das consultas de hoje. */
  protected readonly cards = computed<CardResumo[]>(() => {
    const lista = this.hoje();
    const conta = (s: StatusConsulta) => String(lista.filter((a) => a.status === s).length).padStart(2, '0');
    return [
      { valor: String(lista.length).padStart(2, '0'), rotulo: 'Consultas\nDo dia', icone: 'calendario' },
      { valor: conta('confirmado'), rotulo: 'Consultas\nConfirmadas', icone: 'usuario' },
      { valor: conta('pendente'), rotulo: 'Consultas\nPendentes', icone: 'info' },
      { valor: conta('cancelado'), rotulo: 'Consultas\nCanceladas', icone: 'usuario-x' },
    ];
  });

  /** Agenda do dia (todos os perfis): consultas de hoje com telefone do paciente e nome do profissional. */
  protected readonly agendaClinica = computed<LinhaAgendaClinica[]>(() =>
    this.hoje().map((a) => {
      const paciente = this.clinica.paciente(a.pacienteId);
      const profissional = this.clinica.profissional(a.profissionalId);
      return {
        hora: a.inicio,
        paciente: paciente?.nome ?? '—',
        foto: paciente?.foto,
        telefone: paciente?.telefone ?? '—',
        tipo: a.atendimento,
        profissional: profissional ? `${profissional.tratamento} ${profissional.nome}`.trim() : '—',
        status: a.status,
      };
    }),
  );
}
