import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Auth } from '../../shared/auth';
import { Aviso } from '../../shared/aviso/aviso';
import { ConsultasApiService } from '../../shared/clinica/consultas-api.service';
import { PacientesApiService } from '../../shared/clinica/pacientes-api.service';
import { ProfissionaisApiService } from '../../shared/clinica/profissionais-api.service';
import { PainelInicio } from './painel-inicio';

describe('PainelInicio', () => {
  it('renderiza a tela inicial depois que recebe o perfil da rota', async () => {
    await TestBed.configureTestingModule({
      imports: [PainelInicio],
      providers: [
        provideRouter([]),
        { provide: Auth, useValue: { obterNome: () => 'Usuário de teste' } },
        { provide: Aviso, useValue: { mostrar: () => undefined } },
        { provide: ConsultasApiService, useValue: { listarTodas: () => of([]) } },
        { provide: PacientesApiService, useValue: { listarTodos: () => of([]) } },
        { provide: ProfissionaisApiService, useValue: { listarTodos: () => of([]) } },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(PainelInicio);
    fixture.componentRef.setInput('perfil', 'admin');
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.topo-saudacao')?.textContent).toContain('Olá, Usuário de teste');
    expect(fixture.nativeElement.querySelector('.resumo')).not.toBeNull();
  });
});
