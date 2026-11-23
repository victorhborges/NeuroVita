import {
    ApplicationConfig,
    provideBrowserGlobalErrorListeners,
    provideAppInitializer,
    inject,
} from '@angular/core';

import { provideHttpClient, withInterceptors } from '@angular/common/http';

import {
    provideRouter,
    withComponentInputBinding,
    withRouterConfig,
} from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './shared/auth.interceptor';
import { PreferenciasGlobaisService } from './shared/clinica/preferencias-globais.service';

export const appConfig: ApplicationConfig = {
    providers: [
        provideBrowserGlobalErrorListeners(),

        provideAppInitializer(() => inject(PreferenciasGlobaisService).inicializar()),

        provideHttpClient(
            withInterceptors([authInterceptor]),
        ),

        provideRouter(
            routes,
            withComponentInputBinding(),
            withRouterConfig({
                paramsInheritanceStrategy: 'always',
            }),
        ),
    ],
};
