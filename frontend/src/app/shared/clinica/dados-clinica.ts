/**
 * DADOS FICTÍCIOS da clínica: pacientes, profissionais, agenda e documentos.
 * TODO (back-end): trocar por chamadas à API. O serviço Clinica (clinica.ts) é o único
 * lugar que usa estes dados, então basta trocar lá.
 */
import { SEGUNDA_BASE } from '../dados-agenda';
import { cpfDeTeste, somarDias } from '../util';
import {
  Agendamento, Consulta, Disponibilidade, Documento, Paciente, PlanoSaude, Profissional, Responsavel, Secretaria,
  StatusConsulta, StatusPaciente,
} from './modelos';

/* ---------- Profissionais ---------- */

const DISPONIBILIDADE_PADRAO: Disponibilidade = {
  dias: [0, 1, 2, 3, 4, 5],
  faixas: [{ inicio: '08:00', fim: '12:00' }, { inicio: '13:00', fim: '18:00' }],
  online: true,
  presencial: true,
};

function prof(
  id: string, nome: string, tratamento: Profissional['tratamento'], especialidade: string, tipo: Profissional['tipo'],
  registro: string, nascimento: string, telefone: string, ativo = true,
): Profissional {
  return {
    id, codigo: '', nome, tratamento, especialidade, tipo, registro, nascimento, telefone, ativo,
    cpf: '', email: '', anoConclusao: null, disponibilidade: DISPONIBILIDADE_PADRAO, emailAcesso: '',
  };
}

export const PROFISSIONAIS_INICIAIS: Profissional[] = [
  prof('camila-almeida', 'Camila Almeida', 'Dra.', 'Psicóloga', 'psicologo', 'CRP 06/123456', '1997-04-12', '(61) 99842-7316'),
  prof('juliana-ribeiro', 'Juliana Ribeiro', 'Dra.', 'Psicóloga', 'psicologo', 'CRP 06/123457', '1992-08-27', '(61) 99167-4058'),
  prof('renata-barbosa', 'Renata Barbosa', 'Dra.', 'Psiquiatra', 'psiquiatra', 'CRM 12345-DF', '1985-02-05', '(61) 98654-2197'),
  prof('larissa-mendes', 'Larissa Mendes', 'Dra.', 'Psicóloga', 'psicologo', 'CRP 06/123459', '2000-06-19', '(61) 99431-8625', false),
  prof('marcos-ferreira', 'Marcos Ferreira', 'Dr.', 'Psiquiatra', 'psiquiatra', 'CRM 23456-DF', '1987-11-11', '(61) 98376-5402'),
  prof('patricia-oliveira', 'Patrícia Oliveira', 'Dra.', 'Psicóloga', 'psicologo', 'CRP 06/123461', '1981-03-23', '(61) 99715-3849'),
  prof('andre-carvalho', 'André Carvalho', 'Dr.', 'Psicólogo', 'psicologo', 'CRP 06/123462', '1994-01-30', '(61) 98193-6274', false),
  prof('maria-oliveira', 'Maria Oliveira', 'Dra.', 'Psicóloga', 'psicologo', 'CRP 06/123463', '1989-05-17', '(61) 99555-1020'),
  prof('eduardo-costa', 'Eduardo Costa', 'Dr.', 'Psiquiatra', 'psiquiatra', 'CRM 34567-DF', '1983-09-02', '(61) 99644-3381'),
  prof('gustavo-lima', 'Gustavo Lima', 'Dr.', 'Psicólogo', 'psicologo', 'CRP 06/123465', '1990-12-14', '(61) 98822-7745'),
  prof('nathalia-valente', 'Nathalia Valente', 'Dra.', 'Psicóloga', 'psicologo', 'CRP 06/123466', '1993-07-08', '(61) 99301-6612'),
  prof('juliana-cabral', 'Juliana Cabral', 'Dra.', 'Psicóloga infantil', 'psicologo', 'CRP 06/123467', '1991-10-21', '(61) 99478-2250'),
].map((p, i) => {
  const [primeiro, ...resto] = p.nome.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().split(' ');
  const email = `${primeiro}.${resto[resto.length - 1] ?? 'clinica'}@neurovita.com`;
  return {
    ...p, codigo: String(123 + i).padStart(5, '0'), cpf: cpfDeTeste(String(200300400 + i * 1111111)),
    email, emailAcesso: email, anoConclusao: 2008 + (i % 14),
  };
});

/* ---------- Secretárias ---------- */

export const SECRETARIAS_INICIAIS: Secretaria[] = [
  {
    id: 'juliana-paiva', codigo: '00123', nome: 'Juliana Paiva', nascimento: '1994-01-30', telefone: '(61) 96584-2318',
    email: 'juliana.paiva@gmail.com', cpf: cpfDeTeste('987654321'), endereco: 'Quadra 5, Casa 7 - Valparaíso de Goiás',
    cargo: 'Secretária', admissao: '2023-03-01', ativo: true, emailAcesso: 'secretaria@neurovita.com',
  },
];

/* ---------- Pacientes ---------- */

const PROFISSIONAL_PADRAO = 'Dra. Maria - Psicóloga';
const RESUMO = 'Reavaliação do quadro, ajuste no plano terapêutico';
const SEM_PLANO: PlanoSaude = { possui: false, nome: '', carteirinha: '', validade: '' };

function historico(...datas: string[]): Consulta[] {
  return datas.map((data) => ({ data, profissional: PROFISSIONAL_PADRAO, resumo: RESUMO }));
}

interface Base {
  id: string; codigo: string; nome: string; nascimento: string; telefone: string; email: string;
  endereco: string; status: StatusPaciente; ultima: string | null; prof: string | null; cpf: string;
  resp: { nome: string; relacao: string; telefone: string; email: string; cpf: string; nascimento: string };
  diag: [string, string, string | null];
  consultas?: Consulta[]; plano?: PlanoSaude;
}

function paciente(b: Base): Paciente {
  const responsavel: Responsavel = {
    nome: b.resp.nome, relacao: b.resp.relacao, telefone: b.resp.telefone, endereco: b.endereco,
    email: b.resp.email, cpf: b.resp.cpf, nascimento: b.resp.nascimento,
  };
  return {
    id: b.id, codigo: b.codigo, nome: b.nome, nascimento: b.nascimento, telefone: b.telefone, email: b.email,
    cpf: b.cpf, endereco: b.endereco, status: b.status,
    situacao: b.status === 'inativo' ? 'Inativo' : b.status === 'aguardando' ? 'Aguardando retorno' : 'Em andamento',
    ultimaConsulta: b.ultima, profissionalId: b.prof, responsavel,
    plano: b.plano ?? SEM_PLANO,
    diagnostico: { principal: b.diag[0], cid: b.diag[1], inicioTratamento: b.diag[2] },
    consultas: b.consultas ?? [],
  };
}

export const PACIENTES_INICIAIS: Paciente[] = [
  paciente({
    id: 'lucas-martins', codigo: '00123', nome: 'Lucas Martins', nascimento: '2009-01-23', telefone: '(61) 99524-4456',
    email: 'lucas.martins@email.com', endereco: 'Rua da Maria, Qd 256 - DF', status: 'acompanhamento', ultima: '2026-08-02',
    prof: 'maria-oliveira', cpf: cpfDeTeste('111444777'),
    resp: { nome: 'Ana Martins', relacao: 'Mãe', telefone: '(61) 99524-4456', email: 'ana.martins@email.com', cpf: cpfDeTeste('222333444'), nascimento: '1980-05-10' },
    diag: ['Transtorno de Ansiedade (F41.1)', 'F41.1', '2026-03-12'],
    consultas: historico('2026-08-02', '2026-07-19', '2026-07-05', '2026-06-21', '2026-05-26'),
  }),
  paciente({
    id: 'ana-clara-souza', codigo: '00124', nome: 'Ana Clara Souza', nascimento: '2016-03-15', telefone: '(61) 99847-2631',
    email: 'ana.souza@email.com', endereco: 'Quadra 8, Casa 12 - Valparaíso de Goiás', status: 'acompanhamento', ultima: '2026-08-05',
    prof: 'maria-oliveira', cpf: cpfDeTeste('333555777'),
    resp: { nome: 'Fernanda Souza', relacao: 'Mãe', telefone: '(61) 99847-2631', email: 'fernanda.souza@email.com', cpf: cpfDeTeste('444666888'), nascimento: '1985-02-20' },
    diag: ['TDAH (F90.0)', 'F90.0', '2026-02-10'], consultas: historico('2026-08-05', '2026-07-08'),
  }),
  paciente({
    id: 'lucas-henrique-alves', codigo: '00125', nome: 'Lucas Henrique Alves', nascimento: '2012-03-10', telefone: '(61) 99152-7846',
    email: 'lucas.alves@email.com', endereco: 'Quadra 12, Casa 8 - Valparaíso de Goiás', status: 'aguardando', ultima: '2026-08-08',
    prof: 'eduardo-costa', cpf: cpfDeTeste('555777999'),
    resp: { nome: 'Carla Alves', relacao: 'Mãe', telefone: '(61) 99152-7846', email: 'carla.alves@email.com', cpf: cpfDeTeste('111222333'), nascimento: '1983-07-01' },
    diag: ['TDAH (F90.0)', 'F90.0', '2026-02-20'], consultas: historico('2026-08-08', '2026-07-11', '2026-06-13'),
  }),
  paciente({
    id: 'gabriel-santos-oliveira', codigo: '00126', nome: 'Gabriel Santos Oliveira', nascimento: '2013-05-14', telefone: '(61) 98631-4509',
    email: 'gabriel.oliveira@email.com', endereco: 'Rua 5, Lote 20 - Cidade Ocidental', status: 'acompanhamento', ultima: '2026-08-13',
    prof: 'gustavo-lima', cpf: cpfDeTeste('123123123'),
    resp: { nome: 'Paulo Oliveira', relacao: 'Pai', telefone: '(61) 98631-4509', email: 'paulo.oliveira@email.com', cpf: cpfDeTeste('321321321'), nascimento: '1981-11-03' },
    diag: ['Transtorno do Espectro Autista (F84.0)', 'F84.0', '2026-01-15'], consultas: historico('2026-08-13', '2026-07-16'),
  }),
  paciente({
    id: 'mariana-vitoria-costa', codigo: '00127', nome: 'Mariana Vitória Costa', nascimento: '2011-02-08', telefone: '(61) 99428-6173',
    email: 'mariana.costa@email.com', endereco: 'Av. Central, 450 - Luziânia', status: 'inativo', ultima: '2026-08-15',
    prof: 'maria-oliveira', cpf: cpfDeTeste('456456456'),
    resp: { nome: 'Renata Costa', relacao: 'Mãe', telefone: '(61) 99428-6173', email: 'renata.costa@email.com', cpf: cpfDeTeste('654654654'), nascimento: '1984-04-18' },
    diag: ['Transtorno de Ansiedade Generalizada (F41.1)', 'F41.1', '2026-04-02'], consultas: historico('2026-08-15', '2026-07-18'),
  }),
  paciente({
    id: 'pedro-miguel-rodrigues', codigo: '00128', nome: 'Pedro Miguel Rodrigues', nascimento: '2009-02-19', telefone: '(61) 98375-2914',
    email: 'pedro.rodrigues@email.com', endereco: 'Rua das Flores, 98 - Brasília', status: 'aguardando', ultima: '2026-08-19',
    prof: 'maria-oliveira', cpf: cpfDeTeste('789789789'),
    resp: { nome: 'Marcos Rodrigues', relacao: 'Pai', telefone: '(61) 98375-2914', email: 'marcos.rodrigues@email.com', cpf: cpfDeTeste('987987987'), nascimento: '1979-09-25' },
    diag: ['Episódio Depressivo (F32.1)', 'F32.1', '2026-05-10'], consultas: historico('2026-08-19', '2026-07-22'),
  }),
  paciente({
    id: 'beatriz-fernandes-lima', codigo: '00129', nome: 'Beatriz Fernandes Lima', nascimento: '2010-06-14', telefone: '(61) 99706-5382',
    email: 'beatriz.lima@email.com', endereco: 'Q 3 Conj. B, Casa 14 - Santo Antônio do Descoberto', status: 'acompanhamento', ultima: '2026-08-25',
    prof: 'nathalia-valente', cpf: cpfDeTeste('147147147'),
    resp: { nome: 'Sandra Lima', relacao: 'Mãe', telefone: '(61) 99706-5382', email: 'sandra.lima@email.com', cpf: cpfDeTeste('741741741'), nascimento: '1982-12-12' },
    diag: ['TDAH (F90.0)', 'F90.0', '2026-03-30'], consultas: historico('2026-08-25', '2026-07-28'),
  }),
  paciente({
    id: 'joao-vitor-martins', codigo: '00130', nome: 'João Vitor Martins', nascimento: '2009-05-30', telefone: '(61) 98164-9275',
    email: 'joao.martins@email.com', endereco: 'Rua 10, Casa 3 - Novo Gama', status: 'inativo', ultima: '2026-08-29',
    prof: 'eduardo-costa', cpf: cpfDeTeste('258258258'),
    resp: { nome: 'Helena Martins', relacao: 'Mãe', telefone: '(61) 98164-9275', email: 'helena.martins@email.com', cpf: cpfDeTeste('852852852'), nascimento: '1978-08-08' },
    diag: ['Transtorno de Ansiedade (F41.1)', 'F41.1', '2025-11-18'], consultas: historico('2026-08-29', '2026-06-27'),
  }),
  paciente({
    id: 'laura-santos', codigo: '00131', nome: 'Laura Santos', nascimento: '2014-04-02', telefone: '(61) 93484-7124',
    email: 'lucia.maria@email.com', endereco: 'Unidade Santa Maria, Qd 4 - DF', status: 'acompanhamento', ultima: '2026-08-30',
    prof: 'juliana-cabral', cpf: cpfDeTeste('369369369'),
    resp: { nome: 'Lucia Maria', relacao: 'Mãe', telefone: '(61) 93484-7124', email: 'lucia.maria@email.com', cpf: cpfDeTeste('963963963'), nascimento: '1986-01-15' },
    diag: ['TEA (F84.0)', 'F84.0', '2026-04-10'], consultas: historico('2026-08-30'),
  }),
  paciente({
    id: 'luana-lima', codigo: '00132', nome: 'Luana Lima', nascimento: '2011-01-25', telefone: '(61) 92549-4175',
    email: 'antonia.rocha@email.com', endereco: 'Rua 7, Casa 22 - Santa Maria', status: 'acompanhamento', ultima: '2026-08-31',
    prof: 'juliana-cabral', cpf: cpfDeTeste('159159159'),
    resp: { nome: 'Antônia da Rocha', relacao: 'Avó', telefone: '(61) 92549-4175', email: 'antonia.rocha@email.com', cpf: cpfDeTeste('951951951'), nascimento: '1960-03-30' },
    diag: ['Ansiedade (F41.1)', 'F41.1', '2026-05-20'], consultas: historico('2026-08-31'),
  }),
  paciente({
    id: 'lucas-junior', codigo: '00133', nome: 'Lucas Junior', nascimento: '2010-02-11', telefone: '(61) 99412-8830',
    email: 'valdir.tavares@email.com', endereco: 'Quadra 21, Lote 5 - Gama', status: 'acompanhamento', ultima: '2026-08-28',
    prof: 'juliana-cabral', cpf: cpfDeTeste('753753753'),
    resp: { nome: 'Valdir Tavares', relacao: 'Pai', telefone: '(61) 99412-8830', email: 'valdir.tavares@email.com', cpf: cpfDeTeste('357357357'), nascimento: '1977-06-06' },
    diag: ['TDAH (F90.0)', 'F90.0', '2026-03-03'], consultas: historico('2026-08-28'),
  }),
  paciente({
    id: 'luna', codigo: '00134', nome: 'Luna', nascimento: '2017-07-19', telefone: '(61) 99428-6174',
    email: 'marineide.assuncao@email.com', endereco: 'Rua 3, Casa 9 - Samambaia', status: 'acompanhamento', ultima: '2026-08-27',
    prof: 'juliana-cabral', cpf: cpfDeTeste('246246246'),
    resp: { nome: 'Marineide Assunção', relacao: 'Mãe', telefone: '(61) 99428-6174', email: 'marineide.assuncao@email.com', cpf: cpfDeTeste('642642642'), nascimento: '1988-10-10' },
    diag: ['TEA (F84.0)', 'F84.0', '2026-02-27'], consultas: historico('2026-08-27'),
  }),
  paciente({
    id: 'marina-oliveira', codigo: '00135', nome: 'Marina Oliveira', nascimento: '2011-03-09', telefone: '(61) 93484-7125',
    email: 'antonio.oliveira@email.com', endereco: 'Rua 15, Casa 2 - Ceilândia', status: 'acompanhamento', ultima: '2026-08-26',
    prof: 'eduardo-costa', cpf: cpfDeTeste('135135135'),
    resp: { nome: 'Antônio Oliveira', relacao: 'Pai', telefone: '(61) 93484-7124', email: 'antonio.oliveira@email.com', cpf: cpfDeTeste('531531531'), nascimento: '1980-04-04' },
    diag: ['TDAH (F90.0)', 'F90.0', '2026-01-30'], consultas: historico('2026-08-26'),
  }),
];

/* ---------- Agenda (consultas marcadas) ---------- */

type Modelo = [dia: number, inicio: string, duracao: number, paciente: string, tipo: string, prof: string, status: StatusConsulta, sala: string];

const AC = 'Acompanhamento';
const AI = 'Avaliação inicial';
const AP = 'Avaliação psicológica';
const DV = 'Devolutiva';

/** Semana modelo (segunda a sábado). As outras semanas são variações desta. */
const SEMANA_MODELO: Modelo[] = [
  [0, '08:00', 60, 'marina-oliveira', AI, 'eduardo-costa', 'remarcado', 'Sala 01'],
  [0, '09:30', 30, 'lucas-henrique-alves', AC, 'eduardo-costa', 'confirmado', 'Sala 01'],
  [0, '11:30', 60, 'gabriel-santos-oliveira', AP, 'gustavo-lima', 'confirmado', 'Sala 03'],
  [0, '14:00', 60, 'pedro-miguel-rodrigues', AC, 'maria-oliveira', 'confirmado', 'Sala 02'],
  [0, '15:30', 60, 'beatriz-fernandes-lima', DV, 'nathalia-valente', 'pendente', 'Sala 04'],
  [0, '17:00', 30, 'laura-santos', AC, 'juliana-cabral', 'confirmado', 'Sala 01'],
  // Terça = "hoje" da demonstração: 10 consultas (6 confirmadas, 3 pendentes e 1 cancelada)
  [1, '08:00', 30, 'luana-lima', AC, 'juliana-cabral', 'confirmado', 'Sala 01'],
  [1, '09:30', 60, 'ana-clara-souza', AI, 'maria-oliveira', 'confirmado', 'Sala 02'],
  [1, '11:00', 30, 'lucas-junior', AC, 'juliana-cabral', 'pendente', 'Sala 01'],
  [1, '12:30', 30, 'luna', AC, 'juliana-cabral', 'confirmado', 'Sala 01'],
  [1, '13:30', 60, 'beatriz-fernandes-lima', DV, 'nathalia-valente', 'cancelado', 'Sala 04'],
  [1, '15:00', 30, 'laura-santos', AC, 'juliana-cabral', 'pendente', 'Sala 01'],
  [1, '16:00', 30, 'lucas-martins', AC, 'maria-oliveira', 'confirmado', 'Sala 02'],
  [1, '16:30', 30, 'gabriel-santos-oliveira', AC, 'gustavo-lima', 'confirmado', 'Sala 03'],
  [1, '17:00', 30, 'marina-oliveira', AC, 'eduardo-costa', 'pendente', 'Sala 01'],
  [1, '17:30', 30, 'pedro-miguel-rodrigues', AC, 'maria-oliveira', 'confirmado', 'Sala 02'],
  [2, '08:30', 60, 'luana-lima', AP, 'juliana-cabral', 'confirmado', 'Sala 01'],
  [2, '10:00', 60, 'lucas-henrique-alves', AC, 'eduardo-costa', 'pendente', 'Sala 02'],
  [2, '12:00', 30, 'ana-clara-souza', AC, 'maria-oliveira', 'confirmado', 'Sala 03'],
  [2, '14:30', 60, 'lucas-junior', AP, 'juliana-cabral', 'confirmado', 'Sala 01'],
  [2, '16:00', 30, 'laura-santos', AC, 'juliana-cabral', 'confirmado', 'Sala 01'],
  [3, '09:00', 30, 'pedro-miguel-rodrigues', AC, 'maria-oliveira', 'confirmado', 'Sala 02'],
  [3, '10:30', 60, 'marina-oliveira', AC, 'eduardo-costa', 'remarcado', 'Sala 01'],
  [3, '13:00', 30, 'beatriz-fernandes-lima', AC, 'nathalia-valente', 'confirmado', 'Sala 04'],
  [3, '15:00', 60, 'luna', AP, 'juliana-cabral', 'pendente', 'Sala 01'],
  [3, '17:00', 30, 'gabriel-santos-oliveira', AC, 'gustavo-lima', 'confirmado', 'Sala 03'],
  [4, '08:00', 60, 'lucas-martins', AC, 'maria-oliveira', 'confirmado', 'Sala 02'],
  [4, '10:00', 30, 'luana-lima', AC, 'juliana-cabral', 'pendente', 'Sala 01'],
  [4, '12:30', 60, 'ana-clara-souza', DV, 'maria-oliveira', 'confirmado', 'Sala 02'],
  [4, '14:30', 30, 'lucas-junior', AC, 'juliana-cabral', 'confirmado', 'Sala 01'],
  [5, '08:30', 60, 'lucas-henrique-alves', AC, 'eduardo-costa', 'pendente', 'Sala 01'],
  [5, '10:00', 60, 'laura-santos', AC, 'juliana-cabral', 'confirmado', 'Sala 01'],
  [5, '12:00', 30, 'pedro-miguel-rodrigues', AC, 'maria-oliveira', 'confirmado', 'Sala 02'],
];

function gerarAgenda(): Agendamento[] {
  const lista: Agendamento[] = [];
  for (let semana = -2; semana <= 3; semana++) {
    SEMANA_MODELO.forEach((m, i) => {
      // A semana inicial tem todas as consultas; nas outras, só uma parte (dados fictícios).
      if (semana !== 0 && (((i + semana) % 4) + 4) % 4 === 0) return;
      lista.push({
        id: semana === 0 ? `ag-${i + 1}` : `ag-${i + 1}-s${semana}`,
        data: somarDias(SEGUNDA_BASE, semana * 7 + m[0]), inicio: m[1], duracao: m[2], pacienteId: m[3], tipo: m[4],
        profissionalId: m[5], status: m[6], sala: m[7], atendimento: 'Presencial', local: 'Unidade Santa Maria',
      });
    });
  }
  return lista;
}

export const AGENDAMENTOS_INICIAIS: Agendamento[] = gerarAgenda();

/* ---------- Textos de exemplo dos documentos ---------- */

const bold = (t: string) => `<b>${t}</b>`;

function textoEvolucao(dataBR: string, sessao: number, modalidade: string): string {
  return (
    `<p>${bold('EVOLUÇÃO DO ATENDIMENTO PSICOLÓGICO')}<br>Data: ${dataBR}<br>Sessão: ${sessao}ª sessão<br>` +
    `Modalidade: ${modalidade}<br>Duração: 50 minutos</p><p><br></p>` +
    `<p>Objetivo da sessão: Realizar o acolhimento inicial, compreender a demanda apresentada e iniciar o levantamento do histórico e do contexto atual do paciente.<br>` +
    `Relato e conteúdos abordados: O paciente relatou preocupação frequente com questões profissionais e dificuldade para relaxar após a jornada de trabalho. Referiu que essas preocupações têm interferido em sua rotina de descanso. Foram explorados os contextos em que as dificuldades ocorrem e as estratégias utilizadas para lidar com elas.<br>` +
    `Observações clínicas pertinentes: O paciente apresentou comunicação verbal compreensível e participou das questões exploratórias propostas. As observações se restringem aos aspectos identificados durante o encontro.<br>` +
    `Intervenções realizadas: Foi realizada escuta clínica e acolhimento da demanda. Foram utilizadas perguntas abertas para explorar a percepção do paciente sobre suas dificuldades, os fatores associados e os impactos relatados em sua rotina.<br>` +
    `Resposta às intervenções: O paciente conseguiu apresentar exemplos de situações relacionadas à demanda e demonstrou disponibilidade para continuar explorando o tema.<br>` +
    `Avaliação da evolução: Por se tratar da sessão inicial, as informações são insuficientes para avaliar mudanças no quadro ou estabelecer conclusões clínicas. Recomenda-se aprofundar a compreensão da demanda e do contexto psicossocial nos próximos encontros.<br>` +
    `Planejamento para a próxima sessão: Dar continuidade ao levantamento da demanda, explorar o histórico das dificuldades, seus impactos no cotidiano e os recursos de enfrentamento disponíveis.<br>` +
    `Psicóloga: Dra. Maria<br>CRP: [Número do registro profissional]</p>`
  );
}

const TEXTO_LAUDO =
  `<p>${bold('LAUDO PSICOLÓGICO')}</p>` +
  `<p>${bold('1. IDENTIFICAÇÃO DO PACIENTE')}<br>Nome: Lucas Martins<br>Data de nascimento: 23/01/2009<br>Data da avaliação: 21/10/2026<br>Profissional responsável: Dra. Maria<br>Especialidade: Psicologia<br>CRP: 000000 — dado fictício</p>` +
  `<p>${bold('2. FINALIDADE DO LAUDO')}<br>O presente documento tem como finalidade registrar as informações obtidas durante a avaliação psicológica, descrevendo as condições observadas e as necessidades de acompanhamento.</p>` +
  `<p>${bold('3. HISTÓRICO CLÍNICO')}<br>O paciente relata episódios recorrentes de ansiedade, dificuldade de concentração e alterações no padrão de sono, com impactos em sua rotina profissional e pessoal. Refere que os sintomas se intensificam em períodos de maior demanda e estresse.</p>` +
  `<p>${bold('4. AVALIAÇÃO PSICOLÓGICA')}<br>Durante a consulta, o paciente apresentou-se consciente, orientado e colaborativo. Demonstrou discurso organizado, comunicação adequada e capacidade de relatar suas experiências de forma coerente.</p>`;

const TEXTO_LAUDO_ESCOLAR =
  `<p>${bold('LAUDO ESCOLAR')}</p><p>Destinado à instituição de ensino, com o objetivo de orientar adaptações pedagógicas conforme as necessidades do estudante Lucas Martins, em acompanhamento psicológico desde março de 2026.</p>` +
  `<p>Recomenda-se tempo adicional em avaliações, ambiente com poucos estímulos distratores e comunicação frequente entre escola, família e equipe clínica.</p>`;

const TEXTO_ENCAMINHAMENTO =
  `<p>${bold('ENCAMINHAMENTO PARA AVALIAÇÃO DE NEURODESENVOLVIMENTO E COGNIÇÃO')}<br>Paciente: Lucas Martins<br>Data de nascimento: 23/01/2009<br>Profissional solicitante: Dra. Maria<br>Especialidade: Psicologia<br>Data do encaminhamento: 21/10/2026<br>Profissional ou serviço de destino: Neuropsicologia</p>` +
  `<p>${bold('Motivo do encaminhamento')}<br>Encaminho o paciente para avaliação do neurodesenvolvimento e das funções cognitivas, considerando relatos de dificuldades de atenção, concentração, memória e organização de tarefas cotidianas.</p>` +
  `<p>${bold('Objetivo')}<br>Investigar o funcionamento cognitivo do paciente, incluindo atenção, memória, linguagem, raciocínio e funções executivas, a fim de contribuir para a compreensão de suas dificuldades e orientar possíveis intervenções.</p>` +
  `<p>${bold('Conduta solicitada')}<br>Solicito a realização da avaliação e, quando pertinente, o envio de devolutiva ao profissional solicitante, respeitando o sigilo profissional e as autorizações aplicáveis.</p>` +
  `<p>Atenciosamente,<br>Dra. Maria<br>Psicóloga<br>CRP: 000000 — dado fictício</p>`;

const TEXTO_AVALIACAO =
  `<p>${bold('AVALIAÇÃO PSICOLÓGICA')}</p>` +
  `<p>${bold('1. Identificação do paciente')}<br>Nome: Lucas Martins<br>Data de nascimento: 23/01/2009<br>Data da avaliação: 21/10/2026<br>Psicóloga responsável: Dra. Maria<br>CRP: 00/00000 — dado fictício</p>` +
  `<p>${bold('2. Objetivo da avaliação')}<br>Investigar aspectos emocionais, comportamentais e cognitivos do paciente, buscando compreender as necessidades psicológicas e identificar possíveis fatores que influenciam seu bem-estar e sua rotina.</p>` +
  `<p>${bold('3. Procedimentos realizados')}<br>A avaliação foi estruturada a partir de entrevista clínica, observação comportamental e levantamento de informações sobre histórico pessoal, familiar e escolar. A aplicação de instrumentos psicológicos foi registrada conforme os procedimentos efetivamente realizados.</p>` +
  `<p>${bold('4. Aspectos observados')}<br>Durante a situação fictícia de avaliação, o paciente apresentou comunicação adequada e demonstrou disponibilidade nas atividades propostas.</p>`;

const TEXTO_ATESTADO =
  `<p>${bold('ATESTADO DE COMPARECIMENTO')}</p><p>Atesto, para os devidos fins, que Lucas Martins compareceu a atendimento psicológico nesta data, no período das 08:00 às 08:50.</p><p>Brasília - DF, 21 de outubro de 2026.<br>Dra. Maria - Psicóloga<br>CRP: 000000 — dado fictício</p>`;


const TEXTO_PRESCRICAO =
  `<p>${bold('PRESCRIÇÃO MÉDICA')}<br>Paciente: Lucas Martins<br>Data: 02/08/2026</p>` +
  `<p>${bold('1. Sertralina 50 mg')}<br>Tomar 1 comprimido por via oral, uma vez ao dia, pela manhã, após o café.<br>Duração: 60 dias, com reavaliação na próxima consulta.</p>` +
  `<p>${bold('2. Clonazepam 0,5 mg (uso se necessário)')}<br>Tomar 1 comprimido por via oral, à noite, apenas em caso de insônia importante.<br>Máximo de 15 dias. Receita controlada: uso mediante prescrição médica.</p>` +
  `<p>Orientações: evitar bebidas alcoólicas durante o tratamento e comunicar qualquer efeito adverso.<br>Dr. José - Psiquiatra<br>CRM: 000000 — dado fictício</p>`;

const TEXTO_PRESCRICAO_ANTERIOR =
  `<p>${bold('PRESCRIÇÃO MÉDICA')}<br>Paciente: Lucas Martins<br>Data: 19/07/2026</p>` +
  `<p>${bold('Escitalopram 10 mg')}<br>Tomar 1 comprimido por via oral, uma vez ao dia, pela manhã.<br>Duração: 30 dias.</p>` +
  `<p>Dr. José - Psiquiatra<br>CRM: 000000 — dado fictício</p>`;

const TEXTO_ATESTADO_MEDICO =
  `<p>${bold('ATESTADO MÉDICO')}</p><p>Atesto, para os devidos fins, que o paciente Lucas Martins esteve sob cuidados médicos nesta data e necessita de afastamento das atividades escolares por 2 (dois) dias, a contar de 02/08/2026.</p>` +
  `<p>CID: F41.1 (informado com autorização do paciente ou responsável).<br>Brasília - DF, 02 de agosto de 2026.<br>Dr. José - Psiquiatra<br>CRM: 000000 — dado fictício</p>`;

const TEXTO_EXAMES =
  `<p>${bold('SOLICITAÇÃO DE EXAMES')}<br>Paciente: Lucas Martins<br>Data: 02/08/2026</p>` +
  `<p>${bold('Exames solicitados')}<br>1. Hemograma completo<br>2. TSH e T4 livre<br>3. Glicemia de jejum<br>4. Perfil lipídico</p>` +
  `<p>${bold('Justificativa clínica')}<br>Investigação de causas orgânicas associadas aos sintomas de ansiedade e acompanhamento do uso de medicação.<br>CID: F41.1</p>` +
  `<p>Dr. José - Psiquiatra<br>CRM: 000000 — dado fictício</p>`;

let contador = 0;
function doc(
  secao: Documento['secao'], titulo: string, dataSessao: string, conteudo: string,
  tipoAtendimento: Documento['tipoAtendimento'] = 'Presencial',
): Documento {
  contador += 1;
  return {
    id: `seed-${contador}`, pacienteId: 'lucas-martins', secao, titulo, dataSessao, tipoAtendimento,
    conteudo, atualizadoEm: `${dataSessao}T10:33:00`, anexos: [],
  };
}

export const DOCUMENTOS_INICIAIS: Documento[] = [
  doc('evolucoes', 'Evolução 02/08/2026', '2026-08-02', textoEvolucao('02/08/2026', 6, 'Presencial')),
  doc('evolucoes', 'Evolução 19/07/2026', '2026-07-19', textoEvolucao('19/07/2026', 5, 'Presencial')),
  doc('evolucoes', 'Evolução 05/07/2026', '2026-07-05', textoEvolucao('05/07/2026', 4, 'Online'), 'Online'),
  doc('evolucoes', 'Evolução 21/06/2026', '2026-06-21', textoEvolucao('21/06/2026', 3, 'Presencial')),
  doc('evolucoes', 'Evolução 07/06/2026', '2026-06-07', textoEvolucao('07/06/2026', 2, 'Presencial')),
  doc('evolucoes', 'Evolução 26/05/2026', '2026-05-26', textoEvolucao('26/05/2026', 1, 'Presencial')),
  doc('laudos', 'Laudo do Psicólogo', '2026-08-02', TEXTO_LAUDO),
  doc('laudos', 'Laudo Escolar', '2026-07-19', TEXTO_LAUDO_ESCOLAR),
  doc('encaminhamentos', 'Neurodesenvolvimento e Cognição', '2026-08-02', TEXTO_ENCAMINHAMENTO),
  doc('avaliacoes', 'Avaliação Psicológica', '2026-07-19', TEXTO_AVALIACAO),
  doc('atestados', 'Atestado de comparecimento', '2026-08-02', TEXTO_ATESTADO),
  doc('medicamentos', 'Sertralina 50 mg e Clonazepam 0,5 mg', '2026-08-02', TEXTO_PRESCRICAO),
  doc('medicamentos', 'Escitalopram 10 mg', '2026-07-19', TEXTO_PRESCRICAO_ANTERIOR),
  doc('atestadosMedicos', 'Atestado médico - afastamento escolar', '2026-08-02', TEXTO_ATESTADO_MEDICO),
  doc('exames', 'Exames laboratoriais', '2026-08-02', TEXTO_EXAMES),
];
