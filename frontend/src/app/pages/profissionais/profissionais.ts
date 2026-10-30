import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Profissional } from '../../shared/clinica/modelos';
import { Avatar } from '../../shared/avatar/avatar';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, idadeDe, normalizar } from '../../shared/util';

/**
 * Lista de profissionais da clínica.
 * - Secretária: só consulta.
 * - Administrador: cadastra, edita, inativa e exclui.
 */
@Component({
  selector: 'app-profissionais',
  imports: [Avatar, Dialogo, Icone, RouterLink, TituloPagina],
  templateUrl: './profissionais.html',
})
export class Profissionais {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);

  /** Vem do `data` da rota. */
  readonly perfil = input<Perfil>('secretaria');

  protected readonly busca = signal('');
  protected readonly gerencia = computed(() => this.perfil() === 'admin');
  protected readonly base = computed(() => `/${this.perfil()}/profissionais`);

  // Exclusão
  protected readonly excluindo = signal<Profissional | null>(null);
  protected readonly bloqueado = signal<{ prof: Profissional; consultas: number } | null>(null);

  protected readonly cards = computed(() => {
    const lista = this.clinica.profissionais();
    const psicologos = lista.filter((p) => p.tipo === 'psicologo').length;
    const psiquiatras = lista.filter((p) => p.tipo === 'psiquiatra').length;
    if (this.gerencia()) {
      return [
        { rotulo: 'Psicólogos\ncadastrados', valor: psicologos, legenda: '' },
        { rotulo: 'Psiquiatras\ncadastrados', valor: psiquiatras, legenda: '' },
      ];
    }
    return [
      { rotulo: 'Total de profissionais', valor: lista.length, legenda: 'Cadastrados na clínica' },
      { rotulo: 'Total de psiquiatras', valor: psiquiatras, legenda: 'Cadastrados na clínica' },
    ];
  });

  protected readonly lista = computed(() => {
    const termo = normalizar(this.busca());
    return this.clinica
      .profissionais()
      .filter((p) => !termo || normalizar(p.nome).includes(termo) || normalizar(p.especialidade).includes(termo))
      .map((p) => ({ p, idade: idadeDe(p.nascimento), nascimento: formatarData(p.nascimento) }));
  });

  /** Clique na lixeira: se houver consultas marcadas, o sistema não deixa excluir. */
  protected pedirExclusao(p: Profissional): void {
    const consultas = this.clinica.consultasDoProfissional(p.id);
    if (consultas > 0) this.bloqueado.set({ prof: p, consultas });
    else this.excluindo.set(p);
  }

  protected confirmarExclusao(): void {
    const p = this.excluindo();
    if (!p) return;
    this.clinica.removerProfissional(p.id);
    this.excluindo.set(null);
    this.aviso.mostrar('Profissional excluído com sucesso.');
  }

  protected inativarBloqueado(): void {
    const b = this.bloqueado();
    if (b?.prof.ativo) {
      this.clinica.alterarAtivoProfissional(b.prof.id, false);
      this.aviso.mostrar(`${b.prof.nome} foi inativado.`);
    }
    this.bloqueado.set(null);
  }
}
