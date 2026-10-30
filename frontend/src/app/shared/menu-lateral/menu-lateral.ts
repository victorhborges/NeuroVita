import { Component, computed, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { Icone, NomeIcone } from '../icone/icone';
import { ItemMenu, Perfil, CONFIG_PERFIS, rotaDoMenu } from '../perfil';

const ITENS: Record<ItemMenu, { rotulo: string; icone: NomeIcone }> = {
  inicio: { rotulo: 'Início', icone: 'casa' },
  pacientes: { rotulo: 'Pacientes', icone: 'usuarios' },
  profissionais: { rotulo: 'Profissionais', icone: 'usuarios' },
  secretaria: { rotulo: 'Secretária', icone: 'usuarios' },
  agenda: { rotulo: 'Agenda', icone: 'calendario' },
};

/** Menu escuro da esquerda. O que aparece nele depende do perfil. */
@Component({
  selector: 'app-menu-lateral',
  imports: [Icone, RouterLink, RouterLinkActive],
  templateUrl: './menu-lateral.html',
})
export class MenuLateral {
  readonly perfil = input.required<Perfil>();

  protected readonly itens = computed(() => {
    const config = CONFIG_PERFIS[this.perfil()];
    return config.menu.map((id) => ({ id, ...ITENS[id], rota: rotaDoMenu(this.perfil(), id) }));
  });
}
