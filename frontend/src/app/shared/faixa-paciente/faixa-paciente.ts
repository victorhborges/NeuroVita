import { Component, ElementRef, computed, inject, input, output, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Avatar } from '../avatar/avatar';
import { Icone } from '../icone/icone';
import { Paciente, Secao } from '../clinica/modelos';
import { SECOES, secoesDoPerfil } from '../clinica/secoes';
import { Perfil } from '../perfil';
import { formatarData, idadeDe } from '../util';

/** Faixa com os dados do paciente. Com `secao`, mostra o seletor para trocar de seção (ou o botão de adicionar documentos). */
@Component({
  selector: 'app-faixa-paciente',
  imports: [Avatar, Icone],
  host: { '(document:click)': 'fecharMenu($event)' },
  template: `
    <section class="faixa cartao" aria-label="Dados do paciente">
      <div class="faixa-pessoa">
        <app-avatar [nome]="paciente().nome" [foto]="paciente().foto" />
        <div>
          <p class="faixa-nome">{{ paciente().nome }}</p>
          <p class="faixa-id">ID: {{ paciente().codigo }} - {{ idade() }} anos</p>
        </div>
      </div>
      <div class="faixa-dado"><span class="faixa-rotulo">Idade</span><span class="faixa-valor">{{ idade() }} anos</span></div>
      <div class="faixa-dado"><span class="faixa-rotulo">Status</span><span class="faixa-valor">{{ paciente().situacao }}</span></div>
      <div class="faixa-dado"><span class="faixa-rotulo">Última consulta</span><span class="faixa-valor">{{ ultima() }}</span></div>
      @if (botaoAdicionar() && podeAdicionar()) {
        <div class="faixa-adicionar">
          <button type="button" class="botao-contorno" aria-haspopup="menu" [attr.aria-expanded]="menu()" (click)="menu.set(!menu())">+ Adicionar documentos</button>
          @if (menu()) {
            <div class="faixa-menu" role="menu">
              <button type="button" role="menuitem" (click)="menu.set(false); escrever.emit()"><app-icone nome="arquivo-texto" /> + Novo Documento</button>
              <button type="button" role="menuitem" (click)="menu.set(false); enviar.emit()"><app-icone nome="nuvem-upload" /> Usar modelo padrão</button>
            </div>
          }
        </div>
      } @else if (textoNovo()) {
        <div class="faixa-adicionar">
          <button type="button" class="botao-escuro" (click)="novo.emit()">{{ textoNovo() }}</button>
        </div>
      } @else if (secao()) {
        <label class="faixa-seletor">
          <select [value]="secao()" (change)="trocar($event)" aria-label="Seção do prontuário">
            @for (op of opcoes(); track op.id) {
              <option [value]="op.id" [selected]="op.id === secao()">{{ op.rotulo }}</option>
            }
          </select>
          <app-icone nome="seta-baixo" />
        </label>
      }
    </section>
  `,
})
export class FaixaPaciente {
  private readonly router = inject(Router);

  readonly paciente = input.required<Paciente>();
  readonly secao = input<Secao>();
  readonly perfil = input<Perfil>('psicologo');
  /** Troca o seletor de seção pelo botão "+ Adicionar documentos" (com menu: enviar arquivos ou escrever no editor). */
  readonly botaoAdicionar = input(false);
  /** Mostra um botão escuro com esse texto (ex.: "+ Nova Prescrição") no lugar do seletor de seção. */
  readonly textoNovo = input<string>();
  readonly enviar = output<void>();
  readonly escrever = output<void>();
  readonly novo = output<void>();
  protected readonly menu = signal(false);
  /** O botão "+ Adicionar documentos" existe só para o psiquiatra e o psicólogo. */
  protected readonly podeAdicionar = computed(() => this.perfil() === 'psicologo' || this.perfil() === 'psiquiatra');
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);

  protected fecharMenu(evento: Event): void {
    if (this.menu() && !this.elemento.nativeElement.contains(evento.target as Node)) this.menu.set(false);
  }

  protected readonly opcoes = computed(() => secoesDoPerfil(this.perfil()).map((id) => SECOES[id]));
  protected readonly idade = computed(() => idadeDe(this.paciente().nascimento));
  protected readonly ultima = computed(() => formatarData(this.paciente().ultimaConsulta));

  protected trocar(evento: Event): void {
    const valor = (evento.target as HTMLSelectElement).value;
    this.router.navigate(['/', this.perfil(), 'pacientes', this.paciente().id, valor]);
  }
}
