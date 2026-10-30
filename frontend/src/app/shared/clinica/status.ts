import { StatusConsulta } from './modelos';

/** Texto do botão de status da consulta (ex.: "Confirmada"). */
export const ROTULO_STATUS: Record<StatusConsulta, string> = {
  confirmado: 'Confirmada',
  pendente: 'Pendente',
  remarcado: 'Remarcada',
  cancelado: 'Cancelada',
};

/** Texto da etiqueta nas listas (ex.: "Confirmado"). */
export const ROTULO_CHIP: Record<StatusConsulta, string> = {
  confirmado: 'Confirmado',
  pendente: 'Pendente',
  remarcado: 'Remarcado',
  cancelado: 'Cancelada',
};

export const ORDEM_STATUS: StatusConsulta[] = ['confirmado', 'pendente', 'remarcado', 'cancelado'];

/** Horários da grade da agenda: 08:00 até 18:00, de 30 em 30 minutos. */
export const HORARIOS_AGENDA: string[] = Array.from({ length: 21 }, (_, i) => {
  const m = 8 * 60 + i * 30;
  return `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`;
});

/** Horários oferecidos ao marcar uma consulta (pausa para o almoço entre 12:00 e 13:00). */
export const HORARIOS_CONSULTA: string[] = HORARIOS_AGENDA.filter((h) => h < '12:00' || (h >= '13:00' && h < '17:00'));

export const SALAS = ['Sala 01', 'Sala 02', 'Sala 03', 'Sala 04'];

export const minutosDe = (hora: string): number => Number(hora.slice(0, 2)) * 60 + Number(hora.slice(3));
