import { Component, ElementRef, inject, signal, viewChildren } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthCard } from '../../shared/auth-card/auth-card';
import { RecuperacaoSenhaApiService } from '../../shared/clinica/recuperacao-senha-api.service';

/** Valida o código de recuperação de senha. */
@Component({
    selector: 'app-codigo',
    imports: [AuthCard, RouterLink],
    templateUrl: './codigo.html',
})
export class Codigo {
    private readonly router = inject(Router);
    private readonly api = inject(RecuperacaoSenhaApiService);

    protected readonly erro = signal('');
    protected readonly reenviado = signal(false);
    protected readonly posicoes = [0, 1, 2, 3, 4, 5];

    private readonly digitos = viewChildren<ElementRef<HTMLInputElement>>('digito');

    protected aoDigitar(indice: number): void {
        const campos = this.digitos();
        const campo = campos[indice]?.nativeElement;

        if (!campo) return;

        const valor = campo.value.replace(/\D/g, '');

        this.erro.set('');
        this.reenviado.set(false);

        // Distribui códigos preenchidos automaticamente ou colados.
        if (valor.length > 1) {
            const codigo = valor.slice(0, 6);

            campos.forEach((ref, i) => {
                ref.nativeElement.value = codigo[i] ?? '';
            });

            const proximo = Math.min(codigo.length, campos.length - 1);
            campos[proximo].nativeElement.focus();
            return;
        }

        campo.value = valor.slice(0, 1);

        if (campo.value && indice < campos.length - 1) {
            campos[indice + 1].nativeElement.focus();
        }
    }

    protected aoPressionarTecla(evento: KeyboardEvent, indice: number): void {
        const campos = this.digitos();

        if (
            evento.key === 'Backspace' &&
            !campos[indice].nativeElement.value &&
            indice > 0
        ) {
            campos[indice - 1].nativeElement.focus();
        }
    }

    protected reenviar(): void {
        const email = sessionStorage.getItem('neurovita_recuperacao_email');

        if (!email) {
            this.router.navigate(['/esqueci-senha']);
            return;
        }

        this.api.solicitar(email).subscribe({
            next: () => {
                this.reenviado.set(true);
                this.erro.set('');
            },
            error: (erro) => {
                this.erro.set(
                    erro?.error?.mensagem ?? 'Não foi possível reenviar o código.',
                );
            },
        });
    }

    protected verificar(evento: Event): void {
        evento.preventDefault();

        const email = sessionStorage.getItem('neurovita_recuperacao_email');
        const codigo = this.digitos()
            .map((ref) => ref.nativeElement.value.replace(/\D/g, ''))
            .join('');

        if (!email) {
            this.router.navigate(['/esqueci-senha']);
            return;
        }

        if (codigo.length !== 6) {
            this.erro.set('Informe os seis dígitos do código.');
            return;
        }

        this.api.verificar(email, codigo).subscribe({
            next: ({ token }) => {
                sessionStorage.setItem('neurovita_recuperacao_token', token);
                this.router.navigate(['/redefinir-senha']);
            },
            error: (erro) => {
                this.erro.set(
                    erro?.error?.mensagem ?? 'Código inválido ou expirado.',
                );
            },
        });
    }
}