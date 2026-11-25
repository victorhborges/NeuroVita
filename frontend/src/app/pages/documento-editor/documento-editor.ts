import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { anexoDe } from '../../shared/clinica/arquivos';
import { Clinica } from '../../shared/clinica/clinica';
import { Anexo, Secao, TipoAtendimento } from '../../shared/clinica/modelos';
import { modeloDeDocumento } from '../../shared/clinica/modelos-documento';
import { SECOES, secaoDoPerfil } from '../../shared/clinica/secoes';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { EditorTexto } from '../../shared/editor-texto/editor-texto';
import { FaixaPaciente } from '../../shared/faixa-paciente/faixa-paciente';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, formatarDataHora, htmlVazio, idadeDe, paraISO, primeiraLinha } from '../../shared/util';

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
      sub: d.secao === 'evolucoes'
        ? `Sessão ${d.tipoAtendimento?.toLowerCase() ?? 'sem modalidade informada'}`
        : d.titulo,
    })),
  );

  protected readonly voltarPara = computed(() => {
    const base = `${this.raiz()}/pacientes/${this.id()}`;
    if (this.docId()) return `${base}/${this.cfg().id}/${this.docId()}`;
    return this.cfg().id === 'evolucoes' ? `${base}/evolucoes` : `${base}/${this.cfg().id}`;
  });

  constructor() {
    effect(() => {
      const pacienteId = this.id();
      if (!pacienteId) return;
      this.clinica.carregarDocumentos(pacienteId);
      this.clinica.carregarDiagnosticos(pacienteId);
    });

    // Ao abrir um documento existente, copia os dados dele para o formulário (uma vez).
    effect(() => {
      const d = this.doc();
      if (this.carregado || !d) return;
      this.carregado = true;
      this.conteudo.set(d.conteudo);
      this.dataSessao.set(d.dataSessao);
      const tipo = d.tipoAtendimento ?? 'Presencial';
      this.tipo.set(tipo);
      this.anexos.set(d.anexos);
      this.inicial.set({ conteudo: d.conteudo, dataSessao: d.dataSessao, tipo, anexos: d.anexos.length });
    });

    // Documento novo do psicólogo: já abre com o texto-modelo da seção, com os dados do paciente preenchidos.
    effect(() => {
      const p = this.paciente();
      const secao = this.secaoValida();
      if (this.carregado || this.docId() || !p || !secao || this.perfil() !== 'psicologo') return;
      const modelo = modeloDeDocumento(secao, { nome: p.nome, idade: idadeDe(p.nascimento), data: formatarData(this.hoje) });
      if (!modelo) return;
      this.carregado = true;
      this.conteudo.set(modelo);
      this.inicial.set({ conteudo: modelo, dataSessao: this.hoje, tipo: 'Presencial', anexos: 0 });
    });
  }

  protected anexar(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const novos = Array.from(campo.files ?? []).map(anexoDe);
    campo.value = '';
    if (novos.length) this.anexos.update((l) => [...l, ...novos]);
  }

  protected removerAnexo(indice: number): void {
    const anexo = this.anexos()[indice];
    if (anexo && !this.podeRemoverAnexo(anexo)) {
      this.aviso.mostrar('Anexos vinculados a este registro não podem ser removidos.');
      return;
    }
    this.anexos.update((l) => l.filter((_, i) => i !== indice));
  }

  protected podeRemoverAnexo(anexo: Anexo): boolean {
    const documento = this.doc();
    if (documento?.origem !== 'evolucao' && documento?.origem !== 'prescricao') return true;
    return !documento.anexos.some((existente) =>
      existente.nome === anexo.nome && existente.url === anexo.url);
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

  protected async confirmar(): Promise<void> {
    const existente = this.doc();
    const cfg = this.cfg();
    if (existente?.origem === 'evolucao') {
      this.aviso.mostrar('Evoluções registradas não podem ser alteradas. Registre uma nova evolução.');
      return;
    }
    const dataBR = formatarData(this.dataSessao());
    const titulo =
      existente?.titulo ??
      (cfg.id === 'evolucoes' ? 'Evolução' : primeiraLinha(this.conteudo()) || `${cfg.singular} ${dataBR}`);

    try {
      const salvo = await this.clinica.salvarDocumento({
        id: existente?.id, origem: existente?.origem, pacienteId: this.id(), secao: cfg.id, titulo, dataSessao: this.dataSessao(),
        tipoAtendimento: this.tipo(), conteudo: this.conteudo(), anexos: this.anexos(), arquivo: existente?.arquivo,
        medicamentos: existente?.medicamentos, tipoExame: existente?.tipoExame,
        profissionalId: existente?.profissionalId, dataRealizacao: existente?.dataRealizacao,
        resultado: existente?.resultado, observacoes: existente?.observacoes,
      });

      this.confirmando.set(false);
      this.carregado = true;
      this.inicial.set({ conteudo: this.conteudo(), dataSessao: this.dataSessao(), tipo: this.tipo(), anexos: this.anexos().length });
      this.aviso.mostrar('Alterações salvas com sucesso.');
      this.router.navigate([this.raiz(), 'pacientes', this.id(), cfg.id, salvo.id]);
    } catch (erro) {
      console.error('Falha ao salvar documento:', erro);
      this.aviso.mostrar('Não foi possível salvar o documento. Tente novamente.');
    }
  }
}
