import { Component, computed, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { TOPICOS_AJUDA } from '../../shared/ajuda-conteudo';
import { Icone } from '../../shared/icone/icone';
import { ImagemAjuda } from '../../shared/imagem-ajuda/imagem-ajuda';
import { CONFIG_PERFIS, Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { normalizar } from '../../shared/util';

/** Central de ajuda: /:perfil/ajuda (igual para os quatro perfis). */
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
}
