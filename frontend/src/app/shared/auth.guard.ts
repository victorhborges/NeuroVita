import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Auth } from './auth';
import { Perfil, CONFIG_PERFIS } from './perfil';

export const authGuard: CanActivateFn = (route) => {
    const auth = inject(Auth);
    const router = inject(Router);

    const token = auth.obterToken();
    const perfilSalvo = auth.obterPerfil();

    if (!token || !perfilSalvo) {
        auth.sair();
        return router.createUrlTree(['/']);
    }

    const perfilEsperado = route.data['perfil'] as Perfil;

    if (perfilSalvo !== perfilEsperado) {
        return router.createUrlTree([
            CONFIG_PERFIS[perfilSalvo].rota,
        ]);
    }

    return true;
};