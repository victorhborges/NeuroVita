import { validarNovaSenha } from './validacao-senha';

describe('validarNovaSenha', () => {
  it('recusa senha com menos de 8 caracteres', () => {
    expect(validarNovaSenha('abc1234', 'abc1234')).toBe(
      'A senha deve ter no mínimo 8 caracteres, com letras e números.',
    );
  });

  it('recusa senha sem números', () => {
    expect(validarNovaSenha('abcdefgh', 'abcdefgh')).not.toBeNull();
  });

  it('recusa senha sem letras', () => {
    expect(validarNovaSenha('12345678', '12345678')).not.toBeNull();
  });

  it('recusa quando a confirmação é diferente', () => {
    expect(validarNovaSenha('senha1234', 'senha12345')).toBe('As senhas não coincidem.');
  });

  it('aceita senha de 8 caracteres com letras e números', () => {
    expect(validarNovaSenha('senha123', 'senha123')).toBeNull();
  });
});
