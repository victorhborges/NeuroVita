import { Component, ElementRef, computed, effect, inject, input, signal, viewChild } from '@angular/core';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Agendamento, StatusConsulta } from '../../shared/clinica/modelos';
import { nomeProfissional } from '../../shared/clinica/rotulos';
import { HORARIOS_AGENDA, HORAS_AGENDA, ROTULO_STATUS, SALAS, minutosDe } from '../../shared/clinica/status';
import { Avatar } from '../../shared/avatar/avatar';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { ListaSuspensa, OpcaoLista } from '../../shared/lista-suspensa/lista-suspensa';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { deISO, diaDaSemana, formatarData, formatarDiaMes, idadeDe, normalizar, paraISO, somarDias } from '../../shared/util';

const NOMES_DIAS = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const DIA_MS = 24 * 60 * 60 * 1000;
const DIAS_EXTENSO = ['Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado', 'Domingo'];
const dm = (iso: string) => formatarDiaMes(iso);
const deMinutos = (m: number) => `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`;

const HOJE_REAL = paraISO(new Date());
const SEGUNDA_BASE = (() => {
  const d = deISO(HOJE_REAL);
  const diff = (d.getDay() + 6) % 7;
  return somarDias(HOJE_REAL, -diff);
})();
const HOJE_DEMO = HOJE_REAL;
const AGORA_DEMO = `${String(new Date().getHours()).padStart(2, '0')}:${String(new Date().getMinutes()).padStart(2, '0')}`;

/** Quantas semanas depois (ou antes) da semana inicial está uma data. */
function semanaDe(iso: string): number {
  const dias = Math.round((deISO(iso).getTime() - deISO(SEGUNDA_BASE).getTime()) / DIA_MS);
  return Math.floor(dias / 7);
}

/**
 * Agenda semanal: grade de horários, resumo do dia, próximo atendimento e paciente selecionado.
 * - Psicólogo: troca o status por um menu.
 * - Secretária: abre a janela "Editar consulta" (confirmar, remarcar ou cancelar).
 */
@Component({
  selector: 'app-agenda',
  imports: [Avatar, Dialogo, Icone, ListaSuspensa, TituloPagina],
  templateUrl: './agenda.html',
  host: { '(document:click)': 'fecharMenu($event)', '(document:keydown.escape)': 'fecharEdicao()' },
})
export class Agenda {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);

  /** Vem do `data` da rota. */
  readonly perfil = input<Perfil>('psicologo');
  /** Vem do endereço (?consulta=ID): seleciona essa consulta ao abrir. */
  readonly consulta = input<string>();

  protected readonly horas = HORAS_AGENDA;
  protected readonly colunas = [0, 1, 2, 3, 4, 5];
  protected readonly statusLista = computed<StatusConsulta[]>(() => {
    const status = this.escolhido()?.a.status;
    if (status === 'pendente' || status === 'remarcado') return ['confirmado', 'cancelado'];
    if (status === 'confirmado') return ['cancelado'];
    return [];
  });
  protected readonly rotulo = ROTULO_STATUS;
  protected readonly salas = SALAS;

  protected readonly semana = signal(0);
  protected readonly busca = signal('');
  protected readonly selecionadoId = signal('');
  protected readonly menuStatus = signal(false);

  // Janela "Editar consulta"
  protected readonly editando = signal(false);
  protected readonly cancelando = signal(false);
  protected readonly editData = signal('');
  protected readonly editHora = signal('');
  protected readonly editProf = signal('');
  protected readonly editSala = signal('');
  /** Calendário aberto no campo "Data" e o mês que ele mostra. */
  protected readonly calAberto = signal(false);
  protected readonly calMes = signal(new Date(2026, 0, 1));
  protected readonly diasSemanaCal = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

  private readonly seletorData = viewChild<ElementRef<HTMLInputElement>>('seletorData');
  private readonly menuRaiz = viewChild<ElementRef<HTMLElement>>('menuStatusRaiz');

  /** Secretária e administrador usam a janela "Editar consulta". */
  protected readonly secretaria = computed(() => this.perfil() === 'secretaria' || this.perfil() === 'admin');
  protected readonly hojeISO = HOJE_DEMO;
  protected readonly dataHoje = formatarData(HOJE_DEMO);

  constructor() {
    effect(() => {
      const consultaId = this.consulta();
      const agendamentos = this.clinica.agendamentos();
      if (consultaId) {
        const alvo = agendamentos.find((agendamento) => agendamento.id === consultaId);
        if (!alvo) return;
        this.selecionadoId.set(alvo.id);
        this.semana.set(semanaDe(alvo.data));
        return;
      }
      const diasSemana = this.dias();
      const dentroDaSemana = (data: string) => data >= diasSemana[0].iso && data <= diasSemana[5].iso;
      const selecionado = agendamentos.find((agendamento) =>
        agendamento.id === this.selecionadoId() && dentroDaSemana(agendamento.data));
      if (selecionado) return;
      const primeiro = agendamentos
        .filter((agendamento) => dentroDaSemana(agendamento.data))
        .sort((a, b) => minutosDe(a.inicio) - minutosDe(b.inicio))[0];
      this.selecionadoId.set(primeiro?.id ?? '');
    });
  }

  /** Os 6 dias (seg a sáb) da semana mostrada. */
  protected readonly dias = computed(() =>
    NOMES_DIAS.map((nome, i) => {
      const iso = somarDias(SEGUNDA_BASE, this.semana() * 7 + i);
      return { nome, iso, texto: dm(iso), hoje: iso === HOJE_DEMO, dataCompleta: formatarData(iso) };
    }),
  );

  protected readonly faixa = computed(() => `${this.dias()[0].dataCompleta} - ${this.dias()[5].dataCompleta}`);

  /** Consultas da semana mostrada, já com a posição na grade. */
  protected readonly eventos = computed(() => {
    const termo = normalizar(this.busca());
    const dias = this.dias();
    const primeiro = dias[0].iso;
    const ultimo = dias[5].iso;
    return this.clinica
      .agendamentos()
      .filter((a) => a.data >= primeiro && a.data <= ultimo)
      .map((a) => {
        const paciente = this.clinica.paciente(a.pacienteId);
        const prof = this.clinica.profissional(a.profissionalId);
        return {
          ...a,
          nome: paciente?.nome ?? '—',
          profNome: prof ? nomeProfissional(prof) : '—',
          dia: diaDaSemana(a.data),
          fim: deMinutos(minutosDe(a.inicio) + a.duracao),
          // Cada consulta fica no quadrado da hora em que começa (uma consulta de 08:00 fica na linha das 08:00).
          linhaDaHora: Math.floor((minutosDe(a.inicio) - 8 * 60) / 60),
          destaque: !termo || normalizar(paciente?.nome ?? '').includes(termo),
          selecionado: a.id === this.selecionadoId(),
        };
      });
  });

  /** A grade: para cada hora (linha) e cada dia (coluna), as consultas que começam ali. */
  protected readonly grade = computed(() => {
    const lista = this.eventos();
    return this.horas.map((_, linha) =>
      this.colunas.map((coluna) =>
        lista
          .filter((e) => e.dia === coluna && e.linhaDaHora === linha)
          .sort((x, y) => minutosDe(x.inicio) - minutosDe(y.inicio)),
      ),
    );
  });

  private readonly hoje = computed(() => this.clinica.agendamentos().filter((a) => a.data === HOJE_DEMO));

  protected readonly resumo = computed(() => {
    const lista = this.hoje();
    const conta = (st: StatusConsulta) => lista.filter((a) => a.status === st).length;
    return [
      { classe: 'confirmado', rotulo: 'Confirmados', valor: conta('confirmado') },
      { classe: 'pendente', rotulo: 'A confirmar', valor: conta('pendente') },
      { classe: 'cancelado', rotulo: 'Cancelados', valor: conta('cancelado') },
      { classe: 'remarcado', rotulo: 'Remarcado', valor: conta('remarcado') },
    ];
  });

  protected readonly proximo = computed(() =>
    this.hoje()
      .filter((a) => a.status !== 'cancelado' && minutosDe(a.inicio) > minutosDe(AGORA_DEMO))
      .sort((x, y) => minutosDe(x.inicio) - minutosDe(y.inicio))[0],
  );

  protected readonly escolhido = computed(() => {
    const a = this.clinica.agendamento(this.selecionadoId());
    if (!a) return undefined;
    const paciente = this.clinica.paciente(a.pacienteId);
    const prof = this.clinica.profissional(a.profissionalId);
    const relativo = a.data === HOJE_DEMO ? 'Hoje' : a.data === somarDias(HOJE_DEMO, 1) ? 'Amanhã' : DIAS_EXTENSO[diaDaSemana(a.data)];
    return {
      a, relativo, dataBR: formatarData(a.data),
      nome: paciente?.nome ?? '—',
      idade: paciente ? idadeDe(paciente.nascimento) : 0,
      diagnostico: (paciente?.diagnostico.principal ?? '').split('(')[0].trim(),
      telefone: paciente?.responsavel.telefone ?? paciente?.telefone ?? '—',
      email: paciente?.responsavel.email || paciente?.email || '—',
      profissional: prof ? nomeProfissional(prof) : '—',
    };
  });

  /* ---------- Navegação ---------- */

  protected selecionar(id: string): void {
    this.selecionadoId.set(id);
    this.menuStatus.set(false);
  }

  protected irPara(delta: number): void {
    this.semana.update((s) => s + delta);
  }

  protected irParaHoje(): void {
    this.semana.set(0);
  }

  protected abrirCalendario(): void {
    const campo = this.seletorData()?.nativeElement;
    if (!campo) return;
    try {
      campo.showPicker();
    } catch {
      campo.focus();
    }
  }

  protected escolherData(evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value;
    if (valor) this.semana.set(semanaDe(paraISO(deISO(valor))));
  }

  protected fecharMenu(evento: Event): void {
    if (this.menuStatus() && !this.menuRaiz()?.nativeElement.contains(evento.target as Node)) this.menuStatus.set(false);
  }

  /* ---------- Psicólogo: trocar o status ---------- */

  protected trocarStatus(status: StatusConsulta): void {
    const a = this.escolhido()?.a;
    if (a) this.clinica.atualizarAgendamento({ ...a, status });
    this.menuStatus.set(false);
  }

  /* ---------- Secretária: janela "Editar consulta" ---------- */

  protected abrirEdicao(): void {
    const e = this.escolhido();
    if (!e) return;
    this.editData.set(e.a.data);
    this.editHora.set(e.a.inicio);
    this.editProf.set(e.a.profissionalId);
    this.editSala.set(e.a.sala);
    const d = deISO(e.a.data);
    this.calMes.set(new Date(d.getFullYear(), d.getMonth(), 1));
    this.calAberto.set(false);
    this.editando.set(true);
  }

  protected fecharEdicao(): void {
    this.editando.set(false);
    this.calAberto.set(false);
  }

  /* Calendário suspenso do campo "Data" */
  protected readonly calTitulo = computed(() => {
    const m = this.calMes().toLocaleDateString('pt-BR', { month: 'long' });
    return m.charAt(0).toUpperCase() + m.slice(1);
  });

  protected readonly calSemanas = computed(() => {
    const ano = this.calMes().getFullYear();
    const mes = this.calMes().getMonth();
    const vazios = new Date(ano, mes, 1).getDay();
    const total = new Date(ano, mes + 1, 0).getDate();
    const celulas: ({ numero: number; iso: string } | null)[] = [
      ...Array.from({ length: vazios }, () => null),
      ...Array.from({ length: total }, (_, i) => ({ numero: i + 1, iso: paraISO(new Date(ano, mes, i + 1)) })),
    ];
    return celulas;
  });

  protected mudarMesCal(delta: number): void {
    const m = this.calMes();
    this.calMes.set(new Date(m.getFullYear(), m.getMonth() + delta, 1));
  }

  protected escolherDiaCal(iso: string): void {
    this.editData.set(iso);
    this.calAberto.set(false);
  }

  protected readonly editDataTexto = computed(() => (this.editData() ? formatarData(this.editData()) : 'DD/MM/AAAA'));

  /** Opções das listas da janela (mesmas regras de antes, agora como listas suspensas). */
  protected readonly listaHorarios = computed<OpcaoLista[]>(() => this.opcoesHorario().map((h) => ({ valor: h, rotulo: h })));
  protected readonly listaProfissionais = computed<OpcaoLista[]>(() => this.opcoesProfissionais().map((p) => ({ valor: p.valor, rotulo: p.rotulo })));
  protected readonly listaSalas = computed<OpcaoLista[]>(() => {
    const todas = SALAS.includes(this.editSala()) || !this.editSala() ? SALAS : [...SALAS, this.editSala()];
    return todas.map((s) => ({ valor: s, rotulo: s }));
  });

  protected readonly opcoesProfissionais = computed(() =>
    this.clinica.profissionais().filter((p) => p.ativo || p.id === this.escolhido()?.a.profissionalId)
      .map((p) => ({ valor: p.id, rotulo: nomeProfissional(p) })),
  );

  /** Horários em que o profissional está livre na data escolhida (mais o horário atual da consulta). */
  protected readonly opcoesHorario = computed(() => {
    const e = this.escolhido();
    if (!e) return [];
    return HORARIOS_AGENDA.filter(
      (h) =>
        h === this.editHora() ||
        (this.clinica.disponivel(this.editProf(), this.editData(), h, e.a.duracao, e.a.atendimento) &&
          this.clinica.horarioLivre(this.editData(), h, e.a.duracao, this.editProf(), e.a.pacienteId, e.a.id)),
    );
  });

  private podeSalvar(): boolean {
    const e = this.escolhido();
    if (!e) return false;
    if (!this.editData() || !this.editHora() || !this.editProf()) {
      this.aviso.mostrar('Preencha data, horário e profissional.');
      return false;
    }
    if (!this.clinica.horarioLivre(this.editData(), this.editHora(), e.a.duracao, this.editProf(), e.a.pacienteId, e.a.id)) {
      this.aviso.mostrar('Esse horário já está ocupado para o profissional ou para o paciente.');
      return false;
    }
    return true;
  }

  private async aplicar(status: StatusConsulta, mensagem: string): Promise<void> {
    const e = this.escolhido();
    if (!e) return;
    const atualizado: Agendamento = {
      ...e.a, data: this.editData(), inicio: this.editHora(), profissionalId: this.editProf(), sala: this.editSala(), status,
    };
    try {
      await this.clinica.atualizarAgendamento(atualizado);
      this.semana.set(semanaDe(atualizado.data));
      this.fecharEdicao();
      this.aviso.mostrar(mensagem);
    } catch (erro) {
      console.error('Falha ao atualizar consulta:', erro);
      this.aviso.mostrar('Não foi possível atualizar a consulta. Verifique os dados e tente novamente.');
    }
  }

  protected async confirmarConsulta(): Promise<void> {
    if (this.podeSalvar()) await this.aplicar('confirmado', 'Consulta confirmada.');
  }

  protected async remarcarConsulta(): Promise<void> {
    const e = this.escolhido();
    if (!e || !this.podeSalvar()) return;
    const mudou = this.editData() !== e.a.data || this.editHora() !== e.a.inicio || this.editProf() !== e.a.profissionalId;
    if (!mudou) {
      this.aviso.mostrar('Altere a data, o horário ou o profissional para remarcar.');
      return;
    }
    await this.aplicar('remarcado', 'Consulta remarcada.');
  }

  protected async confirmarCancelamento(): Promise<void> {
    const e = this.escolhido();
    if (!e) return;
    try {
      await this.clinica.atualizarAgendamento({ ...e.a, status: 'cancelado' });
      this.cancelando.set(false);
      this.fecharEdicao();
      this.aviso.mostrar('Consulta cancelada.');
    } catch (erro) {
      console.error('Falha ao cancelar consulta:', erro);
      this.aviso.mostrar('Não foi possível cancelar a consulta.');
    }
  }
}
