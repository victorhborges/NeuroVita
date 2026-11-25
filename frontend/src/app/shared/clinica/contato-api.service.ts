import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ContatoRequestApi {
    nome: string;
    email?: string | null;
    mensagem: string;
}

@Injectable({ providedIn: 'root' })
export class ContatoApiService {
    private readonly http = inject(HttpClient);

    enviar(request: ContatoRequestApi): Observable<void> {
        return this.http.post<void>('/api/contato', request);
    }
}
