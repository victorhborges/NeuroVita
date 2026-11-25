import { Component, ElementRef, computed, effect, inject, input, signal, viewChild } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { anexoDe, baixarDocumento, ehImagem, semExtensao } from '../../shared/clinica/arquivos';
import { Clinica } from '../../shared/clinica/clinica';
import { Documento, Secao } from '../../shared/clinica/modelos';
import { SECOES, secaoDoPerfil } from '../../shared/clinica/secoes';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { FaixaPaciente } from '../../shared/faixa-paciente/faixa-paciente';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, paraISO, resumoTexto } from '../../shared/util';

/** Grupo dos textos escritos no editor do sistema. */
const GRUPO_ESCRITOS = 'Escritos no sistema';
const NOVO_GRUPO = '__novo__';

/**
 * Seção do prontuário: /:perfil/pacientes/:id/:secao
 * Galeria de documentos, organizada em grupos (ex.: "Relatório escolar"), com miniatura, nome, data e botão de baixar.
 * Exceção: "Medicamentos e prescrições" (psiquiatra) mostra duas tabelas: medicamentos em uso e histórico de prescrições.
 */
@Component({
  selector: 'app-secao-paciente',
  imports: [Dialogo, FaixaPaciente, Icone, RouterLink, TituloPagina],
  templateUrl: './secao-paciente.html',
})
export class SecaoPaciente {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);
  private readonly router = inject(Router);

  constructor() {
    effect(() => {
      const pacienteId = this.id();
      if (!pacienteId) return;
      this.clinica.carregarDocumentos(pacienteId);
      this.clinica.carregarDiagnosticos(pacienteId);
    });
  }

  readonly id = input.required<string>();
  readonly secao = input.required<string>();
  /** Vem do `data` da rota (psicologo ou psiquiatra). */
  readonly perfil = input<Perfil>('psicologo');
  protected readonly raiz = computed(() => `/${this.perfil()}`);

  private readonly arquivoCampo = viewChild<ElementRef<HTMLInputElement>>('arquivo');

  protected readonly paciente = computed(() => this.clinica.paciente(this.id()));
  protected readonly secaoValida = computed<Secao | null>(() => secaoDoPerfil(this.secao(), this.perfil()));
  protected readonly cfg = computed(() => SECOES[this.secaoValida() ?? 'evolucoes']);

  /** Documento escolhido em "Selecionar documento" (vazio = todos). */
  protected readonly filtroDoc = signal('');

  // ----- envio de arquivos -----
  protected readonly pendentes = signal<File[]>([]);
  protected readonly grupoEscolhido = signal('');
  protected readonly grupoNovo = signal('');
  protected readonly NOVO = NOVO_GRUPO;

  private grupoDe(d: Documento): string {
    return d.grupo ?? (d.arquivo ? this.cfg().galeria.gruposPadrao[0] : GRUPO_ESCRITOS);
  }

  private paraCartao(doc: Documento) {
    const arquivo = doc.arquivo;
    const imagem = !!arquivo && ehImagem(arquivo.nome);
    return {
      doc,
      dataBR: formatarData(doc.dataSessao),
      imagem,
      /** Imagem: o próprio arquivo. Texto escrito no sistema: um trecho, como numa folha em miniatura. */
      miniatura: arquivo ? '' : resumoTexto(doc.conteudo),
      grupo: this.grupoDe(doc),
    };
  }

  private readonly todos = computed(() =>
    this.clinica.documentosDe(this.id(), this.secaoValida() ?? undefined).map((d) => this.paraCartao(d)),
  );

  /** Documentos por grupo, na ordem dos grupos padrão (os escritos no sistema ficam por último). */
  protected readonly galeria = computed(() => {
    const escolhido = this.filtroDoc();
    const visiveis = escolhido ? this.todos().filter((c) => c.doc.id === escolhido) : this.todos();
    const padrao = this.cfg().galeria.gruposPadrao;
    const nomes = [...new Set(visiveis.map((c) => c.grupo))].sort((a, b) => {
      const peso = (n: string) => (n === GRUPO_ESCRITOS ? 1000 : padrao.indexOf(n) >= 0 ? padrao.indexOf(n) : 500);
      return peso(a) - peso(b);
    });
    return nomes.map((nome) => ({ nome, itens: visiveis.filter((c) => c.grupo === nome) }));
  });

  protected readonly documentos = computed(() => this.todos());

  // ----- Medicamentos e prescrições (tabelas) -----
  protected readonly ehPrescricoes = computed(() => this.secaoValida() === 'medicamentos');

  protected readonly emUso = computed(() => {
    const lista = this.todos().filter((c) => c.doc.origem === 'prescricao');
    return lista.flatMap((c) =>
      (c.doc.medicamentos ?? []).map((m) => ({
        medicamento: m.medicamento,
        dosagem: m.dosagem ?? '—',
        frequencia: m.frequencia ?? m.posologia ?? '—',
        frequenciaCompleta: `${m.dosagem ?? ''} ${m.frequencia ?? m.posologia ?? ''}`.trim() || '—',
        inicioBR: formatarData(c.doc.dataSessao),
        inicioTratamento: c.doc.dataSessao,
        proximaReavaliacao: '',
        observacoes: m.duracao ? `Duração: ${m.duracao}` : c.doc.conteudo || '—',
      })),
    );
  });

  /** Medicamento escolhido no ícone de olho; enquanto ninguém escolhe, vale o primeiro da lista. */
  protected readonly medEscolhido = signal<string | null>(null);

  protected readonly detalhe = computed(() => {
    const lista = this.emUso();
    const m = lista.find((x) => x.medicamento === this.medEscolhido()) ?? lista[0];
    if (!m) return null;
    return {
      medicamento: m.medicamento, dosagem: m.dosagem, frequencia: m.frequencia,
      inicio: m.inicioTratamento ? formatarData(m.inicioTratamento) : '—',
      reavaliacao: m.proximaReavaliacao ? formatarData(m.proximaReavaliacao) : '—',
      observacao: m.observacoes,
    };
  });

  /** Documentos legados e prescrições carregadas da API. */
  protected readonly historico = computed(() => {
    const escritas = this.todos()
      .filter((c) => !c.doc.arquivo && !c.doc.id.startsWith('seed-') && c.doc.origem !== 'prescricao')
      .map((c) => ({ data: c.dataBR, medicamentos: c.doc.titulo, observacoes: '—', ativa: true, docId: c.doc.id as string | undefined }));
    const daApi = this.todos()
      .filter((c) => c.doc.origem === 'prescricao')
      .map((c) => ({
        data: c.dataBR,
        medicamentos: (c.doc.medicamentos ?? []).map((m) => m.medicamento).join(', ') || c.doc.conteudo || 'Prescrição',
        observacoes: c.doc.conteudo || '—',
        ativa: true,
        docId: c.doc.id,
      }));
    return [...escritas, ...daApi];
  });

  /** "Selecionar documento": filtra a galeria ou, nas prescrições, abre o documento escolhido. */
  protected selecionarDocumento(valor: string): void {
    if (!this.ehPrescricoes()) {
      this.filtroDoc.set(valor);
      return;
    }
    if (valor) this.router.navigate([this.raiz(), 'pacientes', this.id(), this.cfg().id, valor]);
  }

  /** Grupos que já existem (ou são padrão), para escolher ao adicionar arquivos. */
  protected readonly gruposParaEscolher = computed(() => {
    const existentes = this.todos().map((c) => c.grupo).filter((g) => g !== GRUPO_ESCRITOS);
    return [...new Set([...this.cfg().galeria.gruposPadrao, ...existentes])];
  });

  protected escolherArquivo(): void {
    this.arquivoCampo()?.nativeElement.click();
  }

  protected aoEscolherArquivos(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const arquivos = Array.from(campo.files ?? []);
    campo.value = '';
    if (!arquivos.length) return;
    this.grupoEscolhido.set(this.gruposParaEscolher()[0] ?? NOVO_GRUPO);
    this.grupoNovo.set('');
    this.pendentes.set(arquivos);
  }

  protected async confirmarEnvio(): Promise<void> {
    const novo = this.grupoEscolhido() === NOVO_GRUPO;
    const grupo = (novo ? this.grupoNovo() : this.grupoEscolhido()).trim();
    if (!grupo) {
      this.aviso.mostrar('Escreva o nome do novo grupo.');
      return;
    }
    const arquivos = this.pendentes();
    try {
      for (const arquivo of arquivos) {
        await this.clinica.salvarDocumento({
          origem: 'documento-clinico',
          pacienteId: this.id(), secao: this.cfg().id, titulo: semExtensao(arquivo.name), dataSessao: paraISO(new Date()),
          tipoAtendimento: 'Presencial', conteudo: '', anexos: [], arquivo: anexoDe(arquivo), grupo,
        });
      }
      this.pendentes.set([]);
      this.aviso.mostrar(arquivos.length > 1 ? 'Documentos adicionados com sucesso.' : 'Documento adicionado com sucesso.');
    } catch (erro) {
      console.error('Falha ao enviar documento:', erro);
      this.aviso.mostrar('Não foi possível enviar o documento.');
    }
  }

  protected escreverNoEditor(): void {
    this.router.navigate([this.raiz(), 'pacientes', this.id(), this.cfg().id, 'novo']);
  }

  protected baixar(doc: Documento): void {
    baixarDocumento(doc);
  }
}
