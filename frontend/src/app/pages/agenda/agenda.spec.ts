import { possuiAlteracaoAgenda, salaConsultaPermitida } from './agenda';

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
});
