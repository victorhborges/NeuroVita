import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import {
  cepValido, cpfValido, deISO, emailValido, formatarCep, formatarCpf, formatarTelefone, gerarSenhaTemporaria, idadeDe, paraISO,
  telefoneValido,
} from '../../shared/util';

interface Formulario {
  nome: string; nascimento: string; cpf: string; telefone: string; cep: string; cidade: string; endereco: string;
  numero: string; complemento: string; bairro: string;
  admissao: string; encerramento: string; ativo: boolean;
  cargo: string; cbo: string; cargaHoraria: string;
  emailAcesso: string; senha: string; boasVindas: boolean;
}
type Campo = keyof Formulario;

const VAZIO: Formulario = {
  nome: '', nascimento: '', cpf: '', telefone: '', cep: '', cidade: '', endereco: '', numero: '', complemento: '', bairro: '', admissao: '', encerramento: '', ativo: true,
  cargo: 'Secretária', cbo: '', cargaHoraria: '', emailAcesso: '', senha: '', boasVindas: true,
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
    if (!f.cpf.trim()) e.cpf = 'Informe o CPF.';
    else if (!cpfValido(f.cpf)) e.cpf = 'CPF inválido.';
    if (f.telefone.trim() && !telefoneValido(f.telefone)) e.telefone = 'Telefone inválido.';
    if (!f.cep.trim()) e.cep = 'Informe o CEP.';
    else if (!cepValido(f.cep)) e.cep = 'CEP inválido.';
    // Número e bairro: obrigatórios no cadastro; ao editar uma secretária antiga (sem esses dados) não bloqueiam.
    if (!this.edicao()) {
      if (!f.numero.trim()) e.numero = 'Informe o número.';
      if (!f.bairro.trim()) e.bairro = 'Informe o bairro.';
    }
    if (f.admissao && f.encerramento && f.encerramento < f.admissao) e.encerramento = 'O encerramento não pode ser antes do início.';
    if (!f.cargo.trim()) e.cargo = 'Informe a área de atuação.';
    if (!f.cbo.trim()) e.cbo = 'Informe o CBO.';
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
        nome: s.nome, nascimento: s.nascimento, cpf: s.cpf, telefone: s.telefone, cep: s.cep ?? '', cidade: s.cidade ?? '',
        endereco: s.endereco === '—' ? '' : s.endereco, numero: s.numero ?? '', complemento: s.complemento ?? '', bairro: s.bairro ?? '', admissao: s.admissao, encerramento: s.encerramento ?? '', ativo: s.ativo,
        cargo: s.cargo, cbo: s.cbo ?? '', cargaHoraria: s.cargaHoraria ?? '', emailAcesso: s.emailAcesso, senha: '', boasVindas: false,
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
    if (campo === 'cep') valor = formatarCep(valor);
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

  protected async confirmar(): Promise<void> {
    const f = this.f();
    try {
      const secretaria = await this.clinica.salvarSecretaria(
        {
          nome: f.nome.trim(), nascimento: f.nascimento, telefone: f.telefone, email: f.emailAcesso.trim(), cpf: f.cpf,
          cep: f.cep, cidade: f.cidade.trim(), endereco: f.endereco.trim(), numero: f.numero.trim(), complemento: f.complemento.trim(), bairro: f.bairro.trim(), cargo: f.cargo.trim(), cbo: f.cbo.trim(),
          cargaHoraria: f.cargaHoraria.trim(), admissao: f.admissao, encerramento: f.encerramento, ativo: f.ativo,
          emailAcesso: f.emailAcesso.trim(),
        },
        this.id(),
      );
      this.confirmando.set(false);
      if (this.edicao()) this.aviso.mostrar(`Dados de ${secretaria.nome} atualizados.`);
      else this.aviso.mostrar(`Secretária cadastrada. Senha temporária: ${f.senha}`, 9000);
      this.router.navigateByUrl(this.voltar());
    } catch (erro) {
      console.error('Falha ao salvar secretária:', erro);
      this.aviso.mostrar('Não foi possível salvar a secretária. Verifique os dados e tente novamente.');
    }
  }

  protected cancelar(): void {
    this.router.navigateByUrl(this.voltar());
  }
}
