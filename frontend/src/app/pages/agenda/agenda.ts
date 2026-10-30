import { Component, ElementRef, computed, effect, inject, input, signal, viewChild } from '@angular/core';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Agendamento, StatusConsulta } from '../../shared/clinica/modelos';
import { nomeProfissional } from '../../shared/clinica/rotulos';
import { HORARIOS_AGENDA, ORDEM_STATUS, ROTULO_STATUS, SALAS, minutosDe } from '../../shared/clinica/status';
import { Avatar } from '../../shared/avatar/avatar';
import { AGORA_DEMO, HOJE_DEMO, SEGUNDA_BASE } from '../../shared/dados-agenda';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { deISO, diaDaSemana, formatarData, idadeDe, normalizar, paraISO, somarDias } from '../../shared/util';

const NOMES_DIAS = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const DIA_MS = 24 * 60 * 60 * 1000;
const DIAS_EXTENSO = ['Segunda-feira', 'Terça-feira', 'Quarta-feira', 'Quinta-feira', 'Sexta-feira', 'Sábado', 'Domingo'];
const dm = (iso: string) => formatarData(iso).slice(0, 5);

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
  imports: [Avatar, Dialogo, Icone, TituloPagina],
  templateUrl: './agenda.html',
  host: { '(document:click)': 'fecharMenu($event)', '(document:keydown.escape)': 'editando.set(false)' },
})
export class Agenda {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);

  /** Vem do `data` da rota. */
  readonly perfil = input<Perfil>('psicologo');
  /** Vem do endereço (?consulta=ID): seleciona essa consulta ao abrir. */
  readonly consulta = input<string>();

  protected readonly horarios = HORARIOS_AGENDA;
  protected readonly linhas = HORARIOS_AGENDA.map((_, i) => i);
  protected readonly colunas = [0, 1, 2, 3, 4, 5];
  protected readonly statusLista = ORDEM_STATUS;
  protected readonly rotulo = ROTULO_STATUS;
  protected readonly salas = SALAS;

  protected readonly semana = signal(0);
  protected readonly busca = signal('');
  protected readonly selecionadoId = signal('ag-1');
  protected readonly menuStatus = signal(false);

  // Janela "Editar consulta"
  protected readonly editando = signal(false);
  protected readonly cancelando = signal(false);
  protected readonly editData = signal('');
  protected readonly editHora = signal('');
  protected readonly editProf = signal('');
  protected readonly editSala = signal('');

  private readonly seletorData = viewChild<ElementRef<HTMLInputElement>>('seletorData');
  private readonly menuRaiz = viewChild<ElementRef<HTMLElement>>('menuStatusRaiz');

  /** Secretária e administrador usam a janela "Editar consulta". */
  protected readonly secretaria = computed(() => this.perfil() === 'secretaria' || this.perfil() === 'admin');
  protected readonly hojeISO = HOJE_DEMO;
  protected readonly dataHoje = formatarData(HOJE_DEMO);

  constructor() {
    effect(() => {
      const alvo = this.clinica.agendamento(this.consulta());
      if (!alvo) return;
      this.selecionadoId.set(alvo.id);
      this.semana.set(semanaDe(alvo.data));
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
          linha: (minutosDe(a.inicio) - 8 * 60) / 30 + 2,
          trechos: Math.max(1, a.duracao / 30),
          destaque: !termo || normalizar(paciente?.nome ?? '').includes(termo),
          selecionado: a.id === this.selecionadoId(),
        };
      });
  });

  private readonly hoje = computed(() => this.clinica.agendamentos().filter((a) => a.data === HOJE_DEMO));

  protected readonly resumo = computed(() => {
    const lista = this.hoje();
    const conta = (s: StatusConsulta) => lista.filter((a) => a.status === s).length;
    const ocupados = lista.filter((a) => a.status !== 'cancelado').reduce((t, a) => t + a.duracao / 30, 0);
    const itens = [
      { classe: 'confirmado', rotulo: 'Confirmados', valor: conta('confirmado') },
      { classe: 'pendente', rotulo: 'A confirmar', valor: conta('pendente') },
      { classe: 'vago', rotulo: 'Horário vago', valor: HORARIOS_AGENDA.length - ocupados },
      { classe: 'cancelado', rotulo: 'Cancelados', valor: conta('cancelado') },
      { classe: 'remarcado', rotulo: 'Remarcado', valor: conta('remarcado') },
    ];
    // A secretária não vê "Horário vago".
    return this.secretaria() ? itens.filter((i) => i.classe !== 'vago') : itens;
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
    this.editando.set(true);
  }

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

  private aplicar(status: StatusConsulta, mensagem: string): void {
    const e = this.escolhido();
    if (!e) return;
    const atualizado: Agendamento = {
      ...e.a, data: this.editData(), inicio: this.editHora(), profissionalId: this.editProf(), sala: this.editSala(), status,
    };
    this.clinica.atualizarAgendamento(atualizado);
    this.semana.set(semanaDe(atualizado.data));
    this.editando.set(false);
    this.aviso.mostrar(mensagem);
  }

  protected confirmarConsulta(): void {
    if (this.podeSalvar()) this.aplicar('confirmado', 'Consulta confirmada.');
  }

  protected remarcarConsulta(): void {
    const e = this.escolhido();
    if (!e || !this.podeSalvar()) return;
    const mudou = this.editData() !== e.a.data || this.editHora() !== e.a.inicio || this.editProf() !== e.a.profissionalId;
    if (!mudou) {
      this.aviso.mostrar('Altere a data, o horário ou o profissional para remarcar.');
      return;
    }
    this.aplicar('remarcado', 'Consulta remarcada.');
  }

  protected confirmarCancelamento(): void {
    const e = this.escolhido();
    if (!e) return;
    this.clinica.atualizarAgendamento({ ...e.a, status: 'cancelado' });
    this.cancelando.set(false);
    this.editando.set(false);
    this.aviso.mostrar('Consulta cancelada.');
  }
}
