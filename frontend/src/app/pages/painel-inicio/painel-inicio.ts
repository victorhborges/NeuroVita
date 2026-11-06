import { Component, computed, inject, input, signal } from '@angular/core';

import { AgendaDoDia } from '../../shared/agenda-do-dia/agenda-do-dia';
import { Banner } from '../../shared/banner/banner';
import { Cabecalho } from '../../shared/cabecalho/cabecalho';
import { Calendario } from '../../shared/calendario/calendario';
import { CardsResumo } from '../../shared/cards-resumo/cards-resumo';
import {
  ConsultasApiService,
  ConsultaApi,
  StatusConsultaApi,
} from '../../shared/clinica/consultas-api.service';
import {
  PacientesApiService,
  PacienteApi,
} from '../../shared/clinica/pacientes-api.service';
import { StatusConsulta } from '../../shared/clinica/modelos';
import { Icone } from '../../shared/icone/icone';
import { RouterLink } from '@angular/router';
import { ProximasConsultas } from '../../shared/proximas-consultas/proximas-consultas';
import {
  CardResumo,
  ConsultaProxima,
  ItemAgenda,
  calendarioDoMes,
} from '../../shared/dados-demo';
import { CONFIG_PERFIS, Perfil, rotaDoMenu } from '../../shared/perfil';
import { Auth } from '../../shared/auth';
import { dataExtensa, formatarData } from '../../shared/util';

@Component({
  selector: 'app-painel-inicio',
  imports: [
    AgendaDoDia,
    Banner,
    Cabecalho,
    Calendario,
    CardsResumo,
    Icone,
    ProximasConsultas,
    RouterLink,
  ],
  templateUrl: './painel-inicio.html',
})
export class PainelInicio {
  readonly perfil = input.required<Perfil>();

  private readonly auth = inject(Auth);
  protected readonly config = computed(() => {
    const perfil = CONFIG_PERFIS[this.perfil()];
    const nome = this.auth.obterNome() ?? perfil.usuario.nome;
    return { ...perfil, saudacao: `Olá, ${nome}`, usuario: { ...perfil.usuario, nome } };
  });
  protected readonly rotaAgenda = computed(() =>
    rotaDoMenu(this.perfil(), 'agenda'),
  );

  private readonly consultasApi = inject(ConsultasApiService);
  private readonly pacientesApi = inject(PacientesApiService);

  private readonly consultas = signal<ConsultaApi[]>([]);
  private readonly pacientes = signal<PacienteApi[]>([]);

  protected readonly mesCalendario = computed(() => new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(new Date()));
  protected readonly semanas = computed(() => calendarioDoMes(new Date(), new Set(this.consultas().map((c) => c.dataHora.slice(0, 10)))));

  private readonly hojeISO = this.dataAtualISO();

  private dataAtualISO(): string {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = String(hoje.getMonth() + 1).padStart(2, '0');
    const dia = String(hoje.getDate()).padStart(2, '0');

    return `${ano}-${mes}-${dia}`;
  }

  constructor() {
    this.carregarDados();
  }

  private carregarDados(): void {
    this.consultasApi.listarTodas().subscribe({
      next: (lista) => this.consultas.set(lista ?? []),
      error: (erro) => {
        console.error('Erro ao carregar consultas do painel:', erro);
        this.consultas.set([]);
      },
    });

    this.pacientesApi.listarTodos().subscribe({
      next: (lista) => this.pacientes.set(lista ?? []),
      error: (erro) => {
        console.error('Erro ao carregar pacientes do painel:', erro);
        this.pacientes.set([]);
      },
    });
  }

  private nomePaciente(pacienteId: string): string {
    return this.pacientes().find((p) => p.id === pacienteId)?.nome ?? '—';
  }

  private converterStatus(status: StatusConsultaApi): StatusConsulta {
    const conversao: Record<StatusConsultaApi, StatusConsulta> = {
      PENDENTE: 'pendente',
      CONFIRMADA: 'confirmado',
      REMARCADA: 'remarcado',
      CANCELADA: 'cancelado',
    };

    return conversao[status] ?? 'pendente';
  }

  private consultasDeHoje(): ConsultaApi[] {
    return this.consultas()
      .filter((consulta) => consulta.dataHora.slice(0, 10) === this.hojeISO)
      .sort((a, b) => a.dataHora.localeCompare(b.dataHora));
  }

  protected readonly cards = computed<CardResumo[]>(() => {
    const lista = this.consultasDeHoje();

    const conta = (status: StatusConsultaApi) =>
      String(lista.filter((consulta) => consulta.status === status).length).padStart(2, '0');

    return [
      {
        valor: String(lista.length).padStart(2, '0'),
        rotulo: 'Consultas\nDo dia',
        icone: 'calendario',
      },
      {
        valor: conta('CONFIRMADA'),
        rotulo: 'Consultas\nConfirmadas',
        icone: 'usuario',
      },
      {
        valor: conta('PENDENTE'),
        rotulo: 'Consultas\nPendentes',
        icone: 'info',
      },
      {
        valor: conta('CANCELADA'),
        rotulo: 'Consultas\nCanceladas',
        icone: 'usuario-x',
      },
    ];
  });

  protected readonly agenda = computed<ItemAgenda[]>(() =>
    this.consultasDeHoje().map((consulta) => ({
      hora: consulta.dataHora.slice(11, 16),
      paciente: this.nomePaciente(consulta.pacienteId),
      tipo: consulta.tipoAtendimento,
      status: this.converterStatus(consulta.status),
    })),
  );

  protected readonly dataAgenda = computed(() =>
    dataExtensa(this.hojeISO),
  );

  protected readonly proximas = computed<ConsultaProxima[]>(() =>
    this.consultas()
      .filter((consulta) => {
        const data = consulta.dataHora.slice(0, 10);

        return (
          consulta.status !== 'CANCELADA' &&
          (
            data > this.hojeISO ||
            (
              data === this.hojeISO &&
              consulta.dataHora.slice(11, 16) >
              `${String(new Date().getHours()).padStart(2, '0')}:${String(new Date().getMinutes()).padStart(2, '0')}`
            )
          )
        );
      })
      .sort((a, b) => a.dataHora.localeCompare(b.dataHora))
      .slice(0, 4)
      .map((consulta) => ({
        paciente: this.nomePaciente(consulta.pacienteId),
        tipo: consulta.tipoAtendimento,
        data: formatarData(consulta.dataHora.slice(0, 10)),
        hora: consulta.dataHora.slice(11, 16),
        status: this.converterStatus(consulta.status),
      })),
  );
}
