# Referência funcional e visual — MVP NeuroVita FBR

**Documento de consulta principal para o frontend.** Atualizado a partir do PDF `NEUROVITA (fbr atual).pdf`, das telas em `Mvp-Fbr/neurovita/src/app/pages/` e do código integrado da branch `develop`. O protótipo Angular da pasta Mvp-Fbr contém demonstrações e dados estáticos; ele serve para comparar estrutura e linguagem visual, não para copiar seus mocks, credenciais ou regras de demonstração para a aplicação integrada.

## Como ler esta referência

- **FBR confirmado** descreve o que aparece no PDF e nos modelos fornecidos.
- **Implementação do projeto** descreve o que o código integrado realmente faz.
- **Limitação/decisão** identifica diferenças que não devem ser preenchidas por suposição.
- As páginas indicadas abaixo são números do PDF atual. Quando não há página explicitamente mapeada, a tela é identificada pelo nome de sua pasta/rota na referência Angular. O PDF contém variantes de telas e estados; exemplos preenchidos não autorizam dados fictícios em produção.

## Identidade visual e composição

### Referência confirmada

- O sistema usa a identidade NeuroVita: marca cerebral multicolorida, áreas de conteúdo claras, azul/índigo como cor de ação e navegação em azul-marinho nos painéis. O login apresentado nas páginas 105 e 138 usa fundo quase branco com ilustrações azul-lavanda nas laterais e cartão branco central.
- A fonte do CSS integrado é `Poppins`, com fallback `system-ui, sans-serif` (`frontend/src/styles.css`). A escala visual deve respeitar o destaque do título, os campos altos e o botão de ação largo mostrados nas referências.
- Login (p. 138): cartão central com largura aproximada de metade da página e quase toda a altura disponível; logo central no alto; título central; campos de e-mail e senha empilhados; link de recuperação alinhado à direita; botão Entrar em largura total. A imagem p. 138 é a referência visual mais recente para login. A tela de seleção de perfil da p. 139 não substitui o login integrado: a API autentica a conta e determina o perfil.
- Painel (p. 33 e p. 140, p. 176): menu lateral marinho, cabeçalho de usuário, cartões e áreas de ação em fundo claro. Os painéis variam por perfil; a secretaria/admin têm atalhos de gestão, profissionais têm foco na agenda e pacientes.
- Agenda (p. 25, p. 62, p. 91, p. 98, p. 100, p. 148 e p. 181): grade semanal de segunda a sábado, navegação de semana, botão Hoje, busca e cartões laterais de resumo/próximo atendimento/paciente selecionado. O diálogo de edição (p. 100) apresenta paciente, data, horário, profissional e sala, além das ações confirmar, remarcar e cancelar.
- Formulário de consulta (p. 80, p. 95 e p. 141): bloco de dados da consulta e bloco de disponibilidade; seleção de paciente/profissional/modalidade/local, datas e horários; resumo e ações. A disponibilidade e os dados devem vir da API.
- Cadastro do paciente (p. 94 e p. 106): dados pessoais, endereço (CEP, cidade, endereço/logradouro, número, complemento e bairro), plano/status e responsáveis; preservar os campos e a capacidade de múltiplos responsáveis prevista no modelo atual. O contrato da aplicação mapeia o endereço para `pacientes` na coleção existente; a equipe do banco precisa refletir os novos campos opcionais `numero`, `complemento` e `bairro`, conforme [discrepancias banco.md](../discrepancias%20banco.md).
- Prontuário/histórico (p. 2, p. 26 e p. 65): faixa de identificação do paciente, seções clínicas e histórico de consultas. Documentos/evoluções possuem estados vazios e preenchidos nas páginas 1–24 e seções clínicas posteriores.
- Ajuda (p. 38 e p. 150) apresenta pesquisa, categorias e contato; ajuda por tópico aparece na p. 64. Contatos precisam ser os definidos pelo projeto, sem inventar canais.
- Configurações (p. 28, p. 63, p. 92, p. 149, p. 164 e p. 182) agrupa preferências de conta, sistema, notificações e aparência.
- Recuperação de senha (p. 151–153) apresenta solicitação, código e redefinição. Preservar a sequência e os endpoints reais.

### Tokens técnicos usados no CSS integrado

Os valores em OKLCH abaixo são tokens de implementação, não medições de pixel extraídas do PDF. Eles preservam uma única paleta e não devem ser convertidos em HEX aproximado como se fossem especificação FBR:

| Token | Valor atual | Aplicação |
|---|---|---|
| `--background` | `oklch(1 0 0)` | fundo claro |
| `--foreground` | `oklch(0.129 0.042 264.695)` | texto principal |
| `--muted` | `oklch(0.968 0.007 247.896)` | superfícies discretas |
| `--muted-foreground` | `oklch(0.554 0.046 257.417)` | texto secundário |
| `--navy` | `oklch(0.18 0.04 265)` | navegação e botões escuros |
| `--brand` | `oklch(0.5 0.22 275)` | marca/ação primária |
| `--action` | `oklch(0.55 0.2 260)` | ação |
| `--border` | `oklch(0.929 0.013 255.508)` | contornos |

O CSS global define raio padrão `0.625rem`, fonte Poppins, componentes de formulário e breakpoints em `800px` e `480px`. Os componentes existentes são a fonte de espaçamentos e dimensões implementados; quando uma página do PDF não oferece medida numérica legível, não inventar um número absoluto. Usar a proporção do screenshot como comparação visual.

## Inventário das telas e navegação

As pastas abaixo existem no protótipo `Mvp-Fbr/neurovita/src/app/pages/` e correspondem às famílias de tela da referência. Algumas implementações integradas são rotas filhas de perfil, e outras compartilham componente. A tabela informa a finalidade confirmada por nomes, templates e código integrado; não significa que cada pasta corresponda a uma rota autônoma.

| Tela / família | Referência Angular | Rota / integração atual |
|---|---|---|
| Agenda | `agenda` (PDF p. 25/62/91/98/100/148/181) | `/{psicologo,psiquiatra,secretaria,admin}/agenda`; consultas e disponibilidades reais |
| Ajuda | `ajuda` (p. 38/150) | `/{perfil}/ajuda`; conteúdo local documentado, navegação protegida por perfil |
| Tópico de ajuda | `ajuda-topico` (p. 64) | `/{perfil}/ajuda/:topico` |
| Código de recuperação | `codigo` (p. 152) | `/codigo`; validação pelo fluxo de recuperação existente |
| Configurações | `configuracoes` (p. 28/63/92/149/164/182) | `/{perfil}/configuracoes`; preferências persistidas em `/api/preferencias` |
| Gerenciamento de consulta | `consulta-form` (p. 80/95/141) | `/secretaria/consultas`; APIs de paciente, profissional, disponibilidade e consulta |
| Criar/editar documento clínico | `documento-editor` (p. 1–24 por tipo/seção) | rota de seção do paciente; APIs clínicas específicas existentes |
| Ler documento clínico | `documento-leitura` | rota de seção/documento; APIs clínicas existentes |
| Recuperação de senha | `esqueci-senha` (p. 151) | `/esqueci-senha`; API de recuperação |
| Fale conosco | `fale-conosco` | `/{perfil}/ajuda/fale-conosco`; formulário usa endpoint de contato configurado |
| Gestão de pacientes | `gestao-pacientes` (p. 177) | secretaria/admin; API de pacientes |
| Login | `login` (p. 105/138) | `/`; JWT e perfil determinados pelo backend; sem credenciais de demonstração |
| Página não encontrada | `nao-encontrada` | rota coringa |
| Início | `painel-inicio` (p. 33/140/176) | rota raiz de cada perfil autenticado; dados devem ser da API |
| Moldura do painel/menu | `painel-shell` | rota pai protegida; menu e cabeçalho conforme os quatro perfis |
| Cadastro/edição de paciente | `paciente-form` (p. 94/106) | `/secretaria/pacientes/novo` e `/secretaria/pacientes/:id/editar` |
| Lista de pacientes | `pacientes` (p. 34/145/177) | profissionais; acesso limitado por vínculo e API |
| Lista de profissionais | `profissionais` (p. 146/178) | secretaria/admin; API de profissionais |
| Cadastro/edição de profissional | `profissional-form` (p. 154/156/158) | admin; API de profissionais/disponibilidades |
| Prontuário | `prontuario` (p. 2/26/65) | `/perfil/pacientes/:id`; dados clínicos e consultas pela API |
| Redefinição de senha | `redefinir-senha` (p. 153) | `/redefinir-senha`; API de recuperação |
| Seção do paciente | `secao-paciente` (p. 1–24 e demais seções clínicas) | `/perfil/pacientes/:id/:secao` |
| Cadastro/edição de secretaria | `secretaria-form` (p. 160) | admin; API de secretarias |
| Lista de secretarias | `secretarias` (p. 168/180) | admin; API de secretarias |

### Perfis e limites de navegação

- Perfis de aplicação: psicólogo, psiquiatra, secretaria e administrador. Guards Angular limitam a rota; o backend continua responsável pela autorização.
- Profissionais consultam sua agenda e pacientes autorizados pelo vínculo ativo. Secretaria gerencia pacientes, profissionais e consultas conforme as permissões atuais. Admin gerencia os cadastros administrativos previstos.
- O protótipo contém estados de demonstração e não deve substituir a sessão real, o perfil do JWT, nem os dados persistidos.
- A tela p. 139 de escolha de perfil não altera a autenticação atual: não há requisito/capacidade confirmada para permitir que uma pessoa escolha um perfil diferente do cadastro autenticado.
- Na edição de consulta, o paciente aparece como cartão de contexto, não como campo editável; o `ConsultaService.atualizar` também impede alterar `pacienteId`. Não adicionar seletor de paciente sem mudança funcional aprovada no FBR/backend.

## Regras funcionais confirmadas no código e referências

### Consulta e agenda

1. Selecionar paciente e profissional válidos para o perfil, modalidade, dia, horário e local.
2. O formulário de criação apresenta disponibilidades reais do profissional e horários; a API valida de novo antes de persistir.
3. Remarcação preserva o paciente da consulta existente no contrato atual. A tela de edição deve apresentar o paciente e permitir revisar data, horário, profissional e sala/local. Se o protótipo não mostrar um seletor de paciente, não o criar.
4. Disponibilidade deve abranger todo o intervalo da consulta. Conflitos do backend consideram sobreposição de horários de consultas não canceladas do profissional, paciente e sala física reconhecida pelo padrão `Sala N`.
5. Confirmação usa `PATCH /api/consultas/{id}/confirmar`; cancelamento usa `PATCH /api/consultas/{id}/cancelar`. Cancelar preserva o registro/histórico; não fazer exclusão definitiva.
6. Atualizar a agenda somente após resposta da API. Erros de conflito/indisponibilidade permanecem visíveis e o backend é autoridade final.
9. Na edição, a seleção de paciente é informativa: a tela mostra o paciente atual e o endpoint `PUT /api/consultas/{id}` rejeita a troca de `pacienteId`. O MVP documentado não define criar uma consulta substituta e cancelar a original como remarcação.
10. Antes de persistir a remarcação, revisar paciente, data/horário, profissional e local da consulta atual e da proposta; enviar a atualização somente após confirmação explícita.
7. A grade do MVP é segunda a sábado. Não mudar para domingo nem aplicar preferência de início de semana à agenda sem regra FBR confirmada.
8. A sala precisa corresponder a uma opção aceita. No código atual as opções de UI são `Sala 01`–`Sala 04`; local legado deve ser preservado apenas quando não alterado. Não considerar texto arbitrário uma sala disponível.

### Pacientes e responsáveis

- Dados de paciente e responsáveis pertencem ao fluxo de cadastro/edição existente. O código aceita múltiplos responsáveis; a tela deve preservar os já cadastrados ao alterar outros dados.
- Identidade, campos obrigatórios e validações vêm dos DTOs e serviços Java. Não adicionar campos clínicos ou documentos por suposição.
- Acesso clínico de profissional continua sujeito a vínculo ativo e autorização backend.

### Prontuário e documentos clínicos

- O prontuário reúne identificação do paciente, histórico de consultas e seções clínicas já modeladas: evoluções, diagnósticos, tratamentos, exames, prescrições e documentos clínicos conforme o contrato existente.
- Lista, edição, histórico, anexos e autoria devem usar a API própria. Documentos clínicos legados permanecem acessíveis; anexos genéricos não devem ser convertidos em resultado de exame.
- Solicitação de exame e resultado são operações distintas. A solicitação não define automaticamente data de realização ou resultado; alterações de solicitação preservam resultado/histórico existentes.
- Tipos de laudo, encaminhamento, avaliação psicológica, acolhimento e atestado só podem ser tratados pelo modelo de documento existente quando os campos dele realmente representam o conteúdo da referência. Novas estruturas precisam de requisito e contrato confirmado.
- Não há requisito suficiente nesta referência para assinatura digital, emissão formal, transmissão externa ou assinatura profissional avançada.

### Autenticação, configurações e comunicação

- Login e recuperação mantêm endpoints existentes, JWT, BCrypt, guard e primeiro acesso. O perfil é o associado à conta pelo backend.
- Preferências de tema, idioma, formato de data, duração, início da semana e notificações são persistidas pela API. Idioma inglês só deve ser apresentado como funcional quando a tradução da interface cobrir os textos fixos.
- Flags de notificação são preferências persistidas; sem mecanismo real de envio, não afirmar que e-mail ou push foi enviado.
- O PDF não define fator, ativação, verificação, recuperação ou fluxo de 2FA. Não implementar mecanismo de 2FA por inferência.
- Fale Conosco deve usar apenas o canal e configuração presentes no projeto. Não inventar endereço institucional ou integração externa.

## Endpoints confirmados no backend

Os controllers atuais declaram estas raízes (contratos detalhados permanecem nos controllers/DTOs; conferir estes arquivos antes de mudar consumidores):

| Recurso | Raiz confirmada |
|---|---|
| Autenticação/recuperação | `/api/auth`, `/api/auth/recuperacao` |
| Pacientes/responsáveis | `/api/pacientes`, `/api/responsaveis` |
| Profissionais/disponibilidades | `/api/profissionais`, `/api/disponibilidades` |
| Consultas/agenda | `/api/consultas`, `/api/agenda` |
| Prontuários/documentos/evoluções | `/api/prontuarios`, `/api/documentos-clinicos`, `/api/evolucoes` |
| Diagnósticos/tratamentos/exames/prescrições | `/api/diagnosticos`, `/api/tratamentos`, `/api/exames`, `/api/prescricoes` |
| Vínculos profissionais-pacientes | `/api/vinculos-profissionais-pacientes` |
| Preferências/contato | `/api/preferencias`, `/api/contato` |
| Administração/secretaria | `/api/administradores`, `/api/secretarias` |

Consulta e edição usam DTOs reais em `ConsultasApiService`; confirmação/cancelamento são PATCH. Não duplicar operações equivalentes nem presumir que existir uma raiz garante autorização ou persistência de cada ação: confirmar no controller e service do módulo.

## Estado da implementação e limitações conhecidas

- Implementação integrada usa APIs e preserva autenticação; os templates do Mvp-Fbr contêm dados de demo (por exemplo, lista de emails/senhas de teste no login do protótipo), que não pertencem à aplicação real.
- A sala no endpoint de consulta é o campo `local`; backend identifica sala física por padrão `Sala N` e confere conflitos no mesmo local. Unidades e salas não têm entidade/API própria no modelo ativo. Portanto, a lista fixa de salas existente não deve ser descrita como cadastro dinâmico de salas.
- A edição da agenda carrega o perfil vindo da rota antes de escolher a fonte de profissionais, e consulta as disponibilidades do profissional selecionado. O frontend filtra conflitos com os dados reais carregados, pede revisão explícita antes de enviar `PUT /api/consultas/{id}` e atualiza a agenda somente após a resposta. A lista de salas segue fixa em `Sala 01`–`Sala 04`, filtrada pelos conflitos conhecidos em `/api/consultas`; não existe endpoint de cadastro/consulta de salas no contrato atual. O backend revalida disponibilidade e conflitos.
- Campos de endereço do paciente são retrocompatíveis no modelo Java; `numero`, `complemento` e `bairro` podem estar ausentes em documentos legados. Cadastro novo via API exige CEP válido, número e bairro, enquanto updates parciais mantêm valores omitidos.
- A p. 94 mostra rótulos repetidos “Data de início” e “Data de encerramento” no formulário do paciente, mas o recorte não deixa claro a que cadastro/registro pertencem. O contrato atual de plano de saúde tem apenas `validadePlano`; não foi criado campo de início nem data de encerramento sem definição funcional confirmada. Confirmar com a FBR/equipe de produto antes de ampliar o modelo.
- `pacienteId` da consulta não é editável no endpoint de atualização atual. A tela não oferece essa alteração; troca de paciente requer criar outra consulta e cancelar a original conforme um fluxo aprovado, que ainda não está definido como parte da remarcação.
- A preferência de idioma só pode ser considerada concluída quando todos os textos fixos da interface e mensagens de erro estiverem traduzidos. Dados de usuários e texto clínico são conteúdo e não se traduzem.
- Notificações e 2FA não devem ser simulados. Flags de preferências não equivalem a entrega de aviso.
- A reprodução exata em todos os tamanhos exige comparação visual em navegador com dimensões equivalentes às capturas. A lista de páginas do PDF aqui citada localiza as referências, mas não declara teste visual automatizado ou homologação FBR.

## Protocolo para futuras alterações

1. Consultar esta referência e o template/estilo da página correspondente.
2. Abrir o screenshot/página do PDF citado; usar o PDF somente para detalhe ausente/ambíguo nesta documentação.
3. Separar diferença visual de regra funcional e de limitação do backend.
4. Reutilizar componente/endpoint; não copiar mocks do protótipo.
5. Atualizar esta referência somente quando requisito confirmado ou decisão aprovada alterar o entendimento do MVP.

## Registro da revisão visual (2026-11-17)

### Alcance e método

O frontend integrado foi iniciado localmente e a tela de login foi observada renderizada no navegador em `http://127.0.0.1:4200/`. A captura disponível foi feita no painel embutido, com viewport estreita (aproximadamente 668 px); nela são visíveis a marca, título, campos de e-mail e senha, recuperação de senha, botão Entrar e decoração lateral responsiva. Os elementos correspondem à composição descrita na referência da tela de login (PDF, p. 138), mas não houve captura da aplicação e do PDF na mesma dimensão desktop. Portanto, esta observação não certifica fidelidade pixel a pixel nem responsividade em todos os breakpoints.

As páginas do PDF cujas imagens foram abertas nesta revisão foram p. 25, 28, 33, 34, 63, 79, 80, 94, 98 e, em revisão anterior, p. 138. Essas consultas às imagens do PDF não equivalem a comparação navegador-a-navegador de todas as telas. As outras páginas e estados não foram comparados visualmente nesta execução.

Foi inventariada a presença de diretórios de página em `C:/Users/victo/Downloads/Mvp-Fbr/neurovita/src/app/pages/`: `agenda`, `ajuda`, `ajuda-topico`, `codigo`, `configuracoes`, `consulta-form`, `documento-editor`, `documento-leitura`, `esqueci-senha`, `fale-conosco`, `gestao-pacientes`, `login`, `nao-encontrada`, `paciente-form`, `pacientes`, `painel-inicio`, `painel-shell`, `profissionais`, `profissional-form`, `prontuario`, `redefinir-senha`, `secao-paciente`, `secretaria-form` e `secretarias`. A tela de primeiro acesso também existe no projeto integrado, mas não aparece como diretório separado nesse inventário do código de referência.

### Códigos originais FBR consultados

Arquivos lidos em detalhe nesta revisão:

- `Mvp-Fbr/neurovita/src/styles.css` — tokens e regras compartilhadas.
- `Mvp-Fbr/neurovita/src/app/pages/agenda/agenda.html` e `agenda.ts` — composição do diálogo e comportamento demonstrativo.
- `Mvp-Fbr/neurovita/src/app/shared/clinica/status.ts` — opções de sala declaradas pela referência.
- `Mvp-Fbr/neurovita/src/app/pages/consulta-form/consulta-form.html` — estrutura do formulário.

Os demais diretórios de tela foram inventariados, mas seus arquivos não foram inspecionados individualmente nesta revisão. Não se afirma comparação de todo o código recebido.

### Medidas e estruturas reutilizáveis

No CSS original foram confirmados valores compartilhados compatíveis com a identidade/estrutura documentada e úteis como referência, sem copiá-los indiscriminadamente: tipografia Poppins; paleta baseada em variáveis OKLCH; raio-base `0.625rem`; campos `.field` com altura de `68px`; `.btn-action` com altura de `72px`; logo do menu com largura `8.5rem`; sidebar de agenda `16.5rem`; e grade de agenda com coluna lateral `3.6rem`, seis colunas de dia com mínimo de `6.5rem` e linhas com `3rem` de cabeçalho mais onze linhas de no mínimo `5.25rem`. Estruturas com potencial de reaproveitamento incluem o cartão de autenticação e campo/senha, moldura/menu do painel, grade de agenda, formulário em duas colunas e diálogos compartilhados. Isso registra parâmetros dos códigos de referência, não uma medição independente de cada componente renderizado do projeto integrado.

### Divergências entre código recebido e PDF

- O CSS antigo da autenticação usa fundo azul-marinho com `/img/auth-bg.jpg`, cartão de `560px`, padding fixo de `2rem` e logo de `90px`. Esses valores/esse fundo não correspondem à composição mais recente do PDF, p. 138, que mostra fundo claro decorado e cartão central alto, com proporções responsivas. A implementação integrada usa `auth-decor.jpg` e dimensões fluidas; os valores antigos não foram transportados. A tela integrada foi vista em viewport estreita, mas falta comparação equivalente em desktop.
- A tela de edição de consulta no HTML original mostra o paciente como cartão informativo e apresenta controles de data, horário, profissional e sala; não apresenta seletor editável de paciente. Isso concorda com o contrato atual, no qual a atualização de consulta rejeita a troca de `pacienteId`.
- `Mvp-Fbr/neurovita/src/app/shared/clinica/status.ts` define a lista `Sala 01`–`Sala 04`. Assim, a lista fixa tem respaldo no código FBR recebido. O PDF p. 100 mostra um controle de sala, mas não especifica inventário dinâmico. O backend atual representa sala em `local`, valida conflitos e não tem coleção/endpoints de salas. Não há base documental para inventar um catálogo dinâmico nesta etapa.
- Os métodos de agenda do protótipo utilizam estado/dados locais de demonstração; essa lógica não foi reaproveitada como persistência e não substitui os serviços reais da aplicação integrada.

### Matriz de comparação visual do frontend renderizado

Status “não comparada” significa que a página não foi aberta e comparada no navegador nesta revisão; a consulta ao HTML/CSS ou à imagem do PDF, por si só, não muda esse status.

| Tela/referência | Situação visual nesta revisão | Observação |
|---|---|---|
| Login (PDF p. 138) | Comparada parcialmente no navegador | Renderizada em viewport estreita; elementos principais conferem, mas desktop/pixel a pixel pendente. |
| Agenda (p. 25/28 e sala p. 100) | Não comparada | PDF consultado; implementação e regras foram lidas, sem comparação lado a lado da página renderizada. |
| Ajuda | Não comparada | Sem captura renderizada nesta revisão. |
| Ajuda — tópico | Não comparada | Sem captura renderizada nesta revisão. |
| Código de recuperação | Não comparada | Sem captura renderizada nesta revisão. |
| Configurações | Não comparada | Sem captura renderizada nesta revisão. |
| Formulário de consulta | Não comparada | Fonte HTML consultada; sem comparação renderizada. |
| Editor de documento clínico | Não comparada | Sem captura renderizada nesta revisão. |
| Leitura de documento clínico | Não comparada | Sem captura renderizada nesta revisão. |
| Esqueci a senha | Não comparada | Sem captura renderizada nesta revisão. |
| Fale conosco | Não comparada | Sem captura renderizada nesta revisão. |
| Gestão de pacientes | Não comparada | Sem captura renderizada nesta revisão. |
| Página não encontrada | Não comparada | Sem captura renderizada nesta revisão. |
| Cadastro/edição de paciente (p. 94) | Não comparada | Imagem do PDF consultada; sem captura renderizada equivalente nesta revisão. |
| Lista de pacientes (p. 34) | Não comparada | Imagem do PDF consultada; sem captura renderizada equivalente nesta revisão. |
| Início/painel (p. 33) | Não comparada | Imagem do PDF consultada; os quatro perfis não foram abertos no navegador. |
| Moldura do painel/menu | Não comparada | Sem captura renderizada por perfil nesta revisão. |
| Lista de profissionais | Não comparada | Sem captura renderizada nesta revisão. |
| Cadastro/edição de profissional | Não comparada | Sem captura renderizada nesta revisão. |
| Prontuário (p. 25/63/65) | Não comparada | Imagens do PDF consultadas; sem captura renderizada equivalente nesta revisão. |
| Redefinir senha | Não comparada | Sem captura renderizada nesta revisão. |
| Seções clínicas do paciente (p. 79/80/98) | Não comparada | Imagens do PDF consultadas; sem comparação renderizada equivalente nesta revisão. |
| Cadastro/edição de secretaria | Não comparada | Sem captura renderizada nesta revisão. |
| Lista de secretarias | Não comparada | Sem captura renderizada nesta revisão. |
| Primeiro acesso (rota integrada) | Não comparada | Sem tela correspondente separada inventariada no código original consultado. |

Consequentemente, esta etapa não atesta fidelidade visual completa do frontend. Para completar a validação visual, ainda é necessário abrir as páginas autenticadas com contas de teste apropriadas, capturá-las no navegador em dimensões comparáveis às referências e conferir os estados interativos por perfil. Nenhuma alteração de CSS foi feita nesta revisão: não foi estabelecida uma divergência visual renderizada que justificasse editar estilos sem comparação confiável.
