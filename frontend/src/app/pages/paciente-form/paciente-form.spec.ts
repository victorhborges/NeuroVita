import { erroEnderecoPaciente, erroPlanoSaude } from './paciente-form';

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

describe('endereço do paciente do MVP FBR', () => {
  it('exige CEP de oito dígitos, número e bairro', () => {
    expect(erroEnderecoPaciente('', '', '')).toBe('Informe um CEP válido.');
    expect(erroEnderecoPaciente('70000-000', '', 'Centro')).toBe('Informe o número do endereço.');
    expect(erroEnderecoPaciente('70000-000', '12', '')).toBe('Informe o bairro.');
  });

  it('aceita CEP formatado e endereço completo', () => {
    expect(erroEnderecoPaciente('70000-000', '12', 'Centro')).toBeNull();
  });
});
