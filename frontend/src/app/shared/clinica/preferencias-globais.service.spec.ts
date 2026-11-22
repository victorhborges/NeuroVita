import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { Auth } from '../auth';
import { PreferenciasUsuarioApi } from './preferencias-api.service';
import { PreferenciasGlobaisService } from './preferencias-globais.service';

function preferencias(tema: 'CLARO' | 'ESCURO'): PreferenciasUsuarioApi {
  return {
    lembretesConsultaEmail: false,
    alertasConsultasPendentes: false,
    notificacoesSistema: false,
    tema,
    idioma: 'PORTUGUES',
    formatoData: 'AAAA/MM/DD',
    duracaoPadraoConsulta: 50,
    inicioSemana: 'SEGUNDA-FEIRA',
    atualizadaEm: null,
  };
}

describe('PreferenciasGlobaisService', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify({ ignoreCancelled: true });
    sessionStorage.clear();
    vi.useRealTimers();
  });

  it('ignora uma resposta de carregamento que chega depois do logout', () => {
    sessionStorage.setItem('neurovita_token', 'token-a');
    const service = TestBed.inject(PreferenciasGlobaisService);
    service.carregar().subscribe();
    const request = http.expectOne('/api/preferencias');

    TestBed.inject(Auth).sair();
    request.flush(preferencias('ESCURO'));

    expect(service.preferencias().tema).toBe('CLARO');
    expect(document.documentElement.dataset['tema']).toBe('claro');
  });

  it('não aplica resposta de salvamento da sessão anterior após troca de usuário', () => {
    sessionStorage.setItem('neurovita_token', 'token-a');
    const service = TestBed.inject(PreferenciasGlobaisService);
    service.salvar({ tema: 'ESCURO' }).subscribe();
    const saveRequest = http.expectOne('/api/preferencias');

    TestBed.inject(Auth).sair();
    sessionStorage.setItem('neurovita_token', 'token-b');
    service.carregar().subscribe();
    const loadUserB = http.expectOne('/api/preferencias');
    loadUserB.flush(preferencias('CLARO'));
    saveRequest.flush(preferencias('ESCURO'));

    expect(service.preferencias().tema).toBe('CLARO');
  });

  it('não propaga erro de salvamento obsoleto para a sessão atual', () => {
    sessionStorage.setItem('neurovita_token', 'token-a');
    const service = TestBed.inject(PreferenciasGlobaisService);
    const erroAtual = vi.fn();
    service.salvar({ tema: 'ESCURO' }).subscribe({ error: erroAtual });
    const saveRequest = http.expectOne('/api/preferencias');

    TestBed.inject(Auth).sair();
    sessionStorage.setItem('neurovita_token', 'token-b');
    service.carregar().subscribe();
    http.expectOne('/api/preferencias').flush(preferencias('CLARO'));
    saveRequest.error(new ProgressEvent('error'));

    expect(erroAtual).not.toHaveBeenCalled();
    expect(service.preferencias().tema).toBe('CLARO');
  });

  it('não deixa o finalize de uma carga antiga remover a operação atual', () => {
    sessionStorage.setItem('neurovita_token', 'token-a');
    const service = TestBed.inject(PreferenciasGlobaisService);
    service.carregar().subscribe();
    const loadUserA = http.expectOne('/api/preferencias');

    TestBed.inject(Auth).sair();
    sessionStorage.setItem('neurovita_token', 'token-b');
    service.carregar().subscribe();
    const loadUserB = http.expectOne('/api/preferencias');

    loadUserA.flush(preferencias('ESCURO'));
    service.carregar().subscribe();
    http.expectNone('/api/preferencias');
    loadUserB.flush(preferencias('CLARO'));

    expect(service.preferencias().tema).toBe('CLARO');
  });

  it('aplica defaults após timeout, expõe a falha e permite nova carga', async () => {
    vi.useFakeTimers();
    sessionStorage.setItem('neurovita_token', 'token-a');
    const service = TestBed.inject(PreferenciasGlobaisService);
    let inicializacaoConcluida = false;
    service.inicializar().subscribe(() => inicializacaoConcluida = true);
    const request = http.expectOne('/api/preferencias');

    await vi.advanceTimersByTimeAsync(4_001);

    expect(inicializacaoConcluida).toBe(true);
    expect(service.erroCarregamento()).toBe(true);
    expect(service.preferencias().tema).toBe('CLARO');
    expect(document.documentElement.dataset['tema']).toBe('claro');
    expect(request.cancelled).toBe(true);
    service.carregar(true).subscribe();
    const retry = http.expectOne('/api/preferencias');
    retry.flush(preferencias('ESCURO'));
    expect(service.erroCarregamento()).toBe(false);
    expect(document.documentElement.dataset['tema']).toBe('escuro');
    expect(document.documentElement.lang).toBe('pt-BR');
    expect(document.documentElement.dataset['formatoData']).toBe('AAAA/MM/DD');
  });
});
