import {
    Component,
    ElementRef,
    computed,
    effect,
    inject,
    input,
    signal,
    untracked,
    viewChild,
} from '@angular/core';

import { Aviso } from '../../shared/aviso/aviso';
import { Auth } from '../../shared/auth';
import {
    ConsultaApi,
    ConsultaRequestApi,
    ConsultasApiService,
} from '../../shared/clinica/consultas-api.service';
import {
    PacienteApi,
    PacientesApiService,
} from '../../shared/clinica/pacientes-api.service';
import {
    DisponibilidadeApi,
    ProfissionalApi,
    ProfissionaisApiService,
} from '../../shared/clinica/profissionais-api.service';
import { PreferenciasGlobaisService } from '../../shared/clinica/preferencias-globais.service';
import { StatusConsulta } from '../../shared/clinica/modelos';
import {
    ORDEM_STATUS,
    ROTULO_STATUS,
    SALAS,
    horariosDaDisponibilidade,
    horariosAgendaParaConsultas,
    intervaloEntreConsultas,
    minutosDe,
} from '../../shared/clinica/status';
import { Avatar } from '../../shared/avatar/avatar';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import {
    deISO,
    diaDaSemana,
    formatarData,
    idadeDe,
    normalizar,
    paraISO,
    somarDias,
} from '../../shared/util';

const NOMES_DIAS = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const DIA_MS = 24 * 60 * 60 * 1000;
const ALTURA_HORA_REM = 5.25;

const DIAS_EXTENSO = [
    'Segunda-feira',
    'Terça-feira',
    'Quarta-feira',
    'Quinta-feira',
    'Sexta-feira',
    'Sábado',
    'Domingo',
];

const DIAS_SEMANA = [
    'DOMINGO',
    'SEGUNDA_FEIRA',
    'TERCA_FEIRA',
    'QUARTA_FEIRA',
    'QUINTA_FEIRA',
    'SEXTA_FEIRA',
    'SABADO',
];

type AgendamentoApiView = {
    id: string;
    data: string;
    inicio: string;
    duracao: number;
    pacienteId: string;
    profissionalId: string;
    tipo: string;
    atendimento: 'Presencial' | 'Online';
    sala: string;
    local: string;
    status: StatusConsulta;
};

function dataAtualISO(): string {
    const hoje = new Date();

    return [
        hoje.getFullYear(),
        String(hoje.getMonth() + 1).padStart(2, '0'),
        String(hoje.getDate()).padStart(2, '0'),
    ].join('-');
}

function segundaDaSemana(iso: string): string {
    const data = deISO(iso);
    const dia = (data.getDay() + 6) % 7;

    data.setDate(data.getDate() - dia);

    return [
        data.getFullYear(),
        String(data.getMonth() + 1).padStart(2, '0'),
        String(data.getDate()).padStart(2, '0'),
    ].join('-');
}

function semanaDe(iso: string, base: string): number {
    const dias = Math.round(
        (deISO(iso).getTime() - deISO(base).getTime()) / DIA_MS,
    );

    return Math.floor(dias / 7);
}

function statusParaTela(status: ConsultaApi['status']): StatusConsulta {
    switch (status) {
        case 'CONFIRMADA':
            return 'confirmado';
        case 'REMARCADA':
            return 'remarcado';
        case 'CANCELADA':
            return 'cancelado';
        default:
            return 'pendente';
    }
}

function montarDataHora(data: string, hora: string): string {
    return `${data}T${hora}:00`;
}

function converterConsulta(consulta: ConsultaApi): AgendamentoApiView {
    const dataHora = consulta.dataHora;
    const data = dataHora.slice(0, 10);
    const inicio = dataHora.slice(11, 16);

    return {
        id: consulta.id,
        data,
        inicio,
        duracao: consulta.duracao,
        pacienteId: consulta.pacienteId,
        profissionalId: consulta.profissionalId,
        tipo: consulta.tipoAtendimento,
        atendimento:
            consulta.modalidade?.toLowerCase() === 'online'
                ? 'Online'
                : 'Presencial',
        sala: consulta.local || '—',
        local: consulta.local || '—',
        status: statusParaTela(consulta.status),
    };
}

function normalizarDiaDisponibilidade(dia: string): string {
    return dia
        .trim()
        .toUpperCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[-\s]/g, '_');
}

function diaDaData(data: string): string {
    return DIAS_SEMANA[deISO(data).getDay()];
}

/** Sala conhecida ou sala legada que está sendo mantida sem alteração. */
export function salaConsultaPermitida(
    sala: string,
    salaAtual: string,
): boolean {
    return SALAS.includes(sala) || sala === salaAtual;
}

export interface EdicaoConsultaComparavel {
    data: string;
    inicio: string;
    profissionalId: string;
    local: string;
}

export function possuiAlteracaoAgenda(
    atual: EdicaoConsultaComparavel,
    edicao: EdicaoConsultaComparavel,
): boolean {
    return (
        atual.data !== edicao.data ||
        atual.inicio !== edicao.inicio ||
        atual.profissionalId !== edicao.profissionalId ||
        atual.local !== edicao.local
    );
}

export interface ConsultaAgendaConflito {
    id: string;
    data: string;
    inicio: string;
    duracao: number;
    pacienteId: string;
    profissionalId: string;
    local: string;
    status: StatusConsulta;
}

export interface HorarioAgendaCandidato {
    consultaId: string;
    data: string;
    inicio: string;
    duracao: number;
    pacienteId: string;
    profissionalId: string;
    local: string;
}

/**
 * Verifica conflitos considerando o tempo efetivamente reservado na agenda.
 * Consultas de 50 minutos reservam 60 minutos (50 + 10 de margem).
 */
export function possuiConflitoAgenda(
    consultas: ConsultaAgendaConflito[],
    candidato: HorarioAgendaCandidato,
): boolean {
    const inicio = minutosDe(candidato.inicio);
    const fim =
        inicio + intervaloEntreConsultas(candidato.duracao);

    const salaFisica = /^sala\s+\d{1,2}$/i.test(
        candidato.local.trim(),
    );

    return consultas.some((existente) => {
        if (
            existente.id === candidato.consultaId ||
            existente.status === 'cancelado' ||
            existente.data !== candidato.data ||
            !existente.duracao ||
            existente.duracao <= 0
        ) {
            return false;
        }

        const inicioExistente = minutosDe(existente.inicio);
        const fimExistente =
            inicioExistente + intervaloEntreConsultas(existente.duracao);

        const sobrepoe =
            inicio < fimExistente && inicioExistente < fim;

        if (!sobrepoe) return false;

        return (
            existente.profissionalId === candidato.profissionalId ||
            existente.pacienteId === candidato.pacienteId ||
            (
                salaFisica &&
                existente.local.localeCompare(
                    candidato.local,
                    undefined,
                    { sensitivity: 'accent' },
                ) === 0
            )
        );
    });
}

@Component({
    selector: 'app-agenda',
    imports: [Avatar, Dialogo, Icone, TituloPagina],
    templateUrl: './agenda.html',
    styleUrl: './agenda.css',
    host: {
        '(document:click)': 'fecharMenu($event)',
        '(document:keydown.escape)': 'editando.set(false)',
    },
})
export class Agenda {
    private readonly consultasApi = inject(ConsultasApiService);
    private readonly pacientesApi = inject(PacientesApiService);
    private readonly profissionaisApi = inject(ProfissionaisApiService);
    private readonly aviso = inject(Aviso);
    private readonly auth = inject(Auth);
    private readonly preferenciasGlobais = inject(
        PreferenciasGlobaisService,
    );

    readonly perfil = input<Perfil>('psicologo');
    readonly consulta = input<string>();

    private readonly hojeBase = dataAtualISO();
    private readonly segundaBase = segundaDaSemana(this.hojeBase);

    protected readonly horarios = computed(() =>
        horariosAgendaParaConsultas(this.consultas()),
    );

    protected readonly horas = computed(() => {
        const horarios = this.horarios();

        const inicio = Math.floor(
            minutosDe(horarios[0] ?? '08:00') / 60,
        ) * 60;

        const fim = Math.ceil(
            minutosDe(horarios.at(-1) ?? '18:00') / 60,
        ) * 60;

        return Array.from(
            { length: Math.floor((fim - inicio) / 60) + 1 },
            (_, i) => {
                const minutos = inicio + i * 60;

                return `${String(Math.floor(minutos / 60)).padStart(2, '0')}:${String(minutos % 60).padStart(2, '0')}`;
            },
        );
    });

    /** Primeiro minuto visível na grade. */
    protected readonly inicioGradeMinutos = computed(() =>
        minutosDe(this.horas()[0] ?? '08:00'),
    );

    protected readonly colunas = [0, 1, 2, 3, 4, 5];
    protected readonly statusLista = ORDEM_STATUS;
    protected readonly rotulo = ROTULO_STATUS;
    protected readonly salas = SALAS;

    protected readonly consultas = signal<AgendamentoApiView[]>([]);
    protected readonly pacientes = signal<PacienteApi[]>([]);
    protected readonly profissionais = signal<ProfissionalApi[]>([]);
    protected readonly disponibilidades = signal<DisponibilidadeApi[]>([]);

    protected readonly carregandoDisponibilidades = signal(false);
    protected readonly falhaDisponibilidades = signal(false);

    private readonly tentativaDisponibilidade = signal(0);

    protected readonly carregando = signal(true);
    protected readonly consultasCarregadas = signal(false);
    protected readonly falhaConsulta = signal(false);

    protected readonly semana = signal(0);
    protected readonly busca = signal('');
    protected readonly selecionadoId = signal('');
    protected readonly menuStatus = signal(false);

    protected readonly editando = signal(false);
    protected readonly cancelando = signal(false);
    protected readonly confirmando = signal(false);
    protected readonly confirmandoRemarcacao = signal(false);
    protected readonly executandoAcao = signal(false);

    protected readonly remarcacaoPendente =
        signal<ConsultaRequestApi | null>(null);

    protected readonly editData = signal('');
    protected readonly editHora = signal('');
    protected readonly editProf = signal('');
    protected readonly editSala = signal('');

    private readonly seletorData =
        viewChild<ElementRef<HTMLInputElement>>('seletorData');

    private readonly menuRaiz =
        viewChild<ElementRef<HTMLElement>>('menuStatusRaiz');

    protected readonly secretaria = computed(
        () =>
            this.perfil() === 'secretaria' ||
            this.perfil() === 'admin',
    );

    protected readonly hojeISO = this.hojeBase;
    protected readonly dataHoje = formatarData(this.hojeBase);

    constructor() {
        this.preferenciasGlobais.carregar().subscribe();

        effect(() => {
            this.perfil();

            untracked(() => this.carregarDados());
        });

        effect(() => {
            const id = this.consulta();

            if (!id) return;

            const alvo = this.consultas().find(
                (item) => item.id === id,
            );

            if (alvo) {
                this.selecionadoId.set(alvo.id);
                this.semana.set(
                    semanaDe(alvo.data, this.segundaBase),
                );
            }
        });

        effect((onCleanup) => {
            const estaEditando = this.editando();
            const profissionalId = this.editProf();

            this.tentativaDisponibilidade();

            if (!estaEditando || !profissionalId) {
                return;
            }

            this.disponibilidades.set([]);
            this.falhaDisponibilidades.set(false);
            this.carregandoDisponibilidades.set(true);

            const inscricao = this.profissionaisApi
                .listarDisponibilidades(profissionalId)
                .subscribe({
                    next: (lista) => {
                        this.disponibilidades.set(lista);
                        this.carregandoDisponibilidades.set(false);

                        const horarios = this.opcoesHorario();

                        if (
                            horarios.length &&
                            !horarios.includes(this.editHora())
                        ) {
                            this.editHora.set(horarios[0]);
                        }
                    },

                    error: () => {
                        this.disponibilidades.set([]);
                        this.falhaDisponibilidades.set(true);
                        this.carregandoDisponibilidades.set(false);

                        this.aviso.mostrar(
                            'Não foi possível carregar a disponibilidade deste profissional.',
                        );
                    },
                });

            onCleanup(() => inscricao.unsubscribe());
        });
    }

    private carregarDados(): void {
        this.carregando.set(true);
        this.consultasCarregadas.set(false);
        this.falhaConsulta.set(false);

        this.consultas.set([]);
        this.pacientes.set([]);
        this.profissionais.set([]);
        this.disponibilidades.set([]);

        this.carregandoDisponibilidades.set(false);
        this.falhaDisponibilidades.set(false);

        this.selecionadoId.set('');

        this.consultasApi.listarTodas().subscribe({
            next: (lista) => {
                const convertidas = lista.map(converterConsulta);

                this.consultas.set(convertidas);
                this.consultasCarregadas.set(true);
                this.falhaConsulta.set(false);

                const idDaRota = this.consulta();

                const selecionada = convertidas.find(
                    (item) => item.id === idDaRota,
                );

                if (selecionada) {
                    this.selecionadoId.set(selecionada.id);

                    this.semana.set(
                        semanaDe(selecionada.data, this.segundaBase),
                    );
                } else if (
                    !this.selecionadoId() &&
                    convertidas.length > 0
                ) {
                    this.selecionadoId.set(convertidas[0].id);
                }

                this.carregando.set(false);
                this.carregarProfissionais();
            },

            error: (erro) => {
                this.carregando.set(false);
                this.consultasCarregadas.set(false);
                this.falhaConsulta.set(true);

                this.aviso.mostrar(
                    erro.error?.message ||
                    'Não foi possível carregar a agenda. Verifique seu acesso.',
                );
            },
        });

        this.pacientesApi.listarTodos().subscribe({
            next: (lista) => this.pacientes.set(lista),

            error: () =>
                this.aviso.mostrar(
                    'Não foi possível carregar os dados dos pacientes.',
                ),
        });
    }

    private carregarProfissionais(): void {
        if (this.secretaria()) {
            this.profissionaisApi.listarTodos().subscribe({
                next: (lista) => this.profissionais.set(lista),

                error: () =>
                    this.aviso.mostrar(
                        'Não foi possível carregar os profissionais.',
                    ),
            });

            return;
        }

        const profissionalId = this.auth.obterEntidadeId();

        if (!profissionalId) {
            this.profissionais.set([]);
            this.disponibilidades.set([]);
            this.carregandoDisponibilidades.set(false);
            this.falhaDisponibilidades.set(true);

            this.aviso.mostrar(
                'Não foi possível identificar o profissional da sessão. Faça login novamente.',
            );

            return;
        }

        this.profissionaisApi.buscarPorId(profissionalId).subscribe({
            next: (profissional) => {
                this.profissionais.set([profissional]);
            },

            error: () => {
                this.aviso.mostrar(
                    'Não foi possível carregar os dados do profissional.',
                );
            },
        });

        this.carregandoDisponibilidades.set(true);
        this.falhaDisponibilidades.set(false);

        this.profissionaisApi
            .listarDisponibilidades(profissionalId)
            .subscribe({
                next: (lista) => {
                    this.disponibilidades.set(lista);
                    this.carregandoDisponibilidades.set(false);
                    this.falhaDisponibilidades.set(false);
                },

                error: () => {
                    this.disponibilidades.set([]);
                    this.carregandoDisponibilidades.set(false);
                    this.falhaDisponibilidades.set(true);

                    this.aviso.mostrar(
                        'Não foi possível carregar a disponibilidade deste profissional.',
                    );
                },
            });
    }

    private recarregarConsultas(): void {
        this.consultasCarregadas.set(false);

        this.consultasApi.listarTodas().subscribe({
            next: (lista) => {
                const convertidas = lista.map(converterConsulta);

                this.consultas.set(convertidas);
                this.consultasCarregadas.set(true);
                this.falhaConsulta.set(false);
            },

            error: () => {
                this.consultasCarregadas.set(false);
                this.falhaConsulta.set(true);

                this.aviso.mostrar(
                    'A alteração foi enviada, mas não foi possível atualizar a agenda.',
                );
            },
        });
    }

    protected readonly dias = computed(() =>
        NOMES_DIAS.map((nome, i) => {
            const iso = somarDias(
                this.segundaBase,
                this.semana() * 7 + i,
            );

            return {
                nome,
                iso,
                texto: formatarData(iso).slice(0, 5),
                hoje: iso === this.hojeBase,
                dataCompleta: formatarData(iso),
            };
        }),
    );

    protected readonly faixa = computed(
        () =>
            `${this.dias()[0].dataCompleta} - ${this.dias()[5].dataCompleta}`,
    );

    protected readonly eventos = computed(() => {
        const termo = normalizar(this.busca());
        const dias = this.dias();

        const primeiro = dias[0].iso;
        const ultimo = dias[5].iso;

        return this.consultas()
            .filter(
                (a) => a.data >= primeiro && a.data <= ultimo,
            )
            .map((a) => {
                const paciente = this.pacientes().find(
                    (p) => p.id === a.pacienteId,
                );

                const profissional = this.profissionais().find(
                    (p) => p.id === a.profissionalId,
                );

                const inicioMinutos = minutosDe(a.inicio);
                const reservaMinutos =
                    intervaloEntreConsultas(a.duracao);
                const fimMinutos =
                    inicioMinutos + reservaMinutos;

                return {
                    ...a,
                    nome: paciente?.nome ?? 'Paciente',
                    profNome: profissional?.nome ?? '—',
                    dia: diaDaSemana(a.data),
                    fim: `${String(Math.floor(fimMinutos / 60)).padStart(2, '0')}:${String(fimMinutos % 60).padStart(2, '0')}`,
                    inicioMinutos,
                    fimMinutos,
                    reservaMinutos,
                    destaque:
                        !termo ||
                        normalizar(paciente?.nome ?? '').includes(termo),
                    selecionado: a.id === this.selecionadoId(),
                };
            });
    });

    /**
     * Organiza os cartões por dia e posiciona cada um pelo minuto real.
     *
     * A grade trabalha com uma escala de 10 minutos. A altura do cartão
     * representa o tempo reservado, e não apenas a duração clínica.
     *
     * Quando existem consultas simultâneas de profissionais diferentes,
     * os cartões são distribuídos em faixas horizontais para não se
     * sobreporem.
     */
    protected readonly eventosPorDia = computed(() => {
        const inicioGrade = this.inicioGradeMinutos();

        return this.colunas.map((coluna) => {
            const lista = this.eventos()
                .filter((evento) => evento.dia === coluna)
                .sort(
                    (a, b) =>
                        a.inicioMinutos - b.inicioMinutos ||
                        a.fimMinutos - b.fimMinutos,
                );

            const fimPorFaixa: number[] = [];

            const posicionados = lista.map((evento) => {
                let faixa = fimPorFaixa.findIndex(
                    (fim) => fim <= evento.inicioMinutos,
                );

                if (faixa === -1) {
                    faixa = fimPorFaixa.length;
                    fimPorFaixa.push(evento.fimMinutos);
                } else {
                    fimPorFaixa[faixa] = evento.fimMinutos;
                }

                return {
                    ...evento,
                    faixa,
                };
            });

            const totalFaixas = Math.max(1, fimPorFaixa.length);
            const largura = 100 / totalFaixas;

            return posicionados.map((evento) => ({
                ...evento,
                topRem:
                    ((evento.inicioMinutos - inicioGrade) / 60) *
                    ALTURA_HORA_REM,
                alturaRem:
                    (evento.reservaMinutos / 60) *
                    ALTURA_HORA_REM,
                leftPercent: evento.faixa * largura,
                widthPercent: largura,
            }));
        });
    });

    private readonly consultasHoje = computed(() =>
        this.consultas().filter(
            (a) => a.data === this.hojeBase,
        ),
    );

    protected readonly horariosVagosHoje = computed(() => {
        if (
            this.secretaria() ||
            !this.consultasCarregadas() ||
            this.carregandoDisponibilidades() ||
            this.falhaDisponibilidades()
        ) {
            return 0;
        }

        const profissionalId = this.auth.obterEntidadeId();

        if (!profissionalId) return 0;

        const duracaoConfigurada =
            this.preferenciasGlobais.preferencias()
                .duracaoPadraoConsulta;

        const duracao =
            duracaoConfigurada === 30 ||
                duracaoConfigurada === 40 ||
                duracaoConfigurada === 50 ||
                duracaoConfigurada === 60
                ? duracaoConfigurada
                : 50;

        const intervalo = intervaloEntreConsultas(duracao);
        const dia = diaDaData(this.hojeBase);

        const disponibilidadesDoDia =
            this.disponibilidades().filter(
                (disponibilidade) =>
                    Boolean(disponibilidade.ativo) &&
                    normalizarDiaDisponibilidade(
                        disponibilidade.diaSemana,
                    ) === dia,
            );

        const horarios = horariosDaDisponibilidade(
            disponibilidadesDoDia,
            duracao,
            intervalo,
        );

        const agora = new Date();

        const minutosAgora =
            agora.getHours() * 60 + agora.getMinutes();

        return horarios.filter((inicio) => {
            if (minutosDe(inicio) <= minutosAgora) {
                return false;
            }

            return !possuiConflitoAgenda(this.consultas(), {
                consultaId: '',
                data: this.hojeBase,
                inicio,
                duracao,
                pacienteId: '',
                profissionalId,
                local: '',
            });
        }).length;
    });

    protected readonly resumo = computed(() => {
        const lista = this.consultasHoje();

        const conta = (status: StatusConsulta) =>
            lista.filter((a) => a.status === status).length;

        const itens = [
            {
                classe: 'confirmado',
                rotulo: 'Confirmados',
                valor: conta('confirmado'),
            },
            {
                classe: 'pendente',
                rotulo: 'A confirmar',
                valor: conta('pendente'),
            },
            {
                classe: 'vago',
                rotulo: 'Horário vago',
                valor: this.horariosVagosHoje(),
            },
            {
                classe: 'cancelado',
                rotulo: 'Cancelados',
                valor: conta('cancelado'),
            },
            {
                classe: 'remarcado',
                rotulo: 'Remarcados',
                valor: conta('remarcado'),
            },
        ];

        return this.secretaria()
            ? itens.filter((item) => item.classe !== 'vago')
            : itens;
    });

    protected readonly proximo = computed(() => {
        const agora = new Date();

        const minutosAgora =
            agora.getHours() * 60 + agora.getMinutes();

        return this.consultasHoje()
            .filter(
                (a) =>
                    a.status !== 'cancelado' &&
                    minutosDe(a.inicio) > minutosAgora,
            )
            .sort(
                (a, b) =>
                    minutosDe(a.inicio) - minutosDe(b.inicio),
            )[0];
    });

    protected readonly escolhido = computed(() => {
        const a = this.consultas().find(
            (item) => item.id === this.selecionadoId(),
        );

        if (!a) return undefined;

        const paciente = this.pacientes().find(
            (item) => item.id === a.pacienteId,
        );

        const profissional = this.profissionais().find(
            (item) => item.id === a.profissionalId,
        );

        const amanha = somarDias(this.hojeBase, 1);

        const relativo =
            a.data === this.hojeBase
                ? 'Hoje'
                : a.data === amanha
                    ? 'Amanhã'
                    : DIAS_EXTENSO[diaDaSemana(a.data)];

        return {
            a,
            relativo,
            dataBR: formatarData(a.data),
            nome: paciente?.nome ?? 'Paciente',
            idade: paciente?.dataNascimento
                ? idadeDe(paciente.dataNascimento)
                : 0,
            diagnostico: '',
            telefone: paciente?.telefone || '—',
            email: paciente?.email || '—',
            profissional: profissional?.nome ?? '—',
        };
    });

    protected selecionar(id: string): void {
        this.selecionadoId.set(id);
        this.menuStatus.set(false);
    }

    protected irPara(delta: number): void {
        this.semana.update((valor) => valor + delta);
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

        if (valor) {
            this.semana.set(
                semanaDe(
                    paraISO(deISO(valor)),
                    this.segundaBase,
                ),
            );
        }
    }

    protected fecharMenu(evento: Event): void {
        const raiz = this.menuRaiz()?.nativeElement;

        if (
            this.menuStatus() &&
            raiz &&
            !raiz.contains(evento.target as Node)
        ) {
            this.menuStatus.set(false);
        }
    }

    protected trocarStatus(status: StatusConsulta): void {
        const consulta = this.escolhido()?.a;

        if (!consulta) return;

        if (status === 'confirmado') {
            if (
                consulta.status === 'cancelado' ||
                consulta.status === 'remarcado'
            ) {
                this.menuStatus.set(false);

                this.aviso.mostrar(
                    'Esta consulta não pode ser confirmada por esta operação.',
                );

                return;
            }

            if (consulta.status === 'confirmado') {
                this.menuStatus.set(false);
                this.aviso.mostrar(
                    'Esta consulta já está confirmada.',
                );

                return;
            }

            this.menuStatus.set(false);
            this.confirmando.set(true);

            return;
        }

        if (status === 'cancelado') {
            if (consulta.status === 'cancelado') {
                this.menuStatus.set(false);
                this.aviso.mostrar(
                    'Esta consulta já está cancelada.',
                );

                return;
            }

            this.menuStatus.set(false);
            this.cancelando.set(true);

            return;
        }

        this.menuStatus.set(false);

        this.aviso.mostrar(
            'Use a opção de remarcação para alterar data, horário ou profissional.',
        );
    }

    protected abrirEdicao(): void {
        const selecionada = this.escolhido();

        if (!selecionada) return;

        this.disponibilidades.set([]);
        this.editData.set(selecionada.a.data);
        this.editHora.set(selecionada.a.inicio);
        this.editProf.set(selecionada.a.profissionalId);
        this.editSala.set(selecionada.a.local);
        this.editando.set(true);
    }

    protected alterarDataEdicao(valor: string): void {
        this.editData.set(valor);

        const horarios = this.opcoesHorario();

        if (
            horarios.length &&
            !horarios.includes(this.editHora())
        ) {
            this.editHora.set(horarios[0]);
        }
    }

    protected alterarProfissionalEdicao(valor: string): void {
        if (valor === this.editProf()) return;

        this.editHora.set('');
        this.disponibilidades.set([]);
        this.editProf.set(valor);
    }

    protected alterarSalaEdicao(valor: string): void {
        this.editSala.set(valor);

        const horarios = this.opcoesHorario();

        if (
            horarios.length &&
            !horarios.includes(this.editHora())
        ) {
            this.editHora.set(horarios[0]);
        }
    }

    protected alterarHorarioEdicao(valor: string): void {
        if (this.opcoesHorario().includes(valor)) {
            this.editHora.set(valor);
        }
    }

    protected tentarNovamenteCarregarAgenda(): void {
        this.recarregarConsultas();

        this.tentativaDisponibilidade.update(
            (tentativa) => tentativa + 1,
        );

        if (!this.secretaria()) {
            const profissionalId = this.auth.obterEntidadeId();

            if (profissionalId) {
                this.carregandoDisponibilidades.set(true);
                this.falhaDisponibilidades.set(false);

                this.profissionaisApi
                    .listarDisponibilidades(profissionalId)
                    .subscribe({
                        next: (lista) => {
                            this.disponibilidades.set(lista);
                            this.carregandoDisponibilidades.set(false);
                        },

                        error: () => {
                            this.disponibilidades.set([]);
                            this.carregandoDisponibilidades.set(false);
                            this.falhaDisponibilidades.set(true);

                            this.aviso.mostrar(
                                'Não foi possível carregar a disponibilidade deste profissional.',
                            );
                        },
                    });
            }
        }
    }

    protected readonly opcoesProfissionais = computed(() =>
        this.profissionais()
            .filter(
                (profissional) =>
                    profissional.ativo ||
                    profissional.id === this.escolhido()?.a.profissionalId,
            )
            .map((profissional) => ({
                valor: profissional.id,
                rotulo: `${profissional.nome} - ${profissional.categoria || 'Profissional'}`,
            })),
    );

    protected readonly opcoesHorario = computed(() => {
        const atual = this.escolhido()?.a;
        const data = this.editData();
        const profissionalId = this.editProf();

        if (
            !atual ||
            !data ||
            !profissionalId ||
            !this.consultasCarregadas() ||
            this.falhaDisponibilidades()
        ) {
            return [];
        }

        const dia = diaDaData(data);

        const disponibilidadesDoDia =
            this.disponibilidades().filter(
                (disponibilidade) => {
                    const diaDisponibilidade =
                        normalizarDiaDisponibilidade(
                            disponibilidade.diaSemana,
                        );

                    return (
                        Boolean(disponibilidade.ativo) &&
                        diaDisponibilidade === dia
                    );
                },
            );

        const agora = new Date();

        const minutosAgora =
            agora.getHours() * 60 + agora.getMinutes();

        const intervalo =
            intervaloEntreConsultas(atual.duracao);

        const horariosDisponiveis = new Set(
            horariosDaDisponibilidade(
                disponibilidadesDoDia,
                atual.duracao,
                intervalo,
            ),
        );

        if (
            data === atual.data &&
            profissionalId === atual.profissionalId
        ) {
            horariosDisponiveis.add(atual.inicio);
        }

        return [...horariosDisponiveis]
            .sort(
                (a, b) => minutosDe(a) - minutosDe(b),
            )
            .filter((horario) => {
                const horarioAtual =
                    data === atual.data &&
                    profissionalId === atual.profissionalId &&
                    horario === atual.inicio;

                if (data < this.hojeBase && !horarioAtual) {
                    return false;
                }

                if (
                    !horarioAtual &&
                    data === this.hojeBase &&
                    minutosDe(horario) <= minutosAgora
                ) {
                    return false;
                }

                const inicio = minutosDe(horario);
                const fim = inicio + intervalo;

                const cabeNaDisponibilidade =
                    horarioAtual ||
                    disponibilidadesDoDia.some((disponibilidade) => {
                        if (
                            !disponibilidade.horaInicio ||
                            !disponibilidade.horaFim
                        ) {
                            return false;
                        }

                        const inicioDisponivel = minutosDe(
                            disponibilidade.horaInicio,
                        );

                        const fimDisponivel = minutosDe(
                            disponibilidade.horaFim,
                        );

                        return (
                            inicio >= inicioDisponivel &&
                            fim <= fimDisponivel
                        );
                    });

                if (!cabeNaDisponibilidade) return false;

                return !possuiConflitoAgenda(
                    this.consultas(),
                    {
                        consultaId: atual.id,
                        data,
                        inicio: horario,
                        duracao: atual.duracao,
                        pacienteId: atual.pacienteId,
                        profissionalId,
                        local: this.editSala(),
                    },
                );
            });
    });

    protected readonly opcoesSala = computed(() => {
        const atual = this.escolhido()?.a;
        const data = this.editData();
        const profissionalId = this.editProf();

        if (
            !atual ||
            !data ||
            !profissionalId ||
            !this.consultasCarregadas() ||
            this.falhaDisponibilidades()
        ) {
            return [];
        }

        const dia = diaDaData(data);

        const disponibilidadesDoDia =
            this.disponibilidades().filter(
                (disponibilidade) =>
                    Boolean(disponibilidade.ativo) &&
                    normalizarDiaDisponibilidade(
                        disponibilidade.diaSemana,
                    ) === dia,
            );

        const intervalo =
            intervaloEntreConsultas(atual.duracao);

        const horarios = new Set(
            horariosDaDisponibilidade(
                disponibilidadesDoDia,
                atual.duracao,
                intervalo,
            ),
        );

        if (
            data === atual.data &&
            profissionalId === atual.profissionalId
        ) {
            horarios.add(atual.inicio);
        }

        const horarioPessoaLivre = [...horarios].filter(
            (inicio) =>
                !possuiConflitoAgenda(this.consultas(), {
                    consultaId: atual.id,
                    data,
                    inicio,
                    duracao: atual.duracao,
                    pacienteId: atual.pacienteId,
                    profissionalId,
                    local: '',
                }),
        );

        const conflitaSala = (
            inicio: string,
            sala: string,
        ) =>
            possuiConflitoAgenda(this.consultas(), {
                consultaId: atual.id,
                data,
                inicio,
                duracao: atual.duracao,
                pacienteId: atual.pacienteId,
                profissionalId,
                local: sala,
            });

        const salasCandidatas =
            atual.local && !this.salas.includes(atual.local)
                ? [...this.salas, atual.local]
                : this.salas;

        const horarioSelecionadoPodeUsarSala =
            this.editHora() &&
            horarioPessoaLivre.includes(this.editHora()) &&
            salasCandidatas.some(
                (sala) =>
                    !conflitaSala(this.editHora(), sala),
            );

        const horariosParaValidarSala =
            horarioSelecionadoPodeUsarSala
                ? [this.editHora()]
                : horarioPessoaLivre;

        const semConflito = (sala: string) =>
            horariosParaValidarSala.some(
                (inicio) => !conflitaSala(inicio, sala),
            );

        const salasConhecidas =
            this.salas.filter(semConflito);

        const salaLegadaPreservavel =
            atual.local &&
                !this.salas.includes(atual.local) &&
                this.editSala() === atual.local &&
                semConflito(atual.local)
                ? [atual.local]
                : [];

        return [
            ...salasConhecidas,
            ...salaLegadaPreservavel,
        ];
    });

    private montarRequest():
        | ConsultaRequestApi
        | undefined {
        const atual = this.escolhido()?.a;

        if (!atual) return undefined;

        if (
            !this.editData() ||
            !this.editHora() ||
            !this.editProf()
        ) {
            this.aviso.mostrar(
                'Preencha data, horário e profissional.',
            );

            return undefined;
        }

        if (!this.opcoesHorario().includes(this.editHora())) {
            this.aviso.mostrar(
                'Selecione um horário disponível para a data e o profissional escolhidos.',
            );

            return undefined;
        }

        const salaSelecionada = this.editSala().trim();

        if (
            !salaSelecionada ||
            !salaConsultaPermitida(
                salaSelecionada,
                atual.local,
            ) ||
            !this.opcoesSala().includes(salaSelecionada)
        ) {
            this.aviso.mostrar(
                'Selecione uma sala disponível.',
            );

            return undefined;
        }

        if (
            !this.consultasCarregadas() ||
            this.falhaDisponibilidades()
        ) {
            this.aviso.mostrar(
                'A agenda não foi carregada. Tente novamente antes de salvar.',
            );

            return undefined;
        }

        if (
            possuiConflitoAgenda(this.consultas(), {
                consultaId: atual.id,
                data: this.editData(),
                inicio: this.editHora(),
                duracao: atual.duracao,
                pacienteId: atual.pacienteId,
                profissionalId: this.editProf(),
                local: salaSelecionada,
            })
        ) {
            this.aviso.mostrar(
                'Esse horário, profissional, paciente ou sala conflita com outra consulta. Selecione outra opção.',
            );

            return undefined;
        }

        const profissional = this.profissionais().find(
            (item) => item.id === this.editProf(),
        );

        if (
            atual.atendimento === 'Online' &&
            profissional?.atendeOnline !== true
        ) {
            this.aviso.mostrar(
                'O profissional selecionado não está habilitado para atendimento online.',
            );

            return undefined;
        }

        if (
            atual.atendimento === 'Presencial' &&
            profissional?.atendePresencial === false
        ) {
            this.aviso.mostrar(
                'O profissional selecionado não está habilitado para atendimento presencial.',
            );

            return undefined;
        }

        return {
            pacienteId: atual.pacienteId,
            profissionalId: this.editProf(),
            dataHora: montarDataHora(
                this.editData(),
                this.editHora(),
            ),
            tipoAtendimento: atual.tipo,
            local: salaSelecionada,
            modalidade: atual.atendimento,
            duracao: atual.duracao,
        };
    }

    private atualizarConsulta(
        request: ConsultaRequestApi,
        mensagem: string,
    ): void {
        const atual = this.escolhido()?.a;

        if (!atual || this.executandoAcao()) return;

        this.executandoAcao.set(true);

        this.consultasApi
            .atualizar(atual.id, request)
            .subscribe({
                next: (resposta) => {
                    this.executandoAcao.set(false);
                    this.confirmandoRemarcacao.set(false);
                    this.remarcacaoPendente.set(null);
                    this.editando.set(false);

                    this.semana.set(
                        semanaDe(
                            resposta.dataHora.slice(0, 10),
                            this.segundaBase,
                        ),
                    );

                    this.selecionadoId.set(resposta.id);
                    this.aviso.mostrar(mensagem);
                    this.recarregarConsultas();
                },

                error: (erro) => {
                    this.executandoAcao.set(false);

                    this.aviso.mostrar(
                        erro.error?.message ||
                        'Não foi possível atualizar a consulta. Verifique a disponibilidade e os conflitos de horário.',
                    );
                },
            });
    }

    protected confirmarConsulta(): void {
        const atual = this.escolhido()?.a;

        if (!atual) return;

        if (
            atual.status === 'remarcado' ||
            atual.status === 'cancelado'
        ) {
            this.aviso.mostrar(
                'Esta consulta não pode ser confirmada por esta operação.',
            );

            return;
        }

        if (atual.status === 'confirmado') {
            this.aviso.mostrar(
                'Esta consulta já está confirmada.',
            );

            return;
        }

        this.confirmando.set(true);
    }

    protected executarConfirmacao(): void {
        const atual = this.escolhido()?.a;

        if (!atual || this.executandoAcao()) return;

        this.executandoAcao.set(true);

        this.consultasApi.confirmar(atual.id).subscribe({
            next: () => {
                this.executandoAcao.set(false);
                this.confirmando.set(false);
                this.editando.set(false);
                this.menuStatus.set(false);

                this.aviso.mostrar('Consulta confirmada.');
                this.recarregarConsultas();
            },

            error: (erro) => {
                this.executandoAcao.set(false);

                this.aviso.mostrar(
                    erro.error?.message ||
                    'Não foi possível confirmar a consulta.',
                );
            },
        });
    }

    protected remarcarConsulta(): void {
        if (this.executandoAcao()) return;

        const atual = this.escolhido()?.a;
        const request = this.montarRequest();

        if (!atual || !request) return;

        const sala = this.editSala().trim();

        const mudou = possuiAlteracaoAgenda(
            {
                data: atual.data,
                inicio: atual.inicio,
                profissionalId: atual.profissionalId,
                local: atual.local,
            },
            {
                data: this.editData(),
                inicio: this.editHora(),
                profissionalId: this.editProf(),
                local: sala,
            },
        );

        if (!mudou) {
            this.aviso.mostrar(
                'Altere a data, o horário, o profissional ou a sala para remarcar.',
            );

            return;
        }

        this.remarcacaoPendente.set(request);
        this.confirmandoRemarcacao.set(true);
    }

    protected executarRemarcacao(): void {
        const request = this.remarcacaoPendente();
        const atual = this.escolhido()?.a;

        if (
            !request ||
            !atual ||
            this.executandoAcao()
        ) {
            return;
        }

        const mudouSomenteSala =
            request.local !== atual.local &&
            request.dataHora.slice(0, 10) === atual.data &&
            request.dataHora.slice(11, 16) === atual.inicio &&
            request.profissionalId === atual.profissionalId;

        this.atualizarConsulta(
            request,
            mudouSomenteSala
                ? 'Sala da consulta atualizada.'
                : 'Consulta remarcada.',
        );
    }

    protected nomeProfissional(id: string): string {
        return (
            this.profissionais().find(
                (profissional) => profissional.id === id,
            )?.nome ?? '—'
        );
    }

    protected dataDaRemarcacao(dataHora: string): string {
        return `${formatarData(dataHora.slice(0, 10))} às ${dataHora.slice(11, 16)}`;
    }

    protected confirmarCancelamento(): void {
        const atual = this.escolhido()?.a;

        if (!atual) return;

        if (atual.status === 'cancelado') {
            this.cancelando.set(false);
            this.aviso.mostrar(
                'Esta consulta já está cancelada.',
            );

            return;
        }

        if (this.executandoAcao()) return;

        this.executandoAcao.set(true);

        this.consultasApi.cancelar(atual.id).subscribe({
            next: () => {
                this.executandoAcao.set(false);
                this.cancelando.set(false);
                this.editando.set(false);
                this.menuStatus.set(false);

                this.aviso.mostrar('Consulta cancelada.');
                this.recarregarConsultas();
            },

            error: (erro) => {
                this.executandoAcao.set(false);

                this.aviso.mostrar(
                    erro.error?.message ||
                    'Não foi possível cancelar a consulta.',
                );
            },
        });
    }
}