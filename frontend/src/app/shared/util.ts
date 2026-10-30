/** Funções de apoio: datas e textos formatados (HTML). */

const dois = (n: number) => String(n).padStart(2, '0');

/** Date -> '2026-09-15' (sem mexer em fuso horário). */
export function paraISO(data: Date): string {
  return `${data.getFullYear()}-${dois(data.getMonth() + 1)}-${dois(data.getDate())}`;
}

/** '2026-09-15' -> Date local. */
export function deISO(iso: string): Date {
  const [a, m, d] = iso.slice(0, 10).split('-').map(Number);
  return new Date(a, m - 1, d);
}

/** '2026-09-15' -> '15/09/2026' */
export function formatarData(iso?: string | null): string {
  if (!iso) return '—';
  const [a, m, d] = iso.slice(0, 10).split('-');
  return `${d}/${m}/${a}`;
}

/** '2026-10-20T10:33:00' -> '20/10/2026 - 10:33' */
export function formatarDataHora(isoDataHora: string): string {
  const d = new Date(isoDataHora);
  return `${dois(d.getDate())}/${dois(d.getMonth() + 1)}/${d.getFullYear()} - ${dois(d.getHours())}:${dois(d.getMinutes())}`;
}

export function idadeDe(nascimentoISO: string, referencia = new Date()): number {
  const n = deISO(nascimentoISO);
  let idade = referencia.getFullYear() - n.getFullYear();
  const fezAniversario =
    referencia.getMonth() > n.getMonth() ||
    (referencia.getMonth() === n.getMonth() && referencia.getDate() >= n.getDate());
  if (!fezAniversario) idade -= 1;
  return idade;
}

/** Tira acentos e maiúsculas (para buscar). */
export function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
}

/**
 * Limpa um HTML digitado/colado no editor: remove scripts, eventos (onclick...) e
 * endereços perigosos. TODO (back-end): repetir a limpeza no servidor antes de gravar.
 */
export function limparHtml(html: string): string {
  if (!html) return '';
  const doc = new DOMParser().parseFromString(html, 'text/html');
  doc
    .querySelectorAll('script,style,iframe,object,embed,link,meta,base,form,input,button,textarea,select,svg,math')
    .forEach((el) => el.remove());
  doc.body.querySelectorAll('*').forEach((el) => {
    for (const attr of Array.from(el.attributes)) {
      const nome = attr.name.toLowerCase();
      const valor = attr.value.replace(/\s/g, '').toLowerCase();
      if (nome.startsWith('on')) el.removeAttribute(attr.name);
      else if ((nome === 'href' || nome === 'src') && /^(javascript|vbscript|data:text)/.test(valor)) el.removeAttribute(attr.name);
      else if (nome === 'style' && /expression|url\(|javascript/.test(valor)) el.removeAttribute(attr.name);
    }
    if (el.tagName === 'A') {
      el.setAttribute('target', '_blank');
      el.setAttribute('rel', 'noopener noreferrer');
    }
  });
  return doc.body.innerHTML;
}

/** Só o texto, sem as marcações. */
export function textoPuro(html: string): string {
  const doc = new DOMParser().parseFromString(html || '', 'text/html');
  return (doc.body.textContent ?? '').replace(/\u00a0/g, ' ').trim();
}

/** true quando não há texto nem imagem. */
export function htmlVazio(html: string): boolean {
  return textoPuro(html) === '' && !/<img\b/i.test(html || '');
}

export function escaparHtml(texto: string): string {
  return texto.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/** Primeira linha de texto de um HTML (usada como título do documento). */
export function primeiraLinha(html: string, limite = 60): string {
  const comQuebras = (html || '').replace(/<\/?(?:br|p|div|h[1-6]|li|ul|ol)\b[^>]*>/gi, '\n');
  const doc = new DOMParser().parseFromString(comQuebras, 'text/html');
  const linhas = (doc.body.textContent ?? '').split('\n').map((l) => l.trim()).filter(Boolean);
  const linha = linhas[0] ?? '';
  return linha.length > limite ? `${linha.slice(0, limite - 1).trimEnd()}…` : linha;
}

/* ---------- Datas extras ---------- */

export const MESES_CURTOS = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
const MESES_LONGOS = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];

/** '2026-09-15' + 7 -> '2026-09-22' */
export function somarDias(iso: string, dias: number): string {
  const d = deISO(iso);
  d.setDate(d.getDate() + dias);
  return paraISO(d);
}

/** '2026-09-15' -> '15 de Setembro' */
export function dataExtensa(iso: string): string {
  const d = deISO(iso);
  return `${String(d.getDate()).padStart(2, '0')} de ${MESES_LONGOS[d.getMonth()]}`;
}

/** 0 = segunda ... 6 = domingo */
export function diaDaSemana(iso: string): number {
  return (deISO(iso).getDay() + 6) % 7;
}

/* ---------- CPF, telefone e e-mail ---------- */

const soNumeros = (t: string) => (t ?? '').replace(/\D/g, '');

export function formatarCpf(valor: string): string {
  const n = soNumeros(valor).slice(0, 11);
  return n
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1-$2');
}

export function formatarTelefone(valor: string): string {
  const n = soNumeros(valor).slice(0, 11);
  if (n.length <= 2) return n.length ? `(${n}` : '';
  if (n.length <= 6) return `(${n.slice(0, 2)}) ${n.slice(2)}`;
  if (n.length <= 10) return `(${n.slice(0, 2)}) ${n.slice(2, 6)}-${n.slice(6)}`;
  return `(${n.slice(0, 2)}) ${n.slice(2, 7)}-${n.slice(7)}`;
}

function digitoCpf(base: string): number {
  const peso = base.length + 1;
  const soma = base.split('').reduce((t, d, i) => t + Number(d) * (peso - i), 0);
  const resto = (soma * 10) % 11;
  return resto === 10 ? 0 : resto;
}

export function cpfValido(cpf: string): boolean {
  const n = soNumeros(cpf);
  if (n.length !== 11 || /^(\d)\1{10}$/.test(n)) return false;
  const d1 = digitoCpf(n.slice(0, 9));
  const d2 = digitoCpf(n.slice(0, 9) + d1);
  return n.endsWith(`${d1}${d2}`);
}

/** Gera um CPF válido (e fictício) a partir de 9 números. Usado só nos dados de teste. */
export function cpfDeTeste(base9: string): string {
  const d1 = digitoCpf(base9);
  const d2 = digitoCpf(base9 + d1);
  return formatarCpf(`${base9}${d1}${d2}`);
}

export function telefoneValido(valor: string): boolean {
  const n = soNumeros(valor);
  return n.length === 10 || n.length === 11;
}

export function emailValido(valor: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(valor.trim());
}

/** Gera uma senha temporária aleatória (ex.: "Nv7k-Qp3x92"). */
export function gerarSenhaTemporaria(): string {
  const letras = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789';
  const sorteio = crypto.getRandomValues(new Uint32Array(10));
  const texto = Array.from(sorteio, (n) => letras[n % letras.length]).join('');
  return `${texto.slice(0, 4)}-${texto.slice(4)}`;
}
