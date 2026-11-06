
import {
  Component,
  ElementRef,
  computed,
  effect,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';

import { Aviso } from '../../shared/aviso/aviso';
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
import { StatusConsulta } from '../../shared/clinica/modelos';
import {
  HORARIOS_AGENDA,
  ORDEM_STATUS,
  ROTULO_STATUS,
  SALAS,
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

@Component({
  selector: 'app-agenda',
  imports: [Avatar, Dialogo, Icone, TituloPagina],
  templateUrl: './agenda.html',
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

  readonly perfil = input<Perfil>('psicologo');
  readonly consulta = input<string>();

  private readonly hojeBase = dataAtualISO();
  private readonly segundaBase = segundaDaSemana(this.hojeBase);

  protected readonly horarios = HORARIOS_AGENDA;
  protected readonly linhas = HORARIOS_AGENDA.map((_, i) => i);
  protected readonly colunas = [0, 1, 2, 3, 4, 5];
  protected readonly statusLista = ORDEM_STATUS;
  protected readonly rotulo = ROTULO_STATUS;
  protected readonly salas = SALAS;

  protected readonly consultas = signal<AgendamentoApiView[]>([]);
  protected readonly pacientes = signal<PacienteApi[]>([]);
  protected readonly profissionais = signal<ProfissionalApi[]>([]);
  protected readonly disponibilidades = signal<DisponibilidadeApi[]>([]);
  protected readonly carregandoDisponibilidades = signal(false);
  protected readonly carregando = signal(true);

  protected readonly semana = signal(0);
  protected readonly busca = signal('');
  protected readonly selecionadoId = signal('');
  protected readonly menuStatus = signal(false);

  protected readonly editando = signal(false);
  protected readonly cancelando = signal(false);
  protected readonly editData = signal('');
  protected readonly editHora = signal('');
  protected readonly editProf = signal('');
  protected readonly editSala = signal('');

  private readonly seletorData =
    viewChild<ElementRef<HTMLInputElement>>('seletorData');

  private readonly menuRaiz =
    viewChild<ElementRef<HTMLElement>>('menuStatusRaiz');

  protected readonly secretaria = computed(
    () => this.perfil() === 'secretaria' || this.perfil() === 'admin',
  );

  protected readonly hojeISO = this.hojeBase;
  protected readonly dataHoje = formatarData(this.hojeBase);

  constructor() {
    this.carregarDados();

    effect(() => {
      const id = this.consulta();

      if (!id) return;

      const alvo = this.consultas().find((item) => item.id === id);

      if (alvo) {
        this.selecionadoId.set(alvo.id);
        this.semana.set(semanaDe(alvo.data, this.segundaBase));
      }
    });

    effect((onCleanup) => {
      const estaEditando = this.editando();
      const profissionalId = this.editProf();

      if (!estaEditando || !profissionalId) {
        return;
      }

      this.carregandoDisponibilidades.set(true);

      const inscricao = this.profissionaisApi
        .listarDisponibilidades(profissionalId)
        .subscribe({
          next: (lista) => {
            this.disponibilidades.set(lista);
            this.carregandoDisponibilidades.set(false);
          },
          error: () => {
            this.disponibilidades.set([]);
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

    this.consultasApi.listarTodas().subscribe({
      next: (lista) => {
        const convertidas = lista.map(converterConsulta);

        this.consultas.set(convertidas);

        const idDaRota = this.consulta();
        const selecionada = convertidas.find(
          (item) => item.id === idDaRota,
        );

        if (selecionada) {
          this.selecionadoId.set(selecionada.id);
          this.semana.set(
            semanaDe(selecionada.data, this.segundaBase),
          );
        } else if (!this.selecionadoId() && convertidas.length > 0) {
          this.selecionadoId.set(convertidas[0].id);
        }

        this.carregando.set(false);

        this.carregarProfissionais(convertidas);
      },
      error: (erro) => {
        this.carregando.set(false);

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

  private carregarProfissionais(
    consultas: AgendamentoApiView[],
  ): void {
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

    const profissionalId = consultas[0]?.profissionalId;

    if (!profissionalId) {
      this.profissionais.set([]);
      return;
    }

    this.profissionaisApi.buscarPorId(profissionalId).subscribe({
      next: (profissional) => {
        this.profissionais.set([profissional]);
      },
      error: () => {
        this.profissionais.set([]);
        this.aviso.mostrar(
          'Não foi possível carregar os dados do profissional.',
        );
      },
    });
  }

  private recarregarConsultas(): void {
    this.consultasApi.listarTodas().subscribe({
      next: (lista) => {
        const convertidas = lista.map(converterConsulta);

        this.consultas.set(convertidas);
      },
      error: () =>
        this.aviso.mostrar(
          'A alteração foi enviada, mas não foi possível atualizar a agenda.',
        ),
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
      .filter((a) => a.data >= primeiro && a.data <= ultimo)
      .map((a) => {
        const paciente = this.pacientes().find(
          (p) => p.id === a.pacienteId,
        );

        const profissional = this.profissionais().find(
          (p) => p.id === a.profissionalId,
        );

        return {
          ...a,
          nome: paciente?.nome ?? 'Paciente',
          profNome: profissional?.nome ?? '—',
          dia: diaDaSemana(a.data),
          linha: (minutosDe(a.inicio) - 8 * 60) / 30 + 2,
          trechos: Math.max(1, a.duracao / 30),
          destaque:
            !termo ||
            normalizar(paciente?.nome ?? '').includes(termo),
          selecionado: a.id === this.selecionadoId(),
        };
      });
  });

  private readonly consultasHoje = computed(() =>
    this.consultas().filter((a) => a.data === this.hojeBase),
  );

  protected readonly resumo = computed(() => {
    const lista = this.consultasHoje();

    const conta = (status: StatusConsulta) =>
      lista.filter((a) => a.status === status).length;

    const ocupados = lista
      .filter((a) => a.status !== 'cancelado')
      .reduce((total, a) => total + a.duracao / 30, 0);

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
        valor: Math.max(0, HORARIOS_AGENDA.length - ocupados),
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
        (a, b) => minutosDe(a.inicio) - minutosDe(b.inicio),
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
        semanaDe(paraISO(deISO(valor)), this.segundaBase),
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

      this.consultasApi.confirmar(consulta.id).subscribe({
        next: () => {
          this.menuStatus.set(false);
          this.aviso.mostrar('Consulta confirmada.');
          this.recarregarConsultas();
        },
        error: (erro) => {
          this.menuStatus.set(false);
          this.aviso.mostrar(
            erro.error?.message ||
            'Não foi possível confirmar a consulta.',
          );
        },
      });

      return;
    }

    if (status === 'cancelado') {
      this.consultasApi.cancelar(consulta.id).subscribe({
        next: () => {
          this.menuStatus.set(false);
          this.aviso.mostrar('Consulta cancelada.');
          this.recarregarConsultas();
        },
        error: (erro) => {
          this.menuStatus.set(false);
          this.aviso.mostrar(
            erro.error?.message ||
            'Não foi possível cancelar a consulta.',
          );
        },
      });

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

    if (!atual || !data || !profissionalId) return [];

    const dia = diaDaData(data);

    const disponibilidadesDoDia = this.disponibilidades().filter(
      (disponibilidade) => {
        const diaDisponibilidade = normalizarDiaDisponibilidade(
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

    return HORARIOS_AGENDA.filter((horario) => {
      // Mantém o horário atual visível durante a edição.
      if (horario === atual.inicio) return true;

      // Não oferece datas anteriores a hoje.
      if (data < this.hojeBase) return false;

      // Não oferece horários que já passaram hoje.
      if (
        data === this.hojeBase &&
        minutosDe(horario) <= minutosAgora
      ) {
        return false;
      }

      const inicio = minutosDe(horario);
      const fim = inicio + atual.duracao;

      // A consulta inteira precisa caber na disponibilidade.
      return disponibilidadesDoDia.some((disponibilidade) => {
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
    });
  });

  private montarRequest(): ConsultaRequestApi | undefined {
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

    return {
      pacienteId: atual.pacienteId,
      profissionalId: this.editProf(),
      dataHora: montarDataHora(
        this.editData(),
        this.editHora(),
      ),
      tipoAtendimento: atual.tipo,
      local: this.editSala().trim() || atual.local,
      modalidade: atual.atendimento,
      duracao: atual.duracao,
    };
  }

  private atualizarConsulta(
    request: ConsultaRequestApi,
    mensagem: string,
  ): void {
    const atual = this.escolhido()?.a;

    if (!atual) return;

    this.consultasApi.atualizar(atual.id, request).subscribe({
      next: (resposta) => {
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

    this.consultasApi.confirmar(atual.id).subscribe({
      next: () => {
        this.editando.set(false);
        this.aviso.mostrar('Consulta confirmada.');
        this.recarregarConsultas();
      },
      error: (erro) => {
        this.aviso.mostrar(
          erro.error?.message ||
          'Não foi possível confirmar a consulta.',
        );
      },
    });
  }

  protected remarcarConsulta(): void {
    const atual = this.escolhido()?.a;
    const request = this.montarRequest();

    if (!atual || !request) return;

    const mudou =
      this.editData() !== atual.data ||
      this.editHora() !== atual.inicio ||
      this.editProf() !== atual.profissionalId;

    if (!mudou) {
      this.aviso.mostrar(
        'Altere a data, o horário ou o profissional para remarcar.',
      );
      return;
    }

    this.atualizarConsulta(
      request,
      'Consulta remarcada.',
    );
  }

  protected confirmarCancelamento(): void {
    const atual = this.escolhido()?.a;

    if (!atual) return;

    this.consultasApi.cancelar(atual.id).subscribe({
      next: () => {
        this.cancelando.set(false);
        this.editando.set(false);
        this.aviso.mostrar('Consulta cancelada.');
        this.recarregarConsultas();
      },
      error: (erro) => {
        this.aviso.mostrar(
          erro.error?.message ||
          'Não foi possível cancelar a consulta.',
        );
      },
    });
  }
}