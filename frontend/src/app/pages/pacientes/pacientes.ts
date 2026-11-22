import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Clinica } from '../../shared/clinica/clinica';
import { StatusPaciente } from '../../shared/clinica/modelos';
import { Avatar } from '../../shared/avatar/avatar';
import { Icone, NomeIcone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, idadeDe, normalizar } from '../../shared/util';

const ROTULO_STATUS: Record<StatusPaciente, string> = {
  acompanhamento: 'Em acompanhamento',
  aguardando: 'Aguardando retorno',
  inativo: 'Inativo',
};

/** Lista de pacientes vinculados ao profissional, com busca, resumo e acesso ao prontuário. */
@Component({
  selector: 'app-pacientes',
  imports: [Avatar, Icone, RouterLink, TituloPagina],
  templateUrl: './pacientes.html',
})
export class Pacientes {
  private readonly clinica = inject(Clinica);

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

}
