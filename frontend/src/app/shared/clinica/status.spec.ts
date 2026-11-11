import { horariosAgendaParaConsultas, horariosDaDisponibilidade, minutosDe } from './status';

describe('horariosDaDisponibilidade', () => {
  it('gera consultas de 50 minutos com inícios a cada 60 minutos', () => {
    expect(
      horariosDaDisponibilidade([
        { horaInicio: '08:00:00', horaFim: '12:00:00', ativo: true },
      ]),
    ).toEqual(['08:00', '09:00', '10:00', '11:00']);
  });

  it('avança de hora em hora a partir do início real da janela', () => {
    expect(
      horariosDaDisponibilidade([
        { horaInicio: '13:30:00', horaFim: '17:30:00', ativo: true },
        { horaInicio: '09:00:00', horaFim: '11:00:00', ativo: false },
      ]),
    ).toEqual(['13:30', '14:30', '15:30', '16:30']);
  });

  it('não oferece início quando a duração não cabe na disponibilidade', () => {
    expect(
      horariosDaDisponibilidade([
        { horaInicio: '08:00:00', horaFim: '08:49:00', ativo: true },
      ]),
    ).toEqual([]);
  });

  it('não inclui o horário final quando a consulta terminaria após a janela', () => {
    expect(
      horariosDaDisponibilidade([
        { horaInicio: '08:00:00', horaFim: '12:00:00', ativo: true },
      ]),
    ).not.toContain('12:00');
  });
});

describe('horariosAgendaParaConsultas', () => {
  it('expande a grade para exibir consultas fora da faixa visual padrão', () => {
    const horarios = horariosAgendaParaConsultas([
      { inicio: '07:30', duracao: 50 },
      { inicio: '19:00', duracao: 50 },
    ]);

    expect(horarios[0]).toBe('07:30');
    expect(horarios.at(-1)).toBe('20:00');
    expect(horarios).toContain('19:00');
  });

  it('mantém a faixa padrão quando não existem consultas fora dela', () => {
    const horarios = horariosAgendaParaConsultas([
      { inicio: '09:00', duracao: 50 },
    ]);

    expect(horarios[0]).toBe('08:00');
    expect(horarios.at(-1)).toBe('18:00');
  });
});

describe('minutosDe', () => {
  it('converte horários com e sem segundos recebidos da API', () => {
    expect(minutosDe('13:30')).toBe(810);
    expect(minutosDe('13:30:00')).toBe(810);
  });
});
