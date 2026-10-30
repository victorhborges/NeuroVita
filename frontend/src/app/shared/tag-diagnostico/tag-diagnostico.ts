import { Component, input } from '@angular/core';

import { Tag } from '../dados-demo';

/** Etiqueta colorida do paciente (TEA / TDAH). */
@Component({
  selector: 'app-tag-diagnostico',
  template: `<span class="tag" [class]="'tag-' + tag().cor">{{ tag().sigla }}</span>`,
})
export class TagDiagnostico {
  readonly tag = input.required<Tag>();
}
