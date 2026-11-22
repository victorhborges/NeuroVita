import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Auth } from '../../shared/auth';
import { AuthCard } from '../../shared/auth-card/auth-card';
import { CampoSenha } from '../../shared/campo-senha/campo-senha';
import { PreferenciasGlobaisService } from '../../shared/clinica/preferencias-globais.service';

@Component({
  selector: 'app-login',
  imports: [AuthCard, CampoSenha, RouterLink],
  templateUrl: './login.html',
})
export class Login implements OnInit {
  private readonly auth = inject(Auth);
  private readonly preferencias = inject(PreferenciasGlobaisService);
  private readonly router = inject(Router);

  readonly sucesso = input<string>();

  protected readonly mostrarAviso = computed(
    () =>
      this.sucesso() === 'true' ||
      this.sucesso() === 'senha-alterada',
  );

  protected readonly email = signal('');
  protected readonly senha = signal('');
  protected readonly erro = signal('');
  protected readonly carregando = signal(false);

  ngOnInit(): void {
    const rota = this.auth.rotaInicialAutenticada();
    if (!rota) return;

    this.carregando.set(true);
    this.preferencias.carregar().subscribe({
      next: () => this.router.navigateByUrl(rota),
      error: () => this.router.navigateByUrl(rota),
    });
  }

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

        // Carrega as preferências da sessão antes de montar o painel para evitar
        // uma renderização inicial com tema/formato diferentes dos salvos.
        this.preferencias.carregar().subscribe(() => {
          this.router.navigateByUrl(
            resultado.primeiroAcesso
              ? '/primeiro-acesso'
              : resultado.rota,
          );
        });
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
