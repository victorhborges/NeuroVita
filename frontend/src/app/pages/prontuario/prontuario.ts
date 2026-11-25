import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Clinica } from '../../shared/clinica/clinica';
import { nomeProfissional } from '../../shared/clinica/rotulos';
import { SECOES, secoesDoPerfil } from '../../shared/clinica/secoes';
import { FaixaPaciente } from '../../shared/faixa-paciente/faixa-paciente';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, paraISO } from '../../shared/util';

const HOJE_DEMO = paraISO(new Date());
const AGORA_DEMO = `${String(new Date().getHours()).padStart(2, '0')}:${String(new Date().getMinutes()).padStart(2, '0')}`;

/** Prontuário do paciente: dados pessoais, responsável, últimas informações e as seções (galerias de documentos). */
@Component({
  selector: 'app-prontuario',
  imports: [FaixaPaciente, Icone, RouterLink, TituloPagina],
  templateUrl: './prontuario.html',
})
export class Prontuario {
  private readonly clinica = inject(Clinica);

  /** Vem da rota: /:perfil/pacientes/:id */
  readonly id = input.required<string>();
  /** Vem do `data` da rota (psicologo ou psiquiatra). */
  readonly perfil = input<Perfil>('psicologo');
  protected readonly raiz = computed(() => `/${this.perfil()}`);

  protected readonly paciente = computed(() => this.clinica.paciente(this.id()));
  protected readonly nascimento = computed(() => formatarData(this.paciente()?.nascimento));
  protected readonly secoes = computed(() => secoesDoPerfil(this.perfil()).map((id) => SECOES[id]));

  /** Próxima consulta e última consulta do paciente, vistas na agenda. */
  protected readonly ultimas = computed(() => {
    const id = this.id();
    const referencia = `${HOJE_DEMO} ${AGORA_DEMO}`;
    const chave = (a: { data: string; inicio: string }) => `${a.data} ${a.inicio}`;
    const consultas = this.clinica.agendamentos().filter((a) => a.pacienteId === id && a.status !== 'cancelado');
    const mostrar = (a: (typeof consultas)[number] | undefined) => {
      if (!a) return null;
      const prof = this.clinica.profissional(a.profissionalId);
      return { dataBR: formatarData(a.data), profissional: prof ? `${nomeProfissional(prof)} - ${prof.especialidade}` : '—' };
    };
    const proxima = consultas.filter((a) => chave(a) >= referencia).sort((x, y) => chave(x).localeCompare(chave(y)))[0];
    const ultima = consultas.filter((a) => chave(a) < referencia).sort((x, y) => chave(y).localeCompare(chave(x)))[0];
    return { proxima: mostrar(proxima), ultima: mostrar(ultima) };
  });
}
