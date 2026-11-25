import { Secao } from './modelos';

export interface ConfigSecao {
  id: Secao;
  /** "Laudos" */
  rotulo: string;
  /** "Laudo" */
  singular: string;
  botaoNovo: string;
  tituloCartao: string;
  botaoSalvar: string;
  dialogoTitulo: string;
  dialogoConfirmar: string;
  vazioEditor: string;
  vazioLista: string;
  informacao: string;
  /** 'leitura' = abre direto no texto (evoluções); 'lista' = tela com a lista de documentos. */
  modo: 'leitura' | 'lista';
  /** Filtros da tela de lista. */
  filtros: 'data-busca' | 'data-tipo-anexo';
  /** O editor mostra data, tipo de atendimento e anexos? */
  camposNoEditor: boolean;
  /** O editor mostra a lista da lateral? */
  listaNoEditor: boolean;
  tituloEditor?: string;
  galeria: {
    titulo: string;
    subtitulo: string;
    atalho: string;
    descricao: string;
    gruposPadrao: string[];
    subtituloGrupos?: string;
  };
}

export const SECOES: Record<Secao, ConfigSecao> = {
  evolucoes: {
    id: 'evolucoes',
    rotulo: 'Evoluções',
    singular: 'Evolução',
    botaoNovo: '+Nova Evolução',
    tituloCartao: 'Evolução do Paciente',
    botaoSalvar: 'Salvar Evolução',
    dialogoTitulo: 'Salvar Edição?',
    dialogoConfirmar: 'Salvar',
    vazioEditor: 'Comece a escrever uma nova evolução',
    vazioLista: 'Nenhuma evolução registrada ainda.',
    informacao: 'Aqui você encontra todas as evoluções registradas no acompanhamento do paciente.',
    modo: 'leitura',
    filtros: 'data-tipo-anexo',
    camposNoEditor: true,
    listaNoEditor: true,
    galeria: {
      titulo: 'Evoluções do paciente',
      subtitulo: 'Histórico de evolução clínica.',
      atalho: 'Evoluções',
      descricao: 'Registros do acompanhamento clínico.',
      gruposPadrao: ['Arquivos'],
    },
  },
  laudos: {
    id: 'laudos',
    rotulo: 'Laudos',
    singular: 'Laudo',
    botaoNovo: '+ Novo Laudo',
    tituloCartao: 'Laudos do Paciente',
    botaoSalvar: 'Salvar Laudo',
    dialogoTitulo: 'Confirmar Laudo?',
    dialogoConfirmar: 'Confirmar',
    vazioEditor: 'Comece a escrever seu laudo',
    vazioLista: 'Nenhum laudo cadastrado ainda.',
    informacao: 'Os laudos devem ser elaborados com base em dados clínicos, entrevistas, testes e observações do acompanhamento.',
    modo: 'lista',
    filtros: 'data-busca',
    camposNoEditor: false,
    listaNoEditor: false,
    galeria: {
      titulo: 'Laudos do paciente',
      subtitulo: 'Laudos e documentos clínicos.',
      atalho: 'Laudos',
      descricao: 'Documentos clínicos elaborados para o paciente.',
      gruposPadrao: ['Arquivos'],
    },
  },
  encaminhamentos: {
    id: 'encaminhamentos',
    rotulo: 'Encaminhamentos',
    singular: 'Encaminhamento',
    botaoNovo: '+ Novo Encaminhamento',
    tituloCartao: 'Encaminhamentos do Paciente',
    botaoSalvar: 'Salvar',
    dialogoTitulo: 'Confirmar Edição?',
    dialogoConfirmar: 'Confirmar',
    vazioEditor: 'Comece a escrever seu encaminhamento',
    vazioLista: 'Nenhum encaminhamento cadastrado ainda.',
    informacao: 'Os encaminhamentos devem informar o motivo, o objetivo e o serviço ou profissional de destino.',
    modo: 'lista',
    filtros: 'data-tipo-anexo',
    camposNoEditor: true,
    listaNoEditor: true,
    galeria: {
      titulo: 'Encaminhamentos do paciente',
      subtitulo: 'Encaminhamentos e documentos relacionados.',
      atalho: 'Encaminhamentos',
      descricao: 'Registros de encaminhamentos clínicos.',
      gruposPadrao: ['Arquivos'],
    },
  },
  avaliacoes: {
    id: 'avaliacoes',
    rotulo: 'Avaliações Psicológicas',
    singular: 'Avaliação Psicológica',
    botaoNovo: '+ Nova Avaliação',
    tituloCartao: 'Avaliações Psicológicas do Paciente',
    botaoSalvar: 'Salvar',
    dialogoTitulo: 'Confirmar Edição?',
    dialogoConfirmar: 'Confirmar',
    vazioEditor: 'Comece a escrever sua avaliação',
    vazioLista: 'Nenhuma avaliação cadastrada ainda.',
    informacao: 'As avaliações devem registrar os procedimentos, instrumentos utilizados e os resultados obtidos.',
    modo: 'lista',
    filtros: 'data-tipo-anexo',
    camposNoEditor: true,
    listaNoEditor: true,
    galeria: {
      titulo: 'Avaliações psicológicas do paciente',
      subtitulo: 'Avaliações e documentos relacionados.',
      atalho: 'Avaliações Psicológicas',
      descricao: 'Registros das avaliações psicológicas.',
      gruposPadrao: ['Arquivos'],
    },
  },
  atestados: {
    id: 'atestados',
    rotulo: 'Atestados',
    singular: 'Atestado',
    botaoNovo: '+ Novo Atestado',
    tituloCartao: 'Atestados do Paciente',
    botaoSalvar: 'Salvar',
    dialogoTitulo: 'Confirmar Edição?',
    dialogoConfirmar: 'Confirmar',
    vazioEditor: 'Comece a escrever seu atestado',
    vazioLista: 'Nenhum atestado cadastrado ainda.',
    informacao: 'Os atestados devem conter data, finalidade e identificação do profissional responsável.',
    modo: 'lista',
    filtros: 'data-tipo-anexo',
    camposNoEditor: true,
    listaNoEditor: true,
    galeria: {
      titulo: 'Atestados do paciente',
      subtitulo: 'Atestados e documentos relacionados.',
      atalho: 'Atestados',
      descricao: 'Atestados emitidos para o paciente.',
      gruposPadrao: ['Arquivos'],
    },
  },
  medicamentos: {
    id: 'medicamentos',
    rotulo: 'Medicamentos e prescrições',
    singular: 'Prescrição',
    botaoNovo: '+ Nova Prescrição',
    tituloCartao: 'Medicamentos e prescrições do Paciente',
    botaoSalvar: 'Salvar',
    dialogoTitulo: 'Confirmar Edição?',
    dialogoConfirmar: 'Confirmar',
    vazioEditor: 'Comece a escrever sua prescrição',
    vazioLista: 'Nenhuma prescrição cadastrada ainda.',
    informacao: 'As prescrições devem informar o medicamento, a dose, a frequência e a duração do tratamento.',
    modo: 'lista',
    filtros: 'data-tipo-anexo',
    camposNoEditor: true,
    listaNoEditor: true,
    galeria: {
      titulo: 'Medicamentos e prescrições do paciente',
      subtitulo: 'Histórico de prescrições.',
      atalho: 'Medicamentos e prescrições',
      descricao: 'Medicamentos e prescrições registrados.',
      gruposPadrao: ['Arquivos'],
    },
  },
  atestadosMedicos: {
    id: 'atestadosMedicos',
    rotulo: 'Atestados Médicos',
    singular: 'Atestado Médico',
    botaoNovo: '+ Novo Atestado',
    tituloCartao: 'Atestados Médicos do Paciente',
    botaoSalvar: 'Salvar',
    dialogoTitulo: 'Confirmar Edição?',
    dialogoConfirmar: 'Confirmar',
    vazioEditor: 'Comece a escrever seu atestado',
    vazioLista: 'Nenhum atestado cadastrado ainda.',
    informacao: 'Os atestados devem conter data, finalidade, período de afastamento (quando houver) e a identificação do médico responsável.',
    modo: 'lista',
    filtros: 'data-tipo-anexo',
    camposNoEditor: true,
    listaNoEditor: true,
    galeria: {
      titulo: 'Atestados médicos do paciente',
      subtitulo: 'Atestados médicos e documentos relacionados.',
      atalho: 'Atestados Médicos',
      descricao: 'Atestados médicos emitidos para o paciente.',
      gruposPadrao: ['Arquivos'],
    },
  },
  exames: {
    id: 'exames',
    rotulo: 'Solicitar Exames',
    singular: 'Solicitação de Exames',
    botaoNovo: '+ Nova Solicitação',
    tituloCartao: 'Solicitações de Exames do Paciente',
    botaoSalvar: 'Salvar',
    dialogoTitulo: 'Confirmar Edição?',
    dialogoConfirmar: 'Confirmar',
    vazioEditor: 'Comece a escrever sua solicitação',
    vazioLista: 'Nenhuma solicitação cadastrada ainda.',
    informacao: 'Informe os exames solicitados, a justificativa clínica e o diagnóstico (CID) relacionado.',
    modo: 'lista',
    filtros: 'data-tipo-anexo',
    camposNoEditor: true,
    listaNoEditor: true,
    galeria: {
      titulo: 'Solicitações de exames do paciente',
      subtitulo: 'Solicitações e resultados de exames.',
      atalho: 'Solicitar Exames',
      descricao: 'Exames solicitados e resultados registrados.',
      gruposPadrao: ['Arquivos'],
    },
  },
};

/** Seções do prontuário de cada perfil, na ordem em que aparecem. */
export const ORDEM_SECOES_POR_PERFIL: Record<'psicologo' | 'psiquiatra', Secao[]> = {
  psicologo: ['evolucoes', 'laudos', 'encaminhamentos', 'avaliacoes', 'atestados'],
  psiquiatra: ['evolucoes', 'laudos', 'medicamentos', 'atestadosMedicos', 'exames'],
};

export function secoesDoPerfil(perfil: string): Secao[] {
  return perfil === 'psiquiatra' ? ORDEM_SECOES_POR_PERFIL.psiquiatra : ORDEM_SECOES_POR_PERFIL.psicologo;
}

/** A seção da URL, se ela existir no prontuário desse perfil (senão, null). */
export function secaoDoPerfil(valor: string | undefined, perfil: string): Secao | null {
  return ehSecao(valor) && secoesDoPerfil(perfil).includes(valor) ? valor : null;
}

export function ehSecao(valor: string | undefined): valor is Secao {
  return !!valor && valor in SECOES;
}
