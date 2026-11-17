import { Component, computed, inject, input, signal } from '@angular/core';

import { finalize } from 'rxjs';

import { Auth } from '../../shared/auth';
import { Aviso } from '../../shared/aviso/aviso';
import { PreferenciasUsuarioApi, PreferenciasUsuarioPatchApi } from '../../shared/clinica/preferencias-api.service';
import { PreferenciasGlobaisService } from '../../shared/clinica/preferencias-globais.service';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { ListaSuspensa, OpcaoLista } from '../../shared/lista-suspensa/lista-suspensa';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';

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

  protected readonly nome = computed(() => this.auth.obterNome() ?? '—');
  protected readonly email = computed(() => this.auth.obterEmail() ?? '—');
  protected readonly cargo = computed(() => ({ psicologo: 'Psicólogo', psiquiatra: 'Psiquiatra', secretaria: 'Secretaria', admin: 'Administrador' })[this.perfil()]);
  protected readonly dias = DIAS;
  protected readonly nomeInicioSemana = computed(() => DIAS.find((d) => d.valor === this.preferencias().inicioSemana)?.nome ?? 'Segunda-feira');
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

  protected definirFormatoData(valor: string): void {
    if (valor === 'dd/mm/aaaa' || valor === 'mm/dd/aaaa' || valor === 'aaaa/mm/dd') this.definir('formatoData', valor);
  }

  protected alternar(chave: 'lembreteEmail' | 'alertaPendentes' | 'notificacaoSistema'): void {
    this.definir(chave, !this.preferencias()[chave]);
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

  protected async alterarSenha(): Promise<void> {
    this.erroSenha.set('');
    if (this.senhaNova().length < 8) {
      this.erroSenha.set('A nova senha deve ter pelo menos 8 caracteres.');
      return;
    }
    if (this.senhaNova() !== this.senhaConfirmacao()) {
      this.erroSenha.set('A confirmação não corresponde à nova senha.');
      return;
    }
    this.salvandoSenha.set(true);
    this.auth.alterarSenhaAutenticada(this.senhaAtual(), this.senhaNova(), this.senhaConfirmacao())
      .pipe(finalize(() => this.salvandoSenha.set(false)))
      .subscribe({
        next: () => {
          this.alterandoSenha.set(false);
          this.senhaAtual.set(''); this.senhaNova.set(''); this.senhaConfirmacao.set('');
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

  protected salvarPerfil(evento: Event): void {
    evento.preventDefault();
    if (this.salvandoPerfil()) return;
    const nome = this.nomePerfil().trim();
    const email = this.emailPerfil().trim();
    if (!nome || !email) {
      this.erroPerfil.set('Informe nome e e-mail.');
      return;
    }
    this.salvandoPerfil.set(true);
    this.erroPerfil.set('');
    this.auth.atualizarPerfil(nome, email)
      .pipe(finalize(() => this.salvandoPerfil.set(false)))
      .subscribe({
        next: () => {
          this.editandoPerfil.set(false);
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
