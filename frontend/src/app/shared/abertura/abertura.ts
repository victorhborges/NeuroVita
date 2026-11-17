import { Component, DestroyRef, ElementRef, Injectable, NgZone, afterNextRender, inject, signal, viewChild } from '@angular/core';

/**
 * Velocidade da animação de abertura. 1 = velocidade do protótipo; menor = mais rápida.
 * (Todos os tempos do CSS são multiplicados por este número.)
 */
const RITMO = 0.85;
/** Tempo (antes do ritmo) até a abertura começar a sumir, em milissegundos. */
const DURACAO_BASE_MS = 7000;
/** Tempo do desaparecimento (precisa ser igual à transição de opacidade do CSS). */
const SAIDA_MS = 500;
/** Duração quando a pessoa pediu "reduzir movimento" no sistema. */
const DURACAO_REDUZIDA_MS = 1400;
const CHAVE = 'neurovita:abertura';
/** Diz se a abertura está na tela (o resto da página fica inativo enquanto isso). */
@Injectable({ providedIn: 'root' })
export class AberturaEstado {
  readonly ativa = signal(false);
}

/**
 * Tela de abertura animada, antes do login.
 * - Toca automaticamente uma vez por sessão; `?abertura` permite pré-visualizar novamente.
 * - Dá para pular com um clique, Enter, Espaço ou Esc.
 * - Com "reduzir movimento" ligado no sistema, mostra só a logo parada.
 */
@Component({
  selector: 'app-abertura',
  templateUrl: './abertura.html',
  host: { '(document:keydown)': 'aoTeclar($event)' },
})
export class Abertura {
  private readonly estado = inject(AberturaEstado);
  private readonly zona = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);
  private readonly botao = viewChild<ElementRef<HTMLButtonElement>>('pular');

  protected readonly visivel = signal(false);
  protected readonly saindo = signal(false);
  protected readonly reduzida = signal(false);
  protected readonly ritmo = RITMO;
  /** Quanto as cápsulas "caem" (para começarem fora da tela em qualquer tamanho de janela). */
  protected readonly queda = signal('-900px');

  private readonly temporizadores: ReturnType<typeof setTimeout>[] = [];

  constructor() {
    if (!this.deveTocar()) return;

    const reduzida = typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;
    this.reduzida.set(reduzida);
    this.queda.set(this.calcularQueda());
    this.visivel.set(true);
    this.estado.ativa.set(true);

    this.agendar(
      () => this.zona.run(() => this.encerrar()),
      reduzida ? DURACAO_REDUZIDA_MS : DURACAO_BASE_MS * RITMO,
    );
    afterNextRender(() => this.botao()?.nativeElement.focus());
    this.destroyRef.onDestroy(() => this.temporizadores.forEach(clearTimeout));
  }

  protected encerrar(): void {
    if (!this.visivel() || this.saindo()) return;
    this.saindo.set(true);
    this.agendar(() => {
      this.visivel.set(false);
      this.estado.ativa.set(false);
    }, SAIDA_MS);
  }

  protected aoTeclar(evento: KeyboardEvent): void {
    if (this.visivel() && ['Enter', ' ', 'Escape'].includes(evento.key)) {
      evento.preventDefault();
      this.encerrar();
    }
  }

  private agendar(acao: () => void, ms: number): void {
    this.temporizadores.push(setTimeout(acao, ms));
  }

  private deveTocar(): boolean {
    const forcar = new URLSearchParams(location.search).has('abertura');
    try {
      // Reproduz automaticamente na primeira abertura desta sessão. O parâmetro
      // continua disponível para pré-visualizar a animação novamente.
      if (!forcar && sessionStorage.getItem(CHAVE)) return false;
      sessionStorage.setItem(CHAVE, '1');
    } catch {
      // Sem acesso ao armazenamento, a abertura ainda toca e termina pelo temporizador.
    }
    return true;
  }

  /** Distância (em unidades do desenho, 960x540) para a cápsula começar acima da borda da tela. */
  private calcularQueda(): string {
    const larguraDoPalco = Math.min(innerWidth, (innerHeight * 16) / 9);
    const topoDaTela = 270 - (innerHeight / 2) * (960 / larguraDoPalco);
    const pontaDeBaixo = 232 + 28 + 48; // ponta de baixo da cápsula já em seu lugar
    return `${Math.round(Math.min(topoDaTela - pontaDeBaixo - 10, -400))}px`;
  }
}
