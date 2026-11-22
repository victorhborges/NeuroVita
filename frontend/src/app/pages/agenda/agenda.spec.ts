import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { Aviso } from '../../shared/aviso/aviso';
import { ConsultaApi, ConsultasApiService } from '../../shared/clinica/consultas-api.service';
import { PacienteApi, PacientesApiService } from '../../shared/clinica/pacientes-api.service';
import { DisponibilidadeApi, ProfissionalApi, ProfissionaisApiService } from '../../shared/clinica/profissionais-api.service';
import { Agenda } from './agenda';

import {
  possuiAlteracaoAgenda,
  possuiConflitoAgenda,
  salaConsultaPermitida,
} from './agenda';

describe('edição de consulta na agenda', () => {
  const atual = {
    data: '2026-12-01',
    inicio: '10:00',
    profissionalId: 'prof-1',
    local: 'Sala 01',
  };

  it('reconhece mudança isolada de sala como alteração persistível', () => {
    expect(possuiAlteracaoAgenda(atual, { ...atual, local: 'Sala 02' })).toBe(true);
  });

  it('não considera sala desconhecida válida, salvo a sala legada atual', () => {
    expect(salaConsultaPermitida('Sala 04', 'Sala 01')).toBe(true);
    expect(salaConsultaPermitida('Sala inventada', 'Sala 01')).toBe(false);
    expect(salaConsultaPermitida('Sala legada', 'Sala legada')).toBe(true);
  });

  it('não considera uma edição sem mudanças como remarcação', () => {
    expect(possuiAlteracaoAgenda(atual, { ...atual })).toBe(false);
  });

  it('remove horários sobrepostos do mesmo profissional', () => {
    expect(possuiConflitoAgenda([
      { id: 'outra', data: atual.data, inicio: '10:30', duracao: 30, pacienteId: 'pac-2', profissionalId: 'prof-1', local: 'Sala 03', status: 'confirmado' },
    ], {
      consultaId: 'atual', data: atual.data, inicio: '10:00', duracao: 60, pacienteId: 'pac-1', profissionalId: 'prof-1', local: 'Sala 01',
    })).toBe(true);
  });

  it('detecta conflito de paciente e de sala física, mas ignora outra sala', () => {
    const outraSala = { id: 'outra', data: atual.data, inicio: '10:00', duracao: 60, pacienteId: 'pac-2', profissionalId: 'prof-2', local: 'Sala 02', status: 'pendente' as const };
    const base = { consultaId: 'atual', data: atual.data, inicio: '10:00', duracao: 30, pacienteId: 'pac-1', profissionalId: 'prof-1' };

    expect(possuiConflitoAgenda([outraSala], { ...base, pacienteId: 'pac-2', local: 'Sala 01' })).toBe(true);
    expect(possuiConflitoAgenda([outraSala], { ...base, local: 'Sala 02' })).toBe(true);
    expect(possuiConflitoAgenda([outraSala], { ...base, local: 'Sala 01' })).toBe(false);
  });

  it('ignora consulta atual, canceladas e horários adjacentes sem sobreposição', () => {
    const base = { id: 'atual', data: atual.data, inicio: '10:00', duracao: 30, pacienteId: 'pac-1', profissionalId: 'prof-1', local: 'Sala 01', status: 'confirmado' as const };
    expect(possuiConflitoAgenda([base], { consultaId: 'atual', data: atual.data, inicio: '10:00', duracao: 30, pacienteId: 'pac-1', profissionalId: 'prof-1', local: 'Sala 01' })).toBe(false);
    expect(possuiConflitoAgenda([{ ...base, id: 'c', status: 'cancelado' }], { consultaId: 'atual', data: atual.data, inicio: '10:00', duracao: 30, pacienteId: 'pac-1', profissionalId: 'prof-1', local: 'Sala 01' })).toBe(false);
    expect(possuiConflitoAgenda([{ ...base, id: 'adjacente', inicio: '10:30' }], { consultaId: 'atual', data: atual.data, inicio: '10:00', duracao: 30, pacienteId: 'pac-1', profissionalId: 'prof-1', local: 'Sala 01' })).toBe(false);
  });

  it('permite selecionar novo profissional, data, horário e sala e envia os valores após revisar', async () => {
    const proximaSegunda = new Date();
    proximaSegunda.setHours(12, 0, 0, 0);
    proximaSegunda.setDate(proximaSegunda.getDate() + ((8 - proximaSegunda.getDay()) % 7 || 7));
    const dataAtual = dataLocal(proximaSegunda);
    const novaDataObj = new Date(proximaSegunda);
    novaDataObj.setDate(novaDataObj.getDate() + 1);
    const novaData = dataLocal(novaDataObj);

    const consulta: ConsultaApi = {
      id: 'consulta-1',
      pacienteId: 'paciente-1',
      profissionalId: 'prof-1',
      dataHora: `${dataAtual}T10:00:00`,
      tipoAtendimento: 'Psicoterapia',
      local: 'Sala 01',
      modalidade: 'Presencial',
      duracao: 50,
      status: 'PENDENTE',
    };
    const conflitoOutroProfissional: ConsultaApi = {
      ...consulta,
      id: 'consulta-conflito',
      pacienteId: 'paciente-2',
      profissionalId: 'prof-2',
      dataHora: `${novaData}T08:00:00`,
      local: 'Sala 02',
    };
    const paciente: PacienteApi = {
      id: 'paciente-1', nome: 'Paciente de teste', dataNascimento: '1990-01-01',
      email: 'paciente@example.test', cpf: '00000000000', telefone: '61000000000',
      responsaveis: [], possuiPlanoSaude: false, planoSaude: '', numeroCarteirinha: '', validadePlano: null,
    };
    const profissional = (id: string, nome: string): ProfissionalApi => ({
      id, nome, email: `${id}@example.test`, cpf: '00000000000', telefone: '61000000000',
      categoria: 'Psicólogo', formacaoAcademica: 'Psicologia', numeroRegistro: 'CRP-1',
      anoConclusao: 2020, atendeOnline: true, atendePresencial: true, ativo: true,
    });
    const disponibilidade: DisponibilidadeApi[] = [
      'SEGUNDA_FEIRA', 'TERCA_FEIRA', 'QUARTA_FEIRA', 'QUINTA_FEIRA', 'SEXTA_FEIRA', 'SABADO',
    ].map((diaSemana, index) => ({
      id: `disp-${index}`, diaSemana, horaInicio: '08:00', horaFim: '18:00', ativo: true,
    }));
    const atualizar = vi.fn((_id: string, request: Parameters<ConsultasApiService['atualizar']>[1]) =>
      of({ ...consulta, ...request }));
    const confirmar = vi.fn((_id: string) => of({ ...consulta, status: 'CONFIRMADA' as const }));
    const cancelar = vi.fn((_id: string) => of({ ...consulta, status: 'CANCELADA' as const }));
    const listarDisponibilidades = vi.fn((_id: string) => of(disponibilidade));

    await TestBed.configureTestingModule({
      imports: [Agenda],
      providers: [
        provideRouter([]),
        { provide: Aviso, useValue: { mostrar: vi.fn() } },
        { provide: ConsultasApiService, useValue: {
          listarTodas: () => of([consulta, conflitoOutroProfissional]), atualizar,
          confirmar, cancelar,
        } },
        { provide: PacientesApiService, useValue: { listarTodos: () => of([paciente]) } },
        { provide: ProfissionaisApiService, useValue: {
          listarTodos: () => of([profissional('prof-1', 'Profissional atual'), profissional('prof-2', 'Novo profissional')]),
          listarDisponibilidades, buscarPorId: (id: string) => of(profissional(id, 'Profissional')),
        } },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(Agenda);
    fixture.componentRef.setInput('perfil', 'secretaria');
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    fixture.nativeElement.querySelector('.ag-status-botao').click();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const data = fixture.nativeElement.querySelector('.dialogo-edicao input[type="date"]') as HTMLInputElement;
    data.value = novaData;
    data.dispatchEvent(new Event('change', { bubbles: true }));
    const profissionalSelect = fixture.nativeElement.querySelector('[aria-label="Profissional da consulta"]') as HTMLSelectElement;
    profissionalSelect.value = 'prof-2';
    profissionalSelect.dispatchEvent(new Event('change', { bubbles: true }));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const sala = fixture.nativeElement.querySelector('[aria-label="Sala da consulta"]') as HTMLSelectElement;
    expect(sala.disabled).toBe(false);
    sala.value = 'Sala 02';
    sala.dispatchEvent(new Event('change', { bubbles: true }));
    const horario = fixture.nativeElement.querySelector('[aria-label="Horário da consulta"]') as HTMLSelectElement;
    horario.value = '11:00';
    horario.dispatchEvent(new Event('change', { bubbles: true }));
    fixture.detectChanges();

    fixture.nativeElement.querySelector('.edicao-remarcar').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Revisar remarcação');
    expect(fixture.nativeElement.textContent).toContain('Novo profissional');
    expect(fixture.nativeElement.textContent).toContain('Sala 02');

    fixture.nativeElement.querySelector('.dialogo-botoes .botao-escuro').click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(listarDisponibilidades).toHaveBeenCalledWith('prof-2');
    expect(atualizar).toHaveBeenCalledWith('consulta-1', expect.objectContaining({
      pacienteId: 'paciente-1', profissionalId: 'prof-2',
      dataHora: `${novaData}T11:00:00`, local: 'Sala 02',
    }));

    fixture.detectChanges();
    fixture.nativeElement.querySelector('.ag-status-botao').click();
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.edicao-confirmar').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Confirmar esta consulta?');
    fixture.nativeElement.querySelector('.dialogo-botoes .botao-escuro').click();
    fixture.detectChanges();
    expect(confirmar).toHaveBeenCalledWith('consulta-1');

    fixture.detectChanges();
    fixture.nativeElement.querySelector('.ag-status-botao').click();
    fixture.detectChanges();
    fixture.nativeElement.querySelector('.edicao-cancelar').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Cancelar esta consulta?');
    fixture.nativeElement.querySelector('.dialogo-botoes .botao-escuro').click();
    fixture.detectChanges();
    expect(cancelar).toHaveBeenCalledWith('consulta-1');
  });
});

function dataLocal(data: Date): string {
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}`;
}
