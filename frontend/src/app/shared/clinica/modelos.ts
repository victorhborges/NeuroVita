export type StatusPaciente = 'acompanhamento' | 'aguardando' | 'inativo';
export type Secao =
  | 'evolucoes' | 'laudos' | 'encaminhamentos' | 'avaliacoes' | 'atestados'
  | 'medicamentos' | 'atestadosMedicos' | 'exames';
export type TipoAtendimento = 'Presencial' | 'Online';
export type StatusConsulta = 'confirmado' | 'pendente' | 'remarcado' | 'cancelado';

export interface Responsavel {
  id?: string;
  nome: string;
  relacao: string;
  telefone: string;
  endereco: string;
  email?: string;
  /** 'aaaa-mm-dd' */
  nascimento?: string;
  cpf?: string;
}

export interface PlanoSaude {
  possui: boolean;
  nome: string;
  carteirinha: string;
  /** 'aaaa-mm-dd' (pode ficar vazio) */
  validade: string;
}

export interface Consulta {
  /** 'aaaa-mm-dd' */
  data: string;
  profissional: string;
  resumo: string;
}

export interface Paciente {
  id: string;
  responsavelId?: string;
  /** Número que aparece como "ID: 00123". */
  codigo: string;
  nome: string;
  /** 'aaaa-mm-dd' */
  nascimento: string;
  telefone: string;
  email: string;
  cpf: string;
  endereco: string;
  status: StatusPaciente;
  /** Texto mostrado no prontuário (ex.: "Em andamento"). */
  situacao: string;
  ultimaConsulta: string | null;
  /** Profissional responsável pelo paciente (null enquanto não houver). */
  profissionalId: string | null;
  responsavel: Responsavel;
  responsaveis: Responsavel[];
  plano: PlanoSaude;
  diagnostico: { principal: string; cid: string; inicioTratamento: string | null };
  consultas: Consulta[];
  foto?: string;
}

/** O que o formulário de cadastro envia. */
export interface DadosPaciente {
  nome: string;
  nascimento: string;
  email: string;
  cpf: string;
  telefone: string;
  endereco?: string;
  responsavel: Responsavel;
  responsaveis?: Responsavel[];
  plano: PlanoSaude;
  ativo: boolean;
}

/** Quando o profissional atende. */
export interface Disponibilidade {
  /** 0 = segunda ... 5 = sábado */
  dias: number[];
  /** Faixas de horário ('HH:MM'). */
  faixas: { inicio: string; fim: string }[];
  online: boolean;
  presencial: boolean;
}

export interface Profissional {
  id: string;
  codigo: string;
  nome: string;
  /** Vazio quando o cadastro não informa. */
  tratamento: 'Dr.' | 'Dra.' | '';
  /** "Psicóloga", "Psiquiatra", "Psicóloga infantil"... */
  especialidade: string;
  tipo: 'psicologo' | 'psiquiatra';
  /** "CRP 06/123456" */
  registro: string;
  nascimento: string;
  telefone: string;
  ativo: boolean;
  cpf: string;
  email: string;
  anoConclusao: number | null;
  disponibilidade: Disponibilidade;
  /** E-mail que o profissional usa para entrar no sistema. */
  emailAcesso: string;
}

/** O que o formulário de cadastro de profissional envia. */
export interface DadosProfissional {
  nome: string;
  nascimento: string;
  email: string;
  cpf: string;
  telefone: string;
  tipo: 'psicologo' | 'psiquiatra';
  /** Só o número (o CRP/CRM é colocado pelo sistema). */
  registro: string;
  anoConclusao: number;
  disponibilidade: Disponibilidade;
  ativo: boolean;
  emailAcesso: string;
  senhaProvisoria?: string;
  enviarEmailBoasVindas?: boolean;
}

export interface Secretaria {
  id: string;
  codigo: string;
  nome: string;
  nascimento: string;
  telefone: string;
  email: string;
  cpf: string;
  endereco: string;
  cargo: string;
  /** 'aaaa-mm-dd' */
  admissao: string;
  ativo: boolean;
  emailAcesso: string;
  foto?: string;
}

export type DadosSecretaria = Omit<Secretaria, 'id' | 'codigo' | 'foto'> & {
  senhaProvisoria?: string;
  enviarEmailBoasVindas?: boolean;
};

/** Uma consulta marcada na agenda. */
export interface Agendamento {
  id: string;
  /** 'aaaa-mm-dd' */
  data: string;
  /** 'HH:MM' */
  inicio: string;
  /** minutos (múltiplos de 30) */
  duracao: number;
  pacienteId: string;
  profissionalId: string;
  /** "Acompanhamento", "Devolutiva", "Avaliação inicial"... */
  tipo: string;
  atendimento: TipoAtendimento;
  sala: string;
  local: string;
  status: StatusConsulta;
}

export interface Anexo {
  nome: string;
  /** URL local ou data URL retornada pelo servidor. */
  url: string;
}

export interface Documento {
  id: string;
  pacienteId: string;
  secao: Secao;
  /** Identifica a API de origem sem alterar IDs dos documentos legados. */
  origem?: 'documento-clinico' | 'evolucao' | 'prescricao' | 'exame';
  subtitulo?: string;
  profissionalId?: string;
  titulo: string;
  /** 'aaaa-mm-dd' */
  dataSessao: string;
  tipoAtendimento: TipoAtendimento | null;
  /** Texto formatado (HTML). */
  conteudo: string;
  atualizadoEm: string;
  anexos: Anexo[];
  tipoEvolucao?: string;
  medicamentos?: {
    medicamento: string;
    dosagem?: string | null;
    posologia?: string | null;
    frequencia?: string | null;
    duracao?: string | null;
  }[];
  tipoExame?: string | null;
  resultado?: string | null;
  observacoes?: string | null;
  ativo?: boolean;
  /** Presente quando o documento foi enviado como arquivo (upload). */
  arquivo?: Anexo;
}
