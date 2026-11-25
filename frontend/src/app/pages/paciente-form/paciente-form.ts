import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import {
  cepValido, cpfValido, deISO, emailValido, formatarCep, formatarCpf, formatarTelefone, idadeDe, paraISO, telefoneValido,
} from '../../shared/util';

interface Formulario {
  nome: string; nascimento: string; email: string; cpf: string; telefone: string;
  cep: string; cidade: string; endereco: string; numero: string; complemento: string; bairro: string; inicio: string; encerramento: string; rEndereco: string;
  rNome: string; rNascimento: string; rEmail: string; rParentesco: string; rTelefone: string; rCpf: string;
  possuiPlano: boolean; plano: string; carteirinha: string; validade: string;
  ativo: boolean;
}

type Campo = keyof Formulario;

const VAZIO: Formulario = {
  nome: '', nascimento: '', email: '', cpf: '', telefone: '',
  cep: '', cidade: '', endereco: '', numero: '', complemento: '', bairro: '', inicio: '', encerramento: '', rEndereco: '',
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
    if (!f.cpf.trim()) e.cpf = 'Informe o CPF.';
    else if (!cpfValido(f.cpf)) e.cpf = 'CPF inválido.';
    // O CEP é obrigatório no cadastro; ao editar um paciente antigo (sem CEP) só é conferido se for preenchido.
    if (!f.cep.trim()) {
      if (!this.edicao()) e.cep = 'Informe o CEP.';
    } else if (!cepValido(f.cep)) e.cep = 'CEP inválido.';
    // Número e bairro: obrigatórios no cadastro; ao editar um paciente antigo (sem esses dados) não bloqueiam.
    if (!this.edicao()) {
      if (!f.numero.trim()) e.numero = 'Informe o número.';
      if (!f.bairro.trim()) e.bairro = 'Informe o bairro.';
    }
    if (f.inicio && f.encerramento && f.encerramento < f.inicio) e.encerramento = 'O encerramento não pode ser antes do início.';

    if (this.menorDeIdade()) {
      if (!f.rNome.trim()) e.rNome = 'Paciente menor de idade: informe o responsável.';
      if (!f.rParentesco.trim()) e.rParentesco = 'Informe o grau de parentesco.';
      if (!f.rTelefone.trim()) e.rTelefone = 'Informe o telefone do responsável.';
    }
    if (f.rEmail.trim() && !emailValido(f.rEmail)) e.rEmail = 'E-mail inválido.';
    if (f.rCpf.trim() && !cpfValido(f.rCpf)) e.rCpf = 'CPF inválido.';
    if (f.rTelefone.trim() && !telefoneValido(f.rTelefone)) e.rTelefone = 'Telefone inválido.';
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
        cep: p.cep ?? '', cidade: p.cidade ?? '', endereco: p.endereco === '—' ? '' : p.endereco, numero: p.numero ?? '', complemento: p.complemento ?? '', bairro: p.bairro ?? '', inicio: p.inicio ?? '', encerramento: p.encerramento ?? '',
        rEndereco: p.responsavel.endereco === '—' ? '' : p.responsavel.endereco,
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
    if (campo === 'cep') valor = formatarCep(valor);
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

  protected async confirmar(): Promise<void> {
    const f = this.f();
    try {
      const paciente = await this.clinica.salvarPaciente(
        {
          nome: f.nome.trim(), nascimento: f.nascimento, email: f.email.trim(), cpf: f.cpf, telefone: f.telefone,
          endereco: f.endereco.trim(), cep: f.cep, cidade: f.cidade.trim(), numero: f.numero.trim(), complemento: f.complemento.trim(), bairro: f.bairro.trim(), inicio: f.inicio, encerramento: f.encerramento,
          responsavel: { id: this.existente()?.responsavel?.id, nome: f.rNome.trim(), nascimento: f.rNascimento, email: f.rEmail.trim(), relacao: f.rParentesco.trim(), telefone: f.rTelefone, cpf: f.rCpf, endereco: f.rEndereco.trim() },
          plano: { possui: f.possuiPlano, nome: f.possuiPlano ? f.plano.trim() : '', carteirinha: f.possuiPlano ? f.carteirinha.trim() : '', validade: f.possuiPlano ? f.validade : '' },
          ativo: f.ativo,
        },
        this.id(),
      );
      this.confirmando.set(false);
      this.aviso.mostrar(this.edicao() ? `Dados de ${paciente.nome} atualizados.` : 'Paciente cadastrado com sucesso.');
      this.router.navigateByUrl(this.voltar());
    } catch (erro) {
      console.error('Falha ao salvar paciente:', erro);
      this.aviso.mostrar('Não foi possível salvar o paciente. Verifique os dados e tente novamente.');
    }
  }

  protected cancelar(): void {
    this.router.navigateByUrl(this.voltar());
  }
}
