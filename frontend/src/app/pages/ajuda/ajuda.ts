import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AJUDA_CARTOES_POR_PERFIL, TOPICOS_AJUDA, TopicoAjudaCartao, partesComNegrito } from '../../shared/ajuda-conteudo';
import { Icone } from '../../shared/icone/icone';
import { ImagemAjuda } from '../../shared/imagem-ajuda/imagem-ajuda';
import { CONFIG_PERFIS, Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { normalizar } from '../../shared/util';

/** Central de ajuda: /:perfil/ajuda. Psiquiatra, psicólogo e administrador têm cartões que se abrem na própria tela; a secretária, uma página por tópico. */
@Component({
  selector: 'app-ajuda',
  imports: [Icone, ImagemAjuda, RouterLink, TituloPagina],
  templateUrl: './ajuda.html',
})
export class Ajuda {
  /** Vem do `data` da rota (ver app.routes.ts). */
  readonly perfil = input<Perfil>('admin');

  protected readonly raiz = computed(() => CONFIG_PERFIS[this.perfil()].rota);
  protected readonly busca = signal('');

  protected readonly topicos = computed(() => {
    const termo = normalizar(this.busca());
    return TOPICOS_AJUDA.filter(
      (t) => !termo || normalizar(`${t.cartao.titulo} ${t.cartao.descricao}`).includes(termo),
    );
  });

  // ----- Psiquiatra, psicólogo e administrador: cartões que se abrem na própria tela (cada um com o seu conteúdo) -----
  protected readonly topicosDoPerfil = computed(() => AJUDA_CARTOES_POR_PERFIL[this.perfil()]);
  protected readonly comCartoesAbertos = computed(() => !!this.topicosDoPerfil());
  /** Com um cartão só (administrador), ele ocupa a linha inteira, como no Figma. */
  protected readonly cartaoUnico = computed(() => this.topicosDoPerfil()?.length === 1);
  protected readonly partes = partesComNegrito;
  protected readonly aberto = signal<string | null>(null);

  protected alternar(id: string): void {
    this.aberto.update((atual) => (atual === id ? null : id));
  }

  /** Cartões na ordem da tela, com o conteúdo aberto logo depois da linha indicada no Figma. */
  protected readonly itensCartoes = computed<
    ({ tipo: 'cartao'; t: TopicoAjudaCartao } | { tipo: 'conteudo'; t: TopicoAjudaCartao })[]
  >(() => {
    const termo = normalizar(this.busca());
    const lista = (this.topicosDoPerfil() ?? []).filter(
      (t) => !termo || normalizar(`${t.titulo} ${t.descricao}`).includes(termo),
    );
    const itens: ({ tipo: 'cartao'; t: TopicoAjudaCartao } | { tipo: 'conteudo'; t: TopicoAjudaCartao })[] = [];
    const abertoAgora = this.aberto();
    const aberto = lista.find((t) => t.id === abertoAgora);
    const posicao = aberto ? Math.min(aberto.linhaDoConteudo * 2, lista.length) : -1;
    lista.forEach((t, i) => {
      itens.push({ tipo: 'cartao', t });
      if (aberto && i + 1 === posicao) itens.push({ tipo: 'conteudo', t: aberto });
    });
    return itens;
  });
}
