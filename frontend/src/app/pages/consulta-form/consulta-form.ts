import {
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';

import { Aviso } from '../../shared/aviso/aviso';
import { Avatar } from '../../shared/avatar/avatar';
import {
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
import {
  ResponsavelApi,
  ResponsaveisApiService,
} from '../../shared/clinica/responsaveis-api.service';
import {
  HORARIOS_CONSULTA,
  minutosDe,
} from '../../shared/clinica/status';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import {
  ListaSuspensa,
  OpcaoLista,
} from '../../shared/lista-suspensa/lista-suspensa';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import {
  MESES_CURTOS,
  deISO,
  formatarData,
  idadeDe,
  somarDias,
} from '../../shared/util';

const NOMES_DIAS = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
const DURACAO = 30;

interface ResponsavelFormulario {
  nome: string;
  relacao: string;
  telefone: string;
  email: string;
}

interface PacienteFormulario {
  id: string;
  nome: string;
  nascimento: string;
  telefone: string;
  email: string;
  foto?: string;
  quantidadeResponsaveis: number;
  responsavel: ResponsavelFormulario;
}

@Component({
  selector: 'app-consulta-form',
  imports: [Avatar, Dialogo, Icone, ListaSuspensa, TituloPagina],
  templateUrl: './consulta-form.html',
})
export class ConsultaForm {
  private readonly pacientesApi = inject(PacientesApiService);
  private readonly profissionaisApi = inject(ProfissionaisApiService);
  private readonly responsaveisApi = inject(ResponsaveisApiService);
  private readonly consultasApi = inject(ConsultasApiService);
  private readonly aviso = inject(Aviso);
  private readonly router = inject(Router);

  readonly perfil = input<Perfil>('secretaria');

  private readonly hoje = this.dataAtual();

  protected readonly pacientes = signal<PacienteApi[]>([]);
  protected readonly profissionais = signal<ProfissionalApi[]>([]);
  protected readonly responsaveisPorId =
    signal<Record<string, ResponsavelApi>>({});
  protected readonly disponibilidades = signal<DisponibilidadeApi[]>([]);
  protected readonly carregando = signal(true);
  protected readonly carregandoDisponibilidade = signal(false);
  protected readonly erroDisponibilidade = signal(false);
  protected readonly salvando = signal(false);

  protected readonly pacienteId = signal('');
  protected readonly profissionalId = signal('');
  protected readonly atendimento =
    signal<'Presencial' | 'Online'>('Presencial');
  protected readonly local = signal('Unidade Santa Maria');
  protected readonly semana = signal(0);
  protected readonly dia = signal(this.hoje);
  protected readonly hora = signal('');
  protected readonly tentou = signal(false);
  protected readonly confirmando = signal(false);

  protected readonly opcoesAtendimento: OpcaoLista[] = [
    { valor: 'Presencial', rotulo: 'Presencial' },
    { valor: 'Online', rotulo: 'Online' },
  ];

  protected readonly opcoesPacientes = computed<OpcaoLista[]>(() =>
    this.pacientes().map((p) => ({
      valor: p.id,
      rotulo: `${p.nome} - ${idadeDe(p.dataNascimento)} anos`,
    })),
  );

  protected readonly opcoesProfissionais = computed<OpcaoLista[]>(() =>
    this.profissionais()
      .filter((p) => p.ativo)
      .map((p) => ({
        valor: p.id,
        rotulo: `${p.nome} - ${p.categoria || 'Profissional'}`,
      })),
  );

  protected readonly paciente = computed<PacienteFormulario | undefined>(
    () => {
      const p = this.pacientes().find(
        (item) => item.id === this.pacienteId(),
      );

      if (!p) return undefined;

      const vinculo = p.responsaveis?.[0];

      const responsavel = vinculo
        ? this.responsaveisPorId()[vinculo.responsavelId]
        : undefined;

      return {
        id: p.id,
        nome: p.nome,
        nascimento: p.dataNascimento,
        telefone: p.telefone,
        email: p.email,
        foto: undefined,
        quantidadeResponsaveis: p.responsaveis?.length ?? 0,
        responsavel: {
          nome: responsavel?.nome ?? '',
          relacao: vinculo?.grauParentesco ?? '',
          telefone: responsavel?.telefone ?? '',
          email: responsavel?.email ?? '',
        },
      };
    },
  );

  protected readonly profissional = computed(() =>
    this.profissionais().find(
      (p) => p.id === this.profissionalId(),
    ),
  );

  protected readonly idade = computed(() =>
    this.paciente() ? idadeDe(this.paciente()!.nascimento) : 0,
  );

  protected readonly rotuloProfissional = computed(() => {
    const p = this.profissional();

    return p
      ? `${p.nome} - ${p.categoria || 'Profissional'}`
      : '';
  });

  protected readonly dias = computed(() =>
    NOMES_DIAS.map((nome, i) => {
      const iso = somarDias(
        this.segundaInicial(),
        this.semana() * 7 + i,
      );

      const d = deISO(iso);

      return {
        nome,
        iso,
        numero: String(d.getDate()).padStart(2, '0'),
        mes: MESES_CURTOS[d.getMonth()],
        passado: iso < this.hoje,
      };
    }),
  );

  protected readonly horarios = computed(() =>
    HORARIOS_CONSULTA.map((hora) => ({
      hora,
      livre: this.horarioPodeSerSelecionado(hora),
    })),
  );

  protected readonly horaValida = computed(() => {
    const h = this.hora();

    return h &&
      this.horarios().some(
        (item) => item.hora === h && item.livre,
      )
      ? h
      : '';
  });

  protected readonly dicaHorarios = computed(() => {
    if (!this.profissional()) {
      return 'Selecione um profissional para escolher o horário.';
    }

    if (
      this.atendimento() === 'Online' &&
      !this.profissional()?.atendeOnline
    ) {
      return 'Este profissional não está habilitado para atendimento online.';
    }

    if (
      this.atendimento() === 'Presencial' &&
      this.profissional()?.atendePresencial === false
    ) {
      return 'Este profissional não está habilitado para atendimento presencial.';
    }

    if (this.carregandoDisponibilidade()) {
      return 'Carregando a disponibilidade do profissional.';
    }

    if (this.erroDisponibilidade()) {
      return 'Não foi possível consultar a disponibilidade deste profissional.';
    }

    if (this.disponibilidadesAtivasDoDia().length === 0) {
      return 'Este profissional não possui disponibilidade cadastrada para este dia.';
    }

    return 'Os conflitos com outras consultas serão validados pelo servidor ao salvar.';
  });

  private readonly disponibilidadesAtivasDoDia = computed(() => {
    const diaSemana = this.nomeDiaSemana(this.dia());

    return this.disponibilidades().filter(
      (item) =>
        item.ativo &&
        this.normalizarDia(item.diaSemana) === diaSemana,
    );
  });

  constructor() {
    this.carregarCadastros();

    effect((onCleanup) => {
      const profissionalId = this.profissionalId();

      this.disponibilidades.set([]);
      this.erroDisponibilidade.set(false);

      if (!profissionalId) {
        this.carregandoDisponibilidade.set(false);
        return;
      }

      this.carregandoDisponibilidade.set(true);

      const requisicao = this.profissionaisApi
        .listarDisponibilidades(profissionalId)
        .subscribe({
          next: (lista) => {
            this.disponibilidades.set(lista);
            this.erroDisponibilidade.set(false);
            this.carregandoDisponibilidade.set(false);
          },
          error: () => {
            this.disponibilidades.set([]);
            this.erroDisponibilidade.set(true);
            this.carregandoDisponibilidade.set(false);

            this.aviso.mostrar(
              'Não foi possível carregar a disponibilidade do profissional.',
            );
          },
        });

      onCleanup(() => requisicao.unsubscribe());
    });
  }

  private carregarCadastros(): void {
    this.carregando.set(true);

    forkJoin({
      pacientes: this.pacientesApi.listarTodos().pipe(
        catchError(() => {
          this.aviso.mostrar(
            'Não foi possível carregar os pacientes.',
          );

          return of([] as PacienteApi[]);
        }),
      ),
      profissionais: this.profissionaisApi.listarTodos().pipe(
        catchError(() => {
          this.aviso.mostrar(
            'Não foi possível carregar os profissionais.',
          );

          return of([] as ProfissionalApi[]);
        }),
      ),
    }).subscribe({
      next: ({ pacientes, profissionais }) => {
        this.pacientes.set(pacientes);
        this.profissionais.set(profissionais);
        this.carregarResponsaveis(pacientes);
      },
      error: () => {
        this.aviso.mostrar(
          'Não foi possível carregar os dados da consulta.',
        );
      },
      complete: () => this.carregando.set(false),
    });
  }

  private carregarResponsaveis(pacientes: PacienteApi[]): void {
    const ids = [
      ...new Set(
        pacientes.flatMap((paciente) =>
          (paciente.responsaveis ?? []).map(
            (vinculo) => vinculo.responsavelId,
          ),
        ),
      ),
    ].filter(Boolean);

    if (ids.length === 0) {
      this.responsaveisPorId.set({});
      return;
    }

    const requisicoes = ids.map((id) =>
      this.responsaveisApi.buscarPorId(id).pipe(
        catchError(() => of(null)),
      ),
    );

    forkJoin(requisicoes).subscribe((lista) => {
      const mapa: Record<string, ResponsavelApi> = {};

      lista.forEach((responsavel) => {
        if (responsavel) {
          mapa[responsavel.id] = responsavel;
        }
      });

      this.responsaveisPorId.set(mapa);
    });
  }

  private dataAtual(): string {
    const agora = new Date();
    const ano = agora.getFullYear();
    const mes = String(agora.getMonth() + 1).padStart(2, '0');
    const dia = String(agora.getDate()).padStart(2, '0');

    return `${ano}-${mes}-${dia}`;
  }

  private segundaInicial(): string {
    const data = deISO(this.hoje);
    const diaSemana = (data.getDay() + 6) % 7;

    data.setDate(data.getDate() - diaSemana);

    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');

    return `${ano}-${mes}-${dia}`;
  }

  private nomeDiaSemana(iso: string): string {
    const nomes = [
      'DOMINGO',
      'SEGUNDA',
      'TERCA',
      'QUARTA',
      'QUINTA',
      'SEXTA',
      'SABADO',
    ];

    return nomes[deISO(iso).getDay()];
  }

  private normalizarDia(dia: string): string {
    const valor = (dia ?? '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toUpperCase()
      .trim()
      .replace(/[\s-]+/g, '_');

    const equivalencias: Record<string, string> = {
      DOM: 'DOMINGO',
      DOMINGO: 'DOMINGO',
      SUN: 'DOMINGO',
      SUNDAY: 'DOMINGO',

      SEG: 'SEGUNDA',
      SEGUNDA: 'SEGUNDA',
      SEGUNDA_FEIRA: 'SEGUNDA',
      MON: 'SEGUNDA',
      MONDAY: 'SEGUNDA',

      TER: 'TERCA',
      TERCA: 'TERCA',
      TERCA_FEIRA: 'TERCA',
      TUE: 'TERCA',
      TUESDAY: 'TERCA',

      QUA: 'QUARTA',
      QUARTA: 'QUARTA',
      QUARTA_FEIRA: 'QUARTA',
      WED: 'QUARTA',
      WEDNESDAY: 'QUARTA',

      QUI: 'QUINTA',
      QUINTA: 'QUINTA',
      QUINTA_FEIRA: 'QUINTA',
      THU: 'QUINTA',
      THURSDAY: 'QUINTA',

      SEX: 'SEXTA',
      SEXTA: 'SEXTA',
      SEXTA_FEIRA: 'SEXTA',
      FRI: 'SEXTA',
      FRIDAY: 'SEXTA',

      SAB: 'SABADO',
      SABADO: 'SABADO',
      SAT: 'SABADO',
      SATURDAY: 'SABADO',
    };

    return equivalencias[valor] ?? valor;
  }

  private horarioPodeSerSelecionado(hora: string): boolean {
    if (
      !this.profissionalId() ||
      this.carregandoDisponibilidade() ||
      this.erroDisponibilidade()
    ) {
      return false;
    }

    if (this.dia() < this.hoje) return false;

    if (this.dia() === this.hoje) {
      const agora = new Date();
      const minutosAgora =
        agora.getHours() * 60 + agora.getMinutes();

      if (minutosDe(hora) <= minutosAgora) return false;
    }

    const inicioConsulta = minutosDe(hora);
    const fimConsulta = inicioConsulta + DURACAO;

    return this.disponibilidadesAtivasDoDia().some(
      (disponibilidade) => {
        const inicioDisponivel = minutosDe(
          disponibilidade.horaInicio,
        );

        const fimDisponivel = minutosDe(
          disponibilidade.horaFim,
        );

        return (
          inicioConsulta >= inicioDisponivel &&
          fimConsulta <= fimDisponivel
        );
      },
    );
  }

  protected escolherDia(iso: string, passado: boolean): void {
    if (!passado) {
      this.dia.set(iso);
      this.hora.set('');
    }
  }

  protected mudarSemana(delta: number): void {
    const proxima = Math.max(0, this.semana() + delta);

    this.semana.set(proxima);

    const primeiroDia = somarDias(
      this.segundaInicial(),
      proxima * 7,
    );

    this.dia.set(
      primeiroDia < this.hoje ? this.hoje : primeiroDia,
    );

    this.hora.set('');
  }

  protected readonly formatar = formatarData;

  protected erroPaciente = () =>
    this.tentou() && !this.pacienteId();

  protected erroProfissional = () =>
    this.tentou() && !this.profissionalId();

  protected salvar(): void {
    this.tentou.set(true);

    if (!this.pacienteId()) {
      this.aviso.mostrar('Selecione o paciente.');
      return;
    }

    if (!this.profissionalId()) {
      this.aviso.mostrar('Selecione o profissional.');
      return;
    }

    if (!this.horaValida()) {
      this.aviso.mostrar('Escolha um horário válido.');
      return;
    }

    if (
      this.atendimento() === 'Online' &&
      !this.profissional()?.atendeOnline
    ) {
      this.aviso.mostrar(
        'Este profissional não atende online.',
      );
      return;
    }

    if (
      this.atendimento() === 'Presencial' &&
      this.profissional()?.atendePresencial === false
    ) {
      this.aviso.mostrar(
        'Este profissional não está habilitado para atendimento presencial.',
      );
      return;
    }

    this.confirmando.set(true);
  }

  protected confirmar(): void {
    if (this.salvando()) return;

    const paciente = this.paciente();
    const profissional = this.profissional();
    const hora = this.horaValida();

    if (!paciente || !profissional || !hora) {
      this.confirmando.set(false);
      this.aviso.mostrar('Confira os dados da consulta.');
      return;
    }

    const dados: ConsultaRequestApi = {
      pacienteId: paciente.id,
      profissionalId: profissional.id,
      dataHora: `${this.dia()}T${hora}:00`,
      tipoAtendimento: 'Consulta',
      local:
        this.local().trim() ||
        (this.atendimento() === 'Online'
          ? 'Online'
          : 'Unidade Santa Maria'),
      modalidade: this.atendimento(),
      duracao: DURACAO,
    };

    this.salvando.set(true);

    this.consultasApi.criar(dados).subscribe({
      next: (consulta) => {
        this.confirmando.set(false);
        this.salvando.set(false);

        this.aviso.mostrar(
          'Consulta agendada com sucesso. Ela ficará pendente até a confirmação.',
        );

        this.router.navigate(['/', this.perfil(), 'agenda'], {
          queryParams: { consulta: consulta.id },
        });
      },
      error: (erro: HttpErrorResponse) => {
        this.salvando.set(false);
        this.confirmando.set(false);

        const mensagem =
          erro.error?.message ||
          erro.error?.mensagem ||
          'Não foi possível agendar. Verifique a disponibilidade do profissional e os conflitos de horário.';

        this.aviso.mostrar(mensagem);
      },
    });
  }

  protected cancelar(): void {
    this.router.navigate(['/', this.perfil()]);
  }
}
