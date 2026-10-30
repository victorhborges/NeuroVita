import { Component, computed, inject, input } from '@angular/core';
import { Router } from '@angular/router';

import { Avatar } from '../avatar/avatar';
import { Icone } from '../icone/icone';
import { Paciente, Secao } from '../clinica/modelos';
import { SECOES, secoesDoPerfil } from '../clinica/secoes';
import { Perfil } from '../perfil';
import { formatarData, idadeDe } from '../util';

/** Faixa com os dados do paciente. Com `secao`, mostra o seletor para trocar de seção. */
@Component({
  selector: 'app-faixa-paciente',
  imports: [Avatar, Icone],
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
      @if (secao()) {
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

  protected readonly opcoes = computed(() => secoesDoPerfil(this.perfil()).map((id) => SECOES[id]));
  protected readonly idade = computed(() => idadeDe(this.paciente().nascimento));
  protected readonly ultima = computed(() => formatarData(this.paciente().ultimaConsulta));

  protected trocar(evento: Event): void {
    const valor = (evento.target as HTMLSelectElement).value;
    this.router.navigate(['/', this.perfil(), 'pacientes', this.paciente().id, valor]);
  }
}
