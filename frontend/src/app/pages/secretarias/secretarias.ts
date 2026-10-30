import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Aviso } from '../../shared/aviso/aviso';
import { Clinica } from '../../shared/clinica/clinica';
import { Secretaria } from '../../shared/clinica/modelos';
import { Avatar } from '../../shared/avatar/avatar';
import { Dialogo } from '../../shared/dialogo/dialogo';
import { Icone } from '../../shared/icone/icone';
import { Perfil } from '../../shared/perfil';
import { TituloPagina } from '../../shared/titulo-pagina/titulo-pagina';
import { formatarData, idadeDe, normalizar } from '../../shared/util';

/** Lista de secretárias (administrador): cadastrar, editar e excluir. */
@Component({
  selector: 'app-secretarias',
  imports: [Avatar, Dialogo, Icone, RouterLink, TituloPagina],
  templateUrl: './secretarias.html',
})
export class Secretarias {
  private readonly clinica = inject(Clinica);
  private readonly aviso = inject(Aviso);

  readonly perfil = input<Perfil>('admin');

  protected readonly busca = signal('');
  protected readonly excluindo = signal<Secretaria | null>(null);
  protected readonly base = computed(() => `/${this.perfil()}/secretarias`);

  protected readonly total = computed(() => {
    const n = this.clinica.secretarias().length;
    return n === 0 ? '0' : String(n).padStart(2, '0');
  });

  protected readonly lista = computed(() => {
    const termo = normalizar(this.busca());
    return this.clinica
      .secretarias()
      .filter((s) => !termo || normalizar(s.nome).includes(termo) || normalizar(s.email).includes(termo))
      .map((s) => ({ s, idade: idadeDe(s.nascimento), nascimento: formatarData(s.nascimento) }));
  });

  protected confirmarExclusao(): void {
    const s = this.excluindo();
    if (!s) return;
    this.clinica.removerSecretaria(s.id);
    this.excluindo.set(null);
    this.aviso.mostrar('Secretária excluída com sucesso.');
  }
}
