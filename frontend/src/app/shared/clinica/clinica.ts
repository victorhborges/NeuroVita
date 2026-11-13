import { Injectable, inject, signal } from '@angular/core';
import { catchError, firstValueFrom, forkJoin, of, Subject, takeUntil } from 'rxjs';

import { diaDaSemana, paraISO } from '../util';
import {
  Agendamento, Consulta, DadosPaciente, DadosProfissional, DadosSecretaria, Documento, Paciente, Profissional,
  Secao, Secretaria, StatusConsulta, TipoAtendimento,
} from './modelos';
import { minutosDe } from './status';
import { DocumentosApiService, DocumentoClinicoApi } from './documentos-api.service';
import { Auth } from '../auth';
import { ConsultasApiService, ConsultaApi } from './consultas-api.service';
import { PacientesApiService, PacienteApi } from './pacientes-api.service';
import { ProfissionaisApiService, ProfissionalApi } from './profissionais-api.service';
import { ResponsaveisApiService, ResponsavelApi } from './responsaveis-api.service';
import { SecretariasApiService } from './secretarias-api.service';
import { DisponibilidadesApiService } from './disponibilidades-api.service';
import { DiagnosticosApiService } from './diagnosticos-api.service';
import { Aviso } from '../aviso/aviso';
import { EvolucaoApi, EvolucoesApiService } from './evolucoes-api.service';
import { PrescricaoApi, PrescricoesApiService } from './prescricoes-api.service';
import { ExameApi, ExamesApiService } from './exames-api.service';
import { VinculoProfissionalPacienteApi, VinculosProfissionaisPacientesApiService } from './vinculos-profissionais-pacientes-api.service';

export type NovoPaciente = Pick<Paciente, 'nome' | 'nascimento' | 'telefone' | 'email' | 'endereco'>;
export type DadosDocumento = Omit<Documento, 'id' | 'atualizadoEm'> & { id?: string };


/**
 * Fachada de domínio que carrega os dados clínicos da API e mantém sinais para as telas.
 */
@Injectable({ providedIn: 'root' })
export class Clinica {
  private readonly _pacientes = signal<Paciente[]>([]);
  private readonly _profissionais = signal<Profissional[]>([]);
  private readonly _secretarias = signal<Secretaria[]>([]);
  private readonly _agendamentos = signal<Agendamento[]>([]);
  private readonly _documentos = signal<Documento[]>([]);
  private readonly _vinculosPorPaciente = signal<Record<string, VinculoProfissionalPacienteApi[]>>({});
  private readonly cancelarLeiturasDaSessao = new Subject<void>();
  private readonly documentosApi = inject(DocumentosApiService);
  private readonly auth = inject(Auth);
  private readonly pacientesApi = inject(PacientesApiService);
  private readonly profissionaisApi = inject(ProfissionaisApiService);
  private readonly responsaveisApi = inject(ResponsaveisApiService);
  private readonly secretariasApi = inject(SecretariasApiService);
  private readonly consultasApi = inject(ConsultasApiService);
  private readonly disponibilidadesApi = inject(DisponibilidadesApiService);
  private readonly diagnosticosApi = inject(DiagnosticosApiService);
  private readonly aviso = inject(Aviso);
  private readonly evolucoesApi = inject(EvolucoesApiService);
  private readonly prescricoesApi = inject(PrescricoesApiService);
  private readonly examesApi = inject(ExamesApiService);
  private readonly vinculosApi = inject(VinculosProfissionaisPacientesApiService);

  readonly pacientes = this._pacientes.asReadonly();
  readonly profissionais = this._profissionais.asReadonly();
  readonly secretarias = this._secretarias.asReadonly();
  readonly agendamentos = this._agendamentos.asReadonly();
  readonly documentos = this._documentos.asReadonly();
  readonly vinculosPorPaciente = this._vinculosPorPaciente.asReadonly();

  constructor() {
    this.auth.sessaoAlterada$.subscribe((autenticado) => {
      this.cancelarLeiturasDaSessao.next();
      this.limparDadosDaSessao();
      if (autenticado) this.carregarDadosDaApi();
    });

    if (this.auth.obterToken()) this.carregarDadosDaApi();
  }

  private limparDadosDaSessao(): void {
    this._pacientes.set([]);
    this._profissionais.set([]);
    this._secretarias.set([]);
    this._agendamentos.set([]);
    this._documentos.set([]);
    this._vinculosPorPaciente.set({});
  }

  private carregarDadosDaApi(): void {
    const perfil = this.auth.obterPerfil();
    const podeListarProfissionais = perfil === 'admin' || perfil === 'secretaria';
    const podeListarSecretarias = perfil === 'admin';
    const podeListarResponsaveis = perfil === 'admin' || perfil === 'secretaria';
    forkJoin({
      pacientes: this.pacientesApi.listarTodos().pipe(catchError(() => of(null))),
      profissionais: podeListarProfissionais
        ? this.profissionaisApi.listarTodos().pipe(catchError(() => of(null)))
        : of([] as ProfissionalApi[]),
      responsaveis: podeListarResponsaveis
        ? this.responsaveisApi.listarTodos().pipe(catchError(() => of(null)))
        : of([] as ResponsavelApi[]),
      secretarias: podeListarSecretarias
        ? this.secretariasApi.listarTodos().pipe(catchError(() => of(null)))
        : of([]),
      consultas: this.consultasApi.listarTodas().pipe(catchError(() => of(null))),
    }).pipe(takeUntil(this.cancelarLeiturasDaSessao)).subscribe(({ pacientes, profissionais, responsaveis, secretarias, consultas }) => {
      if (!pacientes || !profissionais || !responsaveis || !secretarias || !consultas) {
        this.aviso.mostrar('Não foi possível atualizar todos os dados clínicos. Os dados já carregados foram preservados.');
        return;
      }
      const profissionaisMap = new Map(profissionais.map((p) => [p.id, p]));
      this._agendamentos.set(consultas.map((c) => this.agendamentoDaApi(c)));
      this._pacientes.set(pacientes.map((p) => this.pacienteDaApi(p, responsaveis, consultas)));
      if (perfil === 'psicologo' || perfil === 'psiquiatra') {
        this.carregarResponsaveisVinculados(pacientes);
      }
      const disponibilidadeAnteriorPorId = new Map(
        this._profissionais().map((p) => [p.id, p.disponibilidade]),
      );
      this._profissionais.set(profissionais.map((p) => {
        const profissional = this.profissionalDaApi(p);
        return {
          ...profissional,
          disponibilidade: disponibilidadeAnteriorPorId.get(p.id)
            ?? profissional.disponibilidade,
        };
      }));
      profissionais.forEach((p) => this.carregarDisponibilidade(p.id));
      this._secretarias.set(secretarias.map((s) => ({
        id: s.id, codigo: s.id.slice(-5).toUpperCase(), nome: s.nome, nascimento: s.dataNascimento ?? '', telefone: s.telefone ?? '', email: s.emailContato ?? s.email,
        cpf: s.cpf ?? '', endereco: s.endereco ?? '', cargo: s.cargo ?? 'Secretária', admissao: s.admissao ?? '', ativo: s.ativo ?? true, emailAcesso: s.email,
      })));
      // Associa consultas aos profissionais para que a interface use nomes vindos da API.
      this._agendamentos.update((lista) => lista.map((a) => ({ ...a, local: a.local || profissionaisMap.get(a.profissionalId)?.nome || '' })));
    });
  }

  private carregarResponsaveisVinculados(pacientes: PacienteApi[]): void {
    const ids = [...new Set(pacientes.flatMap((paciente) =>
      (paciente.responsaveis ?? []).map((vinculo) => vinculo.responsavelId),
    ).filter(Boolean))];
    if (!ids.length) return;

    forkJoin(ids.map((id) => this.responsaveisApi.buscarPorId(id).pipe(
      catchError(() => of(null)),
    ))).pipe(takeUntil(this.cancelarLeiturasDaSessao)).subscribe((respostas) => {
      const porId = new Map(respostas.filter((item): item is ResponsavelApi => item !== null)
        .map((item) => [item.id, item]));
      if (porId.size !== ids.length) {
        this.aviso.mostrar('Alguns dados de responsáveis não puderam ser carregados.');
      }

      this._pacientes.update((atuais) => atuais.map((paciente) => {
        const contatos = paciente.responsaveis.map((vinculo) => {
          const responsavel = vinculo.id ? porId.get(vinculo.id) : undefined;
          return responsavel ? {
            ...vinculo,
            nome: responsavel.nome,
            telefone: responsavel.telefone ?? '',
            endereco: responsavel.endereco ?? '',
            email: responsavel.email,
            nascimento: responsavel.dataNascimento,
            cpf: responsavel.cpf,
          } : vinculo;
        });
        const principal = contatos[0];
        return {
          ...paciente,
          responsaveis: contatos,
          responsavel: principal ? {
            ...paciente.responsavel,
            nome: principal.nome,
            relacao: principal.relacao,
            telefone: principal.telefone,
            endereco: principal.endereco,
            email: principal.email,
            nascimento: principal.nascimento,
            cpf: principal.cpf,
          } : paciente.responsavel,
        };
      }));
    });
  }

  private pacienteDaApi(p: PacienteApi, responsaveis: ResponsavelApi[], consultas: ConsultaApi[]): Paciente {
    const vinculosAtivos = (p.profissionais ?? []).filter((vinculo) => vinculo.ativo);
    const profissionalAtual = vinculosAtivos.length === 1 ? vinculosAtivos[0].profissionalId : null;
    const vinculo = p.responsaveis?.[0];
    const contatos = (p.responsaveis ?? []).map((link) => {
      const contato = responsaveis.find((item) => item.id === link.responsavelId);
      return {
        id: link.responsavelId, nome: contato?.nome ?? '', relacao: link.grauParentesco,
        telefone: contato?.telefone ?? '', endereco: contato?.endereco ?? '',
        email: contato?.email, nascimento: contato?.dataNascimento, cpf: contato?.cpf,
      };
    });
    const r = responsaveis.find((item) => item.id === vinculo?.responsavelId);
    const historico = consultas.filter((c) => c.pacienteId === p.id).sort((a, b) => b.dataHora.localeCompare(a.dataHora));
    const ultima = historico[0];
    return {
      id: p.id, codigo: p.id.slice(-5).toUpperCase(), nome: p.nome, nascimento: p.dataNascimento,
      telefone: p.telefone ?? '', email: p.email ?? '', cpf: p.cpf ?? '', endereco: p.endereco ?? '',
      status: p.ativo === false ? 'inativo' : 'acompanhamento', situacao: p.ativo === false ? 'Inativo' : 'Em acompanhamento', ultimaConsulta: ultima?.dataHora.slice(0, 10) ?? null,
      profissionalId: profissionalAtual,
      responsavelId: vinculo?.responsavelId,
      responsavel: { nome: r?.nome ?? '', relacao: vinculo?.grauParentesco ?? '', telefone: r?.telefone ?? '', endereco: r?.endereco ?? '', email: r?.email, nascimento: r?.dataNascimento, cpf: r?.cpf },
      responsaveis: contatos,
      plano: { possui: !!p.possuiPlanoSaude, nome: p.planoSaude ?? '', carteirinha: p.numeroCarteirinha ?? '', validade: p.validadePlano ?? '' },
      diagnostico: { principal: '—', cid: '—', inicioTratamento: null }, consultas: historico.map((c) => ({
        data: c.dataHora.slice(0, 10), profissional: c.profissionalId, resumo: c.tipoAtendimento,
      })),
    };
  }

  private profissionalDaApi(p: ProfissionalApi): Profissional {
    const tipo = /psiquiat/i.test(p.categoria) ? 'psiquiatra' : 'psicologo';
    return {
      id: p.id, codigo: p.id.slice(-5).toUpperCase(), nome: p.nome, tratamento: '', especialidade: p.categoria,
      tipo, registro: p.numeroRegistro ?? '', nascimento: p.dataNascimento ?? '', telefone: p.telefone ?? '', ativo: !!p.ativo,
      cpf: p.cpf ?? '', email: p.emailContato ?? p.email ?? '', anoConclusao: p.anoConclusao,
      disponibilidade: { dias: [], faixas: [], online: !!p.atendeOnline, presencial: p.atendePresencial !== false }, emailAcesso: p.email,
    };
  }

  private carregarDisponibilidade(profissionalId: string): void {
    this.profissionaisApi.listarDisponibilidades(profissionalId).pipe(
      takeUntil(this.cancelarLeiturasDaSessao),
    ).subscribe({
      next: (itens) => {
        const diasOrdem = ['SEGUNDA_FEIRA', 'TERCA_FEIRA', 'QUARTA_FEIRA', 'QUINTA_FEIRA', 'SEXTA_FEIRA', 'SABADO'];
        const normalizar = (v: string) => v.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase();
        const faixas = itens.filter((x) => x.ativo).map((x) => ({ inicio: x.horaInicio.slice(0, 5), fim: x.horaFim.slice(0, 5) }));
        const dias = [...new Set(itens.filter((x) => x.ativo).map((x) => diasOrdem.indexOf(normalizar(x.diaSemana))).filter((x) => x >= 0))];
        this._profissionais.update((lista) => lista.map((p) => p.id === profissionalId
          ? { ...p, disponibilidade: { ...p.disponibilidade, dias, faixas } } : p));
      },
      error: () => this.aviso.mostrar('Não foi possível carregar as disponibilidades do profissional.'),
    });
  }

  private async sincronizarDisponibilidade(profissionalId: string, disponibilidade: DadosProfissional['disponibilidade']): Promise<void> {
    const atuais = await firstValueFrom(this.profissionaisApi.listarDisponibilidades(profissionalId));
    const nomesDias = ['SEGUNDA_FEIRA', 'TERCA_FEIRA', 'QUARTA_FEIRA', 'QUINTA_FEIRA', 'SEXTA_FEIRA', 'SABADO'];

    const idsAnteriores = new Set(atuais.map((item) => item.id));
    const reconciliarVinculosAnteriores = async () => {
      const vinculadasAgora = await firstValueFrom(this.profissionaisApi.listarDisponibilidades(profissionalId));
      const idsAgora = new Set(vinculadasAgora.map((item) => item.id));
      for (const id of idsAgora) {
        if (!idsAnteriores.has(id)) await firstValueFrom(this.profissionaisApi.removerDisponibilidade(profissionalId, id));
      }
      for (const id of idsAnteriores) {
        if (!idsAgora.has(id)) await firstValueFrom(this.profissionaisApi.adicionarDisponibilidade(profissionalId, id));
      }
      const confirmacao = await firstValueFrom(this.profissionaisApi.listarDisponibilidades(profissionalId));
      const idsConfirmados = new Set(confirmacao.map((item) => item.id));
      if (idsConfirmados.size !== idsAnteriores.size || [...idsAnteriores].some((id) => !idsConfirmados.has(id))) {
        throw new Error('Não foi possível restaurar as disponibilidades anteriores.');
      }
    };

    try {
      for (const dia of disponibilidade.dias) {
        for (const faixa of disponibilidade.faixas) {
          const nova = await firstValueFrom(this.disponibilidadesApi.criar({
            diaSemana: nomesDias[dia],
            horaInicio: `${faixa.inicio}:00`,
            horaFim: `${faixa.fim}:00`,
            ativo: true,
          }));
          await firstValueFrom(this.profissionaisApi.adicionarDisponibilidade(profissionalId, nova.id));
        }
      }
    } catch (erro) {
      try { await reconciliarVinculosAnteriores(); } catch (falhaReconciliacao) {
        throw new Error('A sincronização falhou e a reconciliação automática não foi confirmada.', { cause: falhaReconciliacao });
      }
      throw erro;
    }

    try {
      for (const item of atuais) {
        await firstValueFrom(this.profissionaisApi.removerDisponibilidade(profissionalId, item.id));
      }
    } catch (erro) {
      try { await reconciliarVinculosAnteriores(); } catch (falhaReconciliacao) {
        throw new Error('A sincronização falhou e a reconciliação automática não foi confirmada.', { cause: falhaReconciliacao });
      }
      throw erro;
    }
  }

  private agendamentoDaApi(c: ConsultaApi): Agendamento {
    const status: Record<string, StatusConsulta> = { PENDENTE: 'pendente', CONFIRMADA: 'confirmado', REMARCADA: 'remarcado', CANCELADA: 'cancelado' };
    return {
      id: c.id, data: c.dataHora.slice(0, 10), inicio: c.dataHora.slice(11, 16), duracao: c.duracao,
      pacienteId: c.pacienteId, profissionalId: c.profissionalId, tipo: c.tipoAtendimento,
      atendimento: c.modalidade === 'ONLINE' ? 'Online' : 'Presencial', sala: c.local, local: c.local,
      status: status[c.status] ?? 'pendente',
    };
  }

  carregarDocumentos(pacienteId: string): void {
    this.documentosApi.listarPorPaciente(pacienteId).pipe(
      takeUntil(this.cancelarLeiturasDaSessao),
    ).subscribe({
      next: (itens) => this._documentos.update((atuais) => [
        ...atuais.filter((d) => d.pacienteId !== pacienteId || d.origem !== 'documento-clinico'),
        ...itens.map((item) => this.documentoDaApi(item)),
      ]),
      error: () => this.aviso.mostrar('Não foi possível carregar os documentos clínicos.'),
    });

    const perfil = this.auth.obterPerfil();
    if (perfil !== 'admin' && perfil !== 'psicologo' && perfil !== 'psiquiatra') return;

    this.evolucoesApi.listarPorPaciente(pacienteId).pipe(
      takeUntil(this.cancelarLeiturasDaSessao),
    ).subscribe({
      next: (itens) => this.substituirOrigemDocumento(
        pacienteId, 'evolucao', itens.map((item) => this.evolucaoComoDocumento(item)),
      ),
      error: () => this.aviso.mostrar('Não foi possível carregar as evoluções clínicas.'),
    });
    this.prescricoesApi.listarPorPaciente(pacienteId).pipe(
      takeUntil(this.cancelarLeiturasDaSessao),
    ).subscribe({
      next: (itens) => this.substituirOrigemDocumento(
        pacienteId, 'prescricao', itens.map((item) => this.prescricaoComoDocumento(item)),
      ),
      error: () => this.aviso.mostrar('Não foi possível carregar as prescrições.'),
    });
    this.examesApi.listarPorPaciente(pacienteId).pipe(
      takeUntil(this.cancelarLeiturasDaSessao),
    ).subscribe({
      next: (itens) => this.substituirOrigemDocumento(
        pacienteId, 'exame', itens.map((item) => this.exameComoDocumento(item)),
      ),
      error: () => this.aviso.mostrar('Não foi possível carregar os exames.'),
    });
    this.vinculosApi.listarPorPaciente(pacienteId).pipe(
      takeUntil(this.cancelarLeiturasDaSessao),
    ).subscribe({
      next: (itens) => this._vinculosPorPaciente.update((atuais) => ({
        ...atuais,
        [pacienteId]: itens,
      })),
      error: () => this.aviso.mostrar('Não foi possível carregar os vínculos profissionais do paciente.'),
    });
  }

  private substituirOrigemDocumento(
    pacienteId: string,
    origem: NonNullable<Documento['origem']>,
    documentos: Documento[],
  ): void {
    this._documentos.update((atuais) => [
      ...atuais.filter((documento) =>
        documento.pacienteId !== pacienteId || documento.origem !== origem),
      ...documentos,
    ]);
  }

  private evolucaoComoDocumento(item: EvolucaoApi): Documento {
    return {
      id: `evolucao:${item.id}`,
      origem: 'evolucao',
      pacienteId: item.pacienteId,
      profissionalId: item.profissionalId,
      secao: 'evolucoes',
      subtitulo: item.tipoEvolucao,
      tipoEvolucao: item.tipoEvolucao,
      titulo: item.titulo,
      dataSessao: item.dataAtendimento.slice(0, 10),
      tipoAtendimento: null,
      conteudo: item.conteudo,
      atualizadoEm: item.atualizadoEm || item.criadoEm,
      anexos: [],
    };
  }

  private prescricaoComoDocumento(item: PrescricaoApi): Documento {
    return {
      id: `prescricao:${item.id}`,
      origem: 'prescricao',
      pacienteId: item.pacienteId,
      profissionalId: item.profissionalId,
      secao: 'medicamentos',
      subtitulo: 'Prescrição',
      titulo: 'Prescrição',
      dataSessao: item.criadaEm.slice(0, 10),
      tipoAtendimento: null,
      conteudo: item.conteudo,
      atualizadoEm: item.atualizadaEm,
      anexos: [],
      medicamentos: item.medicamentos ?? [],
    };
  }

  private exameComoDocumento(item: ExameApi): Documento {
    const anexos = (item.anexosResultado ?? []).map((anexo) => ({
      nome: anexo.nome,
      url: `data:${anexo.tipoMime};base64,${anexo.conteudoBase64}`,
    }));
    return {
      id: `exame:${item.id}`,
      origem: 'exame',
      pacienteId: item.pacienteId,
      profissionalId: item.profissionalId,
      secao: 'exames',
      subtitulo: item.tipo || (item.ativo ? 'Solicitação de exame' : 'Exame arquivado'),
      tipoExame: item.tipo,
      titulo: item.nome,
      dataSessao: item.dataRealizacao || item.solicitadoEm.slice(0, 10),
      dataRealizacao: item.dataRealizacao,
      tipoAtendimento: null,
      conteudo: item.descricaoSolicitacao ?? '',
      atualizadoEm: item.resultadoAtualizadoEm || item.criadoEm,
      anexos,
      arquivo: anexos.length === 1 ? anexos[0] : undefined,
      resultado: item.resultado,
      observacoes: item.observacoes,
      ativo: item.ativo,
    };
  }

  carregarDiagnosticos(pacienteId: string): void {
    this.diagnosticosApi.listarPorPaciente(pacienteId).pipe(
      takeUntil(this.cancelarLeiturasDaSessao),
    ).subscribe({
      next: (itens) => this._pacientes.update((lista) => lista.map((p) => {
        if (p.id !== pacienteId) return p;
        const ativos = itens.filter((diagnostico) => diagnostico.ativo !== false);
        const principal = ativos[0];
        return {
          ...p,
          diagnostico: {
            ...p.diagnostico,
            principal: principal?.descricao || '—',
            cid: principal?.cid || '—',
          },
        };
      })),
      error: () => this.aviso.mostrar('Não foi possível carregar os diagnósticos.'),
    });
  }

  /* ---------- Pacientes ---------- */

  paciente(id: string | undefined | null): Paciente | undefined {
    return this._pacientes().find((p) => p.id === id);
  }

  /** Cria (sem `id`) ou atualiza (com `id`) um paciente a partir do formulário de cadastro. */
  async salvarPaciente(dados: DadosPaciente, id?: string): Promise<Paciente> {
    const lista = this._pacientes();
    const atual = id ? lista.find((p) => p.id === id) : undefined;
    const status = !dados.ativo ? 'inativo' : atual && atual.status !== 'inativo' ? atual.status : 'acompanhamento';
    const situacao = status === 'inativo' ? 'Inativo' : status === 'aguardando' ? 'Aguardando retorno' : 'Em andamento';
    const r = dados.responsavel;
    const informados = dados.responsaveis?.length
      ? dados.responsaveis
      : (r.nome.trim() ? [{ ...r, id: atual?.responsavelId }] : []);
    const informadosValidos = informados.filter((contato) => contato.nome.trim());
    const responsaveisSalvos: ResponsavelApi[] = [];
    const responsaveisCriadosNestaOperacao: string[] = [];

    if (atual) {
      const editado: Paciente = {
        ...atual, nome: dados.nome, nascimento: dados.nascimento, email: dados.email, cpf: dados.cpf, telefone: dados.telefone,
        endereco: dados.endereco ?? atual.endereco, status, situacao, plano: dados.plano,
        responsavel: { ...atual.responsavel, nome: r.nome, nascimento: r.nascimento, email: r.email, relacao: r.relacao, telefone: r.telefone, cpf: r.cpf },
      };
      try {
        for (const contato of informadosValidos) {
          const request = {
            nome: contato.nome.trim(), cpf: contato.cpf || '', email: contato.email || '',
            telefone: contato.telefone || '', dataNascimento: contato.nascimento || undefined,
            endereco: contato.endereco || dados.endereco || undefined,
          };
          const salvo = contato.id
            ? await firstValueFrom(this.responsaveisApi.atualizar(contato.id, request))
            : await firstValueFrom(this.responsaveisApi.criar(request));
          if (!contato.id) responsaveisCriadosNestaOperacao.push(salvo.id);
          responsaveisSalvos.push(salvo);
        }
      } catch (erro) {
        await this.limparResponsaveisCriados(responsaveisCriadosNestaOperacao);
        throw erro;
      }
      const vinculos = responsaveisSalvos.map((contato, index) => ({
        responsavelId: contato.id,
        grauParentesco: informadosValidos[index]?.relacao?.trim() || 'Responsável',
      }));
      let resposta: PacienteApi;
      try {
        resposta = await firstValueFrom(this.pacientesApi.atualizar(id!, this.pacienteRequest(dados, vinculos)));
      } catch (erro) {
        await this.limparResponsaveisCriados(responsaveisCriadosNestaOperacao);
        throw erro;
      }
      const pacienteReal = this.pacienteDaApi(resposta, responsaveisSalvos, []);
      this._pacientes.update((l) => l.map((p) => p.id === pacienteReal.id ? pacienteReal : p));
      return pacienteReal;
    }

    try {
      for (const contato of informadosValidos) {
        const salvo = await firstValueFrom(this.responsaveisApi.criar({
          nome: contato.nome.trim(), cpf: contato.cpf || '', email: contato.email || '',
          telefone: contato.telefone || '', dataNascimento: contato.nascimento || undefined,
          endereco: contato.endereco || dados.endereco || undefined,
        }));
        responsaveisCriadosNestaOperacao.push(salvo.id);
        responsaveisSalvos.push(salvo);
      }
    } catch (erro) {
      await this.limparResponsaveisCriados(responsaveisCriadosNestaOperacao);
      throw erro;
    }
    const vinculos = responsaveisSalvos.map((contato, index) => ({
      responsavelId: contato.id,
      grauParentesco: informadosValidos[index]?.relacao?.trim() || 'Responsável',
    }));
    let cadastro: { paciente: PacienteApi; senhaProvisoria: string };
    try {
      cadastro = await firstValueFrom(this.pacientesApi.criar(this.pacienteRequest(dados, vinculos)));
    } catch (erro) {
      await this.limparResponsaveisCriados(responsaveisCriadosNestaOperacao);
      throw erro;
    }
    const novo = this.pacienteDaApi(cadastro.paciente, responsaveisSalvos, []);
    this._pacientes.update((l) => [...l.filter((p) => p.id !== novo.id), novo]);
    return novo;
  }

  private async limparResponsaveisCriados(ids: string[]): Promise<void> {
    for (const id of ids) {
      try {
        await firstValueFrom(this.responsaveisApi.excluir(id));
      } catch (erro) {
        // A API impede excluir responsáveis que já estejam vinculados.
        console.error('Falha na compensação de um responsável recém-criado:', erro);
      }
    }
  }

  private pacienteRequest(dados: DadosPaciente, responsaveis: { responsavelId: string; grauParentesco: string }[]) {
    return {
      nome: dados.nome, dataNascimento: dados.nascimento, email: dados.email || '', cpf: dados.cpf || '', telefone: dados.telefone || '', endereco: dados.endereco || '', ativo: dados.ativo,
      responsaveis,
      possuiPlanoSaude: dados.plano.possui, planoSaude: dados.plano.nome, numeroCarteirinha: dados.plano.carteirinha,
      validadePlano: dados.plano.validade || null,
    };
  }

  /** Cadastro rápido (usado na lista de pacientes do psicólogo). */
  adicionarPaciente(dados: NovoPaciente): Promise<Paciente> {
    return this.salvarPaciente({
      nome: dados.nome, nascimento: dados.nascimento, email: dados.email, cpf: '', telefone: dados.telefone, endereco: dados.endereco,
      responsavel: { nome: '', nascimento: '', email: '', relacao: '', telefone: '', cpf: '', endereco: '' },
      plano: { possui: false, nome: '', carteirinha: '', validade: '' }, ativo: true,
    });
  }

  /** Remove o paciente, o prontuário e as consultas dele. */
  async removerPaciente(id: string): Promise<void> {
    await firstValueFrom(this.pacientesApi.excluir(id));
    this._pacientes.update((l) => l.map((p) => p.id === id
      ? { ...p, status: 'inativo', situacao: 'Inativo' }
      : p));
  }

  /* ---------- Profissionais ---------- */

  profissional(id: string | undefined | null): Profissional | undefined {
    return this._profissionais().find((p) => p.id === id);
  }

  /** Cria (sem `id`) ou atualiza (com `id`) um profissional a partir do formulário de cadastro. */
  async salvarProfissional(dados: DadosProfissional, id?: string): Promise<Profissional & { senhaProvisoria?: string | null; emailBoasVindasEnviado?: boolean }> {
    if (id) {
      const salvo = await firstValueFrom(this.profissionaisApi.atualizar(id!, {
        nome: dados.nome, email: dados.emailAcesso, emailContato: dados.email, dataNascimento: dados.nascimento, cpf: dados.cpf, telefone: dados.telefone,
        categoria: dados.tipo === 'psiquiatra' ? 'PSIQUIATRA' : 'PSICOLOGO', formacaoAcademica: '',
        numeroRegistro: dados.registro, anoConclusao: dados.anoConclusao, atendeOnline: dados.disponibilidade.online, atendePresencial: dados.disponibilidade.presencial, ativo: dados.ativo, senhaProvisoria: dados.senhaProvisoria,
      }));
      await this.sincronizarDisponibilidade(id!, dados.disponibilidade);
      const atualizado = { ...this.profissionalDaApi(salvo), disponibilidade: dados.disponibilidade };
      this._profissionais.update((l) => l.map((p) => p.id === id ? atualizado : p));
      return atualizado;
    }
    const cadastro = await firstValueFrom(this.profissionaisApi.criar({
      nome: dados.nome, email: dados.emailAcesso, emailContato: dados.email, dataNascimento: dados.nascimento, cpf: dados.cpf, telefone: dados.telefone,
      categoria: dados.tipo === 'psiquiatra' ? 'PSIQUIATRA' : 'PSICOLOGO', formacaoAcademica: '',
      numeroRegistro: dados.registro, anoConclusao: dados.anoConclusao, atendeOnline: dados.disponibilidade.online, atendePresencial: dados.disponibilidade.presencial, ativo: dados.ativo, senhaProvisoria: dados.senhaProvisoria, enviarEmailBoasVindas: dados.enviarEmailBoasVindas,
    }));
    await this.sincronizarDisponibilidade(cadastro.profissional.id, dados.disponibilidade);
    const novo: Profissional & { senhaProvisoria: string | null; emailBoasVindasEnviado: boolean } = { ...this.profissionalDaApi(cadastro.profissional), disponibilidade: dados.disponibilidade, senhaProvisoria: cadastro.senhaProvisoria, emailBoasVindasEnviado: cadastro.emailBoasVindasEnviado };
    this._profissionais.update((l) => [...l, novo]);
    return novo;
  }

  async alterarAtivoProfissional(id: string, ativo: boolean): Promise<void> {
    const p = this.profissional(id);
    if (!p) return;
    await firstValueFrom(this.profissionaisApi.atualizar(id, {
      nome: p.nome, email: p.emailAcesso, emailContato: p.email, dataNascimento: p.nascimento, cpf: p.cpf, telefone: p.telefone,
      categoria: p.tipo === 'psiquiatra' ? 'PSIQUIATRA' : 'PSICOLOGO', formacaoAcademica: '',
      numeroRegistro: p.registro.replace(/^(CRM|CRP)\s*/, ''), anoConclusao: p.anoConclusao,
      atendeOnline: p.disponibilidade.online, atendePresencial: p.disponibilidade.presencial, ativo,
    }));
    this._profissionais.update((l) => l.map((p) => (p.id === id ? { ...p, ativo } : p)));
  }

  /** Quantas consultas (não canceladas) o profissional tem na agenda. */
  consultasDoProfissional(id: string): number {
    return this._agendamentos().filter((a) => a.profissionalId === id && a.status !== 'cancelado').length;
  }

  /** Remove o profissional. Só use quando `consultasDoProfissional` for 0. */
  async removerProfissional(id: string): Promise<void> {
    await firstValueFrom(this.profissionaisApi.excluir(id));
    this._profissionais.update((l) => l.map((p) => p.id === id ? { ...p, ativo: false } : p));
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

  async salvarSecretaria(dados: DadosSecretaria, id?: string): Promise<Secretaria & { senhaProvisoria?: string | null; emailBoasVindasEnviado?: boolean }> {
    const request = {
      nome: dados.nome, email: dados.emailAcesso || dados.email, emailContato: dados.email,
      dataNascimento: dados.nascimento || undefined, cpf: dados.cpf, telefone: dados.telefone,
      endereco: dados.endereco, cargo: dados.cargo, admissao: dados.admissao || undefined, ativo: dados.ativo,
      senhaProvisoria: dados.senhaProvisoria,
    };
    if (id) {
      const salva = await firstValueFrom(this.secretariasApi.atualizar(id, request));
      const editada = { ...dados, id, codigo: id.slice(-5).toUpperCase(), email: salva.emailContato ?? salva.email, emailAcesso: salva.email };
      this._secretarias.update((l) => l.map((s) => s.id === id ? editada : s));
      return editada;
    }
    const cadastro = await firstValueFrom(this.secretariasApi.criar({
      ...request,
      enviarEmailBoasVindas: dados.enviarEmailBoasVindas,
    }));
    const nova: Secretaria & { senhaProvisoria: string | null; emailBoasVindasEnviado: boolean } = {
      ...dados, id: cadastro.secretaria.id, codigo: cadastro.secretaria.id.slice(-5).toUpperCase(),
      email: cadastro.secretaria.emailContato ?? cadastro.secretaria.email, emailAcesso: cadastro.secretaria.email,
      senhaProvisoria: cadastro.senhaProvisoria,
      emailBoasVindasEnviado: cadastro.emailBoasVindasEnviado,
    };
    this._secretarias.update((l) => [...l, nova]);
    return nova;
  }

  async removerSecretaria(id: string): Promise<void> {
    await firstValueFrom(this.secretariasApi.excluir(id));
    this._secretarias.update((l) => l.map((s) => s.id === id ? { ...s, ativo: false } : s));
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

  async adicionarAgendamento(dados: Omit<Agendamento, 'id'>): Promise<Agendamento> {
    const salvo = await firstValueFrom(this.consultasApi.criar({
      pacienteId: dados.pacienteId, profissionalId: dados.profissionalId,
      dataHora: `${dados.data}T${dados.inicio}:00`, tipoAtendimento: dados.tipo,
      local: dados.local, modalidade: dados.atendimento.toUpperCase(), duracao: dados.duracao,
    }));
    const novo = this.agendamentoDaApi(salvo);
    this._agendamentos.update((l) => [...l, novo]);
    return novo;
  }

  async atualizarAgendamento(atualizado: Agendamento): Promise<void> {
    const payload = {
      pacienteId: atualizado.pacienteId, profissionalId: atualizado.profissionalId,
      dataHora: `${atualizado.data}T${atualizado.inicio}:00`, tipoAtendimento: atualizado.tipo,
      local: atualizado.local, modalidade: atualizado.atendimento.toUpperCase(), duracao: atualizado.duracao,
    };
    if (atualizado.status === 'cancelado') await firstValueFrom(this.consultasApi.cancelar(atualizado.id));
    else {
      await firstValueFrom(this.consultasApi.atualizar(atualizado.id, payload));
      if (atualizado.status === 'confirmado') await firstValueFrom(this.consultasApi.confirmar(atualizado.id));
    }
    this._agendamentos.update((l) => l.map((a) => a.id === atualizado.id ? atualizado : a));
  }

  /* ---------- Documentos do prontuário ---------- */

  /** Documentos de um paciente em uma seção, do mais novo para o mais antigo. */
  documentosDe(pacienteId: string | undefined, secao: Secao | undefined): Documento[] {
    return this._documentos()
      .filter((d) => d.pacienteId === pacienteId && d.secao === secao)
      .sort((a, b) => b.dataSessao.localeCompare(a.dataSessao) || b.atualizadoEm.localeCompare(a.atualizadoEm));
  }

  async salvarDocumento(dados: DadosDocumento): Promise<Documento> {
    const completo: Documento = {
      ...dados,
      id: dados.id ?? '',
      atualizadoEm: new Date().toISOString(),
    };

    if (completo.secao === 'evolucoes'
      && (dados.origem === 'evolucao'
        || (!dados.id && dados.origem !== 'documento-clinico'))) {
      if (dados.id && dados.origem === 'evolucao') {
        throw new Error('Evoluções registradas são imutáveis. Registre uma nova evolução para acrescentar informações.');
      }
      if (completo.anexos.length) {
        throw new Error('A API de evoluções não aceita anexos. Remova os arquivos antes de registrar a evolução.');
      }
      const perfil = this.auth.obterPerfil();
      const request = {
        pacienteId: completo.pacienteId,
        ...(perfil === 'admin'
          ? { profissionalId: await this.profissionalUnicoDoPaciente(completo.pacienteId) }
          : {}),
        tipoEvolucao: 'SESSAO',
        areaAtendimento: perfil === 'psiquiatra' ? 'Psiquiatria' : 'Psicologia',
        titulo: completo.titulo,
        dataAtendimento: `${completo.dataSessao}T00:00:00`,
        conteudo: completo.conteudo,
      };
      const salvo = await firstValueFrom(this.evolucoesApi.criar(request));
      const documento = this.evolucaoComoDocumento(salvo);
      this.substituirOrigemDocumento(completo.pacienteId, 'evolucao', [
        ...this._documentos().filter((item) =>
          item.pacienteId === completo.pacienteId && item.origem === 'evolucao'),
        documento,
      ]);
      return documento;
    }

    if (completo.secao === 'medicamentos'
      && (dados.origem === 'prescricao'
        || (!dados.id && dados.origem !== 'documento-clinico'))) {
      if (completo.anexos.length) {
        throw new Error('A API de prescrições não aceita anexos. Remova os arquivos antes de salvar.');
      }
      const perfil = this.auth.obterPerfil();
      const id = dados.id?.startsWith('prescricao:')
        ? dados.id.slice('prescricao:'.length)
        : undefined;
      const request = {
        pacienteId: completo.pacienteId,
        ...(perfil === 'admin'
          ? { profissionalId: dados.profissionalId
            ?? await this.profissionalUnicoDoPaciente(completo.pacienteId) }
          : {}),
        conteudo: completo.conteudo,
        ...(dados.origem === 'prescricao' && dados.medicamentos
          ? { medicamentos: dados.medicamentos }
          : {}),
      };
      const salvo = await firstValueFrom(id
        ? this.prescricoesApi.atualizar(id, request)
        : this.prescricoesApi.criar(request));
      const documento = this.prescricaoComoDocumento(salvo);
      this.substituirOrigemDocumento(completo.pacienteId, 'prescricao', [
        ...this._documentos().filter((item) =>
          item.pacienteId === completo.pacienteId && item.origem === 'prescricao'
          && item.id !== documento.id),
        documento,
      ]);
      return documento;
    }

    if (completo.secao === 'exames'
      && dados.origem !== 'documento-clinico'
      && (!dados.id || dados.origem === 'exame')) {
      const id = dados.id?.startsWith('exame:')
        ? dados.id.slice('exame:'.length)
        : undefined;
      const anexosExistentes = id
        ? this._documentos().find((documento) => documento.id === dados.id)?.anexos ?? []
        : [];
      const alterouAnexos = completo.anexos.length !== anexosExistentes.length
        || completo.anexos.some((anexo, indice) => anexo.url !== anexosExistentes[indice]?.url);
      if (alterouAnexos) {
        throw new Error('Arquivos enviados pela seção do prontuário são documentos clínicos; não podem ser registrados como resultado de exame.');
      }
      const profissionalId = dados.profissionalId
        ?? await this.profissionalUnicoDoPaciente(completo.pacienteId);
      const request = {
        pacienteId: completo.pacienteId,
        profissionalId,
        nome: completo.titulo,
        ...(completo.tipoExame ? { tipo: completo.tipoExame } : {}),
        descricaoSolicitacao: completo.conteudo,
      };
      let salvo: ExameApi;
      if (id) {
        salvo = await firstValueFrom(this.examesApi.atualizar(id, request));
      } else {
        salvo = await firstValueFrom(this.examesApi.criar(request));
      }
      const documento = this.exameComoDocumento(salvo);
      this.substituirOrigemDocumento(completo.pacienteId, 'exame', [
        ...this._documentos().filter((item) =>
          item.pacienteId === completo.pacienteId && item.origem === 'exame'
          && item.id !== documento.id),
        documento,
      ]);
      return documento;
    }

    const anexos = await Promise.all(completo.anexos.map(async (anexo) => {
      const resposta = await fetch(anexo.url);
      const blob = await resposta.blob();
      const bytes = new Uint8Array(await blob.arrayBuffer());
      let binario = '';
      bytes.forEach((byte) => binario += String.fromCharCode(byte));
      return { nome: anexo.nome, conteudoBase64: btoa(binario), tipoMime: blob.type || 'application/octet-stream' };
    }));
    const request = {
      pacienteId: completo.pacienteId, secao: completo.secao, titulo: completo.titulo,
      dataSessao: completo.dataSessao, tipoAtendimento: completo.tipoAtendimento ?? 'Presencial',
      conteudo: completo.conteudo || `Arquivo anexado: ${completo.titulo}`, anexos,
    };
    const salvo = dados.id
      ? await firstValueFrom(this.documentosApi.atualizar(dados.id, request))
      : await firstValueFrom(this.documentosApi.criar(request));
    const documento = this.documentoDaApi(salvo);
    this._documentos.update((l) => dados.id ? l.map((d) => d.id === documento.id ? documento : d) : [...l, documento]);
    return documento;
  }

  private documentoDaApi(item: DocumentoClinicoApi): Documento {
    const anexos = (item.anexos ?? []).map((a) => ({ nome: a.nome, url: `data:${a.tipoMime};base64,${a.conteudoBase64}` }));
    return {
      id: item.id, origem: 'documento-clinico', pacienteId: item.pacienteId, profissionalId: item.profissionalId,
      secao: item.secao as Secao, titulo: item.titulo,
      dataSessao: item.dataSessao, tipoAtendimento: item.tipoAtendimento as TipoAtendimento,
      conteudo: item.conteudo, atualizadoEm: item.atualizadoEm,
      anexos, arquivo: item.conteudo.startsWith('Arquivo anexado:') ? anexos[0] : undefined,
    };
  }

  private async profissionalUnicoDoPaciente(pacienteId: string): Promise<string> {
    const vinculos = await firstValueFrom(this.vinculosApi.listarPorPaciente(pacienteId));
    this._vinculosPorPaciente.update((atuais) => ({ ...atuais, [pacienteId]: vinculos }));
    const profissionaisAtivos = [...new Set(
      vinculos.filter((vinculo) => vinculo.ativo).map((vinculo) => vinculo.profissionalId),
    )];
    if (profissionaisAtivos.length !== 1) {
      throw new Error('Não foi possível identificar um único profissional ativo para este paciente.');
    }
    return profissionaisAtivos[0];
  }

}

export const hojeISO = () => paraISO(new Date());
