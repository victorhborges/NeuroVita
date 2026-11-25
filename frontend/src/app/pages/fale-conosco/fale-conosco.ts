import { Component, computed, inject, input, signal } from '@angular/core';
import { finalize } from 'rxjs';

import { Aviso } from '../../shared/aviso/aviso';
import { ContatoApiService } from '../../shared/clinica/contato-api.service';
import { ImagemAjuda } from '../../shared/imagem-ajuda/imagem-ajuda';
import { Icone } from '../../shared/icone/icone';
import { CONFIG_PERFIS, Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { emailValido } from '../../shared/util';

interface FormularioContato {
  nome: string;
  email: string;
  mensagem: string;
}

type CampoContato = keyof FormularioContato;

const FORMULARIO_VAZIO: FormularioContato = {
  nome: '',
  email: '',
  mensagem: '',
};

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
  protected readonly f = signal<FormularioContato>({ ...FORMULARIO_VAZIO });
  protected readonly enviando = signal(false);
  protected readonly enviado = signal(false);
  protected readonly erro = signal('');
  private readonly tentouEnviar = signal(false);

  protected enviar(evento: Event): void {
    evento.preventDefault();
    if (this.enviando()) return;

    this.erro.set('');
    this.enviado.set(false);
    this.tentouEnviar.set(true);
    if (Object.keys(this.erros()).length) return;

    const formulario = this.f();
    this.enviando.set(true);
    this.contatoApi.enviar({
      nome: formulario.nome.trim(),
      email: formulario.email.trim(),
      mensagem: formulario.mensagem.trim(),
    }).pipe(finalize(() => this.enviando.set(false))).subscribe({
      next: () => {
        this.enviado.set(true);
        this.f.set({ ...FORMULARIO_VAZIO });
        this.tentouEnviar.set(false);
        this.aviso.mostrar('Mensagem enviada.');
      },
      error: (erro) => {
        this.erro.set(erro?.error?.mensagem ?? erro?.error?.message
          ?? 'Não foi possível enviar a mensagem. Verifique a configuração de contato e tente novamente.');
      },
    });
  }

  protected digitar(campo: CampoContato, evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value;
    this.f.update((atual) => ({ ...atual, [campo]: valor }));
    this.enviado.set(false);
  }

  protected readonly erros = computed(() => {
    const formulario = this.f();
    const erros: Partial<Record<CampoContato, string>> = {};
    if (!formulario.nome.trim()) erros.nome = 'Informe seu nome.';
    if (formulario.email.trim() && !emailValido(formulario.email)) erros.email = 'E-mail inválido.';
    if (!formulario.mensagem.trim()) erros.mensagem = 'Informe sua mensagem.';
    else if (formulario.mensagem.length > 5000) erros.mensagem = 'A mensagem deve ter no máximo 5.000 caracteres.';
    return erros;
  });

  protected mostrarErro(campo: CampoContato): string {
    return this.tentouEnviar() ? this.erros()[campo] ?? '' : '';
  }
}
