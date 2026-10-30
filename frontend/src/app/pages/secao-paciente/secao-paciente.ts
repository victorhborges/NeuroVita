import { Component, computed, ElementRef, inject, input, signal, viewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { anexoDe, baixarDocumento, compartilharLink, semExtensao } from '../../shared/clinica/arquivos';
import { Clinica } from '../../shared/clinica/clinica';
import { Documento, Secao } from '../../shared/clinica/modelos';
import { SECOES, secaoDoPerfil } from '../../shared/clinica/secoes';
import { FaixaPaciente } from '../../shared/faixa-paciente/faixa-paciente';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, normalizar, paraISO } from '../../shared/util';
import { DocumentoLeitura } from '../documento-leitura/documento-leitura';

/**
 * Tela de uma seção do prontuário: /:perfil/pacientes/:id/:secao
 * - Evoluções abre direto no texto (DocumentoLeitura).
 * - Laudos, Encaminhamentos, Avaliações e Atestados mostram a lista de documentos.
 */
@Component({
  selector: 'app-secao-paciente',
  imports: [DocumentoLeitura, FaixaPaciente, Icone, RouterLink, TituloPagina],
  templateUrl: './secao-paciente.html',
  host: { '(document:click)': 'fecharMenu($event)' },
})
export class SecaoPaciente {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);
  private readonly router = inject(Router);

  readonly id = input.required<string>();
  /** Vem do `data` da rota (psicologo ou psiquiatra). */
  readonly perfil = input<Perfil>('psicologo');
  protected readonly raiz = computed(() => `/${this.perfil()}`);
  readonly secao = input.required<string>();

  private readonly arquivoCampo = viewChild<ElementRef<HTMLInputElement>>('arquivo');
  private readonly menuRaiz = viewChild<ElementRef<HTMLElement>>('menuCaixa');

  protected readonly paciente = computed(() => this.clinica.paciente(this.id()));
  protected readonly secaoValida = computed<Secao | null>(() => secaoDoPerfil(this.secao(), this.perfil()));
  protected readonly cfg = computed(() => SECOES[this.secaoValida() ?? 'laudos']);

  protected readonly dataFiltro = signal('');
  protected readonly tipoFiltro = signal('');
  protected readonly busca = signal('');
  protected readonly menuNovo = signal(false);
  protected readonly hoje = paraISO(new Date());

  protected readonly itens = computed(() => {
    const termo = normalizar(this.busca());
    return this.clinica
      .documentosDe(this.id(), this.secaoValida() ?? undefined)
      .filter((d) => !this.dataFiltro() || d.dataSessao === this.dataFiltro())
      .filter((d) => !this.tipoFiltro() || d.tipoAtendimento === this.tipoFiltro())
      .filter((d) => !termo || normalizar(d.titulo).includes(termo))
      .map((d) => ({ doc: d, dataBR: formatarData(d.dataSessao) }));
  });

  protected readonly dados = computed(() => {
    const p = this.paciente();
    return [
      { icone: 'arquivo-texto' as const, rotulo: 'Diagnóstico principal', valor: p?.diagnostico.principal ?? '—' },
      { icone: 'relogio' as const, rotulo: 'CID', valor: `(${p?.diagnostico.cid ?? '—'})` },
      { icone: 'arquivo-mais' as const, rotulo: 'Início do tratamento', valor: formatarData(p?.diagnostico.inicioTratamento) },
    ];
  });

  protected fecharMenu(evento: Event): void {
    if (this.menuNovo() && !this.menuRaiz()?.nativeElement.contains(evento.target as Node)) this.menuNovo.set(false);
  }

  protected criarEmBranco(): void {
    this.menuNovo.set(false);
    this.router.navigate([this.raiz(), 'pacientes', this.id(), this.cfg().id, 'novo']);
  }

  protected escolherArquivo(): void {
    this.menuNovo.set(false);
    this.arquivoCampo()?.nativeElement.click();
  }

  protected aoEnviar(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const arquivos = Array.from(campo.files ?? []);
    campo.value = '';
    if (!arquivos.length) return;
    for (const arquivo of arquivos) {
      this.clinica.salvarDocumento({
        pacienteId: this.id(), secao: this.cfg().id, titulo: semExtensao(arquivo.name),
        dataSessao: this.dataFiltro() || this.hoje,
        tipoAtendimento: this.tipoFiltro() === 'Online' ? 'Online' : 'Presencial',
        conteudo: '', anexos: [], arquivo: anexoDe(arquivo),
      });
    }
    this.aviso.mostrar(arquivos.length > 1 ? 'Arquivos enviados com sucesso.' : 'Arquivo enviado com sucesso.');
  }

  protected baixar(doc: Documento): void {
    baixarDocumento(doc);
  }

  protected async compartilhar(doc: Documento): Promise<void> {
    const url = `${location.origin}${this.raiz()}/pacientes/${doc.pacienteId}/${doc.secao}/${doc.id}`;
    const resultado = await compartilharLink(doc.titulo, url);
    if (resultado === 'copiado') this.aviso.mostrar('Link copiado para a área de transferência.');
    else if (resultado === 'erro') this.aviso.mostrar('Não foi possível compartilhar o documento.');
  }
}
