import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AgendaDoDiaTabela, LinhaAgendaClinica } from '../../shared/agenda-do-dia-tabela/agenda-do-dia-tabela';
import { Aviso } from '../../shared/aviso/aviso';
import { Banner } from '../../shared/banner/banner';
import { Cabecalho } from '../../shared/cabecalho/cabecalho';
import { Auth } from '../../shared/auth';
import { CardsResumo } from '../../shared/cards-resumo/cards-resumo';
import { ConsultasApiService, ConsultaApi, StatusConsultaApi } from '../../shared/clinica/consultas-api.service';
import { PacienteApi, PacientesApiService } from '../../shared/clinica/pacientes-api.service';
import { ProfissionaisApiService, ProfissionalApi } from '../../shared/clinica/profissionais-api.service';
import { StatusConsulta } from '../../shared/clinica/modelos';
import { Icone } from '../../shared/icone/icone';
import { CardResumo } from '../../shared/dados-demo';
import { CONFIG_PERFIS, Perfil } from '../../shared/perfil';
import { ROTULO_STATUS } from '../../shared/clinica/status';

@Component({
  selector: 'app-painel-inicio',
  imports: [AgendaDoDiaTabela, Banner, Cabecalho, CardsResumo, Icone, RouterLink],
  templateUrl: './painel-inicio.html',
})
export class PainelInicio implements OnInit {
  readonly perfil = input.required<Perfil>();

  private readonly auth = inject(Auth);
  private readonly aviso = inject(Aviso);
  private readonly consultasApi = inject(ConsultasApiService);
  private readonly pacientesApi = inject(PacientesApiService);
  private readonly profissionaisApi = inject(ProfissionaisApiService);

  private readonly consultas = signal<ConsultaApi[]>([]);
  private readonly pacientes = signal<PacienteApi[]>([]);
  private readonly profissionais = signal<ProfissionalApi[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erroCarga = signal(false);
  protected readonly rotuloStatus = ROTULO_STATUS;
  private readonly hojeISO = this.dataAtualISO();

  protected readonly config = computed(() => {
    const perfil = CONFIG_PERFIS[this.perfil()];
    const nome = this.auth.obterNome()?.trim() || 'Usuário';
    return { ...perfil, saudacao: `Olá, ${nome}`, usuario: { ...perfil.usuario, nome } };
  });

  private dataAtualISO(): string {
    const hoje = new Date();
    return `${hoje.getFullYear()}-${String(hoje.getMonth() + 1).padStart(2, '0')}-${String(hoje.getDate()).padStart(2, '0')}`;
  }

  protected recarregar(): void {
    this.carregando.set(true);
    this.erroCarga.set(false);
    this.consultas.set([]);
    this.pacientes.set([]);
    this.profissionais.set([]);
    this.carregarDados();
  }

  ngOnInit(): void {
    this.carregarDados();
  }

  private carregarDados(): void {
    const carregarProfissionais = this.perfil() === 'admin' || this.perfil() === 'secretaria';
    let pendentes = carregarProfissionais ? 3 : 2;
    const finalizado = (falhou = false) => {
      if (falhou) this.erroCarga.set(true);
      pendentes -= 1;
      if (pendentes === 0) this.carregando.set(false);
    };

    this.consultasApi.listarTodas().subscribe({
      next: (lista) => { this.consultas.set(lista ?? []); finalizado(); },
      error: (erro) => { console.error('Erro ao carregar consultas do painel:', erro); finalizado(true); },
    });
    this.pacientesApi.listarTodos().subscribe({
      next: (lista) => { this.pacientes.set(lista ?? []); finalizado(); },
      error: (erro) => { console.error('Erro ao carregar pacientes do painel:', erro); finalizado(true); },
    });
    if (carregarProfissionais) {
      this.profissionaisApi.listarTodos().subscribe({
        next: (lista) => { this.profissionais.set(lista ?? []); finalizado(); },
        error: (erro) => { console.error('Erro ao carregar profissionais do painel:', erro); finalizado(true); },
      });
    }
  }

  private readonly consultasDeHoje = computed(() => this.consultas()
    .filter((consulta) => consulta.dataHora.slice(0, 10) === this.hojeISO)
    .sort((a, b) => a.dataHora.localeCompare(b.dataHora)));

  protected readonly cards = computed<CardResumo[]>(() => {
    const lista = this.consultasDeHoje();
    const conta = (status: StatusConsultaApi) => String(lista.filter((consulta) => consulta.status === status).length).padStart(2, '0');
    return [
      { valor: String(lista.length).padStart(2, '0'), rotulo: 'Consultas\nDo dia', icone: 'calendario' },
      { valor: conta('CONFIRMADA'), rotulo: 'Consultas\nConfirmadas', icone: 'usuario' },
      { valor: conta('PENDENTE'), rotulo: 'Consultas\nPendentes', icone: 'info' },
      { valor: conta('CANCELADA'), rotulo: 'Consultas\nCanceladas', icone: 'usuario-x' },
    ];
  });

  protected readonly agendaClinica = computed<LinhaAgendaClinica[]>(() => this.consultasDeHoje().map((consulta) => {
    const paciente = this.pacientes().find((item) => item.id === consulta.pacienteId);
    const profissional = this.profissionais().find((item) => item.id === consulta.profissionalId);
    return {
      hora: consulta.dataHora.slice(11, 16),
      paciente: paciente?.nome ?? '—',
      telefone: paciente?.telefone || '—',
      profissional: profissional?.nome ?? (this.perfil() === 'admin' || this.perfil() === 'secretaria' ? '—' : this.auth.obterNome() ?? '—'),
      status: this.converterStatus(consulta.status),
    };
  }));

  private converterStatus(status: StatusConsultaApi): StatusConsulta {
    const conversao: Record<StatusConsultaApi, StatusConsulta> = {
      PENDENTE: 'pendente', CONFIRMADA: 'confirmado', REMARCADA: 'remarcado', CANCELADA: 'cancelado',
    };
    return conversao[status] ?? 'pendente';
  }
}
