import { Component, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Auth, CONTAS_DE_TESTE, ContaDeTeste, SENHA_DE_TESTE } from '../../shared/auth';
import { AuthCard } from '../../shared/auth-card/auth-card';
import { CampoSenha } from '../../shared/campo-senha/campo-senha';

/** Tela de login (ainda sem back-end: usa as contas de teste de shared/auth.ts). */
@Component({
  selector: 'app-login',
  imports: [AuthCard, CampoSenha, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  /** Vem da URL: /?sucesso=true (após redefinir a senha). */
  readonly sucesso = input<string>();

  protected readonly mostrarAviso = computed(() => this.sucesso() === 'true');

  protected readonly email = signal('');
  protected readonly senha = signal('');
  protected readonly erro = signal('');

  // TODO (back-end): remover a lista de e-mails de teste quando o login for real.
  protected readonly listaAberta = signal(false);
  protected readonly contas = CONTAS_DE_TESTE;
  protected readonly senhaDeTeste = SENHA_DE_TESTE;

  protected aoDigitarEmail(evento: Event): void {
    this.email.set((evento.target as HTMLInputElement).value);
    this.erro.set('');
  }

  protected abrirLista(): void {
    this.listaAberta.set(true);
  }

  protected fecharLista(): void {
    this.listaAberta.set(false);
  }

  protected escolher(evento: Event, conta: ContaDeTeste): void {
    evento.preventDefault(); // mantém o foco no campo
    this.email.set(conta.email);
    this.erro.set('');
    this.listaAberta.set(false);
  }

  protected entrar(evento: Event): void {
    evento.preventDefault();
    const resultado = this.auth.entrar(this.email(), this.senha());

    if (resultado.ok) {
      this.router.navigateByUrl(resultado.rota);
      return;
    }

    this.erro.set(
      {
        vazio: 'Informe o e-mail e a senha.',
        email: 'E-mail não encontrado.',
        senha: 'Senha incorreta.',
      }[resultado.motivo],
    );
  }
}
