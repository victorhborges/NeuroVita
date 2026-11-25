import { Component, ElementRef, Injectable, afterNextRender, inject, signal, viewChild } from '@angular/core';

/**
 * Velocidade da animação de abertura. 1 = velocidade do protótipo; menor = mais rápida.
 * (Todos os tempos do CSS são multiplicados por este número.)
 */
const RITMO = 0.85;
/** Tempo (antes do ritmo) até a abertura começar a sumir, em milissegundos. */
const DURACAO_BASE_MS = 3900;
/** Tempo do desaparecimento (precisa ser igual à transição de opacidade do CSS). */
const SAIDA_MS = 500;
/** Duração quando a pessoa pediu "reduzir movimento" no sistema. */
const DURACAO_REDUZIDA_MS = 1400;
/** Guarda, na sessão do navegador, que a abertura já tocou. */
const CHAVE = 'neurovita:abertura';


/**
 * Caminho de cada cápsula (lado esquerdo; o direito é o espelho), no desenho 960x540:
 * desce reta, faz a curva embaixo e segue para o cérebro, por trás dele.
 * A cápsula é uma linha grossa que "anda" por esse caminho (CSS `stroke-dashoffset`): a ponta vai na frente
 * e o resto vem junto, na mesma curva, como se estivesse se juntando ao cérebro.
 */
const RETA_ATE = 232; // onde a descida reta termina e a curva começa
const CURVAS: number[][] = [
  [230, 300, 268, 360, 332, 360], // a curva de baixo
  [392, 360, 445, 345, 478, 300], // a subida para trás do cérebro
];

function caminhoCapsula(topo: number, espelhar: boolean): string {
  const x = (v: number) => (espelhar ? 960 - v : v);
  const c = CURVAS.map((p) => `C${x(p[0])},${p[1]} ${x(p[2])},${p[3]} ${x(p[4])},${p[5]}`).join(' ');
  return `M${x(230)},${topo} L${x(230)},${RETA_ATE} ${c}`;
}

/** Comprimento do caminho (a reta mais as duas curvas, medidas por amostragem). */
function comprimentoCaminho(topo: number): number {
  let total = RETA_ATE - topo;
  let [xAnt, yAnt] = [230, RETA_ATE]; // ponto onde a curva atual começa
  for (const [x1, y1, x2, y2, x3, y3] of CURVAS) {
    const [x0, y0] = [xAnt, yAnt];
    for (let i = 1; i <= 40; i++) {
      const t = i / 40;
      const u = 1 - t;
      const x = u ** 3 * x0 + 3 * u * u * t * x1 + 3 * u * t * t * x2 + t ** 3 * x3;
      const y = u ** 3 * y0 + 3 * u * u * t * y1 + 3 * u * t * t * y2 + t ** 3 * y3;
      total += Math.hypot(x - xAnt, y - yAnt);
      [xAnt, yAnt] = [x, y];
    }
  }
  return total;
}

/** Diz se a abertura está na tela (o resto da página fica inativo enquanto isso). */
@Injectable({ providedIn: 'root' })
export class AberturaEstado {
  readonly ativa = signal(false);
}

/**
 * Tela de abertura animada, antes do login.
 * - Toca uma vez por sessão (feche a aba para ver de novo, ou abra o site com `?abertura`).
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
  private readonly botao = viewChild<ElementRef<HTMLButtonElement>>('pular');

  protected readonly visivel = signal(false);
  protected readonly saindo = signal(false);
  protected readonly reduzida = signal(false);
  protected readonly ritmo = RITMO;
  /** Caminhos das duas cápsulas e quanto elas andam até o cérebro (o começo fica acima da borda da tela, em qualquer janela). */
  protected readonly caminhoEsquerda = signal(caminhoCapsula(-80, false));
  protected readonly caminhoDireita = signal(caminhoCapsula(-80, true));
  protected readonly percurso = signal(`${Math.round(comprimentoCaminho(-80))}px`);

  private readonly temporizadores: ReturnType<typeof setTimeout>[] = [];

  constructor() {
    if (!this.deveTocar()) return;

    const reduzida = typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;
    this.reduzida.set(reduzida);
    this.ajustarCaminho();
    this.visivel.set(true);
    this.estado.ativa.set(true);

    this.agendar(() => this.encerrar(), reduzida ? DURACAO_REDUZIDA_MS : DURACAO_BASE_MS * RITMO);
    afterNextRender(() => this.botao()?.nativeElement.focus());
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
      if (!forcar && sessionStorage.getItem(CHAVE)) return false;
      sessionStorage.setItem(CHAVE, '1');
    } catch {
      // Sem acesso ao armazenamento: toca normalmente.
    }
    return true;
  }

  /** Põe o começo do caminho acima da borda de cima da tela, qualquer que seja o tamanho da janela. */
  private ajustarCaminho(): void {
    const larguraDoPalco = Math.min(innerWidth, (innerHeight * 16) / 9);
    const topoDaTela = 270 - (innerHeight / 2) * (960 / larguraDoPalco); // em unidades do desenho 960x540
    const topo = Math.min(Math.round(topoDaTela) - 80, -80); // 80 = folga para a ponta arredondada ficar fora da tela
    this.caminhoEsquerda.set(caminhoCapsula(topo, false));
    this.caminhoDireita.set(caminhoCapsula(topo, true));
    this.percurso.set(`${Math.round(comprimentoCaminho(topo))}px`);
  }
}
