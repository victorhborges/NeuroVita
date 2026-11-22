import { NomeIcone } from './icone/icone';

/**
 * Texto da central de ajuda (igual para os quatro perfis).
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

/** Divide o texto nos trechos em negrito (entre **). */
export function partesComNegrito(texto: string): { texto: string; negrito: boolean }[] {
  return texto
    .split('**')
    .map((parte, i) => ({ texto: parte, negrito: i % 2 === 1 }))
    .filter((p) => p.texto !== '');
}
