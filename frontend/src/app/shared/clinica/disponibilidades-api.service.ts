import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DisponibilidadeApi, DisponibilidadeRequestApi } from './profissionais-api.service';

@Injectable({ providedIn: 'root' })
export class DisponibilidadesApiService {
  private readonly http = inject(HttpClient);
  private readonly api = '/api/disponibilidades';
  criar(dados: DisponibilidadeRequestApi): Observable<DisponibilidadeApi> {
    return this.http.post<DisponibilidadeApi>(this.api, dados);
  }
  excluir(id: string): Observable<void> { return this.http.delete<void>(`${this.api}/${id}`); }
}
