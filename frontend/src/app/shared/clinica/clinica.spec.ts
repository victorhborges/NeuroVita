import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Subject } from 'rxjs';

import { Auth } from '../auth';
import { Clinica } from './clinica';

describe('Clinica — solicitação de exames', () => {
  let clinica: Clinica;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: Auth,
          useValue: {
            sessaoAlterada$: new Subject<boolean>(),
            obterToken: () => null,
            obterPerfil: () => 'psiquiatra',
          },
        },
      ],
    });
    clinica = TestBed.inject(Clinica);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('salva o texto da solicitação sem enviar data de realização ou resultado', async () => {
    const salva = clinica.salvarDocumento({
      pacienteId: 'pac-1',
      profissionalId: 'prof-1',
      secao: 'exames',
      titulo: 'Hemograma',
      dataSessao: '2026-11-10',
      tipoAtendimento: null,
      conteudo: '<p>Hemograma completo. Justificativa e CID.</p>',
      anexos: [],
    });

    const requisicao = http.expectOne('/api/exames');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body).toEqual({
      pacienteId: 'pac-1',
      profissionalId: 'prof-1',
      nome: 'Hemograma',
      descricaoSolicitacao: '<p>Hemograma completo. Justificativa e CID.</p>',
    });
    requisicao.flush({
      id: 'exame-1',
      pacienteId: 'pac-1',
      profissionalId: 'prof-1',
      nome: 'Hemograma',
      descricaoSolicitacao: '<p>Hemograma completo. Justificativa e CID.</p>',
      solicitadoEm: '2026-11-12T12:00:00',
      criadoEm: '2026-11-12T12:00:00',
      anexosResultado: [],
      historicoResultados: [],
      ativo: true,
    });

    await expect(salva).resolves.toMatchObject({
      id: 'exame:exame-1',
      dataSessao: '2026-11-12',
      dataRealizacao: undefined,
      conteudo: '<p>Hemograma completo. Justificativa e CID.</p>',
    });
  });

  it('mantém uploads genéricos da seção de exames em documentos clínicos', async () => {
    const salva = clinica.salvarDocumento({
      origem: 'documento-clinico',
      pacienteId: 'pac-1',
      secao: 'exames',
      titulo: 'Documento anexado',
      dataSessao: '2026-11-10',
      tipoAtendimento: 'Presencial',
      conteudo: 'Arquivo anexado: Documento anexado',
      anexos: [],
    });

    await Promise.resolve();
    const requisicao = http.expectOne('/api/documentos-clinicos');
    expect(requisicao.request.method).toBe('POST');
    expect(requisicao.request.body.secao).toBe('exames');
    requisicao.flush({
      id: 'doc-1',
      pacienteId: 'pac-1',
      secao: 'exames',
      titulo: 'Documento anexado',
      dataSessao: '2026-11-10',
      tipoAtendimento: 'Presencial',
      conteudo: 'Arquivo anexado: Documento anexado',
      atualizadoEm: '2026-11-12T12:00:00',
      anexos: [],
    });

    await expect(salva).resolves.toMatchObject({
      id: 'doc-1',
      origem: 'documento-clinico',
    });
  });
});
