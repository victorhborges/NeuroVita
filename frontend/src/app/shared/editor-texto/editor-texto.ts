import { Component, ElementRef, effect, input, model, output, signal, viewChild } from '@angular/core';

import { Icone } from '../icone/icone';
import { escaparHtml, htmlVazio, limparHtml } from '../util';

/**
 * Editor de texto com barra de formatação (parágrafo, tamanho, negrito, itálico, sublinhado,
 * cor, alinhamento, link, imagem, listas e tela cheia).
 * O texto formatado (HTML) fica em `valor`.
 */
@Component({
  selector: 'app-editor-texto',
  imports: [Icone],
  templateUrl: './editor-texto.html',
  host: {
    '(document:selectionchange)': 'guardarSelecao()',
    '(document:keydown.escape)': 'telaCheia.set(false)',
    '(document:click)': 'fecharMenu($event)',
  },
})
export class EditorTexto {
  readonly valor = model('');
  readonly tituloVazio = input('Comece a escrever');
  readonly mostrarFinalizar = input(false);
  readonly finalizar = output<void>();

  private readonly area = viewChild<ElementRef<HTMLElement>>('area');
  private readonly raiz = viewChild<ElementRef<HTMLElement>>('raiz');

  protected readonly vazio = signal(true);
  protected readonly focado = signal(false);
  protected readonly telaCheia = signal(false);
  protected readonly menuMais = signal(false);
  protected readonly negrito = signal(false);
  protected readonly italico = signal(false);
  protected readonly sublinhado = signal(false);
  protected readonly tamanho = signal(12);
  protected readonly cor = signal('#0b1220');

  protected readonly blocos = [
    { valor: 'P', rotulo: 'Parágrafo' },
    { valor: 'H1', rotulo: 'Título 1' },
    { valor: 'H2', rotulo: 'Título 2' },
    { valor: 'H3', rotulo: 'Título 3' },
  ];
  protected readonly tamanhos = [10, 12, 14, 16, 18, 24];

  private ultimaSelecao: Range | null = null;

  constructor() {
    // Quando o texto muda por fora (ex.: abrir uma evolução para editar), atualiza a área.
    effect(() => {
      const html = this.valor();
      const el = this.area()?.nativeElement;
      if (!el) return;
      if (el.innerHTML !== html) el.innerHTML = limparHtml(html);
      this.vazio.set(htmlVazio(html));
    });
  }

  protected aoDigitar(): void {
    const el = this.area()?.nativeElement;
    if (!el) return;
    this.valor.set(el.innerHTML);
    this.vazio.set(htmlVazio(el.innerHTML));
  }

  protected guardarSelecao(): void {
    const el = this.area()?.nativeElement;
    const sel = document.getSelection();
    if (!el || !sel || sel.rangeCount === 0) return;
    const range = sel.getRangeAt(0);
    if (!el.contains(range.commonAncestorContainer)) return;
    this.ultimaSelecao = range.cloneRange();
    this.negrito.set(document.queryCommandState('bold'));
    this.italico.set(document.queryCommandState('italic'));
    this.sublinhado.set(document.queryCommandState('underline'));
  }

  protected fecharMenu(evento: Event): void {
    if (this.menuMais() && !this.raiz()?.nativeElement.contains(evento.target as Node)) {
      this.menuMais.set(false);
    }
  }

  private restaurarSelecao(): void {
    const el = this.area()?.nativeElement;
    if (!el) return;
    el.focus();
    if (this.ultimaSelecao) {
      const sel = document.getSelection();
      sel?.removeAllRanges();
      sel?.addRange(this.ultimaSelecao);
    }
  }

  protected executar(comando: string, argumento?: string): void {
    this.restaurarSelecao();
    document.execCommand(comando, false, argumento);
    this.aoDigitar();
    this.guardarSelecao();
  }

  protected mudarBloco(evento: Event): void {
    this.executar('formatBlock', (evento.target as HTMLSelectElement).value);
  }

  protected mudarTamanho(evento: Event): void {
    const px = Number((evento.target as HTMLSelectElement).value);
    this.tamanho.set(px);
    this.executar('fontSize', '7');
    const el = this.area()?.nativeElement;
    el?.querySelectorAll('font[size="7"]').forEach((fonte) => {
      const span = document.createElement('span');
      span.style.fontSize = `${px}px`;
      span.innerHTML = fonte.innerHTML;
      fonte.replaceWith(span);
    });
    this.aoDigitar();
  }

  protected mudarCor(evento: Event): void {
    const cor = (evento.target as HTMLInputElement).value;
    this.cor.set(cor);
    this.executar('foreColor', cor);
  }

  protected inserirLink(): void {
    const texto = window.prompt('Endereço do link (ex.: https://...)');
    if (!texto) return;
    const url = /^(https?:|mailto:)/i.test(texto.trim()) ? texto.trim() : `https://${texto.trim()}`;
    this.executar('createLink', url);
  }

  protected inserirImagem(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const arquivo = campo.files?.[0];
    campo.value = '';
    if (!arquivo || !arquivo.type.startsWith('image/')) return;
    const leitor = new FileReader();
    leitor.onload = () => this.executar('insertImage', String(leitor.result));
    leitor.readAsDataURL(arquivo);
  }

  protected aoColar(evento: ClipboardEvent): void {
    evento.preventDefault();
    const html = evento.clipboardData?.getData('text/html');
    const texto = evento.clipboardData?.getData('text/plain') ?? '';
    const limpo = html ? limparHtml(html) : escaparHtml(texto).replace(/\n/g, '<br>');
    document.execCommand('insertHTML', false, limpo);
    this.aoDigitar();
  }

  protected focar(): void {
    this.area()?.nativeElement.focus();
  }

  protected alternarTelaCheia(): void {
    this.telaCheia.update((v) => !v);
  }
}
