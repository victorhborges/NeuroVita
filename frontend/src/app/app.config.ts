import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding, withRouterConfig } from '@angular/router';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // withComponentInputBinding: parâmetros da URL (ex.: ?sucesso=true) viram input() nas páginas
    // paramsInheritanceStrategy 'always': as telas filhas recebem também o `data` do pai (ex.: o perfil)
    provideRouter(routes, withComponentInputBinding(), withRouterConfig({ paramsInheritanceStrategy: 'always' })),
  ],
};
