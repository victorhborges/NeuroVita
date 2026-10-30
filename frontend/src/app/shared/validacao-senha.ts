/**
 * Regras da nova senha: no mínimo 8 caracteres, com letras e números,
 * e a confirmação precisa ser igual.
 * Retorna a mensagem de erro, ou null quando está tudo certo.
 */
export function validarNovaSenha(nova: string, confirmar: string): string | null {
  if (nova.length < 8 || !/[a-zA-Z]/.test(nova) || !/\d/.test(nova)) {
    return 'A senha deve ter no mínimo 8 caracteres, com letras e números.';
  }
  if (nova !== confirmar) {
    return 'As senhas não coincidem.';
  }
  return null;
}
