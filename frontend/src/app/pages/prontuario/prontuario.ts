import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Auth } from '../../shared/auth';
import { Clinica } from '../../shared/clinica/clinica';
import { SECOES, secoesDoPerfil } from '../../shared/clinica/secoes';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { FaixaPaciente } from '../../shared/faixa-paciente/faixa-paciente';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { escaparHtml, formatarData, paraISO } from '../../shared/util';

/** Prontuário do paciente: dados pessoais, responsável, atalhos e histórico de consultas. */
@Component({
  selector: 'app-prontuario',
  imports: [Dialogo, FaixaPaciente, Icone, RouterLink, TituloPagina],
  templateUrl: './prontuario.html',
})
export class Prontuario {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);
  private readonly auth = inject(Auth);

  /** Vem da rota: /:perfil/pacientes/:id */
  readonly id = input.required<string>();
  /** Vem do `data` da rota (psicologo ou psiquiatra). */
  readonly perfil = input<Perfil>('psicologo');
  protected readonly raiz = computed(() => `/${this.perfil()}`);

  protected readonly paciente = computed(() => this.clinica.paciente(this.id()));
  protected readonly nascimento = computed(() => formatarData(this.paciente()?.nascimento));
  protected readonly secoes = computed(() => secoesDoPerfil(this.perfil()).map((id) => SECOES[id]));
  protected readonly consultas = computed(() =>
    (this.paciente()?.consultas ?? []).map((c) => ({ ...c, dataBR: formatarData(c.data) })),
  );

  protected readonly anotacaoAberta = signal(false);
  protected readonly anotacao = signal('');
  protected readonly erro = signal('');

  constructor() {
    effect(() => this.clinica.carregarDocumentos(this.id()));
    effect(() => this.clinica.carregarDiagnosticos(this.id()));
  }

  protected abrirAnotacao(): void {
    this.anotacao.set('');
    this.erro.set('');
    this.anotacaoAberta.set(true);
  }

  protected async salvarAnotacao(): Promise<void> {
    const texto = this.anotacao().trim();
    if (!texto) {
      this.erro.set('Escreva a anotação antes de salvar.');
      return;
    }
    try {
      await this.clinica.salvarDocumento({
        pacienteId: this.id(), secao: 'evolucoes', titulo: `Anotação ${formatarData(paraISO(new Date()))}`,
        dataSessao: paraISO(new Date()), tipoAtendimento: 'Presencial',
        conteudo: `<p>${escaparHtml(texto).replace(/\n/g, '<br>')}</p>`, anexos: [],
      });
      this.anotacaoAberta.set(false);
      this.aviso.mostrar('Anotação adicionada ao histórico.');
    } catch (erro) {
      console.error('Falha ao salvar anotação do prontuário:', erro);
      this.aviso.mostrar('Não foi possível salvar a anotação.');
    }
  }
}
