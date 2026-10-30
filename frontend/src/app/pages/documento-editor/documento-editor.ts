import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { anexoDe } from '../../shared/clinica/arquivos';
import { Clinica } from '../../shared/clinica/clinica';
import { Anexo, Secao, TipoAtendimento } from '../../shared/clinica/modelos';
import { SECOES, secaoDoPerfil } from '../../shared/clinica/secoes';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { EditorTexto } from '../../shared/editor-texto/editor-texto';
import { FaixaPaciente } from '../../shared/faixa-paciente/faixa-paciente';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, formatarDataHora, htmlVazio, paraISO, primeiraLinha } from '../../shared/util';

/**
 * Criar ou editar um documento.
 * Rotas: /:perfil/pacientes/:id/:secao/novo  e  /:perfil/pacientes/:id/:secao/:docId/editar
 */
@Component({
  selector: 'app-documento-editor',
  imports: [Dialogo, EditorTexto, FaixaPaciente, Icone, RouterLink, TituloPagina],
  templateUrl: './documento-editor.html',
})
export class DocumentoEditor {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);
  private readonly router = inject(Router);

  readonly id = input.required<string>();
  /** Vem do `data` da rota (psicologo ou psiquiatra). */
  readonly perfil = input<Perfil>('psicologo');
  protected readonly raiz = computed(() => `/${this.perfil()}`);
  readonly secao = input.required<string>();
  /** Sem docId = documento novo. */
  readonly docId = input<string>();

  protected readonly hoje = paraISO(new Date());
  protected readonly paciente = computed(() => this.clinica.paciente(this.id()));
  protected readonly secaoValida = computed<Secao | null>(() => secaoDoPerfil(this.secao(), this.perfil()));
  protected readonly cfg = computed(() => SECOES[this.secaoValida() ?? 'evolucoes']);
  protected readonly doc = computed(() => this.clinica.documentos().find((d) => d.id === this.docId()));

  protected readonly conteudo = signal('');
  protected readonly dataSessao = signal(this.hoje);
  protected readonly tipo = signal<TipoAtendimento>('Presencial');
  protected readonly anexos = signal<Anexo[]>([]);
  protected readonly confirmando = signal(false);

  private readonly inicial = signal({ conteudo: '', dataSessao: this.hoje, tipo: 'Presencial', anexos: 0 });
  private carregado = false;

  protected readonly sujo = computed(() => {
    const i = this.inicial();
    return (
      this.conteudo() !== i.conteudo || this.dataSessao() !== i.dataSessao ||
      this.tipo() !== i.tipo || this.anexos().length !== i.anexos
    );
  });

  protected readonly alteradoEm = computed(() => formatarDataHora(this.doc()?.atualizadoEm ?? new Date().toISOString()));

  protected readonly lista = computed(() =>
    this.clinica.documentosDe(this.id(), this.secaoValida() ?? undefined).map((d) => ({
      doc: d,
      dataBR: formatarData(d.dataSessao),
      sub: d.secao === 'evolucoes' ? `Sessão ${d.tipoAtendimento.toLowerCase()}` : d.titulo,
    })),
  );

  protected readonly voltarPara = computed(() => {
    const base = `${this.raiz()}/pacientes/${this.id()}`;
    if (this.docId()) return `${base}/${this.cfg().id}/${this.docId()}`;
    return this.cfg().id === 'evolucoes' ? `${base}/evolucoes` : `${base}/${this.cfg().id}`;
  });

  constructor() {
    // Ao abrir um documento existente, copia os dados dele para o formulário (uma vez).
    effect(() => {
      const d = this.doc();
      if (this.carregado || !d) return;
      this.carregado = true;
      this.conteudo.set(d.conteudo);
      this.dataSessao.set(d.dataSessao);
      this.tipo.set(d.tipoAtendimento);
      this.anexos.set(d.anexos);
      this.inicial.set({ conteudo: d.conteudo, dataSessao: d.dataSessao, tipo: d.tipoAtendimento, anexos: d.anexos.length });
    });
  }

  protected anexar(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const novos = Array.from(campo.files ?? []).map(anexoDe);
    campo.value = '';
    if (novos.length) this.anexos.update((l) => [...l, ...novos]);
  }

  protected removerAnexo(indice: number): void {
    this.anexos.update((l) => l.filter((_, i) => i !== indice));
  }

  /** Pergunta antes de sair para outra tela se houver texto não salvo. */
  protected sair(evento: Event): void {
    if (this.sujo() && !window.confirm('Há alterações não salvas. Deseja sair mesmo assim?')) evento.preventDefault();
  }

  protected pedirConfirmacao(): void {
    if (htmlVazio(this.conteudo())) {
      this.aviso.mostrar('Escreva o conteúdo antes de salvar.');
      return;
    }
    this.confirmando.set(true);
  }

  protected confirmar(): void {
    const existente = this.doc();
    const cfg = this.cfg();
    const dataBR = formatarData(this.dataSessao());
    const titulo =
      existente?.titulo ??
      (cfg.id === 'evolucoes' ? `Evolução ${dataBR}` : primeiraLinha(this.conteudo()) || `${cfg.singular} ${dataBR}`);

    const salvo = this.clinica.salvarDocumento({
      id: existente?.id, pacienteId: this.id(), secao: cfg.id, titulo, dataSessao: this.dataSessao(),
      tipoAtendimento: this.tipo(), conteudo: this.conteudo(), anexos: this.anexos(), arquivo: existente?.arquivo,
    });

    this.confirmando.set(false);
    this.carregado = true;
    this.inicial.set({ conteudo: this.conteudo(), dataSessao: this.dataSessao(), tipo: this.tipo(), anexos: this.anexos().length });
    this.aviso.mostrar('Alterações salvas com sucesso.');
    this.router.navigate([this.raiz(), 'pacientes', this.id(), cfg.id, salvo.id]);
  }
}
