import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Página 404 — aparece para qualquer endereço que não existe. */
@Component({
  selector: 'app-nao-encontrada',
  imports: [RouterLink],
  template: `
    <div class="nf-page">
      <div class="nf-box">
        <h1 class="nf-codigo">404</h1>
        <h2 class="nf-titulo">Página não encontrada</h2>
        <p class="nf-texto">A página que você procura não existe ou foi movida.</p>
        <div class="nf-acao"><a routerLink="/" class="nf-btn">Ir para o início</a></div>
      </div>
    </div>
  `,
})
export class NaoEncontrada {}
