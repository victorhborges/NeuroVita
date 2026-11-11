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

/** Expande a grade base para que consultas fora de 08:00–18:00 também apareçam. */
export function horariosAgendaParaConsultas(
  consultas: { inicio: string; duracao: number }[],
): string[] {
  let inicioGrade = 8 * 60;
  let fimGrade = 18 * 60;

  for (const consulta of consultas) {
    const inicio = minutosDe(consulta.inicio);
    const fim = inicio + Math.max(0, consulta.duracao || 0);
    inicioGrade = Math.min(inicioGrade, Math.floor(inicio / 30) * 30);
    fimGrade = Math.max(fimGrade, Math.ceil(fim / 30) * 30);
  }

  return Array.from({ length: Math.floor((fimGrade - inicioGrade) / 30) + 1 }, (_, i) => {
    const minuto = inicioGrade + i * 30;
    return `${String(Math.floor(minuto / 60)).padStart(2, '0')}:${String(minuto % 60).padStart(2, '0')}`;
  });
}

export const DURACAO_PADRAO_CONSULTA_MINUTOS = 50;
export const INTERVALO_INICIOS_CONSULTA_MINUTOS = 60;

/** Gera inícios espaçados que cabem por completo em disponibilidades reais. */
export function horariosDaDisponibilidade(
  disponibilidades: {
    horaInicio: string;
    horaFim: string;
    ativo: boolean;
  }[],
  duracao = DURACAO_PADRAO_CONSULTA_MINUTOS,
  intervalo = INTERVALO_INICIOS_CONSULTA_MINUTOS,
): string[] {
  if (duracao <= 0 || intervalo <= 0) return [];

  const horarios = new Set<string>();

  for (const disponibilidade of disponibilidades) {
    if (!disponibilidade.ativo) continue;

    const inicio = minutosDe(disponibilidade.horaInicio);
    const fim = minutosDe(disponibilidade.horaFim);

    for (let minuto = inicio; minuto + duracao <= fim; minuto += intervalo) {
      horarios.add(
        `${String(Math.floor(minuto / 60)).padStart(2, '0')}:${String(minuto % 60).padStart(2, '0')}`,
      );
    }
  }

  return [...horarios].sort((a, b) => minutosDe(a) - minutosDe(b));
}

export const SALAS = ['Sala 01', 'Sala 02', 'Sala 03', 'Sala 04'];

export const minutosDe = (hora: string): number => {
  const [horas, minutos] = hora.split(':');
  return Number(horas) * 60 + Number(minutos);
};
