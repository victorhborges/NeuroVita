import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PreferenciasUsuarioApi {
  duracaoPadraoConsulta: number | null;
}

@Injectable({ providedIn: 'root' })
export class PreferenciasApiService {
  private readonly http = inject(HttpClient);

  buscar(): Observable<PreferenciasUsuarioApi> {
    return this.http.get<PreferenciasUsuarioApi>('/api/preferencias');
  }
}
