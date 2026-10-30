import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Paciente } from '../../shared/clinica/modelos';
import { Avatar } from '../../shared/avatar/avatar';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone, NomeIcone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, idadeDe, normalizar } from '../../shared/util';

/** Lista de pacientes para a secretária: buscar, cadastrar, editar e excluir. */
@Component({
  selector: 'app-gestao-pacientes',
  imports: [Avatar, Dialogo, Icone, RouterLink, TituloPagina],
  templateUrl: './gestao-pacientes.html',
})
export class GestaoPacientes {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);

  /** Vem do `data` da rota. */
  readonly perfil = input<Perfil>('secretaria');

  protected readonly busca = signal('');
  protected readonly excluindo = signal<Paciente | null>(null);

  protected readonly base = computed(() => `/${this.perfil()}/pacientes`);
  /** Só a secretária cadastra, edita e exclui. O administrador apenas consulta. */
  protected readonly edita = computed(() => this.perfil() === 'secretaria');

  protected readonly lista = computed(() => {
    const termo = normalizar(this.busca());
    return this.clinica
      .pacientes()
      .filter((p) => !termo || normalizar(p.nome).includes(termo) || p.codigo.includes(termo))
      .map((p) => ({
        p,
        idade: idadeDe(p.nascimento),
        nascimento: formatarData(p.nascimento),
        profissional: this.clinica.profissional(p.profissionalId)?.nome ?? '—',
        ativo: p.status !== 'inativo',
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

  protected confirmarExclusao(): void {
    const paciente = this.excluindo();
    if (!paciente) return;
    this.clinica.removerPaciente(paciente.id);
    this.excluindo.set(null);
    this.aviso.mostrar('Paciente excluído com sucesso.');
  }
}
