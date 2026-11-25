import { Injectable, inject, signal } from '@angular/core';
import { catchError, firstValueFrom, forkJoin, map, of, Subject, takeUntil } from 'rxjs';

import { diaDaSemana, paraISO } from '../util';
import {
    Agendamento, Anexo, Consulta, DadosPaciente, DadosProfissional, DadosSecretaria, Documento, Paciente, Profissional,
    Secao, Secretaria, StatusConsulta, TipoAtendimento,
} from './modelos';
import { minutosDe } from './status';
import { AnexoClinicoApi, DocumentosApiService, DocumentoClinicoApi } from './documentos-api.service';
import { Auth } from '../auth';
import { ConsultasApiService, ConsultaApi } from './consultas-api.service';
import { PacientesApiService, PacienteApi } from './pacientes-api.service';
import { ProfissionaisApiService, ProfissionalApi } from './profissionais-api.service';
import { ResponsaveisApiService, ResponsavelApi } from './responsaveis-api.service';
import { SecretariaApi, SecretariasApiService } from './secretarias-api.service';
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

            if (autenticado) {
                this.carregarDadosDaApi();
            }
        });

        if (this.auth.obterToken()) {
            this.carregarDadosDaApi();
        }
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
        const ehProfissional = perfil === 'psicologo' || perfil === 'psiquiatra';
        const profissionalId = this.auth.obterEntidadeId();

        const profissionais = podeListarProfissionais
            ? this.profissionaisApi.listarTodos().pipe(catchError(() => of(null)))
            : ehProfissional && profissionalId
                ? this.profissionaisApi.buscarPorId(profissionalId).pipe(
                    map((profissional) => [profissional]),
                    catchError(() => of(null)),
                )
                : ehProfissional
                    ? of(null)
                    : of([] as ProfissionalApi[]);

        forkJoin({
            pacientes: this.pacientesApi.listarTodos().pipe(catchError(() => of(null))),
            profissionais,
            responsaveis: podeListarResponsaveis
                ? this.responsaveisApi.listarTodos().pipe(catchError(() => of(null)))
                : of([] as ResponsavelApi[]),
            secretarias: podeListarSecretarias
                ? this.secretariasApi.listarTodos().pipe(catchError(() => of(null)))
                : of([]),
            consultas: this.consultasApi.listarTodas().pipe(catchError(() => of(null))),
        }).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe(({ pacientes, profissionais, responsaveis, secretarias, consultas }) => {
            if (!pacientes || !profissionais || !responsaveis || !secretarias || !consultas) {
                this.aviso.mostrar('Não foi possível atualizar todos os dados clínicos. Os dados já carregados foram preservados.');
                return;
            }

            this._agendamentos.set(consultas.map((consulta) => this.agendamentoDaApi(consulta)));
            this._pacientes.set(pacientes.map((paciente) =>
                this.pacienteDaApi(paciente, responsaveis, consultas),
            ));

            if (ehProfissional) {
                this.carregarResponsaveisVinculados(pacientes);
            }

            const disponibilidadeAnteriorPorId = new Map(
                this._profissionais().map((profissional) => [
                    profissional.id,
                    profissional.disponibilidade,
                ]),
            );

            this._profissionais.set(profissionais.map((item) => {
                const profissional = this.profissionalDaApi(item);

                return {
                    ...profissional,
                    disponibilidade: disponibilidadeAnteriorPorId.get(item.id)
                        ?? profissional.disponibilidade,
                };
            }));

            profissionais.forEach((profissional) =>
                this.carregarDisponibilidade(profissional.id),
            );

            this._secretarias.set(secretarias.map((secretaria) =>
                this.secretariaDaApi(secretaria),
            ));
        });
    }

    private carregarResponsaveisVinculados(pacientes: PacienteApi[]): void {
        const ids = [...new Set(
            pacientes.flatMap((paciente) =>
                (paciente.responsaveis ?? []).map((vinculo) => vinculo.responsavelId),
            ).filter(Boolean),
        )];

        if (!ids.length) return;

        forkJoin(ids.map((id) =>
            this.responsaveisApi.buscarPorId(id).pipe(
                catchError(() => of(null)),
            ),
        )).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe((respostas) => {
            const porId = new Map(
                respostas
                    .filter((item): item is ResponsavelApi => item !== null)
                    .map((item) => [item.id, item]),
            );

            if (porId.size !== ids.length) {
                this.aviso.mostrar('Alguns dados de responsáveis não puderam ser carregados.');
            }

            this._pacientes.update((atuais) => atuais.map((paciente) => {
                const contatos = paciente.responsaveis.map((vinculo) => {
                    const responsavel = vinculo.id
                        ? porId.get(vinculo.id)
                        : undefined;

                    return responsavel ? {
                        ...vinculo,
                        nome: responsavel.nome,
                        telefone: responsavel.telefone ?? '',
                        endereco: responsavel.endereco ?? '',
                        ...(responsavel.email != null ? { email: responsavel.email } : {}),
                        ...(responsavel.dataNascimento != null
                            ? { nascimento: responsavel.dataNascimento }
                            : {}),
                        ...(responsavel.cpf != null ? { cpf: responsavel.cpf } : {}),
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
                        ...(principal.email != null ? { email: principal.email } : {}),
                        ...(principal.nascimento != null
                            ? { nascimento: principal.nascimento }
                            : {}),
                        ...(principal.cpf != null ? { cpf: principal.cpf } : {}),
                    } : paciente.responsavel,
                };
            }));
        });
    }

    private pacienteDaApi(
        p: PacienteApi,
        responsaveis: ResponsavelApi[],
        consultas: ConsultaApi[],
    ): Paciente {
        const vinculosAtivos = (p.profissionais ?? [])
            .filter((vinculo) => vinculo.ativo);

        const profissionalAtual = vinculosAtivos.length === 1
            ? vinculosAtivos[0].profissionalId
            : null;

        const vinculo = p.responsaveis?.[0];

        const contatos = (p.responsaveis ?? []).map((link) => {
            const contato = responsaveis.find((item) =>
                item.id === link.responsavelId,
            );

            return {
                id: link.responsavelId,
                nome: contato?.nome ?? '',
                relacao: link.grauParentesco,
                telefone: contato?.telefone ?? '',
                endereco: contato?.endereco ?? '',
                ...(contato?.email != null ? { email: contato.email } : {}),
                ...(contato?.dataNascimento != null
                    ? { nascimento: contato.dataNascimento }
                    : {}),
                ...(contato?.cpf != null ? { cpf: contato.cpf } : {}),
            };
        });

        const r = responsaveis.find((item) =>
            item.id === vinculo?.responsavelId,
        );

        const historico = consultas
            .filter((consulta) => consulta.pacienteId === p.id)
            .sort((a, b) => b.dataHora.localeCompare(a.dataHora));

        const ultima = historico[0];

        return {
            id: p.id,
            codigo: p.id.slice(-5).toUpperCase(),
            nome: p.nome,
            nascimento: p.dataNascimento,
            telefone: p.telefone ?? '',
            email: p.email ?? '',
            cpf: p.cpf ?? '',
            endereco: p.endereco ?? '',
            cep: p.cep ?? '',
            cidade: p.cidade ?? '',
            numero: p.numero ?? '',
            complemento: p.complemento ?? '',
            bairro: p.bairro ?? '',
            inicio: p.dataInicioAcompanhamento ?? '',
            encerramento: p.dataEncerramentoAcompanhamento ?? '',
            status: p.ativo === false ? 'inativo' : 'acompanhamento',
            situacao: p.ativo === false ? 'Inativo' : 'Em acompanhamento',
            ultimaConsulta: ultima?.dataHora.slice(0, 10) ?? null,
            profissionalId: profissionalAtual,
            responsavelId: vinculo?.responsavelId,
            responsavel: {
                nome: r?.nome ?? '',
                relacao: vinculo?.grauParentesco ?? '',
                telefone: r?.telefone ?? '',
                endereco: r?.endereco ?? '',
                ...(r?.email != null ? { email: r.email } : {}),
                ...(r?.dataNascimento != null ? { nascimento: r.dataNascimento } : {}),
                ...(r?.cpf != null ? { cpf: r.cpf } : {}),
            },
            responsaveis: contatos,
            plano: {
                possui: !!p.possuiPlanoSaude,
                nome: p.planoSaude ?? '',
                carteirinha: p.numeroCarteirinha ?? '',
                validade: p.validadePlano ?? '',
            },
            diagnostico: {
                principal: '—',
                cid: '—',
                inicioTratamento: null,
            },
            consultas: historico.map((consulta) => ({
                data: consulta.dataHora.slice(0, 10),
                profissional: consulta.profissionalId,
                resumo: consulta.tipoAtendimento,
            })),
        };
    }

    private profissionalDaApi(p: ProfissionalApi): Profissional {
        const tipo = /psiquiat/i.test(p.categoria)
            ? 'psiquiatra'
            : 'psicologo';

        return {
            id: p.id,
            codigo: p.id.slice(-5).toUpperCase(),
            nome: p.nome,
            tratamento: '',
            especialidade: p.categoria,
            tipo,
            registro: p.numeroRegistro ?? '',
            nascimento: p.dataNascimento ?? '',
            telefone: p.telefone ?? '',
            ativo: !!p.ativo,
            cep: p.cep ?? '',
            cidade: p.cidade ?? '',
            endereco: p.endereco ?? '',
            numero: p.numero ?? '',
            complemento: p.complemento ?? '',
            bairro: p.bairro ?? '',
            inicio: p.admissao ?? '',
            encerramento: p.encerramento ?? '',
            cpf: p.cpf ?? '',
            email: p.emailContato ?? p.email ?? '',
            anoConclusao: p.anoConclusao,
            disponibilidade: {
                dias: [],
                faixas: [],
                online: !!p.atendeOnline,
                presencial: p.atendePresencial !== false,
            },
            emailAcesso: p.email,
        };
    }

    private secretariaDaApi(s: SecretariaApi): Secretaria {
        return {
            id: s.id,
            codigo: s.id.slice(-5).toUpperCase(),
            nome: s.nome,
            nascimento: s.dataNascimento ?? '',
            telefone: s.telefone ?? '',
            email: s.emailContato ?? s.email,
            emailAcesso: s.email,
            cpf: s.cpf ?? '',
            endereco: s.endereco ?? '',
            cep: s.cep ?? '',
            cidade: s.cidade ?? '',
            numero: s.numero ?? '',
            complemento: s.complemento ?? '',
            bairro: s.bairro ?? '',
            cargo: s.cargo ?? 'Secretária',
            admissao: s.admissao ?? '',
            encerramento: s.encerramento ?? '',
            cbo: s.cbo ?? '',
            cargaHoraria: s.cargaHoraria ?? '',
            ativo: s.ativo ?? true,
        };
    }

    private carregarDisponibilidade(profissionalId: string): void {
        this.profissionaisApi.listarDisponibilidades(profissionalId).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe({
            next: (itens) => {
                const diasOrdem = [
                    'SEGUNDA_FEIRA',
                    'TERCA_FEIRA',
                    'QUARTA_FEIRA',
                    'QUINTA_FEIRA',
                    'SEXTA_FEIRA',
                    'SABADO',
                ];

                const normalizar = (valor: string) =>
                    valor.normalize('NFD')
                        .replace(/[\u0300-\u036f]/g, '')
                        .toUpperCase();

                const faixas = itens
                    .filter((item) => item.ativo)
                    .map((item) => ({
                        inicio: item.horaInicio.slice(0, 5),
                        fim: item.horaFim.slice(0, 5),
                    }));

                const dias = [...new Set(
                    itens
                        .filter((item) => item.ativo)
                        .map((item) => diasOrdem.indexOf(normalizar(item.diaSemana)))
                        .filter((dia) => dia >= 0),
                )];

                this._profissionais.update((lista) =>
                    lista.map((profissional) =>
                        profissional.id === profissionalId
                            ? {
                                ...profissional,
                                disponibilidade: {
                                    ...profissional.disponibilidade,
                                    dias,
                                    faixas,
                                },
                            }
                            : profissional,
                    ),
                );
            },
            error: () => this.aviso.mostrar(
                'Não foi possível carregar as disponibilidades do profissional.',
            ),
        });
    }

    private async sincronizarDisponibilidade(
        profissionalId: string,
        disponibilidade: DadosProfissional['disponibilidade'],
    ): Promise<void> {
        const atuais = await firstValueFrom(
            this.profissionaisApi.listarDisponibilidades(profissionalId),
        );

        const nomesDias = [
            'SEGUNDA_FEIRA',
            'TERCA_FEIRA',
            'QUARTA_FEIRA',
            'QUINTA_FEIRA',
            'SEXTA_FEIRA',
            'SABADO',
        ];

        const idsAnteriores = new Set(atuais.map((item) => item.id));

        const reconciliarVinculosAnteriores = async () => {
            const vinculadasAgora = await firstValueFrom(
                this.profissionaisApi.listarDisponibilidades(profissionalId),
            );

            const idsAgora = new Set(vinculadasAgora.map((item) => item.id));

            for (const id of idsAgora) {
                if (!idsAnteriores.has(id)) {
                    await firstValueFrom(
                        this.profissionaisApi.removerDisponibilidade(profissionalId, id),
                    );
                }
            }

            for (const id of idsAnteriores) {
                if (!idsAgora.has(id)) {
                    await firstValueFrom(
                        this.profissionaisApi.adicionarDisponibilidade(profissionalId, id),
                    );
                }
            }

            const confirmacao = await firstValueFrom(
                this.profissionaisApi.listarDisponibilidades(profissionalId),
            );

            const idsConfirmados = new Set(confirmacao.map((item) => item.id));

            if (
                idsConfirmados.size !== idsAnteriores.size
                || [...idsAnteriores].some((id) => !idsConfirmados.has(id))
            ) {
                throw new Error(
                    'Não foi possível restaurar as disponibilidades anteriores.',
                );
            }
        };

        try {
            for (const dia of disponibilidade.dias) {
                for (const faixa of disponibilidade.faixas) {
                    const nova = await firstValueFrom(
                        this.disponibilidadesApi.criar({
                            diaSemana: nomesDias[dia],
                            horaInicio: `${faixa.inicio}:00`,
                            horaFim: `${faixa.fim}:00`,
                            ativo: true,
                        }),
                    );

                    await firstValueFrom(
                        this.profissionaisApi.adicionarDisponibilidade(
                            profissionalId,
                            nova.id,
                        ),
                    );
                }
            }
        } catch (erro) {
            try {
                await reconciliarVinculosAnteriores();
            } catch (falhaReconciliacao) {
                throw new Error(
                    'A sincronização falhou e a reconciliação automática não foi confirmada.',
                    { cause: falhaReconciliacao },
                );
            }

            throw erro;
        }

        try {
            for (const item of atuais) {
                await firstValueFrom(
                    this.profissionaisApi.removerDisponibilidade(
                        profissionalId,
                        item.id,
                    ),
                );
            }
        } catch (erro) {
            try {
                await reconciliarVinculosAnteriores();
            } catch (falhaReconciliacao) {
                throw new Error(
                    'A sincronização falhou e a reconciliação automática não foi confirmada.',
                    { cause: falhaReconciliacao },
                );
            }

            throw erro;
        }
    }

    private agendamentoDaApi(c: ConsultaApi): Agendamento {
        const status: Record<string, StatusConsulta> = {
            PENDENTE: 'pendente',
            CONFIRMADA: 'confirmado',
            REMARCADA: 'remarcado',
            CANCELADA: 'cancelado',
        };

        return {
            id: c.id,
            data: c.dataHora.slice(0, 10),
            inicio: c.dataHora.slice(11, 16),
            duracao: c.duracao,
            pacienteId: c.pacienteId,
            profissionalId: c.profissionalId,
            tipo: c.tipoAtendimento,
            atendimento: c.modalidade === 'ONLINE' ? 'Online' : 'Presencial',
            sala: c.local,
            local: c.local,
            status: status[c.status] ?? 'pendente',
        };
    }

    carregarDocumentos(pacienteId: string): void {
        this.documentosApi.listarPorPaciente(pacienteId).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe({
            next: (itens) => this._documentos.update((atuais) => [
                ...atuais.filter((documento) =>
                    documento.pacienteId !== pacienteId
                    || documento.origem !== 'documento-clinico',
                ),
                ...itens.map((item) => this.documentoDaApi(item)),
            ]),
            error: () => this.aviso.mostrar(
                'Não foi possível carregar os documentos clínicos.',
            ),
        });

        const perfil = this.auth.obterPerfil();

        if (
            perfil !== 'admin'
            && perfil !== 'psicologo'
            && perfil !== 'psiquiatra'
        ) {
            return;
        }

        this.evolucoesApi.listarPorPaciente(pacienteId).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe({
            next: (itens) => this.substituirOrigemDocumento(
                pacienteId,
                'evolucao',
                itens.map((item) => this.evolucaoComoDocumento(item)),
            ),
            error: () => this.aviso.mostrar(
                'Não foi possível carregar as evoluções clínicas.',
            ),
        });

        this.prescricoesApi.listarPorPaciente(pacienteId).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe({
            next: (itens) => this.substituirOrigemDocumento(
                pacienteId,
                'prescricao',
                itens.map((item) => this.prescricaoComoDocumento(item)),
            ),
            error: () => this.aviso.mostrar(
                'Não foi possível carregar as prescrições.',
            ),
        });

        this.examesApi.listarPorPaciente(pacienteId).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe({
            next: (itens) => this.substituirOrigemDocumento(
                pacienteId,
                'exame',
                itens.map((item) => this.exameComoDocumento(item)),
            ),
            error: () => this.aviso.mostrar(
                'Não foi possível carregar os exames.',
            ),
        });

        this.vinculosApi.listarPorPaciente(pacienteId).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe({
            next: (itens) => this._vinculosPorPaciente.update((atuais) => ({
                ...atuais,
                [pacienteId]: itens,
            })),
            error: () => this.aviso.mostrar(
                'Não foi possível carregar os vínculos profissionais do paciente.',
            ),
        });
    }

    private substituirOrigemDocumento(
        pacienteId: string,
        origem: NonNullable<Documento['origem']>,
        documentos: Documento[],
    ): void {
        this._documentos.update((atuais) => [
            ...atuais.filter((documento) =>
                documento.pacienteId !== pacienteId
                || documento.origem !== origem,
            ),
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
            dataSessao: (item.dataAtendimento ?? '').slice(0, 10),
            tipoAtendimento: null,
            conteudo: item.conteudo,
            atualizadoEm: item.atualizadoEm ?? item.criadoEm ?? '',
            anexos: this.anexosDaApi(item.anexos),
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
            dataSessao: (item.criadaEm ?? '').slice(0, 10),
            tipoAtendimento: null,
            conteudo: item.conteudo,
            atualizadoEm: item.atualizadaEm ?? item.criadaEm ?? '',
            anexos: this.anexosDaApi(item.anexos),
            medicamentos: item.medicamentos ?? [],
        };
    }

    private exameComoDocumento(item: ExameApi): Documento {
        const anexos = this.anexosDaApi(item.anexosResultado);

        return {
            id: `exame:${item.id}`,
            origem: 'exame',
            pacienteId: item.pacienteId,
            profissionalId: item.profissionalId,
            secao: 'exames',
            subtitulo: item.tipo || (
                item.ativo ? 'Solicitação de exame' : 'Exame arquivado'
            ),
            tipoExame: item.tipo,
            titulo: item.nome,
            dataSessao: item.dataRealizacao
                || item.solicitadoEm?.slice(0, 10)
                || item.criadoEm?.slice(0, 10)
                || '',
            dataRealizacao: item.dataRealizacao,
            tipoAtendimento: null,
            conteudo: item.descricaoSolicitacao ?? '',
            atualizadoEm: item.resultadoAtualizadoEm
                ?? item.criadoEm
                ?? '',
            anexos,
            arquivo: anexos.length === 1 ? anexos[0] : undefined,
            resultado: item.resultado,
            observacoes: item.observacoes,
            ativo: item.ativo,
        };
    }

    private anexosDaApi(
        anexos: AnexoClinicoApi[] | null | undefined,
    ): Anexo[] {
        return (anexos ?? []).map((anexo) => ({
            nome: anexo.nome,
            url: `data:${anexo.tipoMime};base64,${anexo.conteudoBase64}`,
        }));
    }

    private async anexosParaApi(
        anexos: Anexo[],
    ): Promise<AnexoClinicoApi[]> {
        return Promise.all(anexos.map(async (anexo) => {
            const resposta = await fetch(anexo.url);

            if (!resposta.ok) {
                throw new Error(
                    `Não foi possível ler o arquivo ${anexo.nome}.`,
                );
            }

            const blob = await resposta.blob();
            const bytes = new Uint8Array(await blob.arrayBuffer());
            let binario = '';

            bytes.forEach((byte) => {
                binario += String.fromCharCode(byte);
            });

            return {
                nome: anexo.nome,
                conteudoBase64: btoa(binario),
                tipoMime: blob.type || 'application/octet-stream',
            };
        }));
    }

    carregarDiagnosticos(pacienteId: string): void {
        this.diagnosticosApi.listarPorPaciente(pacienteId).pipe(
            takeUntil(this.cancelarLeiturasDaSessao),
        ).subscribe({
            next: (itens) => this._pacientes.update((lista) =>
                lista.map((paciente) => {
                    if (paciente.id !== pacienteId) return paciente;

                    const ativos = itens.filter((diagnostico) =>
                        diagnostico.ativo !== false,
                    );

                    const principal = ativos[0];

                    return {
                        ...paciente,
                        diagnostico: {
                            ...paciente.diagnostico,
                            principal: principal?.descricao || '—',
                            cid: principal?.cid || '—',
                        },
                    };
                }),
            ),
            error: () => this.aviso.mostrar(
                'Não foi possível carregar os diagnósticos.',
            ),
        });
    }

    /* ---------- Pacientes ---------- */

    paciente(id: string | undefined | null): Paciente | undefined {
        return this._pacientes().find((paciente) => paciente.id === id);
    }

    /** Cria (sem `id`) ou atualiza (com `id`) um paciente a partir do formulário de cadastro. */
    async salvarPaciente(dados: DadosPaciente, id?: string): Promise<Paciente> {
        const lista = this._pacientes();
        const atual = id
            ? lista.find((paciente) => paciente.id === id)
            : undefined;

        const status = !dados.ativo
            ? 'inativo'
            : atual && atual.status !== 'inativo'
                ? atual.status
                : 'acompanhamento';

        const situacao = status === 'inativo'
            ? 'Inativo'
            : status === 'aguardando'
                ? 'Aguardando retorno'
                : 'Em andamento';

        const r = dados.responsavel;

        const informados = dados.responsaveis?.length
            ? dados.responsaveis
            : (r.nome.trim() ? [{ ...r, id: atual?.responsavelId }] : []);

        const informadosValidos = informados.filter((contato) =>
            contato.nome.trim(),
        );

        const responsaveisSalvos: ResponsavelApi[] = [];
        const responsaveisCriadosNestaOperacao: string[] = [];

        if (atual) {
            try {
                for (const contato of informadosValidos) {
                    const request = {
                        nome: contato.nome.trim(),
                        cpf: contato.cpf || '',
                        email: contato.email || '',
                        telefone: contato.telefone || '',
                        dataNascimento: contato.nascimento || null,
                        endereco: contato.endereco || dados.endereco || null,
                    };

                    const salvo = contato.id
                        ? await firstValueFrom(
                            this.responsaveisApi.atualizar(contato.id, request),
                        )
                        : await firstValueFrom(
                            this.responsaveisApi.criar(request),
                        );

                    if (!contato.id) {
                        responsaveisCriadosNestaOperacao.push(salvo.id);
                    }

                    responsaveisSalvos.push(salvo);
                }
            } catch (erro) {
                await this.limparResponsaveisCriados(
                    responsaveisCriadosNestaOperacao,
                );
                throw erro;
            }

            const vinculos = responsaveisSalvos.map((contato, index) => ({
                responsavelId: contato.id,
                grauParentesco: informadosValidos[index]?.relacao?.trim()
                    || 'Responsável',
            }));

            let resposta: PacienteApi;

            try {
                resposta = await firstValueFrom(
                    this.pacientesApi.atualizar(
                        id!,
                        this.pacienteRequest(dados, vinculos),
                    ),
                );
            } catch (erro) {
                await this.limparResponsaveisCriados(
                    responsaveisCriadosNestaOperacao,
                );
                throw erro;
            }

            const pacienteReal = this.pacienteDaApi(
                resposta,
                responsaveisSalvos,
                [],
            );

            this._pacientes.update((atuais) =>
                atuais.map((paciente) =>
                    paciente.id === pacienteReal.id
                        ? pacienteReal
                        : paciente,
                ),
            );

            return pacienteReal;
        }

        try {
            for (const contato of informadosValidos) {
                const salvo = await firstValueFrom(
                    this.responsaveisApi.criar({
                        nome: contato.nome.trim(),
                        cpf: contato.cpf || '',
                        email: contato.email || '',
                        telefone: contato.telefone || '',
                        dataNascimento: contato.nascimento || null,
                        endereco: contato.endereco || dados.endereco || null,
                    }),
                );

                responsaveisCriadosNestaOperacao.push(salvo.id);
                responsaveisSalvos.push(salvo);
            }
        } catch (erro) {
            await this.limparResponsaveisCriados(
                responsaveisCriadosNestaOperacao,
            );
            throw erro;
        }

        const vinculos = responsaveisSalvos.map((contato, index) => ({
            responsavelId: contato.id,
            grauParentesco: informadosValidos[index]?.relacao?.trim()
                || 'Responsável',
        }));

        let cadastro: {
            paciente: PacienteApi;
            senhaProvisoria: string | null;
        };

        try {
            cadastro = await firstValueFrom(
                this.pacientesApi.criar(
                    this.pacienteRequest(dados, vinculos),
                ),
            );
        } catch (erro) {
            await this.limparResponsaveisCriados(
                responsaveisCriadosNestaOperacao,
            );
            throw erro;
        }

        const novo = this.pacienteDaApi(
            cadastro.paciente,
            responsaveisSalvos,
            [],
        );

        this._pacientes.update((atuais) => [
            ...atuais.filter((paciente) => paciente.id !== novo.id),
            novo,
        ]);

        return novo;
    }

    private async limparResponsaveisCriados(ids: string[]): Promise<void> {
        for (const id of ids) {
            try {
                await firstValueFrom(this.responsaveisApi.excluir(id));
            } catch (erro) {
                // A API impede excluir responsáveis que já estejam vinculados.
                console.error(
                    'Falha na compensação de um responsável recém-criado:',
                    erro,
                );
            }
        }
    }

    private pacienteRequest(
        dados: DadosPaciente,
        responsaveis: {
            responsavelId: string;
            grauParentesco: string;
        }[],
    ) {
        return {
            nome: dados.nome,
            dataNascimento: dados.nascimento,
            email: dados.email || '',
            cpf: dados.cpf || '',
            telefone: dados.telefone || '',
            endereco: dados.endereco || '',
            ativo: dados.ativo,
            cep: dados.cep,
            cidade: dados.cidade,
            numero: dados.numero,
            complemento: dados.complemento,
            bairro: dados.bairro,
            dataInicioAcompanhamento: dados.inicio || null,
            dataEncerramentoAcompanhamento: dados.encerramento || null,
            responsaveis,
            possuiPlanoSaude: dados.plano.possui,
            planoSaude: dados.plano.nome,
            numeroCarteirinha: dados.plano.carteirinha,
            validadePlano: dados.plano.validade || null,
        };
    }

    /** Cadastro rápido (usado na lista de pacientes do psicólogo). */
    adicionarPaciente(dados: NovoPaciente): Promise<Paciente> {
        return this.salvarPaciente({
            nome: dados.nome,
            nascimento: dados.nascimento,
            email: dados.email,
            cpf: '',
            telefone: dados.telefone,
            endereco: dados.endereco,
            responsavel: {
                nome: '',
                nascimento: '',
                email: '',
                relacao: '',
                telefone: '',
                cpf: '',
                endereco: '',
            },
            plano: {
                possui: false,
                nome: '',
                carteirinha: '',
                validade: '',
            },
            ativo: true,
        });
    }

    /** Remove o paciente, o prontuário e as consultas dele. */
    async removerPaciente(id: string): Promise<void> {
        await firstValueFrom(this.pacientesApi.excluir(id));

        this._pacientes.update((lista) =>
            lista.map((paciente) =>
                paciente.id === id
                    ? { ...paciente, status: 'inativo', situacao: 'Inativo' }
                    : paciente,
            ),
        );
    }

    /* ---------- Profissionais ---------- */

    profissional(id: string | undefined | null): Profissional | undefined {
        return this._profissionais().find((profissional) =>
            profissional.id === id,
        );
    }

    /** Cria (sem `id`) ou atualiza (com `id`) um profissional a partir do formulário de cadastro. */
    async salvarProfissional(
        dados: DadosProfissional,
        id?: string,
    ): Promise<Profissional & {
        senhaProvisoria?: string | null;
        emailBoasVindasEnviado?: boolean;
    }> {
        if (id) {
            const salvo = await firstValueFrom(
                this.profissionaisApi.atualizar(id, {
                    nome: dados.nome,
                    email: dados.emailAcesso,
                    emailContato: dados.email,
                    dataNascimento: dados.nascimento,
                    cpf: dados.cpf,
                    telefone: dados.telefone,
                    cep: dados.cep ?? null,
                    cidade: dados.cidade ?? null,
                    endereco: dados.endereco || null,
                    numero: dados.numero ?? null,
                    complemento: dados.complemento ?? null,
                    bairro: dados.bairro ?? null,
                    admissao: dados.inicio || null,
                    encerramento: dados.encerramento || null,
                    categoria: dados.tipo === 'psiquiatra'
                        ? 'PSIQUIATRA'
                        : 'PSICOLOGO',
                    formacaoAcademica: '',
                    numeroRegistro: dados.registro,
                    anoConclusao: dados.anoConclusao,
                    atendeOnline: dados.disponibilidade.online,
                    atendePresencial: dados.disponibilidade.presencial,
                    ativo: dados.ativo,
                    ...(dados.senhaProvisoria
                        ? { senhaProvisoria: dados.senhaProvisoria }
                        : {}),
                }),
            );

            await this.sincronizarDisponibilidade(
                id,
                dados.disponibilidade,
            );

            const atualizado = {
                ...this.profissionalDaApi(salvo),
                disponibilidade: dados.disponibilidade,
            };

            this._profissionais.update((lista) =>
                lista.map((profissional) =>
                    profissional.id === id ? atualizado : profissional,
                ),
            );

            return atualizado;
        }

        const cadastro = await firstValueFrom(
            this.profissionaisApi.criar({
                nome: dados.nome,
                email: dados.emailAcesso,
                emailContato: dados.email,
                dataNascimento: dados.nascimento,
                cpf: dados.cpf,
                telefone: dados.telefone,
                cep: dados.cep ?? null,
                cidade: dados.cidade ?? null,
                endereco: dados.endereco || null, numero: dados.numero ?? null,
                complemento: dados.complemento ?? null,
                bairro: dados.bairro ?? null,
                admissao: dados.inicio || null,
                encerramento: dados.encerramento || null,
                categoria: dados.tipo === 'psiquiatra'
                    ? 'PSIQUIATRA'
                    : 'PSICOLOGO',
                formacaoAcademica: '',
                numeroRegistro: dados.registro,
                anoConclusao: dados.anoConclusao,
                atendeOnline: dados.disponibilidade.online,
                atendePresencial: dados.disponibilidade.presencial,
                ativo: dados.ativo,
                senhaProvisoria: dados.senhaProvisoria,
                enviarEmailBoasVindas: dados.enviarEmailBoasVindas,
            }),
        );

        await this.sincronizarDisponibilidade(
            cadastro.profissional.id,
            dados.disponibilidade,
        );

        const novo: Profissional & {
            senhaProvisoria: string | null;
            emailBoasVindasEnviado: boolean;
        } = {
            ...this.profissionalDaApi(cadastro.profissional),
            disponibilidade: dados.disponibilidade,
            senhaProvisoria: cadastro.senhaProvisoria,
            emailBoasVindasEnviado: cadastro.emailBoasVindasEnviado,
        };

        this._profissionais.update((lista) => [...lista, novo]);

        return novo;
    }

    async alterarAtivoProfissional(
        id: string,
        ativo: boolean,
    ): Promise<void> {
        const profissional = this.profissional(id);

        if (!profissional) return;

        await firstValueFrom(
            this.profissionaisApi.atualizar(id, {
                nome: profissional.nome,
                email: profissional.emailAcesso,
                emailContato: profissional.email,
                dataNascimento: profissional.nascimento || null,
                cpf: profissional.cpf || null,
                telefone: profissional.telefone || null,
                cep: profissional.cep || null,
                cidade: profissional.cidade || null,
                endereco: profissional.endereco || null,
                numero: profissional.numero || null,
                complemento: profissional.complemento || null,
                bairro: profissional.bairro || null,
                admissao: profissional.inicio || null,
                encerramento: profissional.encerramento || null,
                categoria: profissional.tipo === 'psiquiatra'
                    ? 'PSIQUIATRA'
                    : 'PSICOLOGO',
                formacaoAcademica: '',
                numeroRegistro: profissional.registro.replace(/^(CRM|CRP)\s*/, ''),
                anoConclusao: profissional.anoConclusao,
                atendeOnline: profissional.disponibilidade.online,
                atendePresencial: profissional.disponibilidade.presencial,
                ativo,
            }),
        );

        this._profissionais.update((lista) =>
            lista.map((item) =>
                item.id === id ? { ...item, ativo } : item,
            ),
        );
    }

    /** Quantas consultas (não canceladas) o profissional tem na agenda. */
    consultasDoProfissional(id: string): number {
        return this._agendamentos().filter((agendamento) =>
            agendamento.profissionalId === id
            && agendamento.status !== 'cancelado',
        ).length;
    }

    /** Remove o profissional. Só use quando `consultasDoProfissional` for 0. */
    async removerProfissional(id: string): Promise<void> {
        await firstValueFrom(this.profissionaisApi.excluir(id));

        this._profissionais.update((lista) =>
            lista.map((profissional) =>
                profissional.id === id
                    ? { ...profissional, ativo: false }
                    : profissional,
            ),
        );
    }

    /**
     * O profissional atende nesse dia e horário? Considera os dias da semana,
     * as faixas de horário e se ele atende online e/ou presencial.
     */
    disponivel(
        profissionalId: string,
        data: string,
        inicio: string,
        duracao: number,
        atendimento: TipoAtendimento,
    ): boolean {
        const profissional = this.profissional(profissionalId);

        if (!profissional) return false;

        const disponibilidade = profissional.disponibilidade;

        if (
            atendimento === 'Online'
                ? !disponibilidade.online
                : !disponibilidade.presencial
        ) {
            return false;
        }

        if (!disponibilidade.dias.includes(diaDaSemana(data))) {
            return false;
        }

        const inicioMinutos = minutosDe(inicio);

        return disponibilidade.faixas.some((faixa) =>
            inicioMinutos >= minutosDe(faixa.inicio)
            && inicioMinutos + duracao <= minutosDe(faixa.fim),
        );
    }

    /* ---------- Secretárias ---------- */

    secretaria(id: string | undefined | null): Secretaria | undefined {
        return this._secretarias().find((secretaria) =>
            secretaria.id === id,
        );
    }

    async salvarSecretaria(
        dados: DadosSecretaria,
        id?: string,
    ): Promise<Secretaria & {
        senhaProvisoria?: string | null;
        emailBoasVindasEnviado?: boolean;
    }> {
        const request = {
            nome: dados.nome,
            email: dados.emailAcesso || dados.email,
            emailContato: dados.email,
            dataNascimento: dados.nascimento || undefined,
            cpf: dados.cpf,
            telefone: dados.telefone,
            endereco: dados.endereco,
            cep: dados.cep,
            cidade: dados.cidade,
            numero: dados.numero,
            complemento: dados.complemento,
            bairro: dados.bairro,
            cargo: dados.cargo,
            admissao: dados.admissao || undefined,
            encerramento: dados.encerramento || undefined,
            cbo: dados.cbo,
            cargaHoraria: dados.cargaHoraria,
            ativo: dados.ativo,
            ...(dados.senhaProvisoria
                ? { senhaProvisoria: dados.senhaProvisoria }
                : {}),
        };

        if (id) {
            const salva = await firstValueFrom(
                this.secretariasApi.atualizar(id, request),
            );

            const editada = this.secretariaDaApi(salva);

            this._secretarias.update((lista) =>
                lista.map((secretaria) =>
                    secretaria.id === id ? editada : secretaria,
                ),
            );

            return editada;
        }

        const cadastro = await firstValueFrom(
            this.secretariasApi.criar({
                ...request,
                enviarEmailBoasVindas: dados.enviarEmailBoasVindas,
            }),
        );

        const nova: Secretaria & {
            senhaProvisoria: string | null;
            emailBoasVindasEnviado: boolean;
        } = {
            ...this.secretariaDaApi(cadastro.secretaria),
            senhaProvisoria: cadastro.senhaProvisoria,
            emailBoasVindasEnviado: cadastro.emailBoasVindasEnviado,
        };

        this._secretarias.update((lista) => [...lista, nova]);

        return nova;
    }

    async removerSecretaria(id: string): Promise<void> {
        await firstValueFrom(this.secretariasApi.excluir(id));

        this._secretarias.update((lista) =>
            lista.map((secretaria) =>
                secretaria.id === id
                    ? { ...secretaria, ativo: false }
                    : secretaria,
            ),
        );
    }

    /** Esse e-mail de acesso já pertence a outro profissional ou secretária? */
    emailDeAcessoEmUso(email: string, ignorarId?: string): boolean {
        const alvo = email.trim().toLowerCase();

        return (
            this._profissionais().some((profissional) =>
                profissional.id !== ignorarId
                && profissional.emailAcesso.toLowerCase() === alvo,
            )
            || this._secretarias().some((secretaria) =>
                secretaria.id !== ignorarId
                && secretaria.emailAcesso.toLowerCase() === alvo,
            )
        );
    }

    /* ---------- Agenda ---------- */

    agendamento(id: string | undefined | null): Agendamento | undefined {
        return this._agendamentos().find((agendamento) =>
            agendamento.id === id,
        );
    }

    /**
     * O horário está livre? Considera consultas não canceladas do mesmo profissional
     * (e do mesmo paciente, quando informado).
     */
    horarioLivre(
        data: string,
        inicio: string,
        duracao: number,
        profissionalId: string,
        pacienteId?: string,
        ignorarId?: string,
    ): boolean {
        const inicioMinutos = minutosDe(inicio);
        const fimMinutos = inicioMinutos + duracao;

        return !this._agendamentos().some((agendamento) => {
            if (
                agendamento.id === ignorarId
                || agendamento.data !== data
                || agendamento.status === 'cancelado'
            ) {
                return false;
            }

            const conflita = inicioMinutos
                < minutosDe(agendamento.inicio) + agendamento.duracao
                && minutosDe(agendamento.inicio) < fimMinutos;

            return conflita && (
                agendamento.profissionalId === profissionalId
                || (!!pacienteId && agendamento.pacienteId === pacienteId)
            );
        });
    }

    /** Primeira sala livre no horário (ou 'Sala 01'). */
    salaLivre(
        data: string,
        inicio: string,
        duracao: number,
        salas: string[],
    ): string {
        const inicioMinutos = minutosDe(inicio);
        const fimMinutos = inicioMinutos + duracao;

        const ocupadas = new Set(
            this._agendamentos()
                .filter((agendamento) =>
                    agendamento.data === data
                    && agendamento.status !== 'cancelado'
                    && inicioMinutos
                    < minutosDe(agendamento.inicio) + agendamento.duracao
                    && minutosDe(agendamento.inicio) < fimMinutos,
                )
                .map((agendamento) => agendamento.sala),
        );

        return salas.find((sala) => !ocupadas.has(sala)) ?? salas[0];
    }

    async adicionarAgendamento(
        dados: Omit<Agendamento, 'id'>,
    ): Promise<Agendamento> {
        const salvo = await firstValueFrom(
            this.consultasApi.criar({
                pacienteId: dados.pacienteId,
                profissionalId: dados.profissionalId,
                dataHora: `${dados.data}T${dados.inicio}:00`,
                tipoAtendimento: dados.tipo,
                local: dados.local,
                modalidade: dados.atendimento.toUpperCase(),
                duracao: dados.duracao,
            }),
        );

        const novo = this.agendamentoDaApi(salvo);

        this._agendamentos.update((lista) => [...lista, novo]);

        return novo;
    }

    async atualizarAgendamento(atualizado: Agendamento): Promise<void> {
        const payload = {
            pacienteId: atualizado.pacienteId,
            profissionalId: atualizado.profissionalId,
            dataHora: `${atualizado.data}T${atualizado.inicio}:00`,
            tipoAtendimento: atualizado.tipo,
            local: atualizado.local,
            modalidade: atualizado.atendimento.toUpperCase(),
            duracao: atualizado.duracao,
        };

        let resposta: ConsultaApi;

        if (atualizado.status === 'cancelado') {
            resposta = await firstValueFrom(
                this.consultasApi.cancelar(atualizado.id),
            );
        } else {
            resposta = await firstValueFrom(
                this.consultasApi.atualizar(atualizado.id, payload),
            );

            if (atualizado.status === 'confirmado') {
                resposta = await firstValueFrom(
                    this.consultasApi.confirmar(atualizado.id),
                );
            }
        }

        const salvo = this.agendamentoDaApi(resposta);

        this._agendamentos.update((lista) =>
            lista.map((agendamento) =>
                agendamento.id === salvo.id ? salvo : agendamento,
            ),
        );
    }

    /* ---------- Documentos do prontuário ---------- */

    /** Documentos de um paciente em uma seção, do mais novo para o mais antigo. */
    documentosDe(
        pacienteId: string | undefined,
        secao: Secao | undefined,
    ): Documento[] {
        return this._documentos()
            .filter((documento) =>
                documento.pacienteId === pacienteId
                && documento.secao === secao,
            )
            .sort((a, b) =>
                b.dataSessao.localeCompare(a.dataSessao)
                || b.atualizadoEm.localeCompare(a.atualizadoEm),
            );
    }

    async anexarAnexos(
        documento: Documento,
        novosAnexos: Anexo[],
    ): Promise<Documento> {
        if (!novosAnexos.length) return documento;

        const anexosNovosApi = await this.anexosParaApi(novosAnexos);

        if (documento.origem === 'evolucao') {
            const id = documento.id.slice('evolucao:'.length);

            const salvo = await firstValueFrom(
                this.evolucoesApi.anexar(id, anexosNovosApi),
            );

            const atualizado = this.evolucaoComoDocumento(salvo);

            this.substituirOrigemDocumento(
                documento.pacienteId,
                'evolucao',
                [
                    ...this._documentos().filter((item) =>
                        item.pacienteId === documento.pacienteId
                        && item.origem === 'evolucao'
                        && item.id !== atualizado.id,
                    ),
                    atualizado,
                ],
            );

            return atualizado;
        }

        if (documento.origem === 'prescricao') {
            const id = documento.id.slice('prescricao:'.length);

            const salvo = await firstValueFrom(
                this.prescricoesApi.anexar(id, anexosNovosApi),
            );

            const atualizado = this.prescricaoComoDocumento(salvo);

            this.substituirOrigemDocumento(
                documento.pacienteId,
                'prescricao',
                [
                    ...this._documentos().filter((item) =>
                        item.pacienteId === documento.pacienteId
                        && item.origem === 'prescricao'
                        && item.id !== atualizado.id,
                    ),
                    atualizado,
                ],
            );

            return atualizado;
        }

        if (documento.origem === 'exame') {
            const id = documento.id.slice('exame:'.length);
            const anexosExistentesApi = await this.anexosParaApi(
                documento.anexos,
            );

            const salvo = await firstValueFrom(
                this.examesApi.registrarResultado(id, {
                    ...(documento.dataRealizacao
                        ? { dataRealizacao: documento.dataRealizacao }
                        : {}),
                    ...(documento.resultado
                        ? { resultado: documento.resultado }
                        : {}),
                    ...(documento.observacoes
                        ? { observacoes: documento.observacoes }
                        : {}),
                    anexos: [
                        ...anexosExistentesApi,
                        ...anexosNovosApi,
                    ],
                }),
            );

            const atualizado = this.exameComoDocumento(salvo);

            this.substituirOrigemDocumento(
                documento.pacienteId,
                'exame',
                [
                    ...this._documentos().filter((item) =>
                        item.pacienteId === documento.pacienteId
                        && item.origem === 'exame'
                        && item.id !== atualizado.id,
                    ),
                    atualizado,
                ],
            );

            return atualizado;
        }

        if (documento.origem === 'documento-clinico') {
            return this.salvarDocumento({
                ...documento,
                anexos: [
                    ...documento.anexos,
                    ...novosAnexos,
                ],
            });
        }

        throw new Error(
            'A origem deste documento não permite anexar arquivos.',
        );
    }

    async salvarDocumento(dados: DadosDocumento): Promise<Documento> {
        const completo: Documento = {
            ...dados,
            id: dados.id ?? '',
            atualizadoEm: new Date().toISOString(),
        };

        if (
            completo.secao === 'evolucoes'
            && (
                dados.origem === 'evolucao'
                || (!dados.id && dados.origem !== 'documento-clinico')
            )
        ) {
            if (dados.id && dados.origem === 'evolucao') {
                throw new Error(
                    'Evoluções registradas são imutáveis. Registre uma nova evolução para acrescentar informações.',
                );
            }

            const perfil = this.auth.obterPerfil();

            const request = {
                pacienteId: completo.pacienteId,
                ...(perfil === 'admin'
                    ? {
                        profissionalId: await this.profissionalUnicoDoPaciente(
                            completo.pacienteId,
                        ),
                    }
                    : {}),
                tipoEvolucao: 'SESSAO',
                areaAtendimento: perfil === 'psiquiatra'
                    ? 'Psiquiatria'
                    : 'Psicologia',
                titulo: completo.titulo,
                dataAtendimento: `${completo.dataSessao}T00:00:00`,
                conteudo: completo.conteudo,
            };

            let salvo = await firstValueFrom(
                this.evolucoesApi.criar(request),
            );

            if (completo.anexos.length) {
                salvo = await firstValueFrom(
                    this.evolucoesApi.anexar(
                        salvo.id,
                        await this.anexosParaApi(completo.anexos),
                    ),
                );
            }

            const documento = this.evolucaoComoDocumento(salvo);

            this.substituirOrigemDocumento(
                completo.pacienteId,
                'evolucao',
                [
                    ...this._documentos().filter((item) =>
                        item.pacienteId === completo.pacienteId
                        && item.origem === 'evolucao',
                    ),
                    documento,
                ],
            );

            return documento;
        }

        if (
            completo.secao === 'medicamentos'
            && (
                dados.origem === 'prescricao'
                || (!dados.id && dados.origem !== 'documento-clinico')
            )
        ) {
            const perfil = this.auth.obterPerfil();

            const id = dados.id?.startsWith('prescricao:')
                ? dados.id.slice('prescricao:'.length)
                : undefined;

            const anexosAtuais = dados.id
                ? this._documentos().find((item) =>
                    item.id === dados.id,
                )?.anexos ?? []
                : [];

            const anexosNovos = completo.anexos.filter((anexo) =>
                !anexosAtuais.some((atual) =>
                    atual.nome === anexo.nome
                    && atual.url === anexo.url,
                ),
            );

            const request = {
                pacienteId: completo.pacienteId,
                ...(perfil === 'admin'
                    ? {
                        profissionalId: dados.profissionalId
                            ?? await this.profissionalUnicoDoPaciente(
                                completo.pacienteId,
                            ),
                    }
                    : {}),
                conteudo: completo.conteudo,
                ...(dados.origem === 'prescricao' && dados.medicamentos
                    ? { medicamentos: dados.medicamentos }
                    : {}),
            };

            let salvo = await firstValueFrom(
                id
                    ? this.prescricoesApi.atualizar(id, request)
                    : this.prescricoesApi.criar(request),
            );

            if (anexosNovos.length) {
                salvo = await firstValueFrom(
                    this.prescricoesApi.anexar(
                        salvo.id,
                        await this.anexosParaApi(anexosNovos),
                    ),
                );
            }

            const documento = this.prescricaoComoDocumento(salvo);

            this.substituirOrigemDocumento(
                completo.pacienteId,
                'prescricao',
                [
                    ...this._documentos().filter((item) =>
                        item.pacienteId === completo.pacienteId
                        && item.origem === 'prescricao'
                        && item.id !== documento.id,
                    ),
                    documento,
                ],
            );

            return documento;
        }

        if (
            completo.secao === 'exames'
            && dados.origem !== 'documento-clinico'
            && (!dados.id || dados.origem === 'exame')
        ) {
            const id = dados.id?.startsWith('exame:')
                ? dados.id.slice('exame:'.length)
                : undefined;

            const documentoExistente = id
                ? this._documentos().find((documento) =>
                    documento.id === dados.id,
                )
                : undefined;

            const anexosExistentes = documentoExistente?.anexos ?? [];

            const alterouAnexos = completo.anexos.length
                !== anexosExistentes.length
                || completo.anexos.some((anexo, indice) =>
                    anexo.url !== anexosExistentes[indice]?.url,
                );

            const profissionalId = dados.profissionalId
                ?? await this.profissionalUnicoDoPaciente(
                    completo.pacienteId,
                );

            const request = {
                pacienteId: completo.pacienteId,
                profissionalId,
                nome: completo.titulo,
                ...(completo.tipoExame
                    ? { tipo: completo.tipoExame }
                    : {}),
                descricaoSolicitacao: completo.conteudo,
            };

            let salvo: ExameApi;

            if (id) {
                salvo = await firstValueFrom(
                    this.examesApi.atualizar(id, request),
                );
            } else {
                salvo = await firstValueFrom(
                    this.examesApi.criar(request),
                );
            }

            if (alterouAnexos) {
                salvo = await firstValueFrom(
                    this.examesApi.registrarResultado(salvo.id, {
                        ...(completo.dataRealizacao
                            ? { dataRealizacao: completo.dataRealizacao }
                            : {}),
                        ...(completo.resultado
                            ? { resultado: completo.resultado }
                            : {}),
                        ...(completo.observacoes
                            ? { observacoes: completo.observacoes }
                            : {}),
                        anexos: await this.anexosParaApi(completo.anexos),
                    }),
                );
            }

            const documento = this.exameComoDocumento(salvo);

            this.substituirOrigemDocumento(
                completo.pacienteId,
                'exame',
                [
                    ...this._documentos().filter((item) =>
                        item.pacienteId === completo.pacienteId
                        && item.origem === 'exame'
                        && item.id !== documento.id,
                    ),
                    documento,
                ],
            );

            return documento;
        }

        const anexosLocal = completo.arquivo
            && !completo.anexos.some((anexo) =>
                anexo.nome === completo.arquivo?.nome
                && anexo.url === completo.arquivo?.url,
            )
            ? [...completo.anexos, completo.arquivo]
            : completo.anexos;

        const anexos = await this.anexosParaApi(anexosLocal);

        const request = {
            pacienteId: completo.pacienteId,
            secao: completo.secao,
            titulo: completo.titulo,
            grupo: completo.grupo || null,
            dataSessao: completo.dataSessao,
            tipoAtendimento: completo.tipoAtendimento ?? 'Presencial',
            conteudo: completo.conteudo
                || `Arquivo anexado: ${completo.titulo}`,
            anexos,
        };

        const salvo = dados.id
            ? await firstValueFrom(
                this.documentosApi.atualizar(dados.id, request),
            )
            : await firstValueFrom(
                this.documentosApi.criar(request),
            );

        const documento = this.documentoDaApi(salvo);

        this._documentos.update((lista) =>
            dados.id
                ? lista.map((item) =>
                    item.id === documento.id ? documento : item,
                )
                : [...lista, documento],
        );

        return documento;
    }

    private documentoDaApi(item: DocumentoClinicoApi): Documento {
        const anexos = (item.anexos ?? []).map((anexo) => ({
            nome: anexo.nome,
            url: `data:${anexo.tipoMime};base64,${anexo.conteudoBase64}`,
        }));

        return {
            id: item.id,
            origem: 'documento-clinico',
            pacienteId: item.pacienteId,
            profissionalId: item.profissionalId,
            secao: item.secao as Secao,
            titulo: item.titulo,
            grupo: item.grupo ?? undefined,
            dataSessao: item.dataSessao ?? '',
            tipoAtendimento: item.tipoAtendimento as TipoAtendimento,
            conteudo: item.conteudo,
            atualizadoEm: item.atualizadoEm ?? '',
            anexos,
            arquivo: item.conteudo.startsWith('Arquivo anexado:')
                ? anexos[0]
                : undefined,
        };
    }

    private async profissionalUnicoDoPaciente(
        pacienteId: string,
    ): Promise<string> {
        const vinculos = await firstValueFrom(
            this.vinculosApi.listarPorPaciente(pacienteId),
        );

        this._vinculosPorPaciente.update((atuais) => ({
            ...atuais,
            [pacienteId]: vinculos,
        }));

        const profissionaisAtivos = [...new Set(
            vinculos
                .filter((vinculo) => vinculo.ativo)
                .map((vinculo) => vinculo.profissionalId),
        )];

        if (profissionaisAtivos.length !== 1) {
            throw new Error(
                'Não foi possível identificar um único profissional ativo para este paciente.',
            );
        }

        return profissionaisAtivos[0];
    }
}

export const hojeISO = () => paraISO(new Date());