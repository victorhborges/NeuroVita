import { Injectable, signal } from '@angular/core';

import { deISO, diaDaSemana, paraISO } from '../util';
import {
  AGENDAMENTOS_INICIAIS, DOCUMENTOS_INICIAIS, PACIENTES_INICIAIS, PROFISSIONAIS_INICIAIS, SECRETARIAS_INICIAIS,
} from './dados-clinica';
import {
  Agendamento, Consulta, DadosPaciente, DadosProfissional, DadosSecretaria, Documento, Paciente, Profissional,
  Secao, Secretaria, TipoAtendimento,
} from './modelos';
import { minutosDe } from './status';

export type NovoPaciente = Pick<Paciente, 'nome' | 'nascimento' | 'telefone' | 'email' | 'endereco'>;
export type DadosDocumento = Omit<Documento, 'id' | 'atualizadoEm'> & { id?: string };

const gerarId = (prefixo: string) => `${prefixo}${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`;

/**
 * Guarda pacientes, profissionais, consultas e documentos EM MEMÓRIA (somem ao recarregar a página).
 * Psicólogo e secretária usam os mesmos dados: o que um cadastra aparece para o outro.
 * TODO (back-end): trocar o conteúdo dos métodos por chamadas HttpClient. Os componentes
 * não precisam mudar, desde que os métodos devolvam os mesmos tipos.
 */
@Injectable({ providedIn: 'root' })
export class Clinica {
  private readonly _pacientes = signal<Paciente[]>(PACIENTES_INICIAIS);
  private readonly _profissionais = signal<Profissional[]>(PROFISSIONAIS_INICIAIS);
  private readonly _secretarias = signal<Secretaria[]>(SECRETARIAS_INICIAIS);
  private readonly _agendamentos = signal<Agendamento[]>(AGENDAMENTOS_INICIAIS);
  private readonly _documentos = signal<Documento[]>(DOCUMENTOS_INICIAIS);

  readonly pacientes = this._pacientes.asReadonly();
  readonly profissionais = this._profissionais.asReadonly();
  readonly secretarias = this._secretarias.asReadonly();
  readonly agendamentos = this._agendamentos.asReadonly();
  readonly documentos = this._documentos.asReadonly();

  /* ---------- Pacientes ---------- */

  paciente(id: string | undefined | null): Paciente | undefined {
    return this._pacientes().find((p) => p.id === id);
  }

  /** Cria (sem `id`) ou atualiza (com `id`) um paciente a partir do formulário de cadastro. */
  salvarPaciente(dados: DadosPaciente, id?: string): Paciente {
    const lista = this._pacientes();
    const atual = id ? lista.find((p) => p.id === id) : undefined;
    const status = !dados.ativo ? 'inativo' : atual && atual.status !== 'inativo' ? atual.status : 'acompanhamento';
    const situacao = status === 'inativo' ? 'Inativo' : status === 'aguardando' ? 'Aguardando retorno' : 'Em andamento';
    const r = dados.responsavel;

    if (atual) {
      const editado: Paciente = {
        ...atual, nome: dados.nome, nascimento: dados.nascimento, email: dados.email, cpf: dados.cpf, telefone: dados.telefone,
        endereco: dados.endereco ?? atual.endereco, status, situacao, plano: dados.plano,
        responsavel: { ...atual.responsavel, nome: r.nome, nascimento: r.nascimento, email: r.email, relacao: r.relacao, telefone: r.telefone, cpf: r.cpf },
      };
      this._pacientes.update((l) => l.map((p) => (p.id === atual.id ? editado : p)));
      return editado;
    }

    const maior = Math.max(0, ...lista.map((p) => Number(p.codigo) || 0));
    const codigo = String(maior + 1).padStart(5, '0');
    const base = dados.nome
      .normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
      .replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
    const novo: Paciente = {
      id: lista.some((p) => p.id === base) ? `${base}-${codigo}` : base, codigo,
      nome: dados.nome, nascimento: dados.nascimento, telefone: dados.telefone, email: dados.email, cpf: dados.cpf,
      endereco: dados.endereco || '—', status, situacao, ultimaConsulta: null, profissionalId: null,
      responsavel: { nome: r.nome || '—', relacao: r.relacao || '—', telefone: r.telefone || '—', endereco: dados.endereco || '—', email: r.email, nascimento: r.nascimento, cpf: r.cpf },
      plano: dados.plano,
      diagnostico: { principal: '—', cid: '—', inicioTratamento: null },
      consultas: [],
    };
    this._pacientes.update((l) => [...l, novo]);
    return novo;
  }

  /** Cadastro rápido (usado na lista de pacientes do psicólogo). */
  adicionarPaciente(dados: NovoPaciente): Paciente {
    return this.salvarPaciente({
      nome: dados.nome, nascimento: dados.nascimento, email: dados.email, cpf: '', telefone: dados.telefone, endereco: dados.endereco,
      responsavel: { nome: '', nascimento: '', email: '', relacao: '', telefone: '', cpf: '' },
      plano: { possui: false, nome: '', carteirinha: '', validade: '' }, ativo: true,
    });
  }

  /** Remove o paciente, o prontuário e as consultas dele. */
  removerPaciente(id: string): void {
    this._pacientes.update((l) => l.filter((p) => p.id !== id));
    this._documentos.update((l) => l.filter((d) => d.pacienteId !== id));
    this._agendamentos.update((l) => l.filter((a) => a.pacienteId !== id));
  }

  adicionarConsulta(pacienteId: string, consulta: Consulta): void {
    this._pacientes.update((l) =>
      l.map((p) => (p.id === pacienteId ? { ...p, consultas: [consulta, ...p.consultas] } : p)),
    );
  }

  /* ---------- Profissionais ---------- */

  profissional(id: string | undefined | null): Profissional | undefined {
    return this._profissionais().find((p) => p.id === id);
  }

  /** Cria (sem `id`) ou atualiza (com `id`) um profissional a partir do formulário de cadastro. */
  salvarProfissional(dados: DadosProfissional, id?: string): Profissional {
    const lista = this._profissionais();
    const atual = id ? lista.find((p) => p.id === id) : undefined;
    const prefixo = dados.tipo === 'psiquiatra' ? 'CRM' : 'CRP';
    const mesmaCategoria = !!atual && atual.tipo === dados.tipo;
    const campos = {
      nome: dados.nome, nascimento: dados.nascimento, email: dados.email, cpf: dados.cpf, telefone: dados.telefone,
      tipo: dados.tipo, registro: `${prefixo} ${dados.registro}`, anoConclusao: dados.anoConclusao,
      disponibilidade: dados.disponibilidade, ativo: dados.ativo, emailAcesso: dados.emailAcesso,
      // O tratamento (Dr./Dra.) e a especialidade só são mantidos se a categoria não mudou.
      tratamento: mesmaCategoria ? atual.tratamento : ('' as const),
      especialidade: mesmaCategoria ? atual.especialidade : dados.tipo === 'psiquiatra' ? 'Psiquiatra' : 'Psicólogo',
    };
    if (atual) {
      const editado: Profissional = { ...atual, ...campos };
      this._profissionais.update((l) => l.map((p) => (p.id === atual.id ? editado : p)));
      return editado;
    }
    const maior = Math.max(0, ...lista.map((p) => Number(p.codigo) || 0));
    const codigo = String(maior + 1).padStart(5, '0');
    const base = dados.nome
      .normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
      .replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
    const novo: Profissional = { id: lista.some((p) => p.id === base) ? `${base}-${codigo}` : base, codigo, ...campos };
    this._profissionais.update((l) => [...l, novo]);
    return novo;
  }

  alterarAtivoProfissional(id: string, ativo: boolean): void {
    this._profissionais.update((l) => l.map((p) => (p.id === id ? { ...p, ativo } : p)));
  }

  /** Quantas consultas (não canceladas) o profissional tem na agenda. */
  consultasDoProfissional(id: string): number {
    return this._agendamentos().filter((a) => a.profissionalId === id && a.status !== 'cancelado').length;
  }

  /** Remove o profissional. Só use quando `consultasDoProfissional` for 0. */
  removerProfissional(id: string): void {
    this._profissionais.update((l) => l.filter((p) => p.id !== id));
    this._agendamentos.update((l) => l.filter((a) => a.profissionalId !== id));
    this._pacientes.update((l) => l.map((p) => (p.profissionalId === id ? { ...p, profissionalId: null } : p)));
  }

  /**
   * O profissional atende nesse dia e horário? Considera os dias da semana, as faixas de
   * horário e se ele atende online e/ou presencial.
   */
  disponivel(profissionalId: string, data: string, inicio: string, duracao: number, atendimento: TipoAtendimento): boolean {
    const p = this.profissional(profissionalId);
    if (!p) return false;
    const d = p.disponibilidade;
    if (atendimento === 'Online' ? !d.online : !d.presencial) return false;
    if (!d.dias.includes(diaDaSemana(data))) return false;
    const ini = minutosDe(inicio);
    return d.faixas.some((f) => ini >= minutosDe(f.inicio) && ini + duracao <= minutosDe(f.fim));
  }

  /* ---------- Secretárias ---------- */

  secretaria(id: string | undefined | null): Secretaria | undefined {
    return this._secretarias().find((s) => s.id === id);
  }

  salvarSecretaria(dados: DadosSecretaria, id?: string): Secretaria {
    const lista = this._secretarias();
    const atual = id ? lista.find((s) => s.id === id) : undefined;
    if (atual) {
      const editada: Secretaria = { ...atual, ...dados };
      this._secretarias.update((l) => l.map((s) => (s.id === atual.id ? editada : s)));
      return editada;
    }
    const maior = Math.max(0, ...lista.map((s) => Number(s.codigo) || 0));
    const codigo = String(maior + 1).padStart(5, '0');
    const base = dados.nome
      .normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
      .replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
    const nova: Secretaria = { ...dados, id: lista.some((s) => s.id === base) ? `${base}-${codigo}` : base, codigo };
    this._secretarias.update((l) => [...l, nova]);
    return nova;
  }

  removerSecretaria(id: string): void {
    this._secretarias.update((l) => l.filter((s) => s.id !== id));
  }

  /** Esse e-mail de acesso já pertence a outro profissional ou secretária? */
  emailDeAcessoEmUso(email: string, ignorarId?: string): boolean {
    const alvo = email.trim().toLowerCase();
    return (
      this._profissionais().some((p) => p.id !== ignorarId && p.emailAcesso.toLowerCase() === alvo) ||
      this._secretarias().some((s) => s.id !== ignorarId && s.emailAcesso.toLowerCase() === alvo)
    );
  }

  /* ---------- Agenda ---------- */

  agendamento(id: string | undefined | null): Agendamento | undefined {
    return this._agendamentos().find((a) => a.id === id);
  }

  /**
   * O horário está livre? Considera consultas não canceladas do mesmo profissional
   * (e do mesmo paciente, quando informado).
   */
  horarioLivre(data: string, inicio: string, duracao: number, profissionalId: string, pacienteId?: string, ignorarId?: string): boolean {
    const ini = minutosDe(inicio);
    const fim = ini + duracao;
    return !this._agendamentos().some((a) => {
      if (a.id === ignorarId || a.data !== data || a.status === 'cancelado') return false;
      const conflita = ini < minutosDe(a.inicio) + a.duracao && minutosDe(a.inicio) < fim;
      return conflita && (a.profissionalId === profissionalId || (!!pacienteId && a.pacienteId === pacienteId));
    });
  }

  /** Primeira sala livre no horário (ou 'Sala 01'). */
  salaLivre(data: string, inicio: string, duracao: number, salas: string[]): string {
    const ini = minutosDe(inicio);
    const fim = ini + duracao;
    const ocupadas = new Set(
      this._agendamentos()
        .filter((a) => a.data === data && a.status !== 'cancelado' && ini < minutosDe(a.inicio) + a.duracao && minutosDe(a.inicio) < fim)
        .map((a) => a.sala),
    );
    return salas.find((s) => !ocupadas.has(s)) ?? salas[0];
  }

  adicionarAgendamento(dados: Omit<Agendamento, 'id'>): Agendamento {
    const novo: Agendamento = { ...dados, id: gerarId('ag-') };
    this._agendamentos.update((l) => [...l, novo]);
    // O primeiro profissional que atende o paciente passa a ser o responsável.
    const p = this.paciente(dados.pacienteId);
    if (p && !p.profissionalId) {
      this._pacientes.update((l) => l.map((x) => (x.id === p.id ? { ...x, profissionalId: dados.profissionalId } : x)));
    }
    return novo;
  }

  atualizarAgendamento(atualizado: Agendamento): void {
    this._agendamentos.update((l) => l.map((a) => (a.id === atualizado.id ? atualizado : a)));
  }

  /* ---------- Documentos do prontuário ---------- */

  /** Documentos de um paciente em uma seção, do mais novo para o mais antigo. */
  documentosDe(pacienteId: string | undefined, secao: Secao | undefined): Documento[] {
    return this._documentos()
      .filter((d) => d.pacienteId === pacienteId && d.secao === secao)
      .sort((a, b) => b.dataSessao.localeCompare(a.dataSessao) || b.atualizadoEm.localeCompare(a.atualizadoEm));
  }

  salvarDocumento(dados: DadosDocumento): Documento {
    const completo: Documento = {
      ...dados,
      id: dados.id ?? gerarId('d'),
      atualizadoEm: new Date().toISOString(),
    };
    this._documentos.update((l) =>
      dados.id ? l.map((d) => (d.id === dados.id ? completo : d)) : [...l, completo],
    );
    if (completo.secao === 'evolucoes') this.atualizarUltimaConsulta(completo.pacienteId, completo.dataSessao);
    return completo;
  }

  private atualizarUltimaConsulta(pacienteId: string, data: string): void {
    this._pacientes.update((l) =>
      l.map((p) =>
        p.id === pacienteId && (!p.ultimaConsulta || deISO(data) > deISO(p.ultimaConsulta))
          ? { ...p, ultimaConsulta: data }
          : p,
      ),
    );
  }
}

export const hojeISO = () => paraISO(new Date());
