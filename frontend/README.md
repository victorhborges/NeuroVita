# NeuroVita

Front-end do **NeuroVita**, um sistema web para clínicas de psicologia e psiquiatria, com foco em agendamento de consultas e acompanhamento de pacientes. Projeto da faculdade, feito em **Angular**.

> **Status:** em desenvolvimento. Login, agenda e fluxos clínicos descritos na seção de integração abaixo consomem o backend com JWT. Algumas telas ainda usam dados de demonstração ou aguardam os formulários/ações que serão entregues pela equipe frontend.

## Perfis de usuário

O sistema tem 4 perfis. As telas são parecidas, mas o menu, os botões e as funções mudam de acordo com quem entrou.

| Perfil | O que tem hoje |
| --- | --- |
| **Psicólogo** | Início, Pacientes, Prontuário (evoluções, laudos, encaminhamentos, avaliações e atestados) e Agenda |
| **Psiquiatra** | Início, Pacientes, Prontuário (evoluções, laudos, medicamentos e prescrições, atestados médicos e solicitação de exames) e Agenda |
| **Secretária** | Início, Pacientes (cadastrar, editar e excluir), Profissionais, Gerenciamento de consultas e Agenda |
| **Administrador** | Início, Pacientes (só consulta), Profissionais e Secretárias (cadastrar, editar, inativar e excluir) e Agenda |

Todos os 4 perfis já têm as telas do protótipo. Faltam Configurações, Ajuda e as notificações (sino).

## Tecnologias

- [Angular](https://angular.dev) 21 (componentes standalone e signals)
- TypeScript
- CSS puro (arquivo único `src/styles.css`)
- Fonte [Poppins](https://fonts.google.com/specimen/Poppins)

O visual segue o protótipo do Figma da equipe de design.

## Como rodar

**Requisitos:** [Node.js](https://nodejs.org) (recomendado a versão LTS atual) e o Angular CLI (`npm install -g @angular/cli`).

```bash
# 1. Clonar o repositório
git clone https://github.com/SEU-USUARIO/NOME-DO-REPOSITORIO.git

# 2. Entrar na pasta do projeto (a que tem o package.json)
cd NOME-DO-REPOSITORIO

# 3. Instalar as dependências
npm install

# 4. Rodar
npm start
```

Depois abra **http://localhost:4200** no navegador.

> **Windows / PowerShell:** se aparecer o erro de "execução de scripts desabilitada", rode uma vez
> `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser` e abra o terminal de novo.

### Outros comandos

| Comando | Para que serve |
| --- | --- |
| `npm start` | Servidor de desenvolvimento (atualiza sozinho ao salvar) |
| `npm run build` | Gera a versão de produção em `dist/` |
| `npm test` | Roda os testes |

## Contas de demonstração

Na tela de login, a lista de contas de demonstração auxilia a preencher o formulário. O login é validado pelo endpoint real `/api/auth/login`; essas contas precisam existir no ambiente configurado e a senha não é aceita por uma autenticação local simulada.

| E-mail | Vai para |
| --- | --- |
| `psicologo@neurovita.com` | `/psicologo` |
| `psiquiatra@neurovita.com` | `/psiquiatra` |
| `secretaria@neurovita.com` | `/secretaria` |
| `admin@neurovita.com` | `/admin` |

O e-mail não diferencia maiúsculas de minúsculas. Também dá para abrir cada tela direto pelo endereço.

## Rotas

| Endereço | Tela |
| --- | --- |
| `/` | Login |
| `/esqueci-senha`, `/codigo`, `/redefinir-senha` | Recuperação de senha |
| `/psicologo`, `/psiquiatra`, `/secretaria`, `/admin` | Tela inicial de cada perfil |
| `/psicologo/pacientes` | Lista de pacientes |
| `/psicologo/pacientes/:id` | Prontuário do paciente |
| `/psicologo/pacientes/:id/:secao` | Evoluções, laudos, encaminhamentos, avaliações ou atestados |
| `/psicologo/pacientes/:id/:secao/novo` | Novo documento |
| `/psicologo/pacientes/:id/:secao/:docId` | Ler um documento |
| `/psicologo/pacientes/:id/:secao/:docId/editar` | Editar um documento |
| `/psicologo/agenda` | Agenda semanal |
| `/psiquiatra/pacientes`, `/psiquiatra/pacientes/:id` e `/psiquiatra/agenda` | Mesmas telas do psicólogo, com outras seções no prontuário |
| `/secretaria/pacientes` | Lista de pacientes (editar e excluir) |
| `/secretaria/pacientes/novo` | Cadastrar novo paciente |
| `/secretaria/pacientes/:id/editar` | Editar paciente |
| `/secretaria/profissionais` | Lista de profissionais |
| `/secretaria/consultas` | Gerenciamento de consultas (agendar) |
| `/secretaria/agenda` | Agenda semanal (confirmar, remarcar e cancelar) |
| `/admin/pacientes` | Lista de pacientes (só consulta) |
| `/admin/profissionais` | Lista de profissionais (editar, inativar e excluir) |
| `/admin/profissionais/novo` | Cadastrar novo profissional |
| `/admin/profissionais/:id/editar` | Editar profissional |
| `/admin/secretarias` | Lista de secretárias |
| `/admin/secretarias/novo` | Cadastrar nova secretária |
| `/admin/secretarias/:id/editar` | Editar secretária |
| `/admin/agenda` | Agenda semanal (igual à da secretária) |
| qualquer outro | Página não encontrada |

`:secao` no psicólogo: `evolucoes`, `laudos`, `encaminhamentos`, `avaliacoes` ou `atestados`.
No psiquiatra: `evolucoes`, `laudos`, `medicamentos`, `atestadosMedicos` ou `exames`.

## Estrutura do projeto

```
src/
  styles.css                 -> todo o visual do projeto
  app/
    app.routes.ts            -> rotas
    pages/                   -> uma pasta por tela
      login/  esqueci-senha/  codigo/  redefinir-senha/  nao-encontrada/
      painel-shell/          -> moldura com o menu lateral
      painel-inicio/         -> tela inicial dos 4 perfis
      pacientes/  prontuario/  secao-paciente/
      documento-leitura/  documento-editor/  agenda/
    shared/                  -> o que é usado em mais de uma tela
      auth.ts                -> login JWT e gestão da sessão
      perfil.ts              -> os 4 perfis e o que muda em cada um
      dados-demo.ts          -> dados fictícios das telas iniciais
      dados-agenda.ts        -> dados fictícios da agenda
      clinica/               -> fachada e serviços Angular para APIs clínicas, com alguns dados de demonstração legados
      editor-texto/  menu-lateral/  cabecalho/  agenda-do-dia/  calendario/ ...
public/
  img/                       -> logo e imagens
```

## Telas do psiquiatra

São as mesmas páginas do psicólogo (`pages/pacientes`, `prontuario`, `secao-paciente`, `documento-leitura`, `documento-editor` e `agenda`). O perfil chega pela rota e define o endereço-base e as seções do prontuário (`secoesDoPerfil`, em `shared/clinica/secoes.ts`). Os dados são os mesmos da clínica.

## Telas da secretária

- **Dados compartilhados com o psicólogo:** quem cadastra um paciente ou agenda uma consulta aparece nas telas dos dois perfis (tudo vem de `shared/clinica/clinica.ts`).
- **Agendar:** `pages/consulta-form`. Os horários livres consideram as consultas já marcadas do profissional e do paciente. A consulta nasce como *pendente*.
- **Agenda:** `pages/agenda` é a mesma tela dos dois perfis. O psicólogo troca o status por um menu e a secretária usa a janela "Editar consulta".
- **Cadastro de paciente:** `pages/paciente-form` valida CPF, telefone, e-mail e exige o responsável para menores de 18 anos.
- **Datas de demonstração:** "hoje" é 15/09/2026 (`shared/dados-agenda.ts`).

## Telas do administrador

- **Disponibilidade do profissional:** o que o administrador cadastra (dias, faixas de horário, online e/ou presencial) vale na hora de agendar. A tela de consultas só mostra os horários em que o profissional atende (`Clinica.disponivel`).
- **Excluir profissional:** se ele tiver consultas na agenda, o sistema não deixa excluir e sugere inativar. Sem consultas, a exclusão é permitida.
- **Acesso ao sistema:** cadastros de profissionais e secretarias podem persistir no backend; envio de boas-vindas depende de configuração de e-mail. A lista exibida no login é apenas um auxílio visual e não substitui a autenticação JWT.
- **Pacientes:** o administrador só consulta; cadastrar, editar e excluir é da secretária.

## Integração com o back-end

O frontend usa JWT por meio do interceptor HTTP e a fachada `shared/clinica/clinica.ts` para carregar e atualizar os dados consumidos pelas telas existentes.

- **Evoluções:** `evolucoes-api.service.ts` usa `GET /api/evolucoes/paciente/{pacienteId}` e `POST /api/evolucoes`. Novas evoluções são append-only; documentos antigos da seção continuam no endpoint genérico e não são convertidos.
- **Prescrições:** `prescricoes-api.service.ts` usa `GET /api/prescricoes/paciente/{pacienteId}`, `POST /api/prescricoes` e `PUT /api/prescricoes/{id}`. Os medicamentos estruturados já retornados pela API são preservados nas atualizações, mesmo sem campos próprios para editá-los no formulário atual.
- **Exames:** `exames-api.service.ts` integra a tela de solicitação com `/api/exames`; o texto da solicitação é persistido em `descricaoSolicitacao`, sem preencher data de realização ou histórico de resultado. A tela atual não registra resultados; `PUT /{id}/resultado` permanece disponível na API. Uploads genéricos do prontuário continuam em `documentos_clinicos` e não são convertidos em anexos de resultado.
- **Vínculos profissionais-pacientes:** `vinculos-profissionais-pacientes-api.service.ts` consulta o histórico em `GET /api/vinculos-profissionais-pacientes/paciente/{pacienteId}`. A criação automática após salvar ou remarcar uma consulta é responsabilidade do backend.
- **Responsáveis no prontuário:** profissionais consultam responsáveis individualmente por `GET /api/responsaveis/{id}` apenas quando possuem vínculo ativo com algum paciente relacionado. Listagem e escrita permanecem restritas a administrador/secretaria.
- **Preferências:** `preferencias-api.service.ts` lê e atualiza o contrato completo do endpoint `/api/preferencias`. Atualizações omitem campos inalterados; o backend preserva esses valores. O formulário de configuração ainda não está presente.
- **Senha autenticada:** `Auth.alterarSenhaAutenticada` chama `POST /api/auth/alterar-senha`, conforme contrato existente, e encerra a sessão local após sucesso porque o backend invalida o JWT anterior. A ação de configuração ainda não tem tela.
- **Prontuários:** `prontuarios-api.service.ts` disponibiliza consulta por paciente/ID, criação e atualização do contrato existente. O prontuário visual atual é uma composição de paciente, consultas e documentos clínicos e não possui campos de edição para os campos centrais da entidade; conectar esses campos depende da interface correspondente.
- **Administradores:** `administradores-api.service.ts` disponibiliza CRUD para `/api/administradores`, protegido no backend para o perfil administrador. Não há tela de gerenciamento de administradores no frontend atual.
- **Tratamentos:** `tratamentos-api.service.ts` oferece consulta por paciente, consulta por ID, criação, atualização e arquivamento em `/api/tratamentos`. A API também expõe listagem autorizada. Não há formulário ou tela de tratamento existente para conectar; nenhuma tela foi criada. Operações e regras de acesso estão descritas no Swagger.
- **Compatibilidade:** documentos clínicos antigos permanecem acessíveis pelo endpoint `/api/documentos-clinicos`; não há migração ou conversão automática.

Os serviços usam as respostas reais da API. Falhas HTTP são informadas pelas telas e não são tratadas como sucesso. A validação de autorização continua no backend, independentemente dos guards Angular.

## Equipe

- _Nome 1_ — front-end
- _Nome 2_ — front-end
- _Nome(s) do design e do back-end_

## Licença

Projeto acadêmico, sem licença definida.
