import { Documento } from './modelos';
import { escaparHtml, limparHtml } from '../util';

/** Funções para abrir e baixar arquivos e documentos. */

function clicarNoLink(url: string, nome: string): void {
  const a = document.createElement('a');
  a.href = url;
  a.download = nome;
  document.body.appendChild(a);
  a.click();
  a.remove();
}

function nomeSeguro(texto: string): string {
  return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/[^a-zA-Z0-9]+/g, '-').replace(/^-|-$/g, '') || 'documento';
}

/**
 * Baixa o documento. Textos escritos no editor saem como .doc (o Word abre).
 * TODO (back-end): gerar PDF no servidor.
 */
export function baixarDocumento(d: Documento): void {
  if (d.arquivo) {
    clicarNoLink(d.arquivo.url, d.arquivo.nome);
    return;
  }
  const html =
    `<html><head><meta charset="utf-8"><title>${escaparHtml(d.titulo)}</title></head>` +
    `<body style="font-family:Arial,sans-serif">${limparHtml(d.conteudo)}</body></html>`;
  const blob = new Blob(['\ufeff', html], { type: 'application/msword' });
  const url = URL.createObjectURL(blob);
  clicarNoLink(url, `${nomeSeguro(d.titulo)}.doc`);
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export function baixarAnexo(nome: string, url: string): void {
  clicarNoLink(url, nome);
}

/** Cria o endereço temporário de um arquivo escolhido pelo usuário. */
export function anexoDe(arquivo: File): { nome: string; url: string } {
  return { nome: arquivo.name, url: URL.createObjectURL(arquivo) };
}

export function semExtensao(nome: string): string {
  return nome.replace(/\.[^.]+$/, '');
}

/** Compartilha o endereço do documento (menu do celular) ou copia o link. */
export async function compartilharLink(titulo: string, url: string): Promise<'compartilhado' | 'copiado' | 'erro'> {
  try {
    if (navigator.share) {
      await navigator.share({ title: titulo, url });
      return 'compartilhado';
    }
    await navigator.clipboard.writeText(url);
    return 'copiado';
  } catch {
    return 'erro';
  }
}
