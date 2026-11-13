import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AdministradoresApiService } from './administradores-api.service';
import { PreferenciasApiService } from './preferencias-api.service';
import { ProntuariosApiService } from './prontuarios-api.service';
import { TratamentosApiService } from './tratamentos-api.service';

describe('clientes Angular para APIs aguardando tela', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('salva preferências no endpoint pessoal sem descartar campos omitidos', () => {
    const api = TestBed.inject(PreferenciasApiService);
    const requisicao = api.salvar({ notificacoesSistema: false }).subscribe();
    const request = http.expectOne('/api/preferencias');

    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ notificacoesSistema: false });
    request.flush({ notificacoesSistema: false });
    requisicao.unsubscribe();
  });

  it('usa os endpoints existentes de prontuário com ID codificado', () => {
    const api = TestBed.inject(ProntuariosApiService);
    api.atualizar('prontuario/1', {
      pacienteId: 'pac-1',
      profissionalId: 'prof-1',
      avaliacao: 'Atualização parcial',
    }).subscribe();
    const request = http.expectOne('/api/prontuarios/prontuario%2F1');

    expect(request.request.method).toBe('PUT');
    expect(request.request.body.avaliacao).toBe('Atualização parcial');
    request.flush({ id: 'prontuario/1', pacienteId: 'pac-1', profissionalId: 'prof-1' });
  });

  it('consulta tratamentos pelo endpoint de listagem existente', () => {
    const api = TestBed.inject(TratamentosApiService);
    api.listarTodos().subscribe();
    const request = http.expectOne('/api/tratamentos');

    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('expõe o CRUD existente de administradores sem incluir senha na resposta', () => {
    const api = TestBed.inject(AdministradoresApiService);
    api.criar({ nome: 'Admin', email: 'admin@neurovita.test', senha: 'senha-segura' }).subscribe();
    const request = http.expectOne('/api/administradores');

    expect(request.request.method).toBe('POST');
    expect(request.request.body.senha).toBe('senha-segura');
    request.flush({ id: 'adm-1', nome: 'Admin', email: 'admin@neurovita.test', perfil: 'ADMINISTRADOR' });
  });
});
