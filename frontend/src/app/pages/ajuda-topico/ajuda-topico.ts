import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { TOPICOS_AJUDA, partesComNegrito } from '../../shared/ajuda-conteudo';
import { Icone } from '../../shared/icone/icone';
import { ImagemAjuda } from '../../shared/imagem-ajuda/imagem-ajuda';
import { CONFIG_PERFIS, Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';

/** Um assunto da ajuda: /:perfil/ajuda/:topico (passo a passo com captura de tela). */
@Component({
  selector: 'app-ajuda-topico',
  imports: [Icone, ImagemAjuda, RouterLink, TituloPagina],
  templateUrl: './ajuda-topico.html',
})
export class AjudaTopico {
  /** Vem do `data` da rota (ver app.routes.ts). */
  readonly perfil = input<Perfil>('admin');
  readonly topico = input.required<string>();

  protected readonly raiz = computed(() => CONFIG_PERFIS[this.perfil()].rota);
  protected readonly conteudo = computed(() => TOPICOS_AJUDA.find((t) => t.id === this.topico()));
  protected readonly partes = partesComNegrito;
}
