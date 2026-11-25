import { Component, computed, inject, input, signal } from '@angular/core';

import { finalize } from 'rxjs';

import { Auth } from '../../shared/auth';
import { Aviso } from '../../shared/aviso/aviso';
import { PreferenciasUsuarioApi, PreferenciasUsuarioPatchApi } from '../../shared/clinica/preferencias-api.service';
import { PreferenciasGlobaisService } from '../../shared/clinica/preferencias-globais.service';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone, NomeIcone } from '../../shared/icone/icone';
import { ListaSuspensa, OpcaoLista } from '../../shared/lista-suspensa/lista-suspensa';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { emailValido } from '../../shared/util';
import { validarNovaSenha } from '../../shared/validacao-senha';

type Tema = 'claro' | 'escuro';
type FormatoData = 'dd/mm/aaaa' | 'mm/dd/aaaa' | 'aaaa/mm/dd';
interface PreferenciasTela {
  tema: Tema;
  idioma: 'pt' | 'en';
  formatoData: FormatoData;
  duracaoConsulta: string;
  inicioSemana: 0 | 1;
  lembreteEmail: boolean;
  alertaPendentes: boolean;
  notificacaoSistema: boolean;
}

const PADRAO: PreferenciasTela = {
  tema: 'claro', idioma: 'pt', formatoData: 'dd/mm/aaaa', duracaoConsulta: '50', inicioSemana: 1,
  lembreteEmail: false, alertaPendentes: false, notificacaoSistema: false,
};
const DIAS = [
  { valor: 1 as const, nome: 'Segunda-feira' }, { valor: 0 as const, nome: 'Domingo' },
];
type ChaveNotificacao = 'lembreteEmail' | 'alertaPendentes' | 'notificacaoSistema';
type ChaveTexto = 'idioma' | 'formatoData' | 'duracaoConsulta';

@Component({
  selector: 'app-configuracoes',
  imports: [Dialogo, Icone, ListaSuspensa, TituloPagina],
  templateUrl: './configuracoes.html',
})
export class Configuracoes {
  private readonly preferenciasGlobais = inject(PreferenciasGlobaisService);
  private readonly auth = inject(Auth);
  private readonly aviso = inject(Aviso);
  private preferenciaPersistida: PreferenciasTela = { ...PADRAO };

  readonly perfil = input<Perfil>('admin');
  protected readonly preferencias = signal<PreferenciasTela>({ ...PADRAO });
  protected readonly carregando = signal(true);
  protected readonly salvando = signal(false);
  protected readonly erroCarga = signal('');
  protected readonly semanaAberta = signal(false);
  protected readonly confirmarSalvar = signal(false);
  protected readonly confirmarRestaurar = signal(false);
  protected readonly alterandoSenha = signal(false);
  protected readonly senhaAtual = signal('');
  protected readonly senhaNova = signal('');
  protected readonly senhaConfirmacao = signal('');
  protected readonly erroSenha = signal('');
  protected readonly salvandoSenha = signal(false);
  protected readonly editandoPerfil = signal(false);
  protected readonly nomePerfil = signal('');
  protected readonly emailPerfil = signal('');
  protected readonly salvandoPerfil = signal(false);
  protected readonly erroPerfil = signal('');
  protected readonly mesCalendario = signal(new Date(new Date().getFullYear(), new Date().getMonth(), 1));

  protected readonly nome = computed(() => this.auth.obterNome() ?? '—');
  protected readonly email = computed(() => this.auth.obterEmail() ?? '—');
  protected readonly cargo = computed(() => ({ psicologo: 'Psicólogo', psiquiatra: 'Psiquiatra', secretaria: 'Secretaria', admin: 'Administrador' })[this.perfil()]);
  protected readonly dias = DIAS;
  protected readonly nomeInicioSemana = computed(() => DIAS.find((d) => d.valor === this.preferencias().inicioSemana)?.nome ?? 'Segunda-feira');
  protected readonly rascunho = computed(() => this.preferencias());
  protected readonly editando = this.editandoPerfil;
  protected readonly conta = computed(() => ({
    nome: this.editandoPerfil() ? this.nomePerfil() : this.nome(),
    email: this.editandoPerfil() ? this.emailPerfil() : this.email(),
    especialidade: this.cargo(),
  }));
  protected readonly erroNome = computed(() => this.nomePerfil().trim() ? '' : 'Informe seu nome.');
  protected readonly erroEmail = computed(() => {
    const valor = this.emailPerfil().trim();
    return !valor ? 'Informe seu e-mail.' : !emailValido(valor) ? 'Informe um e-mail válido.' : '';
  });
  protected readonly notificacoes: { chave: ChaveNotificacao; texto: string; icone: NomeIcone }[] = [
    { chave: 'lembreteEmail', texto: 'Receber lembretes de consultas por e-mail', icone: 'sino' },
    { chave: 'alertaPendentes', texto: 'Alertas de consultas pendentes', icone: 'sino' },
    { chave: 'notificacaoSistema', texto: 'Notificações do sistema', icone: 'sino' },
  ];
  protected readonly semanaEscolhida = signal<0 | 1>(1);
  protected readonly cabecalhoSemana = computed(() => {
    const dias = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'];
    const inicio = this.semanaEscolhida();
    return Array.from({ length: 7 }, (_, indice) => dias[(inicio + indice) % 7]);
  });
  protected readonly nomeMes = computed(() => this.mesCalendario().toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' }));
  protected readonly diasDoMes = computed(() => {
    const data = this.mesCalendario();
    const primeiroDia = new Date(data.getFullYear(), data.getMonth(), 1).getDay();
    const deslocamento = (primeiroDia - this.semanaEscolhida() + 7) % 7;
    const total = new Date(data.getFullYear(), data.getMonth() + 1, 0).getDate();
    const dias: (number | null)[] = [
      ...Array.from({ length: deslocamento }, () => null),
      ...Array.from({ length: total }, (_, indice) => indice + 1),
    ];
    while (dias.length % 7) dias.push(null);
    return dias;
  });
  protected readonly diaDestaque = computed(() => {
    const agora = new Date();
    const mes = this.mesCalendario();
    return agora.getFullYear() === mes.getFullYear() && agora.getMonth() === mes.getMonth()
      ? agora.getDate()
      : -1;
  });
  protected readonly idiomas: OpcaoLista[] = [
    { valor: 'pt', rotulo: '🇧🇷 Português' },
    { valor: 'en', rotulo: '🇺🇸 English (em breve)', desabilitada: true },
  ];
  protected readonly formatosData: OpcaoLista[] = [
    { valor: 'dd/mm/aaaa', rotulo: 'DD/MM/AAAA' },
    { valor: 'mm/dd/aaaa', rotulo: 'MM/DD/AAAA' },
    { valor: 'aaaa/mm/dd', rotulo: 'AAAA/MM/DD' },
  ];
  protected readonly duracoes: OpcaoLista[] = ['30', '40', '50', '60'].map((m) => ({ valor: m, rotulo: `${m} minutos` }));

  constructor() {
    this.carregar();
  }

  protected carregar(forcar = false): void {
    this.carregando.set(true);
    this.erroCarga.set('');
    this.preferenciasGlobais.carregar(forcar).pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: (dto) => {
        this.aplicarResposta(dto);
        if (this.preferenciasGlobais.erroCarregamento()) {
          this.erroCarga.set('Não foi possível carregar suas preferências. Tente novamente.');
        }
      },
      error: () => this.erroCarga.set('Não foi possível carregar suas preferências. Tente novamente.'),
    });
  }

  protected definir<K extends keyof PreferenciasTela>(chave: K, valor: PreferenciasTela[K]): void {
    this.preferencias.update((p) => ({ ...p, [chave]: valor }));
    this.aplicarRascunhoGlobal();
  }

  protected definirTexto(chave: ChaveTexto, valor: string): void {
    if (chave === 'idioma') {
      if (valor === 'pt') this.definir('idioma', 'pt');
      return;
    }
    if (chave === 'formatoData') {
      this.definirFormatoData(valor);
      return;
    }
    if (['30', '40', '50', '60'].includes(valor)) this.definir('duracaoConsulta', valor);
  }

  protected definirTema(valor: Tema): void {
    this.definir('tema', valor);
  }

  protected definirFormatoData(valor: string): void {
    if (valor === 'dd/mm/aaaa' || valor === 'mm/dd/aaaa' || valor === 'aaaa/mm/dd') this.definir('formatoData', valor);
  }

  protected alternar(chave: ChaveNotificacao): void {
    this.definir(chave, !this.preferencias()[chave]);
  }

  protected abrirSemana(): void {
    this.semanaEscolhida.set(this.preferencias().inicioSemana);
    this.mesCalendario.set(new Date(new Date().getFullYear(), new Date().getMonth(), 1));
    this.semanaAberta.set(true);
  }

  protected mudarMes(deslocamento: number): void {
    const atual = this.mesCalendario();
    this.mesCalendario.set(new Date(atual.getFullYear(), atual.getMonth() + deslocamento, 1));
  }

  protected salvarSemana(): void {
    this.definir('inicioSemana', this.semanaEscolhida());
    this.semanaAberta.set(false);
  }

  protected pedirSalvar(): void {
    if (!this.carregando() && !this.erroCarga() && !this.salvando()) this.confirmarSalvar.set(true);
  }

  protected salvar(): void {
    if (this.carregando() || this.erroCarga() || this.salvando()) return;
    this.salvando.set(true);
    this.preferenciasGlobais.salvar(this.paraPatch(this.preferencias())).pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: (dto) => {
        this.aplicarResposta(dto);
        this.confirmarSalvar.set(false);
        this.aviso.mostrar('Configurações salvas.');
      },
      error: () => {
        this.reverterRascunho();
        this.aviso.mostrar('Não foi possível salvar as configurações. Tente novamente.');
      },
    });
  }

  protected restaurar(): void {
    if (this.carregando() || this.erroCarga() || this.salvando()) return;
    this.salvando.set(true);
    this.preferenciasGlobais.salvar(this.paraPatch(PADRAO)).pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: (dto) => {
        this.aplicarResposta(dto);
        this.confirmarRestaurar.set(false);
        this.aviso.mostrar('Preferências restauradas para o padrão.');
      },
      error: () => {
        this.reverterRascunho();
        this.aviso.mostrar('Não foi possível restaurar as preferências.');
      },
    });
  }

  protected abrirAlteracaoSenha(): void {
    this.erroSenha.set('');
    this.senhaAtual.set('');
    this.senhaNova.set('');
    this.senhaConfirmacao.set('');
    this.alterandoSenha.set(true);
  }

  protected cancelarAlteracaoSenha(): void {
    this.alterandoSenha.set(false);
    this.erroSenha.set('');
    this.senhaAtual.set('');
    this.senhaNova.set('');
    this.senhaConfirmacao.set('');
  }

  protected async alterarSenha(evento?: Event): Promise<void> {
    evento?.preventDefault();
    if (this.salvandoSenha()) return;
    this.erroSenha.set('');
    const erro = validarNovaSenha(this.senhaNova(), this.senhaConfirmacao());
    if (erro) {
      this.erroSenha.set(erro);
      return;
    }
    if (!this.senhaAtual()) {
      this.erroSenha.set('Informe sua senha atual.');
      return;
    }
    this.salvandoSenha.set(true);
    this.auth.alterarSenhaAutenticada(this.senhaAtual(), this.senhaNova(), this.senhaConfirmacao())
      .pipe(finalize(() => this.salvandoSenha.set(false)))
      .subscribe({
        next: () => {
          this.cancelarAlteracaoSenha();
          this.aviso.mostrar('Senha alterada. Entre novamente com a nova senha.');
        },
        error: (erro) => this.erroSenha.set(erro?.error?.message ?? 'Não foi possível alterar a senha. Confira a senha atual.'),
      });
  }

  protected abrirEdicaoPerfil(): void {
    this.nomePerfil.set(this.nome() === '—' ? '' : this.nome());
    this.emailPerfil.set(this.email() === '—' ? '' : this.email());
    this.erroPerfil.set('');
    this.editandoPerfil.set(true);
  }

  protected concluirEdicaoPerfil(): void {
    if (this.editandoPerfil()) this.salvarPerfil();
    else this.abrirEdicaoPerfil();
  }

  protected digitarConta(campo: 'nome' | 'email', evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value;
    if (campo === 'nome') this.nomePerfil.set(valor);
    else this.emailPerfil.set(valor);
  }

  protected salvarPerfil(evento?: Event): void {
    evento?.preventDefault();
    if (this.salvandoPerfil()) return;
    const nome = this.nomePerfil().trim();
    const email = this.emailPerfil().trim();
    if (this.erroNome() || this.erroEmail()) {
      this.erroPerfil.set(this.erroNome() || this.erroEmail());
      return;
    }
    this.salvandoPerfil.set(true);
    this.erroPerfil.set('');
    this.auth.atualizarPerfil(nome, email)
      .pipe(finalize(() => this.salvandoPerfil.set(false)))
      .subscribe({
        next: () => {
          this.editandoPerfil.set(false);
          this.editando.set(false);
          this.aviso.mostrar('Perfil atualizado.');
        },
        error: (erro) => this.erroPerfil.set(erro?.error?.mensagem ?? erro?.error?.message
          ?? 'Não foi possível atualizar o perfil. Confira os dados e tente novamente.'),
      });
  }

  private aplicarResposta(dto: PreferenciasUsuarioApi): void {
    this.preferencias.set({
      tema: dto.tema === 'ESCURO' ? 'escuro' : 'claro',
      idioma: dto.idioma === 'INGLES' ? 'en' : 'pt',
      formatoData: dto.formatoData === 'MM/DD/AAAA' ? 'mm/dd/aaaa' : dto.formatoData === 'AAAA/MM/DD' ? 'aaaa/mm/dd' : 'dd/mm/aaaa',
      duracaoConsulta: String(dto.duracaoPadraoConsulta ?? 50),
      inicioSemana: dto.inicioSemana === 'DOMINGO' ? 0 : 1,
      lembreteEmail: dto.lembretesConsultaEmail ?? false,
      alertaPendentes: dto.alertasConsultasPendentes ?? false,
      notificacaoSistema: dto.notificacoesSistema ?? false,
    });
    this.preferenciaPersistida = { ...this.preferencias() };
    this.preferenciasGlobais.aplicarRascunho(this.paraPatch(this.preferencias()));
  }

  protected cancelarSalvar(): void {
    this.confirmarSalvar.set(false);
    this.reverterRascunho();
  }

  private reverterRascunho(): void {
    this.preferencias.set({ ...this.preferenciaPersistida });
    this.preferenciasGlobais.aplicarRascunho(this.paraPatch(this.preferenciaPersistida));
  }

  private aplicarRascunhoGlobal(): void {
    // Tema e formato são aplicados de imediato; a API só é atualizada ao confirmar Salvar.
    this.preferenciasGlobais.aplicarRascunho(this.paraPatch(this.preferencias()));
  }

  private paraPatch(p: PreferenciasTela): PreferenciasUsuarioPatchApi {
    return {
      tema: p.tema === 'escuro' ? 'ESCURO' : 'CLARO',
      idioma: p.idioma === 'en' ? 'INGLES' : 'PORTUGUES',
      formatoData: p.formatoData === 'mm/dd/aaaa' ? 'MM/DD/AAAA' : p.formatoData === 'aaaa/mm/dd' ? 'AAAA/MM/DD' : 'DD/MM/AAAA',
      duracaoPadraoConsulta: Number(p.duracaoConsulta),
      inicioSemana: p.inicioSemana === 0 ? 'DOMINGO' : 'SEGUNDA-FEIRA',
      lembretesConsultaEmail: p.lembreteEmail,
      alertasConsultasPendentes: p.alertaPendentes,
      notificacoesSistema: p.notificacaoSistema,
    };
  }
}
