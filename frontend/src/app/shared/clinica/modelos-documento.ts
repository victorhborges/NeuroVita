import { Secao } from './modelos';

/** Dados do paciente usados para preencher o modelo. */
export interface DadosModelo {
  nome: string;
  idade: number;
  /** Data de hoje, já formatada (ex.: 09/11/2026). */
  data: string;
}

const LINHA = '______________________________';

/** Cada linha vira um parágrafo. "## texto" fica em negrito; linha vazia vira espaço. */
function emHtml(linhas: string[]): string {
  const esc = (t: string): string => t.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  return linhas
    .map((l) => (l === '' ? '<p><br></p>' : l.startsWith('## ') ? `<p><strong>${esc(l.slice(3))}</strong></p>` : `<p>${esc(l)}</p>`))
    .join('');
}

const MODELOS: Partial<Record<Secao, (d: DadosModelo) => string[]>> = {
  atestados: (d) => [
    '## ATESTADO PSICOLÓGICO',
    '## IDENTIFICAÇÃO',
    `Nome do paciente: ${d.nome}`,
    `Idade: ${d.idade} anos`,
    `Data: ${d.data}`,
    '',
    '## DECLARAÇÃO',
    `Atesto, para os devidos fins, que ${d.nome}, de ${d.idade} anos, esteve em atendimento psicológico na presente data, para acompanhamento e atenção à sua saúde emocional.`,
    'O atendimento teve como finalidade oferecer acolhimento psicológico, escuta profissional e suporte relacionado aos aspectos emocionais e comportamentais do paciente.',
    'O presente documento é emitido mediante solicitação do interessado ou de seu responsável legal, para fins de comprovação de comparecimento.',
    '',
    `Local e data: ${LINHA}`,
    `Psicólogo(a) responsável: ${LINHA}`,
    `CRP: ${LINHA}`,
    `Assinatura e carimbo: ${LINHA}`,
  ],
  avaliacoes: (d) => [
    '## AVALIAÇÃO PSICOLÓGICA',
    '## 1. IDENTIFICAÇÃO',
    `Nome: ${d.nome}`,
    `Idade: ${d.idade} anos`,
    `Data: ${d.data}`,
    'Tipo de avaliação: Avaliação psicológica individual.',
    '',
    '## 2. OBJETIVO DA AVALIAÇÃO',
    'A avaliação tem como objetivo compreender aspectos emocionais, cognitivos, comportamentais e sociais do paciente, considerando as características individuais e os desafios relacionados à fase da adolescência.',
    '',
    '## 3. ASPECTOS OBSERVADOS',
    'Durante o processo avaliativo, devem ser investigados aspectos relacionados à comunicação, interação social, expressão emocional, autoestima, concentração e comportamento diante de diferentes situações.',
    'Também devem ser considerados fatores familiares, escolares e sociais que possam influenciar o desenvolvimento psicológico do adolescente.',
    '',
    '## 4. ASPECTOS EMOCIONAIS E COMPORTAMENTAIS',
    'A investigação psicológica busca identificar como o paciente reconhece, interpreta e expressa suas emoções, bem como suas estratégias para lidar com situações de pressão, frustração e conflitos interpessoais.',
    '',
    '## 5. CONSIDERAÇÕES FINAIS',
    'Registrar aqui as conclusões da avaliação e as orientações para o acompanhamento.',
    '',
    `Psicólogo(a) responsável: ${LINHA}`,
    `CRP: ${LINHA}`,
  ],
  encaminhamentos: (d) => [
    '## ENCAMINHAMENTO PSICOLÓGICO',
    '## 1. IDENTIFICAÇÃO DO PACIENTE',
    `Nome: ${d.nome}`,
    `Idade: ${d.idade} anos`,
    `Data: ${d.data}`,
    '',
    '## 2. MOTIVO DO ENCAMINHAMENTO',
    `O paciente ${d.nome}, de ${d.idade} anos, é encaminhado para avaliação e acompanhamento especializado, com o objetivo de aprofundar a compreensão de aspectos relacionados ao seu desenvolvimento emocional, comportamental e social.`,
    'Considera-se importante oferecer suporte profissional para trabalhar questões relacionadas à autoestima, ao reconhecimento e à expressão das emoções, ao desenvolvimento da autonomia e às relações interpessoais.',
    '',
    '## 3. OBJETIVO DO ENCAMINHAMENTO',
    'Recomenda-se acompanhamento psicológico especializado, visando proporcionar um espaço de acolhimento, escuta qualificada e orientação, contribuindo para o fortalecimento das suas habilidades socioemocionais e para a promoção da sua saúde mental.',
    '',
    '## 4. CONSIDERAÇÕES FINAIS',
    'Solicita-se avaliação do profissional responsável e, se necessário, a elaboração de um plano de acompanhamento adequado às necessidades identificadas durante o atendimento.',
    'Atenciosamente,',
  ],
  laudos: (d) => [
    '## LAUDO PSICOLÓGICO',
    '## 1. IDENTIFICAÇÃO',
    `Nome: ${d.nome}`,
    `Idade: ${d.idade} anos`,
    '',
    'Solicitante: Responsável legal',
    '',
    'Finalidade: Avaliação e descrição das condições psicológicas do paciente.',
    '',
    '## 2. DESCRIÇÃO DA DEMANDA',
    `O presente documento tem como finalidade apresentar informações referentes ao acompanhamento e à avaliação psicológica de ${d.nome}, considerando aspectos emocionais, comportamentais, sociais e familiares, relacionados ao seu desenvolvimento durante a adolescência.`,
    '## 3. PROCEDIMENTOS REALIZADOS',
    'Para a elaboração de uma avaliação psicológica, poderão ser utilizadas entrevistas clínicas, observação comportamental, escuta psicológica e instrumentos de avaliação devidamente reconhecidos e adequados à faixa etária do paciente.',
    '## 4. ANÁLISE PSICOLÓGICA',
    'Durante o acompanhamento, busca-se compreender o funcionamento emocional e comportamental do paciente, considerando suas experiências pessoais, relações interpessoais e contexto familiar.',
    'Os aspectos relacionados à autoestima, à expressão dos sentimentos, à autonomia e ao enfrentamento de situações do cotidiano são observados ao longo do processo.',
    '## 5. CONCLUSÃO',
    'Registrar aqui a conclusão do laudo e as recomendações.',
    '',
    `Local e data: ${LINHA}`,
    `Psicólogo(a) responsável: ${LINHA}`,
    `CRP: ${LINHA}`,
  ],
  evolucoes: (d) => [
    '## EVOLUÇÃO PSICOLÓGICA',
    `Paciente: ${d.nome}`,
    `Idade: ${d.idade} anos`,
    `Data: ${d.data}`,
    `Durante o atendimento psicológico, o paciente ${d.nome} apresentou-se consciente, orientado e colaborativo, demonstrando disposição para participar da sessão. Inicialmente, mostrou-se um pouco reservado, mas, ao longo da conversa, apresentou maior abertura para expressar seus sentimentos, pensamentos e experiências pessoais.`,
    'Foram abordados aspectos relacionados ao desenvolvimento emocional, às relações familiares, à convivência social e aos desafios característicos da adolescência. O paciente foi incentivado a reconhecer e expressar suas emoções, refletindo sobre situações que podem gerar desconforto, ansiedade ou insegurança.',
    'Ao longo da sessão, foram utilizadas técnicas de escuta ativa e acolhimento, buscando proporcionar um ambiente seguro e livre de julgamentos. Observou-se participação adequada nas atividades propostas.',
    'Conduta: Dar continuidade ao acompanhamento psicológico, trabalhando o reconhecimento emocional, o fortalecimento da autoestima, o desenvolvimento da autonomia e estratégias saudáveis para lidar com situações do cotidiano.',
    'Objetivo terapêutico: Promover o bem-estar emocional e contribuir para o desenvolvimento pessoal e psicológico do paciente.',
  ],
};

/** Texto-modelo de um documento novo (HTML), ou '' se a seção não tem modelo. */
export function modeloDeDocumento(secao: Secao, dados: DadosModelo): string {
  const monta = MODELOS[secao];
  return monta ? emHtml(monta(dados)) : '';
}
