import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';

import { Auth } from './auth';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
    const auth = inject(Auth);
    const token = auth.obterToken();

    // Só adiciona o token às chamadas da API.
    if (!token || !request.url.startsWith('/api/')) {
        return next(request);
    }

    const requestAutenticada = request.clone({
        setHeaders: {
            Authorization: `Bearer ${token}`,
        },
    });

    return next(requestAutenticada);
};