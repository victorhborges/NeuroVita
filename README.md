# 🧠 NeuroVita - Backend

Backend da plataforma **NeuroVita**, desenvolvida para auxiliar no gerenciamento de atendimentos psicológicos, pacientes, profissionais, consultas, agendas e informações relacionadas ao atendimento clínico.

O projeto está sendo desenvolvido como parte das atividades acadêmicas da faculdade, com foco na aplicação prática de conceitos de **Programação Orientada a Objetos, desenvolvimento de APIs REST, arquitetura de software, banco de dados, segurança e boas práticas de desenvolvimento**.

> 🚧 **Status:** Em desenvolvimento

> ☕ **Linguagem:** Java 21

> 🌱 **Framework:** Spring Boot 4.0.8

> 🗄️ **Banco:** MongoDB

> 🐳 **Containerização:** Docker

> ☁️ **Hospedagem planejada:** Render

---

## 📌 Sobre o projeto

O NeuroVita é uma plataforma voltada para instituições e profissionais da área de psicologia, com o objetivo de centralizar e organizar informações relacionadas aos atendimentos.

O backend é responsável por disponibilizar uma **API REST**, permitindo a comunicação entre o frontend e os serviços da aplicação.

Entre os principais módulos estão:

* 👤 Pacientes
* 🧑‍⚕️ Profissionais
* 📅 Consultas
* 🗓️ Agendas
* ⏰ Disponibilidades
* 📋 Prontuários
* 🧠 Diagnósticos
* 💊 Tratamentos
* 🧪 Exames
* 👨‍💼 Administradores
* 🔐 Autenticação e segurança

O módulo de **Receitas** ainda aguarda a definição dos campos e regras no Figma.

---

## 🎯 Objetivos

O backend tem como principais objetivos:

* Disponibilizar uma API REST para o NeuroVita;
* Gerenciar pacientes, profissionais e consultas;
* Controlar agendas e disponibilidades;
* Implementar regras de negócio;
* Implementar autenticação e autorização;
* Proteger informações sensíveis;
* Permitir integração com o frontend;
* Aplicar boas práticas de desenvolvimento;
* Preparar a aplicação para execução em ambiente de produção;
* Disponibilizar a aplicação em ambiente de hospedagem através de containers.

---

## 🛠️ Tecnologias

### Backend

* ☕ Java 21
* 🌱 Spring Boot 4.0.8
* 🌐 Spring Web MVC
* 🗄️ Spring Data MongoDB
* 📦 Maven

### Segurança

* 🔐 Spring Security
* 🔑 JWT
* 🔒 BCrypt

### Documentação e testes

* 📖 Swagger / OpenAPI
* 🧪 JUnit
* 📬 Postman

### Containerização e hospedagem

* 🐳 Docker
* ☁️ Render

### Versionamento

* 🔀 Git
* 🐙 GitHub

### Banco de dados

* 🍃 MongoDB

---

## 🏗️ Arquitetura

O backend utiliza uma arquitetura organizada em camadas, separando as responsabilidades da aplicação.

```text
Cliente / Frontend / Postman

            ↓

       Controller

            ↓

         Service

            ↓

       Repository

            ↓

         MongoDB
```

### Controller

Responsável por receber as requisições HTTP e retornar as respostas da API.

Exemplos:

```text
GET    /api/pacientes
POST   /api/pacientes
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
```

### Service

Responsável pela lógica e pelas regras de negócio da aplicação.

Exemplos:

* validação de dados;
* regras de agendamento;
* verificação de conflitos de horários;
* controle das operações do sistema.

### Repository

Responsável pelo acesso aos dados utilizando **Spring Data MongoDB**.

O Spring Data fornece métodos de acesso ao banco, como:

```text
save()

findAll()

findById()

deleteById()
```

### Model

Representa os objetos utilizados pela aplicação e os documentos armazenados no MongoDB.

Exemplos:

* Paciente
* Profissional
* Consulta
* Disponibilidade
* Prontuário
* Diagnóstico
* Tratamento
* Exame
* Administrador

### DTO

Os DTOs (**Data Transfer Objects**) são utilizados para separar os dados recebidos e enviados pela API dos documentos utilizados internamente pela aplicação.

Exemplo:

```text
PacienteRequest
PacienteResponse
```

Essa separação facilita a organização da API e permite que o modelo de persistência evolua de forma independente do contrato da API.

---

## 📁 Estrutura atual

```text
neurovita/

│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── neurovita/
│   │   │           │
│   │   │           ├── config/
│   │   │           │   └── OpenApiConfig.java
│   │   │           │
│   │   │           ├── controller/
│   │   │           │   ├── TesteController.java
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── AdministradorController.java
│   │   │           │   ├── PacienteController.java
│   │   │           │   ├── ProfissionalController.java
│   │   │           │   ├── DisponibilidadeController.java
│   │   │           │   ├── ConsultaController.java
│   │   │           │   ├── AgendaController.java
│   │   │           │   ├── ProntuarioController.java
│   │   │           │   ├── DiagnosticoController.java
│   │   │           │   ├── TratamentoController.java
│   │   │           │   └── ExameController.java
│   │   │           │
│   │   │           ├── dto/
│   │   │           │   ├── LoginRequest.java
│   │   │           │   ├── LoginResponse.java
│   │   │           │   ├── AdministradorRequest.java
│   │   │           │   ├── AdministradorResponse.java
│   │   │           │   ├── PacienteRequest.java
│   │   │           │   ├── PacienteResponse.java
│   │   │           │   └── ...
│   │   │           │
│   │   │           ├── model/
│   │   │           │   ├── Administrador.java
│   │   │           │   ├── Paciente.java
│   │   │           │   ├── Profissional.java
│   │   │           │   ├── Disponibilidade.java
│   │   │           │   ├── Consulta.java
│   │   │           │   ├── Prontuario.java
│   │   │           │   ├── Diagnostico.java
│   │   │           │   ├── Tratamento.java
│   │   │           │   └── Exame.java
│   │   │           │
│   │   │           ├── repository/
│   │   │           │   ├── AdministradorRepository.java
│   │   │           │   ├── PacienteRepository.java
│   │   │           │   ├── ProfissionalRepository.java
│   │   │           │   ├── DisponibilidadeRepository.java
│   │   │           │   ├── ConsultaRepository.java
│   │   │           │   ├── ProntuarioRepository.java
│   │   │           │   ├── DiagnosticoRepository.java
│   │   │           │   ├── TratamentoRepository.java
│   │   │           │   └── ExameRepository.java
│   │   │           │
│   │   │           ├── security/
│   │   │           │   ├── JwtService.java
│   │   │           │   ├── JwtAuthenticationFilter.java
│   │   │           │   └── SecurityConfig.java
│   │   │           │
│   │   │           ├── service/
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── AdministradorService.java
│   │   │           │   ├── PacienteService.java
│   │   │           │   ├── ProfissionalService.java
│   │   │           │   ├── DisponibilidadeService.java
│   │   │           │   ├── ConsultaService.java
│   │   │           │   ├── AgendaService.java
│   │   │           │   ├── ProntuarioService.java
│   │   │           │   ├── DiagnosticoService.java
│   │   │           │   ├── TratamentoService.java
│   │   │           │   └── ExameService.java
│   │   │           │
│   │   │           └── NeurovitaApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── .gitignore
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

A estrutura será ampliada conforme novos módulos e funcionalidades forem implementados.

---

# 👤 Paciente

O módulo de pacientes foi implementado seguindo a arquitetura em camadas do backend.

### Funcionalidades

* 🟢 Cadastrar paciente
* 🟢 Listar pacientes
* 🟢 Buscar paciente por ID
* 🟢 Atualizar paciente
* 🟢 Remover paciente

### Endpoints

```text
POST   /api/pacientes
GET    /api/pacientes
GET    /api/pacientes/{id}
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
```

### Estrutura do módulo

```text
Paciente
├── Model
├── Request DTO
├── Response DTO
├── Repository
├── Service
└── Controller
```

O módulo utiliza DTOs para separar os dados recebidos pela API dos documentos persistidos no MongoDB.

---

# 🧑‍⚕️ Profissional

Módulo responsável pelo gerenciamento dos profissionais que realizam os atendimentos.

### Funcionalidades

* Cadastrar profissional
* Listar profissionais
* Buscar profissional
* Atualizar profissional
* Remover profissional
* Vincular disponibilidades
* Consultar disponibilidades
* Remover disponibilidade vinculada

### Endpoints

```text
POST   /api/profissionais
GET    /api/profissionais
GET    /api/profissionais/{id}
PUT    /api/profissionais/{id}
DELETE /api/profissionais/{id}
```

### Disponibilidades do profissional

```text
POST   /api/profissionais/{profissionalId}/disponibilidades/{disponibilidadeId}

GET    /api/profissionais/{profissionalId}/disponibilidades

DELETE /api/profissionais/{profissionalId}/disponibilidades/{disponibilidadeId}
```

---

# ⏰ Disponibilidade

Responsável pelos horários em que cada profissional está disponível para atendimento.

### Funcionalidades

* Cadastrar disponibilidade
* Consultar disponibilidades
* Buscar disponibilidade por ID
* Atualizar disponibilidade
* Remover disponibilidade

### Endpoints

```text
POST   /api/disponibilidades
GET    /api/disponibilidades
GET    /api/disponibilidades/{id}
PUT    /api/disponibilidades/{id}
DELETE /api/disponibilidades/{id}
```

---

# 📅 Consulta

Módulo responsável pelo agendamento e gerenciamento das consultas.

### Funcionalidades

* Agendar consulta
* Listar consultas
* Buscar consulta
* Atualizar consulta
* Remover consulta
* Consultar consultas de um paciente
* Consultar consultas de um profissional

### Endpoints

```text
POST   /api/consultas
GET    /api/consultas
GET    /api/consultas/{id}
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}
```

### Consultas por paciente

```text
GET /api/consultas/paciente/{pacienteId}
```

### Consultas por profissional

```text
GET /api/consultas/profissional/{profissionalId}
```

A consulta possui informações relacionadas a:

```text
Paciente
Profissional
Data e horário
Tipo de atendimento
Local
Modalidade
Duração
```

---

# 🗓️ Agenda

A agenda é construída a partir das consultas cadastradas.

Não existe um documento separado de `Agenda` no MongoDB.

O módulo consulta as informações de `Consulta` para organizar os atendimentos por data, profissional ou paciente.

### Endpoints

```text
GET /api/agenda
```

Consultar agenda de um dia:

```text
GET /api/agenda/dia?data=2026-09-20
```

Consultar agenda de um profissional:

```text
GET /api/agenda/profissional/{id}?data=2026-09-20
```

Consultar agenda de um paciente:

```text
GET /api/agenda/paciente/{id}
```

Essa abordagem evita duplicação de informações no banco de dados.

---

# 📋 Prontuário

Módulo responsável pelo armazenamento das informações relacionadas ao acompanhamento clínico do paciente.

### Informações

* Paciente
* Profissional
* Consulta
* Queixa principal
* Avaliação
* Plano de tratamento
* Prescrição

### Endpoints

```text
POST /api/prontuarios
GET  /api/prontuarios/{id}
PUT  /api/prontuarios/{id}
GET  /api/prontuarios/paciente/{pacienteId}
```

---

# 🧠 Diagnóstico

Módulo responsável pelo gerenciamento dos diagnósticos relacionados aos pacientes.

### Informações

* Paciente
* Profissional
* Descrição

### Endpoints

```text
POST   /api/diagnosticos
GET    /api/diagnosticos
GET    /api/diagnosticos/{id}
PUT    /api/diagnosticos/{id}
DELETE /api/diagnosticos/{id}
GET    /api/diagnosticos/paciente/{pacienteId}
```

---

# 💊 Tratamento

Módulo responsável pelo gerenciamento dos tratamentos definidos para os pacientes.

### Informações

* Paciente
* Profissional
* Descrição
* Objetivo
* Frequência
* Duração
* Observações

### Endpoints

```text
POST   /api/tratamentos
GET    /api/tratamentos
GET    /api/tratamentos/{id}
PUT    /api/tratamentos/{id}
DELETE /api/tratamentos/{id}
GET    /api/tratamentos/paciente/{pacienteId}
```

Os campos de frequência e duração são representados atualmente como texto, podendo ser ajustados posteriormente conforme as regras definitivas do sistema.

---

# 🧪 Exame

Módulo responsável pelo gerenciamento dos exames relacionados aos pacientes.

### Informações

* Paciente
* Profissional
* Nome
* Tipo
* Data de realização
* Resultado
* Observações

### Endpoints

```text
POST   /api/exames
GET    /api/exames
GET    /api/exames/{id}
PUT    /api/exames/{id}
DELETE /api/exames/{id}
GET    /api/exames/paciente/{pacienteId}
```

---

# 💊 Receita

O módulo de receitas ainda **não foi implementado**.

A implementação está aguardando a definição dos campos e regras no Figma.

Dessa forma, os campos finais não foram definidos antecipadamente para evitar uma implementação diferente do requisito visual e funcional do projeto.

---

# 👨‍💼 Administrador

Módulo responsável pelo gerenciamento dos administradores do sistema.

### Funcionalidades

* Cadastrar administrador
* Listar administradores
* Buscar administrador
* Atualizar administrador
* Remover administrador
* Realizar login

### Endpoints

```text
POST   /api/administradores
GET    /api/administradores
GET    /api/administradores/{id}
PUT    /api/administradores/{id}
DELETE /api/administradores/{id}
```

As senhas dos administradores não são retornadas pelos DTOs de resposta.

---

# 🔐 Autenticação e Segurança

O NeuroVita utiliza **Spring Security** para proteger os endpoints da API.

A autenticação utiliza **JWT (JSON Web Token)**.

O fluxo básico é:

```text
Login
  ↓
Validação do e-mail
  ↓
Validação da senha com BCrypt
  ↓
Geração do JWT
  ↓
Cliente recebe o token
  ↓
Token enviado nas próximas requisições
  ↓
JwtAuthenticationFilter
  ↓
Validação do token
  ↓
Acesso ao endpoint protegido
```

### Login

Endpoint público:

```text
POST /api/auth/login
```

Exemplo de requisição:

```json
{
  "email": "admin@email.com",
  "senha": "123456"
}
```

Após a autenticação, a API retorna um token JWT.

Nas requisições protegidas, o token deve ser enviado no cabeçalho:

```text
Authorization: Bearer <token>
```

### Rotas públicas

Atualmente, as principais rotas públicas incluem:

```text
POST /api/auth/login

GET /teste
```

As demais rotas da API são protegidas pela configuração de segurança.

---

# 🔑 JWT

O JWT é utilizado para manter a autenticação do usuário entre as requisições.

O projeto possui:

* Serviço de geração de tokens;
* Filtro de autenticação JWT;
* Validação do token;
* Integração com Spring Security;
* Chave secreta configurada por variável de ambiente.

A chave utilizada para assinar os tokens não deve ser armazenada diretamente no código-fonte.

---

# 🔒 BCrypt

As senhas dos administradores são armazenadas utilizando **BCrypt**.

O BCrypt realiza o hash da senha antes da persistência.

Dessa forma, a senha original não precisa ser armazenada diretamente no banco de dados.

---

# ⚙️ Variáveis de ambiente

Informações sensíveis devem ser configuradas através de variáveis de ambiente.

Exemplo:

```text
JWT_SECRET
MONGODB_URI
```

### JWT_SECRET

Utilizada para a assinatura dos tokens JWT.

### MONGODB_URI

Utilizada para definir a conexão com o MongoDB.

As credenciais e informações de acesso ao banco nunca devem ser enviadas para o GitHub.

---

# 📖 Swagger / OpenAPI

A API possui documentação utilizando **Swagger / OpenAPI**.

Com a aplicação executando localmente:

### Swagger UI

```text
http://localhost:8080/swagger-ui/index.html
```

Também pode ser acessado através de:

```text
http://localhost:8080/swagger-ui.html
```

### OpenAPI

```text
http://localhost:8080/v3/api-docs
```

O Swagger permite visualizar e testar os endpoints da API diretamente pelo navegador.

---

# 🐳 Docker

O projeto possui um `Dockerfile` para facilitar a execução e o deploy da aplicação em ambientes que utilizam containers.

A imagem utiliza **Java 21** e realiza o build da aplicação através do Maven Wrapper.

O container disponibiliza a aplicação na porta:

```text
8080
```

O Dockerfile também permite que a aplicação seja preparada para serviços de hospedagem compatíveis com containers, como o Render.

---

# 🗄️ Banco de dados

O backend utiliza **MongoDB** como banco de dados.

Os documentos utilizados pela aplicação incluem informações relacionadas a:

* Administradores;
* Profissionais;
* Pacientes;
* Consultas;
* Disponibilidades;
* Prontuários;
* Diagnósticos;
* Tratamentos;
* Exames;
* Outros módulos definidos durante o desenvolvimento.

A conexão com o banco é configurada através da variável de ambiente:

```text
MONGODB_URI
```

Durante o desenvolvimento local, a disponibilidade do MongoDB depende da configuração do ambiente utilizado.

> ⚠️ Dados reais de acesso ao banco nunca devem ser enviados para o GitHub.

---

# 🧪 Testes

Os testes automatizados fazem parte do projeto e são executados através do Maven.

Para executar os testes:

```bash
mvn test
```

Ou utilizando o Maven Wrapper:

### Windows

```bash
mvnw.cmd test
```

### Linux / macOS

```bash
./mvnw test
```

Atualmente, o projeto possui testes básicos para validação do contexto da aplicação.

A implementação de testes mais abrangentes será desenvolvida conforme a evolução do projeto e a organização da equipe responsável por testes.

---

# 🔄 Reimplementação em Java

O backend anterior do projeto foi desenvolvido utilizando:

```text
Node.js
JavaScript
Express.js
MySQL
```

O novo backend está sendo **reimplementado em Java**, utilizando:

```text
Java
Spring Boot
Spring Web MVC
Spring Data MongoDB
MongoDB
Spring Security
JWT
```

A implementação utiliza a documentação e o backend anterior como referência.

O objetivo não é simplesmente traduzir o código JavaScript para Java, mas **reorganizar e adaptar o sistema ao ecossistema Java/Spring Boot**, aplicando uma arquitetura adequada para uma API REST.

---

# 🚀 Executando o projeto localmente

## Pré-requisitos

* Java JDK 21 ou superior;
* Maven;
* Git;
* MongoDB configurado no ambiente, quando forem utilizadas funcionalidades que dependem da persistência;
* Acesso ao GitHub, caso seja necessário clonar o projeto.

Verificar Java:

```bash
java -version
```

Verificar Maven:

```bash
mvn -version
```

---

## Clonar o projeto

```bash
git clone https://github.com/victorhborges/NeuroVita.git
```

Entrar na pasta:

```bash
cd NeuroVita
```

---

## Executar a aplicação

Utilizando Maven:

```bash
mvn spring-boot:run
```

Ou utilizando o Maven Wrapper no Windows:

```bash
mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

Por padrão, a aplicação utiliza:

```text
http://localhost:8080
```

---

# 🧪 Endpoint de teste

Endpoint:

```text
GET /teste
```

URL local:

```text
http://localhost:8080/teste
```

Resposta esperada:

```text
API NeuroVita funcionando!
```

Esse endpoint é utilizado para verificar rapidamente se a aplicação está em execução.

---

# 🔗 Integração com o Frontend

O backend será consumido pelo frontend através de requisições HTTP utilizando a API REST.

Os endpoints utilizam JSON para comunicação entre as aplicações.

Exemplo:

```text
Frontend
   ↓
HTTP / JSON
   ↓
NeuroVita API
   ↓
Spring Boot
   ↓
MongoDB
```

A estrutura da API está sendo desenvolvida considerando a integração com o frontend do NeuroVita.

---

# 📊 Status do desenvolvimento

| Módulo                   | Status                     |
| ------------------------ | -------------------------- |
| Estrutura inicial Java   | 🟢 Concluído               |
| Configuração Spring Boot | 🟢 Concluído               |
| Spring Web MVC           | 🟢 Concluído               |
| Spring Data MongoDB      | 🟢 Concluído               |
| Endpoint de teste        | 🟢 Concluído               |
| Model Paciente           | 🟢 Concluído               |
| DTOs Paciente            | 🟢 Concluído               |
| Repository Paciente      | 🟢 Concluído               |
| Service Paciente         | 🟢 Concluído               |
| Controller Paciente      | 🟢 Concluído               |
| CRUD Paciente            | 🟢 Concluído               |
| Profissionais            | 🟢 Concluído               |
| Disponibilidades         | 🟢 Concluído               |
| Consultas                | 🟢 Concluído               |
| Agenda                   | 🟢 Concluído               |
| Prontuários              | 🟢 Concluído               |
| Diagnósticos             | 🟢 Concluído               |
| Tratamentos              | 🟢 Concluído               |
| Exames                   | 🟢 Concluído               |
| Administradores          | 🟢 Concluído               |
| Autenticação             | 🟢 Concluído               |
| JWT                      | 🟢 Concluído               |
| BCrypt                   | 🟢 Concluído               |
| Spring Security          | 🟢 Concluído               |
| Swagger/OpenAPI          | 🟢 Concluído               |
| Dockerfile               | 🟢 Concluído               |
| Testes básicos           | 🟢 Concluído               |
| Receita                  | ⚪ Aguardando requisitos    |
| Testes completos         | 🟡 Em desenvolvimento      |
| Integração com frontend  | ⚪ Planejado                |
| Deploy no Render         | 🔵 Em preparação           |
| Banco de produção        | ⚪ Pendente de configuração |

### Legenda

* 🟢 Concluído
* 🟡 Em desenvolvimento
* 🔵 Em preparação
* ⚪ Planejado / pendente

---

# 🗺️ Roadmap

## Etapa 1 — Estrutura

* [x] Criar projeto Spring Boot
* [x] Configurar Maven
* [x] Configurar Spring Web MVC
* [x] Configurar Spring Data MongoDB
* [x] Criar estrutura de pacotes
* [x] Criar primeiro endpoint

## Etapa 2 — API

* [x] Criar Model Paciente
* [x] Criar Repository Paciente
* [x] Criar Service Paciente
* [x] Criar Controller Paciente
* [x] Criar DTOs de Paciente
* [x] Finalizar CRUD de Paciente
* [x] Implementar Profissional
* [x] Implementar Disponibilidade
* [x] Implementar Consulta
* [x] Implementar Agenda
* [x] Implementar Prontuário
* [x] Implementar Diagnóstico
* [x] Implementar Tratamento
* [x] Implementar Exame
* [x] Implementar Administrador
* [ ] Implementar Receita após definição dos requisitos

## Etapa 3 — Segurança

* [x] Spring Security
* [x] JWT
* [x] BCrypt
* [x] Proteção dos endpoints
* [x] Autenticação
* [ ] Refinar regras de autorização por perfil

## Etapa 4 — Testes e documentação

* [x] Testes básicos
* [x] Swagger / OpenAPI
* [ ] Testes unitários completos
* [ ] Testes da API
* [ ] Testes de integração
* [ ] Integração com frontend

## Etapa 5 — Deploy

* [x] Criar Dockerfile
* [ ] Ajustar configuração para produção
* [ ] Configurar variáveis de ambiente
* [ ] Configurar MongoDB de produção
* [ ] Criar serviço no Render
* [ ] Realizar deploy
* [ ] Testar API em produção

---

# 🌿 Git e GitHub

O projeto utiliza Git para controle de versão e GitHub para armazenamento e colaboração.

Criar uma nova branch:

```bash
git checkout -b feature/nome-da-feature
```

Adicionar alterações:

```bash
git add .
```

Criar commit:

```bash
git commit -m "feat: implementa cadastro de pacientes"
```

Enviar para o GitHub:

```bash
git push origin feature/nome-da-feature
```

### Padrão de commits

```text
feat: nova funcionalidade

fix: correção de problema

docs: alteração na documentação

test: adição ou alteração de testes

refactor: melhoria na estrutura do código

chore: configuração ou manutenção do projeto
```

---

# 📌 Boas práticas

Durante o desenvolvimento são priorizados:

* Programação Orientada a Objetos;
* Separação de responsabilidades;
* Princípios SOLID;
* Código legível;
* Validação de dados;
* Tratamento de exceções;
* Uso de DTOs;
* Padronização dos endpoints;
* Segurança das informações;
* Testes;
* Documentação;
* Commits organizados;
* Uso de variáveis de ambiente para informações sensíveis;
* Separação entre configuração de desenvolvimento e produção.

---

# 🎓 Projeto acadêmico

**Projeto:** NeuroVita

**Contexto:** Projeto acadêmico desenvolvido na Faculdade Brasília

**Curso:** Análise e Desenvolvimento de Sistemas

**Área:** Tecnologia / Desenvolvimento de Software

O projeto possui finalidade acadêmica e prática, buscando aplicar conhecimentos de:

* Programação;
* Desenvolvimento de APIs;
* Banco de dados;
* Arquitetura de software;
* Programação Orientada a Objetos;
* Segurança;
* Desenvolvimento backend;
* Boas práticas de desenvolvimento.

---

# 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos.

A utilização, distribuição ou modificação do código deverá respeitar as decisões definidas pela equipe responsável pelo projeto.
