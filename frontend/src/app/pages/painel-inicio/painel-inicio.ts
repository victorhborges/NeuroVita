import { Component, computed, inject, input } from '@angular/core';

import { AgendaDoDia } from '../../shared/agenda-do-dia/agenda-do-dia';
import { Banner } from '../../shared/banner/banner';
import { Cabecalho } from '../../shared/cabecalho/cabecalho';
import { Calendario } from '../../shared/calendario/calendario';
import { CardsResumo } from '../../shared/cards-resumo/cards-resumo';
import { Clinica } from '../../shared/clinica/clinica';
import { StatusConsulta } from '../../shared/clinica/modelos';
import { minutosDe } from '../../shared/clinica/status';
import { Icone } from '../../shared/icone/icone';
import { RouterLink } from '@angular/router';
import { ProximasConsultas } from '../../shared/proximas-consultas/proximas-consultas';
import {
  AGENDA_DO_DIA,
  CARDS_RESUMO,
  CardResumo,
  DATA_AGENDA,
  MES_CALENDARIO,
  ConsultaProxima,
  ItemAgenda,
  PROXIMAS_CONSULTAS,
  SEMANAS_CALENDARIO,
} from '../../shared/dados-demo';
import { AGORA_DEMO, HOJE_DEMO } from '../../shared/dados-agenda';
import { CONFIG_PERFIS, Perfil, rotaDoMenu } from '../../shared/perfil';
import { dataExtensa, formatarData, idadeDe } from '../../shared/util';

/**
 * Tela inicial dos 4 perfis (psicólogo, psiquiatra, secretária e administrador).
 * É a mesma tela: o que muda (menu, cards, botões, banner...) vem de CONFIG_PERFIS.
 * O perfil chega pela rota (o `data` do pai, ver app.routes.ts) e o menu lateral vem do PainelShell.
 */
@Component({
  selector: 'app-painel-inicio',
  imports: [AgendaDoDia, Banner, Cabecalho, Calendario, CardsResumo, Icone, ProximasConsultas, RouterLink],
  templateUrl: './painel-inicio.html',
})
export class PainelInicio {
  readonly perfil = input.required<Perfil>();

  protected readonly config = computed(() => CONFIG_PERFIS[this.perfil()]);
  protected readonly rotaAgenda = computed(() => rotaDoMenu(this.perfil(), 'agenda'));

  private readonly clinica = inject(Clinica);

  // TODO (back-end): trocar por dados vindos da API.
  protected readonly mesCalendario = MES_CALENDARIO;
  protected readonly semanas = SEMANAS_CALENDARIO;

  private readonly deAgenda = computed(() => this.config().fonte === 'agenda');

  /** Consultas de hoje, em ordem de horário (só para os perfis ligados à agenda). */
  private readonly hoje = computed(() =>
    this.clinica.agendamentos()
      .filter((a) => a.data === HOJE_DEMO)
      .sort((a, b) => minutosDe(a.inicio) - minutosDe(b.inicio)),
  );

  protected readonly cards = computed<CardResumo[]>(() => {
    if (!this.deAgenda()) return CARDS_RESUMO;
    const lista = this.hoje();
    const conta = (s: StatusConsulta) => String(lista.filter((a) => a.status === s).length).padStart(2, '0');
    return [
      { valor: String(lista.length).padStart(2, '0'), rotulo: 'Consultas\nDo dia', icone: 'calendario' },
      { valor: conta('confirmado'), rotulo: 'Consultas\nConfirmadas', icone: 'usuario' },
      { valor: conta('pendente'), rotulo: 'Consultas\nPendentes', icone: 'info' },
      { valor: conta('cancelado'), rotulo: 'Consultas\nCanceladas', icone: 'usuario-x' },
    ];
  });

  protected readonly agenda = computed<ItemAgenda[]>(() => {
    if (!this.deAgenda()) return AGENDA_DO_DIA;
    return this.hoje().map((a) => ({
      hora: a.inicio, paciente: this.clinica.paciente(a.pacienteId)?.nome ?? '—', tipo: a.tipo, status: a.status,
    }));
  });

  protected readonly dataAgenda = computed(() => (this.deAgenda() ? dataExtensa(HOJE_DEMO) : DATA_AGENDA));

  protected readonly proximas = computed<ConsultaProxima[]>(() => {
    if (!this.deAgenda()) return PROXIMAS_CONSULTAS;
    return this.clinica.agendamentos()
      .filter((a) => a.data > HOJE_DEMO || (a.data === HOJE_DEMO && minutosDe(a.inicio) > minutosDe(AGORA_DEMO)))
      .sort((a, b) => a.data.localeCompare(b.data) || minutosDe(a.inicio) - minutosDe(b.inicio))
      .slice(0, 4)
      .map((a) => ({
        paciente: this.clinica.paciente(a.pacienteId)?.nome ?? '—', tipo: a.tipo, data: formatarData(a.data), hora: a.inicio, status: a.status,
      }));
  });
}
