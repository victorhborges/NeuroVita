import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../shared/auth';
import { AuthCard } from '../../shared/auth-card/auth-card';
import { CampoSenha } from '../../shared/campo-senha/campo-senha';

@Component({
  selector: 'app-login',
  imports: [AuthCard, CampoSenha, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  readonly sucesso = input<string>();

  protected readonly mostrarAviso = computed(
    () => this.sucesso() === 'true',
  );

  protected readonly email = signal('');
  protected readonly senha = signal('');
  protected readonly erro = signal('');
  protected readonly carregando = signal(false);

  protected aoDigitarEmail(evento: Event): void {
    this.email.set((evento.target as HTMLInputElement).value);
    this.erro.set('');
  }

  protected entrar(evento: Event): void {
    evento.preventDefault();

    if (this.carregando()) {
      return;
    }

    if (!this.email().trim() || !this.senha()) {
      this.erro.set('Informe o e-mail e a senha.');
      return;
    }

    this.carregando.set(true);
    this.erro.set('');

    this.auth.entrar(this.email(), this.senha()).subscribe({
      next: (resultado) => {
        this.carregando.set(false);

        if (!resultado.ok) {
          if (resultado.motivo === 'paciente') {
            this.erro.set(
              'O acesso de pacientes ainda não está disponível nesta versão.',
            );
          } else {
            this.erro.set(
              'Não foi possível identificar o perfil desta conta. Verifique o cadastro.',
            );
          }

          return;
        }

        this.router.navigateByUrl(resultado.rota);
      },

      error: (erro: HttpErrorResponse) => {
        this.carregando.set(false);

        if (erro.status === 401 || erro.status === 403) {
          this.erro.set('E-mail ou senha inválidos.');
        } else if (erro.status === 0) {
          this.erro.set(
            'Não foi possível conectar ao servidor. Verifique se a API está disponível.',
          );
        } else {
          this.erro.set(
            'Ocorreu um erro ao tentar entrar. Tente novamente.',
          );
        }
      },
    });
  }
}