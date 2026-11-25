import { Component, ElementRef, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Avatar } from '../avatar/avatar';
import { Auth } from '../auth';
import { Icone } from '../icone/icone';
import { ConfigPerfil } from '../perfil';

/** Topo da tela: saudação à esquerda, sino e usuário à direita. */
@Component({
  selector: 'app-cabecalho',
  imports: [Avatar, Icone, RouterLink],
  templateUrl: './cabecalho.html',
  host: { '(document:click)': 'fecharSeClicouFora($event)' },
})
export class Cabecalho {
  readonly config = input.required<ConfigPerfil>();

  protected readonly menuAberto = signal(false);
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected alternarMenu(): void {
    this.menuAberto.update((aberto) => !aberto);
  }

  protected sair(): void {
    this.auth.sair();
    this.menuAberto.set(false);
    this.router.navigateByUrl('/');
  }

  protected fecharSeClicouFora(evento: Event): void {
    if (!this.elemento.nativeElement.contains(evento.target as Node)) {
      this.menuAberto.set(false);
    }
  }
}
