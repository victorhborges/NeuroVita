/** Contratos visuais usados pelos componentes de agenda, calendário e resumo. */
import { StatusConsulta } from './clinica/modelos';

export type CorTag = 'azul' | 'vermelho' | 'verde' | 'roxo';

export interface Tag {
  sigla: 'TEA' | 'TDAH';
  cor: CorTag;
}

export interface ItemAgenda {
  hora: string;
  paciente: string;
  tipo: string;
  /** TEA / TDAH (telas com dados de protótipo). */
  tag?: Tag;
  /** Status da consulta (telas ligadas à agenda de verdade). */
  status?: StatusConsulta;
}

export interface ConsultaProxima {
  paciente: string;
  tipo: string;
  data: string;
  hora: string;
  tag?: Tag;
  status?: StatusConsulta;
  /** Caminho da foto do paciente. Sem foto, aparece um círculo com a inicial. */
  foto?: string;
}

export interface CardResumo {
  valor: number | string;
  rotulo: string;
  icone: 'usuario' | 'calendario' | 'info' | 'usuario-x';
}

export interface DiaCalendario {
  numero: number;
  /** Dia que não é do mês exibido (cinza). */
  foraDoMes?: boolean;
  /** Dia com consulta (círculo cinza). */
  comConsulta?: boolean;
  /** Dia selecionado (círculo escuro). */
  selecionado?: boolean;
}

export const DIAS_DA_SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

export function calendarioDoMes(
  data: Date,
  datasComConsulta: Set<string>,
  dataSelecionada?: string,
): DiaCalendario[][] {
  const ano = data.getFullYear();
  const mes = data.getMonth();
  const primeiro = new Date(ano, mes, 1);
  const inicio = new Date(ano, mes, 1 - primeiro.getDay());
  const hoje = dataSelecionada
    ?? `${ano}-${String(mes + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}`;
  const semanas: DiaCalendario[][] = [];
  for (let semana = 0; semana < 6; semana++) {
    const dias: DiaCalendario[] = [];
    for (let coluna = 0; coluna < 7; coluna++) {
      const atual = new Date(inicio);
      atual.setDate(inicio.getDate() + semana * 7 + coluna);
      const iso = `${atual.getFullYear()}-${String(atual.getMonth() + 1).padStart(2, '0')}-${String(atual.getDate()).padStart(2, '0')}`;
      dias.push({ numero: atual.getDate(), foraDoMes: atual.getMonth() !== mes, selecionado: iso === hoje, comConsulta: datasComConsulta.has(iso) });
    }
    semanas.push(dias);
    if (semana >= 4 && dias.every((dia) => dia.foraDoMes)) break;
  }
  return semanas;
}
