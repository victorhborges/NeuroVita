import { Component, computed, ElementRef, inject, input, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { anexoDe, baixarAnexo } from '../../shared/clinica/arquivos';
import { Clinica } from '../../shared/clinica/clinica';
import { SECOES, secaoDoPerfil } from '../../shared/clinica/secoes';
import { Secao } from '../../shared/clinica/modelos';
import { FaixaPaciente } from '../../shared/faixa-paciente/faixa-paciente';
import { HtmlSeguro } from '../../shared/html-seguro';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData } from '../../shared/util';

/**
 * Leitura de um documento (evolução, laudo, encaminhamento...), com a lista ao lado.
 * Rotas: /:perfil/pacientes/:id/evolucoes  e  /:perfil/pacientes/:id/:secao/:docId
 */
@Component({
  selector: 'app-documento-leitura',
  imports: [FaixaPaciente, HtmlSeguro, Icone, RouterLink, TituloPagina],
  templateUrl: './documento-leitura.html',
})
export class DocumentoLeitura {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);

  readonly id = input.required<string>();
  /** Vem do `data` da rota (psicologo ou psiquiatra). */
  readonly perfil = input<Perfil>('psicologo');
  protected readonly raiz = computed(() => `/${this.perfil()}`);
  readonly secao = input.required<string>();
  readonly docId = input<string>();

  private readonly arquivoCampo = viewChild<ElementRef<HTMLInputElement>>('arquivo');

  protected readonly paciente = computed(() => this.clinica.paciente(this.id()));
  protected readonly secaoValida = computed<Secao | null>(() => secaoDoPerfil(this.secao(), this.perfil()));
  protected readonly cfg = computed(() => SECOES[this.secaoValida() ?? 'evolucoes']);

  protected readonly docs = computed(() =>
    this.clinica.documentosDe(this.id(), this.secaoValida() ?? undefined).map((d) => ({
      doc: d,
      dataBR: formatarData(d.dataSessao),
      sub: d.secao === 'evolucoes' ? `Sessão ${d.tipoAtendimento.toLowerCase()}` : d.titulo,
    })),
  );

  protected readonly selecionado = computed(() => {
    const lista = this.docs();
    const alvo = this.docId();
    return alvo ? lista.find((i) => i.doc.id === alvo)?.doc : lista[0]?.doc;
  });

  protected readonly voltarPara = computed(() =>
    this.cfg().id === 'evolucoes' ? `${this.raiz()}/pacientes/${this.id()}` : `${this.raiz()}/pacientes/${this.id()}/${this.cfg().id}`,
  );

  protected readonly formatar = formatarData;

  protected escolherArquivo(): void {
    this.arquivoCampo()?.nativeElement.click();
  }

  protected anexar(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const arquivos = Array.from(campo.files ?? []);
    const doc = this.selecionado();
    campo.value = '';
    if (!doc || !arquivos.length) return;
    this.clinica.salvarDocumento({ ...doc, anexos: [...doc.anexos, ...arquivos.map(anexoDe)] });
    this.aviso.mostrar('Arquivo anexado com sucesso.');
  }

  protected baixar(nome: string, url: string): void {
    baixarAnexo(nome, url);
  }
}
