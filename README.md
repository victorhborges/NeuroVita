# NeuroVita - Plataforma Integrada

Plataforma acadêmica desenvolvida para a **Faculdade Brasília**, no curso de **Análise e Desenvolvimento de Sistemas (ADS)**.

O NeuroVita é um sistema de gestão de atendimentos psicológicos. Integra uma aplicação frontend em Angular, backend Java com Spring Boot e persistência em MongoDB.

O frontend é compilado e servido pela própria aplicação Spring Boot, que também disponibiliza a API REST.

**Referência funcional do projeto:** `docs/MVP-FBR-REFERENCIA.md`

**Documentação complementar:** `docs/INDICE.md`

---

## 1. Tecnologias

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem do backend |
| Spring Boot 4.0.8 | Framework backend |
| Angular 21 | Frontend |
| Node.js / npm | Compilação do frontend |
| MongoDB Atlas | Banco de dados |
| Spring Data MongoDB | Persistência |
| Spring Security | Autenticação e autorização |
| JWT | Autenticação por token |
| BCrypt | Criptografia de senhas |
| Jakarta Bean Validation | Validação de dados |
| SpringDoc OpenAPI | Documentação da API |
| Maven | Build e dependências |
| Docker | Containerização |
| Render | Hospedagem |

---

## 2. Arquitetura

A aplicação utiliza uma arquitetura integrada:

```text
Usuário / Navegador
        |
        v
Frontend Angular
        |
        v
Aplicação Spring Boot
        |
        +----------------------+
        |                      |
        v                      v
Arquivos estáticos         API REST
                               |
                           Controller
                               |
                            Service
                               |
                           Repository
                               |
                             Model
                               |
                           MongoDB
                               |
                         MongoDB Atlas
```

### Backend

O backend é organizado em pacotes com responsabilidades específicas:

- `controller`: endpoints e recebimento das requisições HTTP.
- `service`: regras de negócio e validações.
- `repository`: acesso e persistência dos dados.
- `model`: entidades e documentos MongoDB.
- `dto`: objetos de entrada e saída da API.
- `security`: autenticação e autorização.
- `config`: configurações da aplicação.

### Frontend

O frontend é desenvolvido em Angular e utiliza serviços para comunicação com a API.

Os arquivos compilados são incorporados à aplicação Spring Boot em:

```text
src/main/resources/static/
```

A autenticação utiliza JWT. As requisições protegidas enviam o token no cabeçalho `Authorization`.

A proteção de rotas no Angular complementa, mas não substitui, as verificações de autorização realizadas pelo backend.

---

## 3. Funcionalidades

O sistema contempla os seguintes módulos:

- Autenticação, primeiro acesso e recuperação de senha.
- Cadastro e gerenciamento de pacientes.
- Cadastro e gerenciamento de responsáveis.
- Vínculos entre pacientes e responsáveis.
- Cadastro e gerenciamento de profissionais.
- Cadastro e gerenciamento de secretarias.
- Vínculos entre profissionais e pacientes.
- Disponibilidades profissionais.
- Consultas e agenda.
- Prontuários.
- Evoluções clínicas.
- Documentos clínicos e anexos.
- Diagnósticos.
- Tratamentos.
- Exames, solicitações e resultados.
- Prescrições.
- Preferências individuais dos usuários.
- Controle de acesso por perfil e vínculo profissional ativo.

A implementação das funcionalidades deve seguir os contratos e requisitos definidos para o MVP FBR. A existência de uma API não significa, necessariamente, que todas as telas correspondentes estejam concluídas ou validadas funcionalmente.

---

## 4. Pacientes e responsáveis

O módulo permite cadastrar, consultar, atualizar e inativar pacientes e responsáveis.

### Regras principais

- Pacientes menores de 18 anos precisam possuir pelo menos um responsável.
- Pacientes maiores de idade não precisam obrigatoriamente de responsável.
- Um paciente pode possuir vários responsáveis.
- Um responsável pode estar associado a vários pacientes.
- O grau de parentesco é registrado no vínculo.
- O cadastro do responsável é independente do vínculo com o paciente.
- A remoção de um vínculo não apaga automaticamente o cadastro pessoal do responsável.
- A inativação preserva o histórico relacionado.

### Plano de saúde

- Quando `possuiPlanoSaude=true`, o nome do plano é obrigatório.
- Número da carteirinha e validade permanecem opcionais.
- Quando o paciente não possui plano, os dados de identificação do plano não são obrigatórios.

### Endereço

O cadastro contempla campos de endereço, incluindo CEP, cidade, número, complemento e bairro, conforme os contratos e regras aplicáveis ao cadastro.

---

## 5. Profissionais e vínculos

O sistema permite associar pacientes a um ou mais profissionais.

Os vínculos são armazenados na coleção:

```text
vinculos_profissional_paciente
```

A estrutura permite:

- Registrar a abertura de vínculos.
- Registrar o encerramento de vínculos.
- Preservar o histórico dos relacionamentos.
- Consultar a equipe profissional associada ao paciente.
- Listar pacientes considerando os vínculos ativos.

### Controle de acesso clínico

O vínculo ativo é utilizado como critério de autorização para o compartilhamento de informações clínicas.

As regras contemplam:

- Profissionais podem consultar informações compartilhadas de pacientes com vínculo ativo.
- O encerramento do vínculo interrompe o acesso compartilhado correspondente.
- O histórico dos vínculos encerrados é preservado.
- A leitura compartilhada não concede automaticamente permissão para editar ou excluir registros de outros profissionais.
- A autoria dos registros clínicos deve ser preservada.

As permissões são verificadas no backend, não apenas nas rotas do frontend.

---

## 6. Consultas e agenda

O módulo de consultas contempla criação, consulta, atualização, remarcação e cancelamento de atendimentos.

Principais endpoints:

```http
POST   /api/consultas
GET    /api/consultas
GET    /api/consultas/{id}
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}
PATCH  /api/consultas/{id}/cancelar
```

Consultas por paciente e profissional:

```http
GET /api/consultas/paciente/{pacienteId}
GET /api/consultas/profissional/{profissionalId}
```

As consultas também podem ser filtradas por intervalo de datas, conforme os parâmetros aceitos pelos endpoints.

### Regras de agendamento

O backend valida:

- Existência do paciente e do profissional.
- Estado ativo dos cadastros.
- Disponibilidade do profissional.
- Modalidade de atendimento oferecida.
- Conflitos de horário do profissional.
- Conflitos de horário do paciente.
- Sobreposição de horários de salas.
- Compatibilidade entre duração e disponibilidade.
- Permissões do usuário autenticado.

A validação considera o intervalo completo da consulta, e não somente o horário inicial.

Consultas canceladas não bloqueiam novos agendamentos. Na remarcação, a própria consulta é desconsiderada na verificação de conflitos.

### Salas e locais de atendimento

A identificação das salas é normalizada pelo backend para evitar que diferenças de escrita sejam interpretadas como salas distintas.

Exemplos:

| Valor recebido | Valor normalizado |
|---|---|
| `Sala 1` | `Sala 01` |
| `sala 01` | `Sala 01` |
| `SALA 001` | `Sala 01` |
| `Sala 5` | `Sala 05` |
| `Sala 12` | `Sala 12` |
| `Sala 105` | `Sala 105` |
| `sAlA 00105` | `Sala 105` |

A numeração é aberta, sem limite fixo de quatro salas.

A verificação de conflitos também considera registros antigos com diferenças de caixa, espaços e zeros à esquerda. Assim, `Sala 5` e `SALA 0005` são reconhecidas como a mesma sala.

Uma sala pode ser utilizada por consultas diferentes desde que seus horários não apresentem sobreposição.

Locais que não representam salas, como `Online` e `Teleconsulta`, não são convertidos para identificadores de sala.

### Status das consultas

| Status | Significado |
|---|---|
| `PENDENTE` | Consulta aguardando confirmação |
| `CONFIRMADA` | Consulta confirmada |
| `REMARCADA` | Data ou horário alterado |
| `CANCELADA` | Consulta cancelada |

O cancelamento preserva o registro e seu histórico, sem exclusão física da consulta.

### Agenda

A agenda é derivada das consultas, não possuindo uma coleção independente.

Endpoints principais:

```http
GET /api/agenda
GET /api/agenda/dia?data=2026-11-20
GET /api/agenda/profissional/{id}?data=2026-11-20
GET /api/agenda/paciente/{id}
GET /api/agenda/resumo?data=2026-11-20
```

O resumo diário disponibiliza informações agregadas das consultas, incluindo contagens por status e próximo atendimento não cancelado.

A grade visual da agenda contempla os dias de segunda-feira a sábado.

---

## 7. Prontuários e módulos clínicos

O sistema utiliza um prontuário centralizado por paciente.

Os registros clínicos são organizados em módulos próprios:

```text
/api/prontuarios
/api/documentos-clinicos
/api/evolucoes
/api/diagnosticos
/api/tratamentos
/api/exames
/api/prescricoes
```

### Documentos clínicos

O mecanismo de documentos clínicos contempla registros textuais organizados por seção, incluindo acolhimento, assistência social, relatórios escolares, declarações, rastreio, devolutiva e outros documentos previstos na implementação.

O modelo contempla conteúdo textual, autoria, profissional responsável, paciente, data e anexos.

A implementação atual não deve ser confundida com geração formal de documentos assinados digitalmente ou emissão de PDFs oficiais.

### Evoluções

As evoluções possuem API específica para registro e consulta.

```http
GET  /api/evolucoes/paciente/{pacienteId}
POST /api/evolucoes
```

Os registros específicos são append-only nesta versão, preservando a autoria e o histórico.

### Diagnósticos e tratamentos

Diagnósticos e tratamentos possuem operações próprias de gerenciamento e regras de acesso clínico.

O arquivamento lógico preserva os registros históricos.

### Exames

O módulo diferencia solicitação de exame e registro de resultado.

```http
POST /api/exames
PUT  /api/exames/{id}
PUT  /api/exames/{id}/resultado
```

A atualização de resultado é independente da solicitação. Campos omitidos preservam informações existentes, conforme as regras do serviço.

### Prescrições

As prescrições permitem conteúdo textual e itens estruturados de medicamento, dosagem, posologia, frequência e duração.

O histórico de revisões preserva informações anteriores da prescrição.

Endpoints principais:

```http
POST /api/prescricoes
GET  /api/prescricoes/{id}
GET  /api/prescricoes/paciente/{pacienteId}
PUT  /api/prescricoes/{id}
```

Os dados estruturados são informativos e não substituem a avaliação clínica do profissional responsável.

### Arquivamento lógico

Os módulos clínicos que possuem arquivamento lógico preservam os registros no banco de dados, evitando exclusões físicas indevidas.

---

## 8. Autenticação e segurança

A aplicação utiliza:

- Spring Security.
- JWT.
- BCrypt.
- Filtro de autenticação JWT.
- Controle de acesso por perfil.
- Verificação de contas ativas.
- Validação de permissões nos serviços.
- Invalidação de tokens após alterações de senha.

### Perfis

- `ADMINISTRADOR`
- `SECRETARIA`
- `PROFISSIONAL`
- `PACIENTE`

As credenciais são centralizadas no modelo `Usuario`.

### Login

```http
POST /api/auth/login
```

Requisições protegidas utilizam:

```http
Authorization: Bearer <token>
```

### Primeiro acesso

```http
POST /api/auth/primeiro-acesso/alterar-senha
```

### Alteração de senha autenticada

```http
POST /api/auth/alterar-senha
```

### Recuperação de senha

O fluxo de recuperação utiliza código temporário enviado por e-mail e depende da configuração SMTP do ambiente.

### Invalidação de tokens

A aplicação utiliza a claim `tokenVersion`, comparada à versão atual do usuário. Alterações de senha incrementam essa versão, invalidando tokens anteriores.

A autenticação em duas etapas (2FA) depende de definição funcional específica e não deve ser considerada implementada apenas pela existência de uma opção visual.

---

## 9. Preferências e configurações

As preferências individuais são persistidas por usuário autenticado.

Endpoint:

```http
GET /api/preferencias
PUT /api/preferencias
```

As preferências contemplam configurações individuais disponíveis na implementação, como tema, idioma, formato de data, notificações, duração padrão de consulta e início da semana.

Salvar preferências de notificação não significa que um serviço de envio de notificações esteja ativo.

---

## 10. Banco de dados

O projeto utiliza MongoDB com Spring Data MongoDB.

Entre as coleções utilizadas estão:

| Coleção | Finalidade |
|---|---|
| `pacientes` | Cadastro de pacientes |
| `responsaveis` | Cadastro de responsáveis |
| `vinculosResponsaveis` | Vínculos entre pacientes e responsáveis |
| `profissionais` | Cadastro de profissionais |
| `vinculos_profissional_paciente` | Vínculos profissionais |
| `disponibilidades` | Horários disponíveis |
| `consultas` | Agendamentos |
| `prontuarios` | Prontuários |
| `diagnosticos` | Diagnósticos |
| `tratamentos` | Tratamentos |
| `exames` | Solicitações e resultados |
| `prescricoes` | Prescrições |
| `preferencias_usuarios` | Preferências individuais |
| `usuarios` | Credenciais e perfis |

A agenda é derivada da coleção `consultas`.

### Configuração

A conexão é definida pela variável:

```text
MONGODB_URI
```

Cada ambiente deve utilizar seu banco de dados correspondente.

Não versionar URIs com credenciais, senhas ou tokens.

---

## 11. Docker e hospedagem

O projeto utiliza Docker multi-stage para compilar e executar a aplicação integrada.

O processo contempla:

1. Node.js para instalação das dependências e compilação do Angular.
2. Java 21 e Maven para construção do backend.
3. Incorporação dos arquivos Angular em `src/main/resources/static/`.
4. Execução da aplicação em uma imagem Java 21 JRE.

### Construção da imagem

```bash
docker build -t neurovita-integrado .
```

### Execução

```bash
docker run --rm --name neurovita-local \
  --env-file .env \
  -p 8080:8080 \
  neurovita-integrado
```

### Variáveis de ambiente

As configurações podem incluir:

```text
MONGODB_URI
JWT_SECRET
PORT
SMTP_ENABLED
SMTP_HOST
SMTP_PORT
SMTP_USERNAME
SMTP_PASSWORD
SMTP_FROM
SMTP_AUTH
SMTP_STARTTLS
NEUROVITA_CONTACT_TO
```

As variáveis SMTP são necessárias para os fluxos de e-mail correspondentes.

### Ambientes Render

| Ambiente | Branch | Serviço |
|---|---|---|
| Desenvolvimento | `develop` | https://neurovita-dev.onrender.com |
| Testes | `testes` | https://neurovita-testes.onrender.com |
| Homologação | `homologacao` | https://neurovita-homologacao.onrender.com |
| Produção | `main` | https://neurovita.onrender.com |

Cada ambiente deve utilizar sua configuração e seu banco de dados correspondentes.

A branch `main` representa a produção.

---

## 12. Executando localmente

### Pré-requisitos

- Java JDK 21.
- Node.js e npm.
- Git.
- MongoDB Atlas ou instância MongoDB acessível.
- Variáveis de ambiente configuradas.
- Docker Desktop, caso deseje executar em container.

### Clonar o projeto

```bash
git clone https://github.com/victorhborges/NeuroVita.git

cd NeuroVita
```

### Selecionar a branch de desenvolvimento

```bash
git switch develop
git pull origin develop
```

### Configurar variáveis no Windows

```cmd
set "MONGODB_URI=SUA_URI_DO_MONGODB"
set "JWT_SECRET=SUA_CHAVE_JWT"
```

Utilize credenciais próprias para o ambiente local. Não publique valores reais no repositório.

### Executar o backend

Windows:

```cmd
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

### Compilar o frontend

```bash
cd frontend
npm ci
npm run build
```

Os arquivos compilados são gerados em:

```text
frontend/dist/neurovita/browser/
```

### Endereços locais

- Aplicação: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/v3/api-docs

---

## 13. Testes e validação

Os testes Java são executados pelo Maven.

### Compilação

```cmd
mvnw.cmd compile
```

### Executar a suíte completa

```cmd
mvnw.cmd test
```

### Executar testes específicos

```cmd
mvnw.cmd -Dtest=ConsultaServiceTest test
```

```cmd
mvnw.cmd -Dtest=AgendaServiceTest test
```

### Empacotamento

```cmd
mvnw.cmd package -DskipTests
```

### Estado da validação

**Última validação informada: 21/11/2026.**

- Suíte completa de testes Java: aprovada.
- `ConsultaServiceTest`: 30 testes aprovados, sem falhas ou erros.
- `AgendaServiceTest`: testes aprovados.
- Regras de normalização e conflito de salas: cobertas por testes automatizados.

Os avisos de inicialização do agente Mockito/OpenJDK não representam, por si só, falha nos testes.

A aprovação dos testes automatizados não substitui os testes funcionais da equipe, nem a validação completa da aplicação com MongoDB Atlas e Render.

---

## 14. Estrutura do projeto

```text
NeuroVita/
├── frontend/
│   ├── src/
│   │   └── app/
│   ├── package.json
│   ├── package-lock.json
│   └── angular.json
│
├── src/
│   ├── main/
│   │   ├── java/com/neurovita/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── static/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/neurovita/
│
├── docs/
├── Dockerfile
├── .dockerignore
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## 15. Git e GitHub

Repositório oficial:

https://github.com/victorhborges/NeuroVita

### Branches

| Branch | Finalidade |
|---|---|
| `develop` | Desenvolvimento e integração |
| `testes` | Validação pela equipe de testes |
| `homologacao` | Validação antes da produção |
| `main` | Produção |

### Fluxo de promoção

```text
develop
   |
   | Pull Request
   v
testes
   |
   | Pull Request
   v
homologacao
   |
   | Pull Request
   v
main
```

As promoções devem ocorrer por Pull Requests, preservando o histórico do Git.

### Comandos úteis

```bash
git branch --show-current
git status --short
git diff --stat
git diff --check
```

### Padrão de commits

```text
feat: nova funcionalidade
fix: correção de problema
docs: atualização da documentação
test: alteração nos testes
refactor: melhoria estrutural
chore: configuração ou manutenção
```

Nunca versionar credenciais, tokens, URIs privadas ou arquivos `.env`.

---

## 16. Próximas etapas

As próximas atividades devem priorizar a conclusão do MVP e a validação integrada:

- [x] Implementar os principais módulos do backend.
- [x] Integrar frontend Angular e backend Spring Boot.
- [x] Implementar vínculos entre pacientes e responsáveis.
- [x] Implementar vínculos profissionais-pacientes.
- [x] Implementar controle de acesso clínico por vínculo ativo.
- [x] Implementar consultas e agenda.
- [x] Implementar validação de conflitos de agendamento.
- [x] Implementar normalização de salas e prevenção de conflitos entre salas.
- [x] Implementar módulos clínicos existentes no backend.
- [x] Implementar autenticação JWT e alteração de senha.
- [x] Implementar persistência de preferências.
- [x] Validar a suíte completa de testes Java.
- [ ] Revisar e concluir a integração de todas as telas com as APIs.
- [ ] Conferir todos os campos obrigatórios do MVP FBR.
- [ ] Validar os fluxos completos com MongoDB Atlas.
- [ ] Validar a versão integrada com Docker.
- [ ] Publicar e validar a versão atual no ambiente `develop` do Render.
- [ ] Disponibilizar a versão para a equipe de testes.
- [ ] Executar os testes funcionais.
- [ ] Promover a versão aprovada para homologação.
- [ ] Publicar a versão final em produção.
- [ ] Definir requisitos ainda pendentes do MVP, como o módulo de Receitas e eventual 2FA.

---

## 17. Projeto acadêmico

**Projeto:** NeuroVita
**Instituição:** Faculdade Brasília
**Curso:** Análise e Desenvolvimento de Sistemas (ADS)
**Área:** Desenvolvimento de Software

Projeto acadêmico com aplicação prática de desenvolvimento backend e frontend, APIs REST, banco de dados, arquitetura de software, segurança e integração de sistemas.

---

## 18. Licença

Projeto desenvolvido para fins acadêmicos.

A utilização, distribuição ou modificação do código deve respeitar as decisões da equipe responsável pelo projeto.