import { Component, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { StatusPaciente } from '../../shared/clinica/modelos';
import { Avatar } from '../../shared/avatar/avatar';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone, NomeIcone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, idadeDe, normalizar, paraISO } from '../../shared/util';

const ROTULO_STATUS: Record<StatusPaciente, string> = {
  acompanhamento: 'Em acompanhamento',
  aguardando: 'Aguardando retorno',
  inativo: 'Inativo',
};

/** Lista de pacientes com busca, números e cadastro de novo paciente. */
@Component({
  selector: 'app-pacientes',
  imports: [Avatar, Dialogo, Icone, RouterLink, TituloPagina],
  templateUrl: './pacientes.html',
})
export class Pacientes {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);
  private readonly router = inject(Router);

  /** Vem do `data` da rota (psicologo ou psiquiatra). */
  readonly perfil = input<Perfil>('psicologo');
  protected readonly raiz = computed(() => `/${this.perfil()}`);

  protected readonly busca = signal('');

  protected readonly lista = computed(() => {
    const termo = normalizar(this.busca());
    return this.clinica
      .pacientes()
      .filter((p) => !termo || normalizar(p.nome).includes(termo) || p.codigo.includes(termo))
      .map((p) => ({
        ...p,
        idade: idadeDe(p.nascimento),
        ultima: formatarData(p.ultimaConsulta),
        rotuloStatus: ROTULO_STATUS[p.status],
      }));
  });

  protected readonly cards = computed<{ valor: number; rotulo: string; icone: NomeIcone }[]>(() => {
    const pacientes = this.clinica.pacientes();
    const avaliacoes = this.clinica.documentos().filter((d) => d.secao === 'avaliacoes').length;
    return [
      { valor: pacientes.length, rotulo: 'Pacientes\nCadastrados', icone: 'usuarios' },
      { valor: pacientes.filter((p) => p.status !== 'inativo').length, rotulo: 'Pacientes\nem tratamento', icone: 'check-grande' },
      { valor: avaliacoes, rotulo: 'Avaliações\nrealizadas', icone: 'prancheta' },
    ];
  });

  // ----- Novo paciente -----
  protected readonly novoAberto = signal(false);
  protected readonly nome = signal('');
  protected readonly nascimento = signal('');
  protected readonly telefone = signal('');
  protected readonly email = signal('');
  protected readonly endereco = signal('');
  protected readonly erro = signal('');
  protected readonly hoje = paraISO(new Date());

  protected abrirNovo(): void {
    this.nome.set(''); this.nascimento.set(''); this.telefone.set('');
    this.email.set(''); this.endereco.set(''); this.erro.set('');
    this.novoAberto.set(true);
  }

  protected digitar(campo: { set(v: string): void }, evento: Event): void {
    campo.set((evento.target as HTMLInputElement).value);
    this.erro.set('');
  }

  protected salvarNovo(): void {
    if (!this.nome().trim() || !this.nascimento()) {
      this.erro.set('Informe o nome e a data de nascimento.');
      return;
    }
    const novo = this.clinica.adicionarPaciente({
      nome: this.nome().trim(), nascimento: this.nascimento(), telefone: this.telefone().trim() || '—',
      email: this.email().trim() || '—', endereco: this.endereco().trim() || '—',
    });
    this.novoAberto.set(false);
    this.busca.set('');
    this.aviso.mostrar('Paciente cadastrado com sucesso.');
    this.router.navigate([this.raiz(), 'pacientes', novo.id]);
  }
}
