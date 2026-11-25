import { NomeIcone } from './icone/icone';
import { Perfil } from './perfil';

/**
 * Central de ajuda da SECRETÁRIA: uma página por tópico (/secretaria/ajuda/:topico).
 * Cada perfil tem a sua ajuda (psicólogo, psiquiatra e administrador estão mais abaixo);
 * só o "Fale conosco" e o "Suporte" são iguais para todos.
 * Nos passos, o que fica entre ** ** aparece em negrito.
 *
 * As capturas de tela ficam em public/img/ajuda/ (nomes abaixo). Enquanto o arquivo não existir,
 * a tela mostra um espaço reservado no lugar da imagem.
 */
export interface BlocoAjuda {
  passos: string[];
  /** Número do primeiro passo (para continuar a contagem do bloco anterior). */
  numeroInicial?: number;
  /** Quadro roxo com uma dica, embaixo dos passos. */
  dica?: string;
  imagem: string;
  /** Também serve de texto alternativo e de legenda do espaço reservado. */
  descricaoImagem: string;
  /** Mostra a imagem à esquerda e os passos à direita. */
  invertido?: boolean;
}

export interface TopicoAjuda {
  id: string;
  cartao: { titulo: string; descricao: string; icone: NomeIcone };
  pagina: { titulo: string; subtitulo: string; bannerTitulo: string; bannerTexto: string };
  blocos: BlocoAjuda[];
}

export const TOPICOS_AJUDA: TopicoAjuda[] = [
  {
    id: 'cadastro-e-perfis',
    cartao: { titulo: 'Cadastro e Perfis', descricao: 'Como cadastrar pacientes, profissionais e secretárias.', icone: 'usuario' },
    pagina: {
      titulo: 'Cadastro e Perfis',
      subtitulo: 'Nesta página, você aprenderá a como realizar o cadastro de usuários e entender os perfis disponíveis no sistema',
      bannerTitulo: 'Como funciona o cadastro?',
      bannerTexto: 'Consulte os passos para cadastrar pacientes e profissionais.',
    },
    blocos: [
      {
        passos: [
          'Acesse o menu Pacientes e clique em **Novo Paciente.**',
          'Preencha os **Dados Pessoais** do paciente, com nome, data de nascimento, CPF, endereço e status.',
          'Informe os dados do **Responsável** como nome, CPF, telefone e grau de parentesco.',
          'Clique em **Cadastrar** para salvar as informações.',
        ],
        dica: '**Dica:** O status pode ser Ativo ou Inativo, conforme a situação do paciente.',
        imagem: 'img/ajuda/cadastro-paciente.png',
        descricaoImagem: 'Tela Cadastrar Novo Paciente',
      },
    ],
  },
  {
    id: 'agenda-principal',
    cartao: { titulo: 'Agenda Principal', descricao: 'Visualizar todos os agendamentos.', icone: 'calendario' },
    pagina: {
      titulo: 'Agenda principal',
      subtitulo: 'Aprenda a gerenciar a agenda de todas as consultas marcadas.',
      bannerTitulo: 'Como funciona a agenda principal?',
      bannerTexto: 'Consulte os passos para visualizar todos os agendamentos.',
    },
    blocos: [
      {
        passos: [
          'No topo da **Agenda**, use o botão **Hoje** e as setas para escolher o período que deseja visualizar.',
          'Cada quadro colorido é uma consulta: a cor mostra se ela está **confirmada**, **a confirmar**, **cancelada** ou **remarcada**.',
          'Ao lado, o **Resumo da agenda** mostra o total de consultas por situação e o **Próximo atendimento**.',
        ],
        imagem: 'img/ajuda/agenda.png',
        descricaoImagem: 'Tela Agenda',
      },
    ],
  },
  {
    id: 'prontuarios',
    cartao: { titulo: 'Prontuários', descricao: 'Como visualizar e exportar relatórios de avaliações.', icone: 'arquivo-texto' },
    pagina: {
      titulo: 'Pacientes',
      subtitulo: 'Aprenda a visualizar o prontuário de cada paciente',
      bannerTitulo: 'Como funciona o histórico?',
      bannerTexto: 'Consulte todas as funcionalidades na página de paciente.',
    },
    blocos: [
      {
        passos: [
          'No menu lateral esquerdo, clique na opção "**Pacientes**" para visualizar os pacientes cadastrados no sistema.',
          'Pacientes cadastrados: na parte superior, o sistema apresenta a quantidade total de pacientes registrados, permitindo acompanhar o número de cadastros.',
          'No perfil de cada paciente está o histórico dele, como o status de ativo, o **prontuário** e as ações de poder editar ou excluir quando ele estiver inativo.',
        ],
        imagem: 'img/ajuda/lista-pacientes.png',
        descricaoImagem: 'Tela Pacientes',
      },
    ],
  },
  {
    id: 'agendamentos',
    cartao: { titulo: 'Agendamentos', descricao: 'Como marcar e visualizar as consultas.', icone: 'calendario' },
    pagina: {
      titulo: 'Agendamentos',
      subtitulo: 'Aprenda a marcar e visualizar as consultas do paciente.',
      bannerTitulo: 'Como marcar uma consulta?',
      bannerTexto: 'Consulte os passos para marcar e visualizar as consultas.',
    },
    blocos: [
      {
        passos: [
          'Nos campos de **Paciente**, **Responsável** e **Profissional** é possível selecionar o paciente que receberá o atendimento; o responsável é preenchido automaticamente.',
          'O campo do **tipo de atendimento** é para selecionar a modalidade ou categoria do atendimento que será realizado.',
          'A **disponibilidade** mostra os dias em que o profissional pode atender, enquanto os **horários disponíveis** permitem selecionar o melhor horário para a consulta.',
        ],
        imagem: 'img/ajuda/agendamento.png',
        descricaoImagem: 'Tela Gerenciamento de consultas',
      },
    ],
  },
];

/**
 * Ajuda dos perfis com cartões que se abrem na própria tela (PSIQUIATRA, PSICÓLOGO e ADMINISTRADOR):
 * passos + captura de tela, como no Figma. As capturas ficam em public/img/ajuda/<perfil>/.
 */
export interface TopicoAjudaCartao {
  id: string;
  titulo: string;
  descricao: string;
  icone: NomeIcone;
  passos: string[];
  imagem: string;
  descricaoImagem: string;
  /** Captura de tela à esquerda e passos à direita. */
  imagemAEsquerda: boolean;
  /** Em qual linha de cartões (1 ou 2) o conteúdo se abre, como no Figma. */
  linhaDoConteudo: 1 | 2;
}

export const TOPICOS_AJUDA_PSIQUIATRA: TopicoAjudaCartao[] = [
  {
    id: 'pagina-de-inicio',
    titulo: 'Página de início',
    descricao: 'Como cadastrar pacientes, profissionais e secretárias.',
    icone: 'usuario',
    passos: [
      'Na página de início ficam os campos de **Horário e Paciente**: você visualiza os horários agendados e identifica quem será atendido ao longo do dia.',
      'Na **Agenda do dia** estão todas as informações da consulta, como telefone, profissional responsável e status (confirmada ou pendente).',
    ],
    imagem: 'img/ajuda/psiquiatra/inicio.png',
    descricaoImagem: 'Tela de início do psiquiatra',
    imagemAEsquerda: false,
    linhaDoConteudo: 1,
  },
  {
    id: 'prontuario-do-paciente',
    titulo: 'Prontuário do paciente',
    descricao: 'Visualizar todo o histórico do paciente.',
    icone: 'arquivo-texto',
    passos: [
      'No **prontuário** de cada paciente estão o histórico, as informações pessoais e as do responsável.',
      'Nos campos ao lado é possível consultar as **Evoluções**, os **Laudos**, os **Medicamentos e prescrições**, os **Atestados médicos** e os pedidos de **exames** do paciente.',
    ],
    imagem: 'img/ajuda/psiquiatra/prontuario.png',
    descricaoImagem: 'Tela Prontuário do paciente',
    imagemAEsquerda: false,
    linhaDoConteudo: 2,
  },
  {
    id: 'pacientes',
    titulo: 'Pacientes',
    descricao: 'Como visualizar e exportar relatórios de avaliações.',
    icone: 'arquivo-texto',
    passos: [
      'Na página de **Pacientes** ficam o total de pacientes cadastrados na clínica e um resumo dos pacientes em tratamento.',
      'Na lista, veja a **última consulta** e o **status** de cada paciente. Use a busca no topo para localizar um paciente pelo nome.',
      'Clique em **Ver prontuário** para abrir o histórico completo do paciente.',
    ],
    imagem: 'img/ajuda/psiquiatra/pacientes.png',
    descricaoImagem: 'Tela Pacientes',
    imagemAEsquerda: true,
    linhaDoConteudo: 2,
  },
  {
    id: 'agendamentos',
    titulo: 'Agendamentos',
    descricao: 'Como marcar e visualizar as consultas.',
    icone: 'calendario',
    passos: [
      'A **agenda** permite visualizar e organizar os atendimentos por dias e horários, além de consultar informações dos pacientes e profissionais.',
      'Use o botão **Hoje** e as setas para trocar o período. A cor de cada quadro mostra se a consulta está confirmada, a confirmar, cancelada ou remarcada.',
      'O **Resumo da agenda** mostra o total por situação e o **Próximo atendimento**.',
    ],
    imagem: 'img/ajuda/psiquiatra/agenda.png',
    descricaoImagem: 'Tela Agenda',
    imagemAEsquerda: true,
    linhaDoConteudo: 2,
  },
];

/**
 * Ajuda do PSICÓLOGO. Mesmo formato do psiquiatra, com o texto das telas do psicólogo.
 * As capturas em public/img/ajuda/psicologo/ foram recortadas do Figma; se tiver prints das telas em 1280x800, é só substituir mantendo os nomes.
 */
export const TOPICOS_AJUDA_PSICOLOGO: TopicoAjudaCartao[] = [
  {
    id: 'pagina-de-inicio',
    titulo: 'Página de início',
    descricao: 'Como cadastrar pacientes, profissionais e secretárias.',
    icone: 'usuario',
    passos: [
      'Na página de início tem os campos de **Horário e Paciente**: você pode visualizar os horários agendados e identificar quem será atendido ao longo do dia.',
      '**Na agenda do dia** tem todas as informações da consulta, como telefone, profissional responsável, tipo (presencial ou online) e status (confirmada ou pendente).',
    ],
    imagem: 'img/ajuda/psicologo/inicio.png',
    descricaoImagem: 'Tela de início do psicólogo',
    imagemAEsquerda: false,
    linhaDoConteudo: 1,
  },
  {
    id: 'prontuario-do-paciente',
    titulo: 'Prontuário do paciente',
    descricao: 'Visualizar todo o histórico do paciente.',
    icone: 'arquivo-texto',
    passos: [
      'No **prontuário** de cada paciente terá seu histórico, suas informações pessoais e as do responsável.',
      'Nessa seção, você poderá acessar a **Documentação**, as **Evoluções**, os **Laudos**, os **Encaminhamentos**, as **Avaliações** e os **Atestados** do paciente.',
    ],
    imagem: 'img/ajuda/psicologo/prontuario.png',
    descricaoImagem: 'Tela Prontuário do paciente',
    imagemAEsquerda: false,
    linhaDoConteudo: 2,
  },
  {
    id: 'pacientes',
    titulo: 'Pacientes',
    descricao: 'Como visualizar e exportar relatórios de avaliações.',
    icone: 'arquivo-texto',
    passos: [
      'Na página de **Pacientes** tem os campos de total de pacientes cadastrados na clínica e um resumo de pacientes em tratamento.',
      'Na lista, veja a **última consulta** e o **status** de cada paciente. Use a busca no topo para localizar um paciente pelo nome.',
      'Clique em **Ver prontuário** para abrir o histórico completo do paciente.',
    ],
    imagem: 'img/ajuda/psicologo/pacientes.png',
    descricaoImagem: 'Tela Pacientes',
    imagemAEsquerda: true,
    linhaDoConteudo: 2,
  },
  {
    id: 'agendamentos',
    titulo: 'Agendamentos',
    descricao: 'Como marcar e visualizar as consultas.',
    icone: 'calendario',
    passos: [
      'A **agenda** permite visualizar e organizar os atendimentos por dias e horários, consultar informações dos pacientes e profissionais.',
      'Use o botão **Hoje** e as setas para trocar o período. A cor de cada quadro mostra se a consulta está confirmada, a confirmar, cancelada ou remarcada.',
      'O **Resumo da agenda** mostra o total por situação e o **Próximo atendimento**.',
    ],
    imagem: 'img/ajuda/psicologo/agenda.png',
    descricaoImagem: 'Tela Agenda',
    imagemAEsquerda: true,
    linhaDoConteudo: 2,
  },
];

/**
 * Ajuda do ADMINISTRADOR: um cartão só, que se abre na própria tela.
 * Captura em public/img/ajuda/admin/ (recortada do Figma).
 */
export const TOPICOS_AJUDA_ADMIN: TopicoAjudaCartao[] = [
  {
    id: 'inicio-cadastros-e-agenda',
    titulo: 'Inicio, Cadastros e Agenda',
    descricao: 'Como cadastrar pacientes, profissionais e secretárias.',
    icone: 'usuario',
    passos: [
      'Acessando o **Gerenciar Profissional:** permite acessar o cadastro de novos profissionais, editar informações e gerenciar os profissionais já registrados na clínica. **Gerenciar Secretária:** permite cadastrar novas secretárias, editar seus dados e gerenciar os acessos ao sistema. A agenda do dia também apresenta os horários, pacientes, profissionais responsáveis e status das consultas.',
    ],
    imagem: 'img/ajuda/admin/inicio.png',
    descricaoImagem: 'Tela de início do administrador',
    imagemAEsquerda: false,
    linhaDoConteudo: 1,
  },
];

/** Perfis que usam cartões que se abrem na própria tela. A secretária usa uma página por tópico (TOPICOS_AJUDA). */
export const AJUDA_CARTOES_POR_PERFIL: Partial<Record<Perfil, TopicoAjudaCartao[]>> = {
  psiquiatra: TOPICOS_AJUDA_PSIQUIATRA,
  psicologo: TOPICOS_AJUDA_PSICOLOGO,
  admin: TOPICOS_AJUDA_ADMIN,
};

/** Divide o texto nos trechos em negrito (entre **). */
export function partesComNegrito(texto: string): { texto: string; negrito: boolean }[] {
  return texto
    .split('**')
    .map((parte, i) => ({ texto: parte, negrito: i % 2 === 1 }))
    .filter((p) => p.texto !== '');
}
