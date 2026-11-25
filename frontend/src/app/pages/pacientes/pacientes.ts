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

/**
 * Coluna "Ação" (editar e excluir) na lista do psiquiatra.
 * O desenho atual da tela inicial do psiquiatra não tem essa coluna; mude para `true` para voltar a mostrá-la.
 */
const MOSTRAR_COLUNA_ACAO_PSIQUIATRA = false;

/** Lista de pacientes com busca, números e cadastro de novo paciente. */
@Component({
  selector: 'app-pacientes',
  imports: [Avatar, Dialogo, Icone, RouterLink, TituloPagina],
  templateUrl: './pacientes.html',
})
export class Pacientes {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);

  /** Vem do `data` da rota (psicologo ou psiquiatra). */
  readonly perfil = input<Perfil>('psicologo');
  protected readonly raiz = computed(() => `/${this.perfil()}`);

  /** Psicólogo e psiquiatra seguem o mesmo desenho do Figma: 2 cards, avatar neutro e status Ativo/Inativo. */
  protected readonly colunaAcao = computed(() => this.perfil() === 'psiquiatra' && MOSTRAR_COLUNA_ACAO_PSIQUIATRA);

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
        rotuloStatus: p.status === 'inativo' ? 'Inativo' : 'Ativo',
        classeStatus: p.status === 'inativo' ? 'aguardando' : 'acompanhamento',
      }));
  });

  protected readonly cards = computed<{ valor: number; rotulo: string; icone: NomeIcone }[]>(() => {
    const pacientes = this.clinica.pacientes();
    return [
      { valor: pacientes.length, rotulo: 'Pacientes\nCadastrados', icone: 'usuarios' },
      {
        valor: pacientes.filter((p) => p.status !== 'inativo').length,
        rotulo: 'Pacientes\nem tratamento',
        icone: 'check-grande',
      },
    ];
  });

  // ----- Excluir paciente -----
  protected readonly excluindo = signal<Paciente | null>(null);

  protected async confirmarExclusao(): Promise<void> {
    const paciente = this.excluindo();
    if (!paciente) return;
    try {
      await this.clinica.removerPaciente(paciente.id);
      this.excluindo.set(null);
      this.aviso.mostrar('Paciente inativado. O prontuário e o histórico foram preservados.');
    } catch (erro) {
      console.error('Falha ao excluir paciente:', erro);
      this.aviso.mostrar('Não foi possível excluir o paciente.');
    }
  }
}
