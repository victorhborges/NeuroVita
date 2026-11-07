# 🧠 NeuroVita — Plataforma Integrada

Plataforma desenvolvida como projeto acadêmico da **Faculdade Brasília**, no curso de **Análise e Desenvolvimento de Sistemas (ADS)**.

O NeuroVita é um sistema de gestão de atendimentos psicológicos, composto por frontend Angular, backend Java com Spring Boot e banco de dados MongoDB Atlas.

O frontend é compilado e servido pela própria aplicação Spring Boot, que disponibiliza também a API REST.

> **Status:** backend e frontend integrados localmente. A última execução da suíte Maven apresentou 47 testes aprovados, sem falhas ou erros. A validação completa da versão atual com Docker, MongoDB Atlas real e publicação nos ambientes ainda está pendente.

---

## 🛠️ Tecnologias

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem do backend |
| Spring Boot 4.0.8 | Framework backend |
| Angular 21 | Frontend |
| Node.js / npm | Compilação do frontend |
| MongoDB Atlas | Banco de dados |
| Spring Data MongoDB | Persistência |
| Spring Security | Segurança e autorização |
| JWT | Autenticação |
| BCrypt | Proteção de senhas |
| Jakarta Bean Validation | Validação de dados |
| SpringDoc OpenAPI | Documentação da API |
| Maven | Gerenciamento e build |
| Docker | Containerização |
| Render | Hospedagem |

---

## 📌 Funcionalidades

O projeto contempla:

- Cadastro e gerenciamento de pacientes.
- Cadastro e gerenciamento de responsáveis.
- Múltiplos responsáveis por paciente.
- Vínculos entre pacientes e responsáveis.
- Cadastro e gerenciamento de profissionais.
- Vínculos entre profissionais e pacientes, com histórico.
- Disponibilidades e modalidades de atendimento.
- Consultas, agenda, confirmação, remarcação e cancelamento.
- Prontuário centralizado por paciente.
- Diagnósticos, tratamentos e exames.
- Documentos clínicos e anexos.
- Administradores e secretarias.
- Autenticação JWT e controle de acesso por perfil.
- Primeiro acesso e recuperação de senha.
- Inativação de cadastros, preservando o histórico.
- Compartilhamento de leitura clínica conforme vínculo profissional ativo.
- Preservação da autoria dos registros clínicos.
- Frontend Angular integrado à aplicação Spring Boot.

O mecanismo de documentos clínicos contempla diferentes seções, incluindo acolhimento, assistência social, relatórios escolares, declarações escolares, rastreio, devolutiva e evoluções, conforme os tipos suportados pela implementação.

O módulo de Receitas aguarda a definição dos campos e das regras de negócio.

A existência de uma funcionalidade implementada não substitui sua validação funcional.

---

## 🏗️ Arquitetura

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

O backend Java utiliza Spring Boot e organiza as responsabilidades em:

- `controller`: recebe requisições HTTP.
- `service`: implementa as regras de negócio.
- `repository`: realiza operações de persistência.
- `model`: representa os documentos do banco.
- `dto`: define os dados de entrada e saída da API.
- `security`: contém autenticação e autorização.

### Frontend

O frontend é desenvolvido em Angular.

Seus arquivos compilados são incorporados aos recursos estáticos do Spring Boot em:

```text
src/main/resources/static/
```

A autenticação utiliza JWT. O token é armazenado em `sessionStorage` e enviado nas requisições protegidas por um interceptor HTTP.

A proteção de navegação do Angular não substitui as verificações de autorização do backend.

---

## 📁 Estrutura do projeto

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
├── .dockerignore
├── .gitignore
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## 👥 Pacientes e responsáveis

O módulo de pacientes contempla operações de cadastro, consulta, atualização e inativação.

Principais endpoints:

```text
POST   /api/pacientes
GET    /api/pacientes
GET    /api/pacientes/{id}
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
```

### Regras de negócio

- Nome e data de nascimento são obrigatórios.
- A data de nascimento deve ser anterior à data atual.
- Pacientes menores de idade precisam possuir pelo menos um responsável.
- Pacientes maiores de idade não precisam obrigatoriamente de responsável.
- Um paciente pode possuir vários responsáveis.
- Um responsável pode estar associado a vários pacientes.
- O grau de parentesco é registrado individualmente em cada vínculo.
- O cadastro pessoal do responsável é independente do vínculo com o paciente.
- A remoção de um vínculo não apaga o cadastro pessoal do responsável.
- Responsáveis com vínculos ativos não podem ser excluídos.
- Quando o paciente possui plano de saúde, os campos correspondentes devem ser informados.

Os dados de responsáveis são armazenados separadamente dos pacientes, com os relacionamentos registrados na coleção `vinculosResponsaveis`.

A inativação preserva o cadastro e o histórico relacionado.

---

## 🧑‍⚕️ Profissionais e vínculos com pacientes

O módulo de profissionais contempla cadastro, consulta, atualização, inativação e gerenciamento de disponibilidades.

O profissional pode oferecer atendimento presencial, online ou ambas as modalidades.

### Vínculos profissionais-pacientes

Os relacionamentos são armazenados na coleção:

```text
vinculos_profissional_paciente
```

A estrutura permite:

- Um paciente ser acompanhado por vários profissionais.
- Registrar abertura e encerramento de vínculos.
- Preservar o histórico dos atendimentos.
- Consultar os profissionais vinculados a um paciente.
- Listar pacientes de um profissional considerando os vínculos ativos, independentemente da existência de consultas anteriores.

A abertura e o encerramento dos vínculos são restritos aos perfis autorizados de administrador e secretaria.

O profissional pode consultar a equipe multidisciplinar vinculada ao paciente por meio de:

```text
GET /api/vinculos-profissionais-pacientes/paciente/{pacienteId}
```

A resposta de pacientes também contempla a lista de profissionais vinculados por meio do campo `profissionais`, permitindo que o frontend apresente a equipe responsável sem depender das consultas cadastradas.

### Controle de acesso clínico

O vínculo ativo é utilizado como critério para o compartilhamento de leitura das informações clínicas.

As regras implementadas contemplam:

- Profissionais com vínculo ativo podem consultar informações clínicas compartilhadas.
- Profissionais sem vínculo ativo não possuem acesso de leitura compartilhada.
- O encerramento do vínculo interrompe o acesso compartilhado correspondente.
- O histórico dos vínculos encerrados é preservado.
- A autoria dos registros clínicos é mantida.
- A leitura compartilhada não concede automaticamente permissão para editar ou excluir registros de outros profissionais.

As permissões também são aplicadas aos módulos de diagnósticos, tratamentos e exames.

---

## 📅 Consultas e agenda

O módulo de consultas permite cadastrar, consultar, atualizar, remarcar e cancelar atendimentos.

Principais endpoints:

```text
POST   /api/consultas
GET    /api/consultas
GET    /api/consultas/{id}
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}
PATCH  /api/consultas/{id}/cancelar
```

Consultas por paciente e profissional:

```text
GET /api/consultas/paciente/{pacienteId}
GET /api/consultas/profissional/{profissionalId}
```

### Status

| Status | Significado |
|---|---|
| `PENDENTE` | Consulta aguardando confirmação |
| `CONFIRMADA` | Consulta confirmada |
| `REMARCADA` | Data ou horário alterado |
| `CANCELADA` | Consulta cancelada |

O status inicial é `PENDENTE`. A alteração de data ou horário atualiza o status para `REMARCADA`.

O cancelamento pelo endpoint específico preserva o registro e altera seu status para `CANCELADA`.

O endpoint DELETE permanece disponível para exclusão definitiva, conforme as regras da implementação.

### Regras de agendamento

O backend verifica:

- Existência do paciente e do profissional.
- Disponibilidade do profissional.
- Conflitos de horário do profissional e do paciente.
- Modalidade de atendimento oferecida.
- Estado ativo do paciente e do profissional.
- Permissões do usuário autenticado.

Consultas canceladas não bloqueiam novos agendamentos.

### Agenda

A agenda é construída a partir das consultas, sem uma coleção independente.

```text
GET /api/agenda
GET /api/agenda/dia?data=2026-09-20
GET /api/agenda/profissional/{id}?data=2026-09-20
GET /api/agenda/paciente/{id}
```

---

## 📋 Prontuários e documentos clínicos

O sistema utiliza um prontuário centralizado por paciente, evitando a criação de múltiplos prontuários para a mesma pessoa.

O prontuário reúne informações do acompanhamento clínico, enquanto os registros específicos são organizados nos módulos correspondentes.

### Módulos clínicos

```text
/api/prontuarios
/api/diagnosticos
/api/tratamentos
/api/exames
/api/documentos-clinicos
```

Os módulos possuem operações próprias de consulta e gerenciamento.

### Documentos clínicos

O mecanismo unificado de documentos clínicos contempla diferentes seções, incluindo:

- Acolhimento.
- Assistência social.
- Evolução.
- Relatório escolar.
- Declaração escolar.
- Rastreio.
- Devolutiva.
- Laudos e encaminhamentos.
- Avaliação psicológica.
- Atestados.
- Documentos e anexos.

A consulta pode ser filtrada por seção:

```text
GET /api/documentos-clinicos/paciente/{pacienteId}?secao=ACOLHIMENTO
```

Exemplos de valores de seção:

```text
ACOLHIMENTO
ASSISTENCIA_SOCIAL
RELATORIO_ESCOLAR
DECLARACAO_ESCOLAR
RASTREIO
DEVOLUTIVA
EVOLUCAO
```

Quando o parâmetro `secao` não é informado, a consulta retorna os documentos clínicos do paciente, ordenados cronologicamente.

Os registros contemplam informações de autoria, profissional responsável, data da sessão, tipo de atendimento e atualização.

O mecanismo de anexos possui validações de tamanho e tipo MIME.

O compartilhamento de leitura respeita os vínculos profissionais ativos, preservando a autoria dos registros.

---

## 🔐 Autenticação e segurança

A aplicação utiliza:

- Spring Security.
- JWT.
- BCrypt.
- Filtro de autenticação JWT.
- Modelo centralizado de usuários.
- Controle de acesso por perfil.
- Verificação de contas ativas.
- Validação de permissões nos serviços.

### Perfis do backend

- `ADMINISTRADOR`
- `SECRETARIA`
- `PROFISSIONAL`
- `PACIENTE`

As contas e credenciais são centralizadas no modelo `Usuario`.

As senhas são armazenadas utilizando BCrypt e não devem ser retornadas nas respostas da API.

### Login

```text
POST /api/auth/login
```

Exemplo ilustrativo:

```json
{
  "email": "admin@email.com",
  "senha": "senha-de-exemplo"
}
```

As requisições protegidas utilizam:

```text
Authorization: Bearer <token>
```

### Primeiro acesso

```text
POST /api/auth/primeiro-acesso/alterar-senha
```

O fluxo contempla troca obrigatória de senha provisória, validação da nova senha e atualização das credenciais.

### Recuperação de senha

A recuperação utiliza código temporário enviado por e-mail.

O código possui validade de 10 minutos. O envio depende da configuração SMTP no ambiente.

### Variáveis de segurança

As configurações sensíveis devem ser fornecidas por variáveis de ambiente, nunca armazenadas diretamente no código ou publicadas no Git.

---

## 📖 Swagger / OpenAPI

A documentação interativa da API utiliza SpringDoc OpenAPI.

Localmente:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/v3/api-docs

Para testar endpoints protegidos:

1. Realize o login.
2. Copie o JWT retornado.
3. Clique em **Authorize** no Swagger.
4. Informe o token conforme o formato solicitado.
5. Execute os endpoints autorizados.

Não compartilhe senhas, tokens ou credenciais reais.

---

## 🗄️ Banco de dados

O projeto utiliza MongoDB Atlas com Spring Data MongoDB.

Entre as coleções utilizadas estão:

| Coleção | Finalidade |
|---|---|
| `pacientes` | Cadastro de pacientes |
| `responsaveis` | Cadastro de responsáveis |
| `vinculosResponsaveis` | Relação entre pacientes e responsáveis |
| `profissionais` | Cadastro de profissionais |
| `vinculos_profissional_paciente` | Relação e histórico de vínculos profissionais |
| `disponibilidades` | Horários disponíveis |
| `consultas` | Agendamentos |
| `prontuarios` | Prontuários centrais |
| `diagnosticos` | Diagnósticos |
| `tratamentos` | Tratamentos |
| `exames` | Exames |
| `usuarios` | Credenciais e perfis |

A agenda é derivada das consultas, não possuindo coleção própria.

### Conexão

A URI é configurada pela variável:

```text
MONGODB_URI
```

Cada ambiente deve utilizar o banco de dados correspondente, mantendo a separação entre desenvolvimento, testes, homologação e produção.

**Segurança:**
- Não versionar URIs com credenciais.
- Não publicar senhas ou tokens.
- Não versionar arquivos `.env`.
- Utilizar variáveis de ambiente nos serviços hospedados.

A validação dos fluxos mais recentes com MongoDB Atlas real ainda precisa ser concluída. Os testes unitários com mocks não comprovam a persistência no banco real.

---

## 🐳 Docker e deploy

O projeto utiliza Docker multi-stage para compilar e executar a aplicação integrada.

O processo contempla:

1. Node.js para instalação das dependências e compilação do Angular.
2. Java 21 e Maven para construção do backend.
3. Incorporação dos arquivos Angular em `src/main/resources/static/`.
4. Execução da aplicação em uma imagem Java 21 JRE.

### Construção

```bash
docker build -t neurovita-integrado .
```

### Execução

```bash
docker run --rm --name neurovita-local --env-file .env -p 8080:8080 neurovita-integrado
```

### Variáveis de ambiente

Entre as principais configurações:

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
```

O SMTP permanece desabilitado por padrão.

A validação da imagem Docker e a publicação da versão integrada atual ainda estão pendentes.

---

## 🚀 Executando localmente

### Pré-requisitos

- Java JDK 21.
- Node.js e npm.
- Git.
- MongoDB Atlas ou outra instância MongoDB acessível.
- Variáveis de ambiente configuradas.
- Docker Desktop, caso deseje executar em container.

### Clonar o projeto

```bash
git clone https://github.com/victorhborges/NeuroVita.git

cd NeuroVita
```

### Atualizar a branch develop

```bash
git switch develop
git pull origin develop
```

### Configurar as variáveis

Configure `MONGODB_URI` e `JWT_SECRET` no ambiente local.

No Windows:

```cmd
set "MONGODB_URI=SUA_URI_DO_MONGODB"
set "JWT_SECRET=SUA_CHAVE_JWT"
```

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

O caminho esperado dos arquivos compilados é:

```text
frontend/dist/neurovita/browser/
```

Após a compilação, os arquivos devem ser incorporados aos recursos estáticos do backend.

No Windows, a partir da raiz:

```cmd
xcopy /E /I /Y frontend\dist\neurovita\browser src\main\resources\static
```

Sempre que o frontend for alterado, é necessário recompilar e copiar os arquivos antes de executar a versão integrada.

A aplicação local estará disponível em:

- Frontend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- OpenAPI: http://localhost:8080/v3/api-docs

---

## 🧪 Testes e validação

Os comandos Maven podem ser executados na raiz do projeto.

### Compilação

Windows:

```cmd
mvnw.cmd compile
```

Linux/macOS:

```bash
./mvnw compile
```

### Testes

Windows:

```cmd
mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

### Empacotamento

Windows:

```cmd
mvnw.cmd -DskipTests package
```

Linux/macOS:

```bash
./mvnw -DskipTests package
```

### Última validação local — 07/11/2026

| Verificação | Resultado |
|---|---:|
| Testes executados | 47 |
| Falhas | 0 |
| Erros | 0 |
| Ignorados | 0 |
| Build Maven | `BUILD SUCCESS` |
| `git diff --check` | Sem erros |

A suíte contempla testes de aplicação, consultas, diagnósticos, documentos clínicos, exames, pacientes, profissionais, prontuários, secretarias, tratamentos e vínculos profissionais-pacientes.

Os testes unitários utilizam mocks em parte das operações. Portanto, o resultado positivo não substitui a validação funcional e a verificação da persistência no MongoDB Atlas real.

A equipe designada é responsável pela execução dos testes funcionais completos.

---

## 📊 Status do projeto

| Área | Situação |
|---|---|
| Backend Java / Spring Boot | Implementado |
| Frontend Angular integrado | Implementado localmente |
| MongoDB Atlas | Configurado; validação integral pendente |
| Pacientes e responsáveis | Implementados |
| Múltiplos responsáveis | Implementado |
| Profissionais e disponibilidades | Implementados |
| Vínculos profissionais-pacientes | Implementados |
| Histórico de vínculos | Implementado |
| Permissões clínicas por vínculo | Implementadas localmente |
| Prontuário centralizado | Implementado |
| Documentos clínicos e anexos | Implementados |
| Diagnósticos | Implementados |
| Tratamentos | Implementados |
| Exames | Implementados |
| Consultas e agenda | Implementadas |
| Autenticação JWT | Implementada |
| Recuperação de senha | Implementada; requer SMTP |
| Swagger / OpenAPI | Implementado |
| Testes Maven | 47 aprovados na última execução |
| Docker da versão atual | Validação pendente |
| Deploy integrado atual | Pendente |
| Testes funcionais completos | Pendentes |
| Receitas | Aguardando requisitos |

---

## 🌿 Git e GitHub

Repositório oficial:

https://github.com/victorhborges/NeuroVita

### Organização das branches

| Branch | Finalidade |
|---|---|
| `develop` | Desenvolvimento e integração |
| `testes` | Validação pela equipe de testes |
| `homologacao` | Validação final antes da produção |
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

As promoções devem ocorrer por Pull Requests, preservando o histórico Git.

A branch `main` é a branch oficial de produção. Não utilizar uma branch separada chamada `producao`.

### Fluxo de trabalho

1. Atualizar a branch `develop`.
2. Criar uma branch para a tarefa.
3. Realizar as alterações.
4. Executar os testes aplicáveis.
5. Revisar o código.
6. Atualizar o README quando houver mudanças relevantes.
7. Fazer commit e push da branch de trabalho.
8. Abrir Pull Request para `develop`.

Comandos úteis:

```bash
git branch --show-current
git status
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

## 🗺️ Próximas etapas

- [x] Implementar módulos principais do backend.
- [x] Integrar frontend Angular e backend Spring Boot.
- [x] Implementar múltiplos responsáveis por paciente.
- [x] Implementar vínculos profissionais-pacientes.
- [x] Implementar histórico de vínculos.
- [x] Implementar compartilhamento clínico por vínculo ativo.
- [x] Preservar autoria dos registros clínicos.
- [x] Implementar consulta de documentos por seção.
- [x] Criar testes para diagnósticos, exames e tratamentos.
- [x] Executar suíte Maven com 47 testes aprovados.
- [ ] Validar a versão atual com Docker.
- [ ] Validar os fluxos com MongoDB Atlas real.
- [ ] Revisar integralmente as permissões dos endpoints.
- [ ] Validar a integração com o frontend.
- [ ] Disponibilizar a versão atual para a equipe de testes.
- [ ] Executar testes funcionais.
- [ ] Promover a versão aprovada para homologação.
- [ ] Publicar a versão final em produção.
- [ ] Definir os requisitos do módulo de Receitas.

---

## 🎓 Projeto acadêmico

**Projeto:** NeuroVita
**Instituição:** Faculdade Brasília
**Curso:** Análise e Desenvolvimento de Sistemas (ADS)
**Área:** Desenvolvimento de Software

Projeto acadêmico com aplicação prática de programação, APIs REST, desenvolvimento backend, frontend, banco de dados, arquitetura de software, segurança e integração de sistemas.

---

## 📄 Licença

Projeto desenvolvido para fins acadêmicos.

A utilização, distribuição ou modificação do código deve respeitar as decisões da equipe responsável pelo projeto.