import { Profissional } from './modelos';

/** "Dra. Juliana Cabral" (ou só o nome, quando não há tratamento). */
export function nomeProfissional(p: Pick<Profissional, 'tratamento' | 'nome'>): string {
  return `${p.tratamento ? p.tratamento + ' ' : ''}${p.nome}`;
}

/** "Dra. Juliana Cabral - Psicóloga infantil" */
export function rotuloProfissional(p: Pick<Profissional, 'tratamento' | 'nome' | 'especialidade'>): string {
  return `${nomeProfissional(p)} - ${p.especialidade}`;
}
