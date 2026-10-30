import { Component, ElementRef, computed, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Avatar } from '../avatar/avatar';
import { Icone } from '../icone/icone';
import { ConfigPerfil } from '../perfil';
import { Auth } from '../auth';

@Component({
  selector: 'app-cabecalho',
  imports: [Avatar, Icone],
  templateUrl: './cabecalho.html',
  host: { '(document:click)': 'fecharSeClicouFora($event)' },
})
export class Cabecalho {
  readonly config = input.required<ConfigPerfil>();

  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly menuAberto = signal(false);

  protected readonly nomeUsuario = computed(
    () => this.auth.obterNome() ?? this.config().usuario.nome,
  );

  protected alternarMenu(): void {
    this.menuAberto.update((aberto) => !aberto);
  }

  protected fecharSeClicouFora(evento: Event): void {
    if (!this.elemento.nativeElement.contains(evento.target as Node)) {
      this.menuAberto.set(false);
    }
  }

  protected sair(): void {
    this.auth.sair();
    this.menuAberto.set(false);
    this.router.navigateByUrl('/');
  }
}
