import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RecuperacaoSenhaApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/auth/recuperacao';
  solicitar(email: string): Observable<void> { return this.http.post<void>(`${this.api}/solicitar`, { email }); }
  verificar(email: string, codigo: string): Observable<{ token: string }> { return this.http.post<{ token: string }>(`${this.api}/verificar`, { email, codigo }); }
  redefinir(token: string, novaSenha: string, confirmarSenha: string): Observable<void> {
    return this.http.post<void>(`${this.api}/redefinir`, { token, novaSenha, confirmarSenha });
  }
}
