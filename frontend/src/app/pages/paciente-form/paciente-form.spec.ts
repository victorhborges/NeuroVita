import { erroPlanoSaude } from './paciente-form';

describe('erroPlanoSaude', () => {
  it('exige o nome do plano quando o paciente possui convênio', () => {
    expect(erroPlanoSaude(true, '  ')).toBe('Informe o plano de saúde.');
  });

  it('aceita nome do plano sem número de carteirinha ou validade', () => {
    expect(erroPlanoSaude(true, 'Plano exemplo')).toBeNull();
  });

  it('não exige identificação de plano quando o paciente não possui plano', () => {
    expect(erroPlanoSaude(false, '')).toBeNull();
  });
});
