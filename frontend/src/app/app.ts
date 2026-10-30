import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { AvisoTela } from './shared/aviso/aviso';

/** Componente raiz: exibe a página da rota atual e os avisos rápidos. */
@Component({
  selector: 'app-root',
  imports: [AvisoTela, RouterOutlet],
  template: '<router-outlet /><app-aviso />',
})
export class App {}
