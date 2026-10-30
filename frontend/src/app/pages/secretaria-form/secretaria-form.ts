import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import {
  cpfValido, deISO, emailValido, formatarCpf, formatarTelefone, gerarSenhaTemporaria, idadeDe, paraISO, telefoneValido,
} from '../../shared/util';

interface Formulario {
  nome: string; nascimento: string; email: string; cpf: string; telefone: string; endereco: string;
  cargo: string; admissao: string; ativo: boolean;
  emailAcesso: string; senha: string; boasVindas: boolean;
}
type Campo = keyof Formulario;

const VAZIO: Formulario = {
  nome: '', nascimento: '', email: '', cpf: '', telefone: '', endereco: '', cargo: 'Secretária', admissao: '',
  ativo: true, emailAcesso: '', senha: '', boasVindas: true,
};

/** Cadastrar nova secretária ou editar uma secretária existente (administrador). */
@Component({
  selector: 'app-secretaria-form',
  imports: [Dialogo, Icone, TituloPagina],
  templateUrl: './secretaria-form.html',
})
export class SecretariaForm {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);
  private readonly router = inject(Router);

  readonly perfil = input<Perfil>('admin');
  readonly id = input<string>();

  protected readonly hoje = paraISO(new Date());
  protected readonly f = signal<Formulario>({ ...VAZIO });
  protected readonly tentou = signal(false);
  protected readonly confirmando = signal(false);

  private carregado = false;

  protected readonly edicao = computed(() => !!this.id());
  protected readonly existente = computed(() => this.clinica.secretaria(this.id()));
  protected readonly voltar = computed(() => `/${this.perfil()}/secretarias`);

  protected readonly erros = computed(() => {
    const f = this.f();
    const e: Partial<Record<Campo, string>> = {};
    if (f.nome.trim().length < 3) e.nome = 'Informe o nome completo.';
    if (!f.nascimento) e.nascimento = 'Informe a data de nascimento.';
    else if (idadeDe(f.nascimento) < 16 || deISO(f.nascimento) > new Date()) e.nascimento = 'Data de nascimento inválida.';
    if (!f.email.trim()) e.email = 'Informe o e-mail.';
    else if (!emailValido(f.email)) e.email = 'E-mail inválido.';
    if (!f.cpf.trim()) e.cpf = 'Informe o CPF.';
    else if (!cpfValido(f.cpf)) e.cpf = 'CPF inválido.';
    if (!f.telefone.trim()) e.telefone = 'Informe o telefone.';
    else if (!telefoneValido(f.telefone)) e.telefone = 'Telefone inválido.';
    if (!f.cargo.trim()) e.cargo = 'Informe o cargo.';
    if (!f.admissao) e.admissao = 'Informe a data de admissão.';
    else if (deISO(f.admissao) > new Date()) e.admissao = 'A data não pode ser no futuro.';
    if (!f.emailAcesso.trim()) e.emailAcesso = 'Informe o e-mail de acesso.';
    else if (!emailValido(f.emailAcesso)) e.emailAcesso = 'E-mail inválido.';
    else if (this.clinica.emailDeAcessoEmUso(f.emailAcesso, this.id())) e.emailAcesso = 'Esse e-mail de acesso já está em uso.';
    if ((!this.edicao() || f.senha) && f.senha.length < 6) e.senha = 'A senha temporária precisa ter 6 caracteres ou mais.';
    return e;
  });

  constructor() {
    effect(() => {
      const s = this.existente();
      if (this.carregado || !s) return;
      this.carregado = true;
      this.f.set({
        nome: s.nome, nascimento: s.nascimento, email: s.email, cpf: s.cpf, telefone: s.telefone, endereco: s.endereco,
        cargo: s.cargo, admissao: s.admissao, ativo: s.ativo, emailAcesso: s.emailAcesso, senha: '', boasVindas: false,
      });
    });
  }

  protected mostrarErro(campo: Campo): string {
    return this.tentou() ? (this.erros()[campo] ?? '') : '';
  }

  protected digitar(campo: Campo, evento: Event): void {
    let valor = (evento.target as HTMLInputElement).value;
    if (campo === 'cpf') valor = formatarCpf(valor);
    if (campo === 'telefone') valor = formatarTelefone(valor);
    this.f.update((atual) => ({ ...atual, [campo]: valor }));
    (evento.target as HTMLInputElement).value = valor;
  }

  protected definir<K extends Campo>(campo: K, valor: Formulario[K]): void {
    this.f.update((atual) => ({ ...atual, [campo]: valor }));
  }

  protected gerarSenha(): void {
    this.definir('senha', gerarSenhaTemporaria());
  }

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
    const secretaria = this.clinica.salvarSecretaria(
      {
        nome: f.nome.trim(), nascimento: f.nascimento, telefone: f.telefone, email: f.email.trim(), cpf: f.cpf,
        endereco: f.endereco.trim() || '—', cargo: f.cargo.trim(), admissao: f.admissao, ativo: f.ativo, emailAcesso: f.emailAcesso.trim(),
      },
      this.id(),
    );
    // TODO (back-end): gravar a senha temporária com segurança e enviar o e-mail de boas-vindas (f.boasVindas).
    this.confirmando.set(false);
    if (this.edicao()) this.aviso.mostrar(`Dados de ${secretaria.nome} atualizados.`);
    else this.aviso.mostrar(`Secretária cadastrada. Senha temporária: ${f.senha}`, 9000);
    this.router.navigateByUrl(this.voltar());
  }

  protected cancelar(): void {
    this.router.navigateByUrl(this.voltar());
  }
}
