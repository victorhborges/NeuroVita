import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import {
  cpfValido, deISO, emailValido, formatarCpf, formatarTelefone, idadeDe, paraISO, telefoneValido,
} from '../../shared/util';

interface Formulario {
  nome: string; nascimento: string; email: string; cpf: string; telefone: string;
  rNome: string; rNascimento: string; rEmail: string; rParentesco: string; rTelefone: string; rCpf: string;
  possuiPlano: boolean; plano: string; carteirinha: string; validade: string;
  ativo: boolean;
}

type Campo = keyof Formulario;

const VAZIO: Formulario = {
  nome: '', nascimento: '', email: '', cpf: '', telefone: '',
  rNome: '', rNascimento: '', rEmail: '', rParentesco: '', rTelefone: '', rCpf: '',
  possuiPlano: false, plano: '', carteirinha: '', validade: '', ativo: true,
};

/** Cadastrar novo paciente ou editar um paciente existente. */
@Component({
  selector: 'app-paciente-form',
  imports: [Dialogo, Icone, TituloPagina],
  templateUrl: './paciente-form.html',
})
export class PacienteForm {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);
  private readonly router = inject(Router);

  readonly perfil = input<Perfil>('secretaria');
  /** Sem id = cadastro novo. */
  readonly id = input<string>();

  protected readonly hoje = paraISO(new Date());
  protected readonly f = signal<Formulario>({ ...VAZIO });
  protected readonly tentou = signal(false);
  protected readonly confirmando = signal(false);

  private carregado = false;

  protected readonly edicao = computed(() => !!this.id());
  protected readonly existente = computed(() => this.clinica.paciente(this.id()));
  protected readonly voltar = computed(() => `/${this.perfil()}/pacientes`);

  protected readonly menorDeIdade = computed(() => {
    const n = this.f().nascimento;
    return !!n && idadeDe(n) < 18;
  });

  /** Mensagens de erro de cada campo (vazio = campo certo). */
  protected readonly erros = computed(() => {
    const f = this.f();
    const e: Partial<Record<Campo, string>> = {};
    if (f.nome.trim().length < 3) e.nome = 'Informe o nome completo.';
    if (!f.nascimento) e.nascimento = 'Informe a data de nascimento.';
    else if (deISO(f.nascimento) > new Date()) e.nascimento = 'A data não pode ser no futuro.';
    if (f.email.trim() && !emailValido(f.email)) e.email = 'E-mail inválido.';
    if (!f.cpf.trim()) e.cpf = 'Informe o CPF.';
    else if (!cpfValido(f.cpf)) e.cpf = 'CPF inválido.';
    if (!f.telefone.trim()) e.telefone = 'Informe o telefone.';
    else if (!telefoneValido(f.telefone)) e.telefone = 'Telefone inválido.';

    if (this.menorDeIdade()) {
      if (!f.rNome.trim()) e.rNome = 'Paciente menor de idade: informe o responsável.';
      if (!f.rParentesco.trim()) e.rParentesco = 'Informe o grau de parentesco.';
      if (!f.rTelefone.trim()) e.rTelefone = 'Informe o telefone do responsável.';
    }
    if (f.rEmail.trim() && !emailValido(f.rEmail)) e.rEmail = 'E-mail inválido.';
    if (f.rCpf.trim() && !cpfValido(f.rCpf)) e.rCpf = 'CPF inválido.';
    if (f.rTelefone.trim() && !telefoneValido(f.rTelefone)) e.rTelefone = 'Telefone inválido.';
    if (f.possuiPlano) {
      if (!f.plano.trim()) e.plano = 'Informe o plano de saúde.';
      if (!f.carteirinha.trim()) e.carteirinha = 'Informe o número da carteirinha.';
    }
    return e;
  });

  constructor() {
    // Na edição, preenche o formulário com os dados do paciente (uma vez).
    effect(() => {
      const p = this.existente();
      if (this.carregado || !p) return;
      this.carregado = true;
      this.f.set({
        nome: p.nome, nascimento: p.nascimento, email: p.email === '—' ? '' : p.email, cpf: p.cpf, telefone: p.telefone === '—' ? '' : p.telefone,
        rNome: p.responsavel.nome === '—' ? '' : p.responsavel.nome, rNascimento: p.responsavel.nascimento ?? '',
        rEmail: p.responsavel.email ?? '', rParentesco: p.responsavel.relacao === '—' ? '' : p.responsavel.relacao,
        rTelefone: p.responsavel.telefone === '—' ? '' : p.responsavel.telefone, rCpf: p.responsavel.cpf ?? '',
        possuiPlano: p.plano.possui, plano: p.plano.nome, carteirinha: p.plano.carteirinha, validade: p.plano.validade,
        ativo: p.status !== 'inativo',
      });
    });
  }

  protected mostrarErro(campo: Campo): string {
    return this.tentou() ? (this.erros()[campo] ?? '') : '';
  }

  protected digitar(campo: Campo, evento: Event): void {
    let valor = (evento.target as HTMLInputElement).value;
    if (campo === 'cpf' || campo === 'rCpf') valor = formatarCpf(valor);
    if (campo === 'telefone' || campo === 'rTelefone') valor = formatarTelefone(valor);
    this.f.update((atual) => ({ ...atual, [campo]: valor }));
    // O campo mostra a máscara mesmo se o navegador tentar guardar o texto digitado.
    (evento.target as HTMLInputElement).value = valor;
  }

  protected definir<K extends Campo>(campo: K, valor: Formulario[K]): void {
    this.f.update((atual) => ({ ...atual, [campo]: valor }));
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
    const paciente = this.clinica.salvarPaciente(
      {
        nome: f.nome.trim(), nascimento: f.nascimento, email: f.email.trim() || '—', cpf: f.cpf, telefone: f.telefone,
        responsavel: { nome: f.rNome.trim(), nascimento: f.rNascimento, email: f.rEmail.trim(), relacao: f.rParentesco.trim(), telefone: f.rTelefone, cpf: f.rCpf },
        plano: { possui: f.possuiPlano, nome: f.possuiPlano ? f.plano.trim() : '', carteirinha: f.possuiPlano ? f.carteirinha.trim() : '', validade: f.possuiPlano ? f.validade : '' },
        ativo: f.ativo,
      },
      this.id(),
    );
    this.confirmando.set(false);
    this.aviso.mostrar(this.edicao() ? `Dados de ${paciente.nome} atualizados.` : 'Paciente cadastrado com sucesso.');
    this.router.navigateByUrl(this.voltar());
  }

  protected cancelar(): void {
    this.router.navigateByUrl(this.voltar());
  }
}
