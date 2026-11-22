import { Component, computed, inject, input, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { Aviso } from '../../shared/aviso/aviso';
import { ContatoApiService } from '../../shared/clinica/contato-api.service';
import { ImagemAjuda } from '../../shared/imagem-ajuda/imagem-ajuda';
import { Icone } from '../../shared/icone/icone';
import { CONFIG_PERFIS, Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';

@Component({
  selector: 'app-fale-conosco',
  imports: [ImagemAjuda, Icone, TituloPagina],
  templateUrl: './fale-conosco.html',
})
export class FaleConosco {
  private readonly contatoApi = inject(ContatoApiService);
  private readonly aviso = inject(Aviso);

  readonly perfil = input<Perfil>('admin');
  protected readonly raiz = computed(() => CONFIG_PERFIS[this.perfil()].rota);
  protected readonly nome = signal('');
  protected readonly email = signal('');
  protected readonly mensagem = signal('');
  protected readonly enviando = signal(false);
  protected readonly enviado = signal(false);
  protected readonly erro = signal('');

  protected enviar(evento: Event): void {
    evento.preventDefault();
    if (this.enviando()) return;

    this.erro.set('');
    this.enviado.set(false);
    this.enviando.set(true);
    this.contatoApi.enviar({
      nome: this.nome().trim(),
      email: this.email().trim(),
      mensagem: this.mensagem().trim(),
    }).pipe(finalize(() => this.enviando.set(false))).subscribe({
      next: () => {
        this.enviado.set(true);
        this.nome.set('');
        this.email.set('');
        this.mensagem.set('');
        this.aviso.mostrar('Mensagem enviada.');
      },
      error: (erro) => {
        this.erro.set(erro?.error?.mensagem ?? erro?.error?.message
          ?? 'Não foi possível enviar a mensagem. Verifique a configuração de contato e tente novamente.');
      },
    });
  }
}
