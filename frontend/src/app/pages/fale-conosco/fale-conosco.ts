import { Component, computed, input } from '@angular/core';

import { ImagemAjuda } from '../../shared/imagem-ajuda/imagem-ajuda';
import { CONFIG_PERFIS, Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';

@Component({
  selector: 'app-fale-conosco',
  imports: [ImagemAjuda, TituloPagina],
  templateUrl: './fale-conosco.html',
})
export class FaleConosco {
  readonly perfil = input<Perfil>('admin');
  protected readonly raiz = computed(() => CONFIG_PERFIS[this.perfil()].rota);
}
