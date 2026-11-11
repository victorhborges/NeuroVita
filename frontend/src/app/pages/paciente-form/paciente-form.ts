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
  rId?: string;
  responsaveisExtras: { id?: string; nome: string; nascimento: string; email: string; parentesco: string; telefone: string; cpf: string }[];
  possuiPlano: boolean; plano: string; carteirinha: string; validade: string;
  ativo: boolean;
}

type Campo = keyof Formulario;

export function erroPlanoSaude(possuiPlano: boolean, nome: string): string | null {
  return possuiPlano && !nome.trim() ? 'Informe o plano de saúde.' : null;
}

const VAZIO: Formulario = {
  nome: '', nascimento: '', email: '', cpf: '', telefone: '',
  rNome: '', rNascimento: '', rEmail: '', rParentesco: '', rTelefone: '', rCpf: '',
  responsaveisExtras: [],
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

    const possuiResponsavel = !!f.rNome.trim() || f.responsaveisExtras.some((r) => r.nome.trim());
    if (this.menorDeIdade() && !possuiResponsavel) e.rNome = 'Paciente menor de idade: informe pelo menos um responsável.';
    if (f.rNome.trim() && !f.rParentesco.trim()) e.rParentesco = 'Informe o grau de parentesco.';
    if (f.rNome.trim() && (!f.rTelefone.trim() || !telefoneValido(f.rTelefone))) e.rTelefone = 'Informe um telefone válido do responsável.';
    if (f.rEmail.trim() && !emailValido(f.rEmail)) e.rEmail = 'E-mail inválido.';
    if (f.rCpf.trim() && !cpfValido(f.rCpf)) e.rCpf = 'CPF inválido.';
    if (f.rTelefone.trim() && !telefoneValido(f.rTelefone)) e.rTelefone = 'Telefone inválido.';
    for (const r of f.responsaveisExtras) {
      if (!r.nome.trim() && (r.telefone.trim() || r.cpf.trim() || r.email.trim() || r.parentesco.trim())) {
        e.rNome = 'Informe o nome de todos os responsáveis adicionados.';
        break;
      }
      if (r.nome.trim() && !r.parentesco.trim()) e.rNome = 'Informe o grau de parentesco de cada responsável.';
      if (r.nome.trim() && (!r.telefone.trim() || !telefoneValido(r.telefone))) e.rNome = 'Informe um telefone válido para cada responsável.';
      if (r.email.trim() && !emailValido(r.email)) e.rNome = 'Informe um e-mail válido para cada responsável.';
      if (r.cpf.trim() && !cpfValido(r.cpf)) e.rNome = 'Informe um CPF válido para cada responsável.';
    }
    const erroPlano = erroPlanoSaude(f.possuiPlano, f.plano);
    if (erroPlano) e.plano = erroPlano;
    return e;
  });

  constructor() {
    // Na edição, preenche o formulário com os dados do paciente (uma vez).
    effect(() => {
      const p = this.existente();
      if (this.carregado || !p) return;
      this.carregado = true;
      const todosResponsaveis = p.responsaveis.length
        ? p.responsaveis
        : (p.responsavel.nome ? [{ ...p.responsavel, id: p.responsavelId }] : []);
      const principal = todosResponsaveis[0] ?? p.responsavel;
      this.f.set({
        nome: p.nome, nascimento: p.nascimento, email: p.email === '—' ? '' : p.email, cpf: p.cpf, telefone: p.telefone === '—' ? '' : p.telefone,
        rId: principal.id, rNome: principal.nome === '—' ? '' : principal.nome, rNascimento: principal.nascimento ?? '',
        rEmail: principal.email ?? '', rParentesco: principal.relacao === '—' ? '' : principal.relacao,
        rTelefone: principal.telefone === '—' ? '' : principal.telefone, rCpf: principal.cpf ?? '',
        responsaveisExtras: todosResponsaveis.slice(1).map((r) => ({
          id: r.id, nome: r.nome, nascimento: r.nascimento ?? '', email: r.email ?? '',
          parentesco: r.relacao, telefone: r.telefone, cpf: r.cpf ?? '',
        })),
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

  protected adicionarResponsavel(): void {
    this.f.update((atual) => ({
      ...atual,
      responsaveisExtras: [...atual.responsaveisExtras, { nome: '', nascimento: '', email: '', parentesco: '', telefone: '', cpf: '' }],
    }));
  }

  protected removerResponsavel(indice: number): void {
    this.f.update((atual) => ({
      ...atual,
      responsaveisExtras: atual.responsaveisExtras.filter((_, i) => i !== indice),
    }));
  }

  protected digitarExtra(indice: number, campo: 'nome' | 'nascimento' | 'email' | 'parentesco' | 'telefone' | 'cpf', evento: Event): void {
    let valor = (evento.target as HTMLInputElement).value;
    if (campo === 'cpf') valor = formatarCpf(valor);
    if (campo === 'telefone') valor = formatarTelefone(valor);
    this.f.update((atual) => ({
      ...atual,
      responsaveisExtras: atual.responsaveisExtras.map((r, i) => i === indice ? { ...r, [campo]: valor } : r),
    }));
    (evento.target as HTMLInputElement).value = valor;
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
        responsavel: { id: f.rId, nome: f.rNome.trim(), nascimento: f.rNascimento, email: f.rEmail.trim(), relacao: f.rParentesco.trim(), telefone: f.rTelefone, cpf: f.rCpf, endereco: '' },
        responsaveis: [
          { id: f.rId, nome: f.rNome.trim(), nascimento: f.rNascimento, email: f.rEmail.trim(), relacao: f.rParentesco.trim(), telefone: f.rTelefone, cpf: f.rCpf, endereco: '' },
          ...f.responsaveisExtras.map((r) => ({ id: r.id, nome: r.nome.trim(), nascimento: r.nascimento, email: r.email.trim(), relacao: r.parentesco.trim(), telefone: r.telefone, cpf: r.cpf, endereco: '' })),
        ].filter((r) => r.nome),
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
