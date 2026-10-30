import { Pipe, PipeTransform, inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

import { limparHtml } from './util';

/** Mostra o texto formatado dos documentos depois de limpá-lo (ver limparHtml). */
@Pipe({ name: 'htmlSeguro' })
export class HtmlSeguro implements PipeTransform {
  private readonly sanitizer = inject(DomSanitizer);

  transform(html: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(limparHtml(html));
  }
}
