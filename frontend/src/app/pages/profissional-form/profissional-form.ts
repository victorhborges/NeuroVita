import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { minutosDe } from '../../shared/clinica/status';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { ListaSuspensa, OpcaoLista } from '../../shared/lista-suspensa/lista-suspensa';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import {
  cpfValido, deISO, emailValido, formatarCpf, formatarTelefone, gerarSenhaTemporaria, idadeDe, paraISO, telefoneValido,
} from '../../shared/util';

interface Faixa { inicio: string; fim: string }

interface Formulario {
  nome: string; nascimento: string; email: string; cpf: string; telefone: string;
  tipo: string; registro: string; anoConclusao: string;
  dias: number[]; online: boolean; presencial: boolean; ativo: boolean;
  emailAcesso: string; senha: string; boasVindas: boolean;
}
type Campo = keyof Formulario;

const VAZIO: Formulario = {
  nome: '', nascimento: '', email: '', cpf: '', telefone: '', tipo: '', registro: '', anoConclusao: '',
  dias: [], online: false, presencial: false, ativo: true, emailAcesso: '', senha: '', boasVindas: true,
};

const DIAS = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

/** Cadastrar novo profissional ou editar um profissional existente (administrador). */
@Component({
  selector: 'app-profissional-form',
  imports: [Dialogo, Icone, ListaSuspensa, TituloPagina],
  templateUrl: './profissional-form.html',
})
export class ProfissionalForm {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);
  private readonly router = inject(Router);

  readonly perfil = input<Perfil>('admin');
  /** Sem id = cadastro novo. */
  readonly id = input<string>();

  protected readonly hoje = paraISO(new Date());
  protected readonly nomesDias = DIAS;
  protected readonly categorias: OpcaoLista[] = [
    { valor: 'psicologo', rotulo: 'Psicólogo' },
    { valor: 'psiquiatra', rotulo: 'Psiquiatra' },
  ];

  protected readonly f = signal<Formulario>({ ...VAZIO });
  protected readonly faixas = signal<Faixa[]>([{ inicio: '08:00', fim: '11:00' }, { inicio: '13:00', fim: '18:00' }]);
  protected readonly faixaInicio = signal('08:00');
  protected readonly faixaFim = signal('18:00');
  protected readonly faixaEditando = signal<number | null>(null);
  protected readonly erroFaixa = signal('');
  protected readonly tentou = signal(false);
  protected readonly confirmando = signal(false);

  private carregado = false;

  protected readonly edicao = computed(() => !!this.id());
  protected readonly existente = computed(() => this.clinica.profissional(this.id()));
  protected readonly voltar = computed(() => `/${this.perfil()}/profissionais`);
  protected readonly prefixoRegistro = computed(() => (this.f().tipo === 'psiquiatra' ? 'CRM' : this.f().tipo === 'psicologo' ? 'CRP' : ''));

  protected readonly erros = computed(() => {
    const f = this.f();
    const e: Partial<Record<Campo | 'faixas', string>> = {};
    const ano = Number(f.anoConclusao);
    if (f.nome.trim().length < 3) e.nome = 'Informe o nome completo.';
    if (!f.nascimento) e.nascimento = 'Informe a data de nascimento.';
    else if (idadeDe(f.nascimento) < 18 || deISO(f.nascimento) > new Date()) e.nascimento = 'O profissional precisa ter 18 anos ou mais.';
    if (!f.email.trim()) e.email = 'Informe o e-mail.';
    else if (!emailValido(f.email)) e.email = 'E-mail inválido.';
    if (!f.cpf.trim()) e.cpf = 'Informe o CPF.';
    else if (!cpfValido(f.cpf)) e.cpf = 'CPF inválido.';
    if (!f.telefone.trim()) e.telefone = 'Informe o telefone.';
    else if (!telefoneValido(f.telefone)) e.telefone = 'Telefone inválido.';
    if (!f.tipo) e.tipo = 'Escolha a categoria profissional.';
    if (!f.registro.trim()) e.registro = 'Informe o número de registro.';
    else if (!/^[\w./-]{3,}$/.test(f.registro.trim())) e.registro = 'Número de registro inválido.';
    if (!f.anoConclusao) e.anoConclusao = 'Informe o ano de conclusão.';
    else if (!Number.isInteger(ano) || ano < 1950 || ano > new Date().getFullYear()) e.anoConclusao = 'Ano de conclusão inválido.';
    if (!f.dias.length) e.dias = 'Marque pelo menos um dia de atendimento.';
    if (!this.faixas().length) e.faixas = 'Adicione pelo menos um horário de atendimento.';
    if (!f.online && !f.presencial) e.presencial = 'Marque online, presencial ou os dois.';
    if (!f.emailAcesso.trim()) e.emailAcesso = 'Informe o e-mail de acesso.';
    else if (!emailValido(f.emailAcesso)) e.emailAcesso = 'E-mail inválido.';
    else if (this.clinica.emailDeAcessoEmUso(f.emailAcesso, this.id())) e.emailAcesso = 'Esse e-mail de acesso já está em uso.';
    if (!this.edicao() && f.senha.length < 6) e.senha = 'A senha temporária precisa ter 6 caracteres ou mais.';
    else if (this.edicao() && f.senha && f.senha.length < 6) e.senha = 'A senha temporária precisa ter 6 caracteres ou mais.';
    return e;
  });

  constructor() {
    effect(() => {
      const p = this.existente();
      if (this.carregado || !p) return;
      this.carregado = true;
      this.f.set({
        nome: p.nome, nascimento: p.nascimento, email: p.email, cpf: p.cpf, telefone: p.telefone, tipo: p.tipo,
        registro: p.registro.replace(/^CR[PM]\s*/i, ''), anoConclusao: p.anoConclusao ? String(p.anoConclusao) : '',
        dias: [...p.disponibilidade.dias], online: p.disponibilidade.online, presencial: p.disponibilidade.presencial,
        ativo: p.ativo, emailAcesso: p.emailAcesso, senha: '', boasVindas: false,
      });
      this.faixas.set(p.disponibilidade.faixas.map((x) => ({ ...x })));
    });
  }

  protected mostrarErro(campo: Campo | 'faixas'): string {
    return this.tentou() ? (this.erros()[campo] ?? '') : '';
  }

  protected digitar(campo: Campo, evento: Event): void {
    let valor = (evento.target as HTMLInputElement).value;
    if (campo === 'cpf') valor = formatarCpf(valor);
    if (campo === 'telefone') valor = formatarTelefone(valor);
    if (campo === 'anoConclusao') valor = valor.replace(/\D/g, '').slice(0, 4);
    this.f.update((atual) => ({ ...atual, [campo]: valor }));
    (evento.target as HTMLInputElement).value = valor;
  }

  protected definir<K extends Campo>(campo: K, valor: Formulario[K]): void {
    this.f.update((atual) => ({ ...atual, [campo]: valor }));
  }

  protected alternarDia(indice: number): void {
    this.f.update((atual) => ({
      ...atual,
      dias: atual.dias.includes(indice) ? atual.dias.filter((d) => d !== indice) : [...atual.dias, indice].sort((a, b) => a - b),
    }));
  }

  /* ---------- Horários de atendimento ---------- */

  protected salvarFaixa(): void {
    const inicio = this.faixaInicio();
    const fim = this.faixaFim();
    const editando = this.faixaEditando();
    if (!inicio || !fim || minutosDe(inicio) >= minutosDe(fim)) {
      this.erroFaixa.set('O horário final precisa ser depois do inicial.');
      return;
    }
    const sobrepoe = this.faixas().some(
      (x, i) => i !== editando && minutosDe(inicio) < minutosDe(x.fim) && minutosDe(x.inicio) < minutosDe(fim),
    );
    if (sobrepoe) {
      this.erroFaixa.set('Esse horário se sobrepõe a outro já cadastrado.');
      return;
    }
    this.erroFaixa.set('');
    const nova = { inicio, fim };
    this.faixas.update((l) => (editando === null ? [...l, nova] : l.map((x, i) => (i === editando ? nova : x))));
    this.faixas.update((l) => [...l].sort((a, b) => minutosDe(a.inicio) - minutosDe(b.inicio)));
    this.faixaEditando.set(null);
  }

  protected editarFaixa(indice: number): void {
    const x = this.faixas()[indice];
    this.faixaInicio.set(x.inicio);
    this.faixaFim.set(x.fim);
    this.faixaEditando.set(indice);
    this.erroFaixa.set('');
  }

  protected removerFaixa(indice: number): void {
    this.faixas.update((l) => l.filter((_, i) => i !== indice));
    if (this.faixaEditando() === indice) this.faixaEditando.set(null);
    this.erroFaixa.set('');
  }

  protected gerarSenha(): void {
    this.definir('senha', gerarSenhaTemporaria());
  }

  /* ---------- Salvar ---------- */

  protected cadastrar(): void {
    this.tentou.set(true);
    if (Object.keys(this.erros()).length) {
      this.aviso.mostrar('Corrija os campos destacados para continuar.');
      return;
    }
    this.confirmando.set(true);
  }

  protected confirmar(): void {
    const f = this.f();
    const profissional = this.clinica.salvarProfissional(
      {
        nome: f.nome.trim(), nascimento: f.nascimento, email: f.email.trim(), cpf: f.cpf, telefone: f.telefone,
        tipo: f.tipo === 'psiquiatra' ? 'psiquiatra' : 'psicologo', registro: f.registro.trim(), anoConclusao: Number(f.anoConclusao),
        disponibilidade: { dias: f.dias, faixas: this.faixas(), online: f.online, presencial: f.presencial },
        ativo: f.ativo, emailAcesso: f.emailAcesso.trim(),
      },
      this.id(),
    );
    // TODO (back-end): gravar a senha temporária com segurança e enviar o e-mail de boas-vindas (f.boasVindas).
    this.confirmando.set(false);
    if (this.edicao()) this.aviso.mostrar(`Dados de ${profissional.nome} atualizados.`);
    else this.aviso.mostrar(`Profissional cadastrado. Senha temporária: ${f.senha}`, 9000);
    this.router.navigateByUrl(this.voltar());
  }

  protected cancelar(): void {
    this.router.navigateByUrl(this.voltar());
  }
}
