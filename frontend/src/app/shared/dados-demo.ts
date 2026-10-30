/**
 * DADOS FICTÍCIOS (iguais aos do protótipo do Figma).
 * TODO (back-end): trocar tudo daqui por chamadas à API. As telas só recebem os dados
 * pelos tipos abaixo, então basta devolver o mesmo formato.
 */
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

const TEA_AZUL: Tag = { sigla: 'TEA', cor: 'azul' };
const TEA_VERMELHO: Tag = { sigla: 'TEA', cor: 'vermelho' };
const TDAH_VERDE: Tag = { sigla: 'TDAH', cor: 'verde' };
const TDAH_ROXO: Tag = { sigla: 'TDAH', cor: 'roxo' };

export const DATA_AGENDA = '01 de Setembro';

export const AGENDA_DO_DIA: ItemAgenda[] = [
  { hora: '08:00', paciente: 'Ana Beatriz Silva', tipo: 'Avaliação Psicológica', tag: TEA_AZUL },
  { hora: '09:30', paciente: 'João Pedro Lima', tipo: 'Acompanhamento', tag: TDAH_VERDE },
  { hora: '10:30', paciente: 'Maria Clara Santos', tipo: 'Devolutiva', tag: TDAH_ROXO },
  { hora: '12:30', paciente: 'Lucas Martins', tipo: 'Acompanhamento', tag: TEA_AZUL },
  { hora: '13:30', paciente: 'Júlia Fernandes', tipo: 'Acompanhamento', tag: TEA_VERMELHO },
  { hora: '15:00', paciente: 'Mateus Santos', tipo: 'Avaliação psicológica', tag: TDAH_VERDE },
  { hora: '16:30', paciente: 'Higor silva', tipo: 'Devolutiva', tag: TDAH_ROXO },
];

export const PROXIMAS_CONSULTAS: ConsultaProxima[] = [
  { paciente: 'Maria Clara Santos', tipo: 'Avaliação psicológica', data: '01/09/2026', hora: '9:30', tag: TEA_VERMELHO },
  { paciente: 'Lucas dos Santos', tipo: 'Devolutiva', data: '01/09/2026', hora: '9:30', tag: TDAH_VERDE },
  { paciente: 'Miguel Soares', tipo: 'Acompanhamento', data: '01/09/2026', hora: '9:30', tag: TDAH_ROXO },
  { paciente: 'Ana Clara Rodrigues', tipo: 'Avaliação psicológica', data: '01/09/2026', hora: '9:30', tag: TDAH_ROXO },
];

export const CARDS_RESUMO: CardResumo[] = [
  { valor: 150, rotulo: 'Total de pacientes', icone: 'usuario' },
  { valor: 10, rotulo: 'Consultas Hoje', icone: 'calendario' },
  { valor: 3, rotulo: 'Consultas Pendentes', icone: 'info' },
  { valor: 1, rotulo: 'Consultas Canceladas', icone: 'usuario-x' },
];

export const MES_CALENDARIO = 'Setembro de 2026';
export const DIAS_DA_SEMANA = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

const fora = (numero: number): DiaCalendario => ({ numero, foraDoMes: true });
const dia = (numero: number, extra: Partial<DiaCalendario> = {}): DiaCalendario => ({ numero, ...extra });

/**
 * Calendário igual ao do protótipo (semana a semana).
 * TODO: gerar de verdade a partir da data atual e das consultas vindas do back-end.
 */
export const SEMANAS_CALENDARIO: DiaCalendario[][] = [
  [fora(26), fora(27), fora(28), fora(29), fora(30), dia(1), dia(2)],
  [dia(3), dia(4, { comConsulta: true }), dia(5), dia(6), dia(7), dia(8), dia(9)],
  [dia(10), dia(11, { comConsulta: true }), dia(12, { comConsulta: true }), dia(13), dia(14), dia(15), dia(16)],
  [dia(17), dia(18), dia(19), dia(20), dia(21), dia(22), dia(23)],
  [dia(24), dia(25), dia(26), dia(27), dia(28), dia(29), dia(30)],
  [dia(31, { selecionado: true }), fora(1), fora(2), fora(3), fora(4), fora(5), fora(6)],
];
