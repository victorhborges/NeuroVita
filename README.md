# 🧠 NeuroVita - Backend

Backend da plataforma **NeuroVita**, desenvolvida para auxiliar no gerenciamento de atendimentos psicológicos, pacientes, profissionais, consultas, agendas e informações relacionadas ao atendimento clínico.

O projeto está sendo desenvolvido como parte das atividades acadêmicas da faculdade, com foco na aplicação prática de **Programação Orientada a Objetos, desenvolvimento de APIs REST, arquitetura de software, banco de dados, segurança e boas práticas de desenvolvimento**.

> 🚧 **Status:** Em desenvolvimento
> ☕ **Linguagem:** Java 21
> 🌱 **Framework:** Spring Boot 4.0.8
> 🗄️ **Banco:** MongoDB
> 📖 **Documentação:** Swagger / OpenAPI
> ☁️ **Hospedagem planejada:** Render

---

## 📌 Sobre o projeto

O NeuroVita é uma plataforma voltada para instituições e profissionais da área de psicologia, com o objetivo de centralizar e organizar informações relacionadas aos atendimentos.

O backend é responsável por disponibilizar uma **API REST**, permitindo a comunicação entre o frontend e os serviços da aplicação.

Atualmente, os principais módulos implementados são:

* 👤 Pacientes
* 🧑‍⚕️ Profissionais
* ⏰ Disponibilidades
* 📅 Consultas
* 🗓️ Agenda
* 📋 Prontuários
* 🩺 Diagnósticos
* 📝 Tratamentos
* 🧪 Exames

Outros módulos, como autenticação, administradores e receitas, serão implementados conforme as definições do projeto.

---

## 🎯 Objetivos

O backend tem como principais objetivos:

* Disponibilizar uma API REST para o NeuroVita;
* Gerenciar pacientes e profissionais;
* Gerenciar consultas e disponibilidades;
* Organizar a agenda dos atendimentos;
* Gerenciar informações clínicas;
* Implementar regras de negócio;
* Implementar autenticação e autorização;
* Proteger informações sensíveis;
* Permitir integração com o frontend;
* Aplicar boas práticas de desenvolvimento;
* Preparar a aplicação para execução em ambiente de produção.

---

## 🛠️ Tecnologias

### Backend

* ☕ Java 21
* 🌱 Spring Boot 4.0.8
* 🌐 Spring Web MVC
* 🗄️ Spring Data MongoDB
* 📦 Maven

### Segurança — planejado

* 🔐 Spring Security
* 🔑 JWT

### Testes e documentação

* 🧪 JUnit
* 📬 Postman
* 📖 Swagger / OpenAPI

### Versionamento e hospedagem

* 🔀 Git
* 🐙 GitHub
* ☁️ Render

### Banco de dados

* 🍃 MongoDB

---

## 🏗️ Arquitetura

O backend utiliza uma arquitetura organizada em camadas, separando as responsabilidades da aplicação.

```text
Cliente / Frontend / Postman / Swagger
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

Exemplo:

```text
GET    /api/pacientes
POST   /api/pacientes
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
```

### Service

Responsável pela lógica e pelas regras de negócio da aplicação.

É nessa camada que ficam as operações realizadas antes ou depois do acesso ao banco de dados.

### Repository

Responsável pelo acesso aos dados utilizando **Spring Data MongoDB**.

O Spring Data fornece métodos de acesso ao banco, como:

```text
save()
findAll()
findById()
deleteById()
```

Também são utilizados métodos derivados para consultas específicas, como buscas de consultas por paciente, profissional ou período.

### Model

Representa os documentos persistidos no MongoDB.

Exemplos:

* Paciente
* Profissional
* Disponibilidade
* Consulta
* Prontuário
* Diagnóstico
* Tratamento
* Exame

A **Agenda não possui um documento próprio**. Ela utiliza as informações das consultas para realizar consultas por data, profissional ou paciente.

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
│   │   │           ├── controller/
│   │   │           │   ├── AgendaController.java
│   │   │           │   ├── ConsultaController.java
│   │   │           │   ├── DiagnosticoController.java
│   │   │           │   ├── DisponibilidadeController.java
│   │   │           │   ├── ExameController.java
│   │   │           │   ├── PacienteController.java
│   │   │           │   ├── ProfissionalController.java
│   │   │           │   ├── ProntuarioController.java
│   │   │           │   ├── TratamentoController.java
│   │   │           │   └── TesteController.java
│   │   │           │
│   │   │           ├── dto/
│   │   │           │   ├── Agenda/
│   │   │           │   ├── Consulta/
│   │   │           │   ├── Diagnostico/
│   │   │           │   ├── Disponibilidade/
│   │   │           │   ├── Exame/
│   │   │           │   ├── Paciente/
│   │   │           │   ├── Profissional/
│   │   │           │   ├── Prontuario/
│   │   │           │   └── Tratamento/
│   │   │           │
│   │   │           ├── model/
│   │   │           │   ├── Consulta.java
│   │   │           │   ├── Diagnostico.java
│   │   │           │   ├── Disponibilidade.java
│   │   │           │   ├── Exame.java
│   │   │           │   ├── Paciente.java
│   │   │           │   ├── Profissional.java
│   │   │           │   ├── Prontuario.java
│   │   │           │   └── Tratamento.java
│   │   │           │
│   │   │           ├── repository/
│   │   │           │   ├── ConsultaRepository.java
│   │   │           │   ├── DiagnosticoRepository.java
│   │   │           │   ├── DisponibilidadeRepository.java
│   │   │           │   ├── ExameRepository.java
│   │   │           │   ├── PacienteRepository.java
│   │   │           │   ├── ProfissionalRepository.java
│   │   │           │   ├── ProntuarioRepository.java
│   │   │           │   └── TratamentoRepository.java
│   │   │           │
│   │   │           ├── service/
│   │   │           │   ├── AgendaService.java
│   │   │           │   ├── ConsultaService.java
│   │   │           │   ├── DiagnosticoService.java
│   │   │           │   ├── DisponibilidadeService.java
│   │   │           │   ├── ExameService.java
│   │   │           │   ├── PacienteService.java
│   │   │           │   ├── ProfissionalService.java
│   │   │           │   ├── ProntuarioService.java
│   │   │           │   └── TratamentoService.java
│   │   │           │
│   │   │           └── NeurovitaApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── .gitignore
├── pom.xml
└── README.md
```

A estrutura será ampliada conforme novos módulos forem implementados.

---

# 👤 Paciente

Módulo responsável pelo gerenciamento dos pacientes.

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

O módulo utiliza:

```text
Paciente
├── Model
├── Request DTO
├── Response DTO
├── Repository
├── Service
└── Controller
```

---

# 🧑‍⚕️ Profissional

Módulo responsável pelo gerenciamento dos profissionais que realizam os atendimentos.

### Funcionalidades

* 🟢 Cadastrar profissional
* 🟢 Listar profissionais
* 🟢 Buscar profissional por ID
* 🟢 Atualizar profissional
* 🟢 Remover profissional
* 🟢 Vincular disponibilidade
* 🟢 Listar disponibilidades do profissional
* 🟢 Remover disponibilidade vinculada

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

O relacionamento é realizado através dos IDs das disponibilidades armazenados no documento do profissional.

---

# ⏰ Disponibilidade

Módulo responsável pelos horários em que cada profissional está disponível para atendimento.

### Dados principais

* Dia da semana
* Hora inicial
* Hora final
* Ativo

### Funcionalidades

* 🟢 Cadastrar disponibilidade
* 🟢 Listar disponibilidades
* 🟢 Buscar disponibilidade por ID
* 🟢 Atualizar disponibilidade
* 🟢 Remover disponibilidade

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

Módulo responsável pelo gerenciamento dos atendimentos agendados.

### Dados principais

* Paciente
* Profissional
* Data e hora
* Tipo de atendimento
* Local
* Modalidade
* Duração

### Funcionalidades

* 🟢 Criar consulta
* 🟢 Listar consultas
* 🟢 Buscar consulta por ID
* 🟢 Atualizar consulta
* 🟢 Remover consulta
* 🟢 Listar consultas de um paciente
* 🟢 Listar consultas de um profissional

### Endpoints

```text
POST   /api/consultas
GET    /api/consultas
GET    /api/consultas/{id}
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}

GET    /api/consultas/paciente/{pacienteId}
GET    /api/consultas/profissional/{profissionalId}
```

> ℹ️ O controle de status da consulta e regras mais avançadas de conflito de horários poderão ser implementados conforme as regras definitivas do sistema.

---

# 🗓️ Agenda

A Agenda utiliza as informações armazenadas nas consultas para disponibilizar consultas por diferentes critérios.

Não existe um documento `Agenda` separado no MongoDB.

### Funcionalidades

* 🟢 Listar agenda
* 🟢 Consultar agenda por dia
* 🟢 Consultar agenda de um profissional por dia
* 🟢 Consultar agenda de um paciente

### Endpoints

```text
GET /api/agenda

GET /api/agenda/dia?data=2026-09-20

GET /api/agenda/profissional/{profissionalId}?data=2026-09-20

GET /api/agenda/paciente/{pacienteId}
```

---

# 📋 Prontuário

Módulo responsável pelo armazenamento das informações clínicas relacionadas ao paciente.

### Dados principais

* Paciente
* Profissional
* Consulta
* Queixa principal
* Avaliação
* Plano de tratamento
* Prescrição

### Funcionalidades

* 🟢 Criar prontuário
* 🟢 Buscar prontuário por ID
* 🟢 Atualizar prontuário
* 🟢 Buscar prontuário por paciente

### Endpoints

```text
POST /api/prontuarios

GET  /api/prontuarios/{id}

PUT  /api/prontuarios/{id}

GET  /api/prontuarios/paciente/{pacienteId}
```

---

# 🩺 Diagnóstico

Módulo responsável pelo gerenciamento dos diagnósticos associados aos pacientes.

### Dados principais

* Paciente
* Profissional
* Descrição

### Funcionalidades

* 🟢 Criar diagnóstico
* 🟢 Listar diagnósticos
* 🟢 Buscar diagnóstico por ID
* 🟢 Atualizar diagnóstico
* 🟢 Remover diagnóstico
* 🟢 Listar diagnósticos por paciente

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

# 📝 Tratamento

Módulo responsável pelo gerenciamento dos tratamentos definidos para os pacientes.

### Dados principais

* Paciente
* Profissional
* Descrição
* Objetivo
* Frequência
* Duração
* Observações

### Funcionalidades

* 🟢 Criar tratamento
* 🟢 Listar tratamentos
* 🟢 Buscar tratamento por ID
* 🟢 Atualizar tratamento
* 🟢 Remover tratamento
* 🟢 Listar tratamentos por paciente

### Endpoints

```text
POST   /api/tratamentos
GET    /api/tratamentos
GET    /api/tratamentos/{id}
PUT    /api/tratamentos/{id}
DELETE /api/tratamentos/{id}

GET    /api/tratamentos/paciente/{pacienteId}
```

---

# 🧪 Exame

Módulo responsável pelo gerenciamento dos exames relacionados aos pacientes.

### Dados principais

* Paciente
* Profissional
* Nome
* Tipo
* Data de realização
* Resultado
* Observações

### Funcionalidades

* 🟢 Criar exame
* 🟢 Listar exames
* 🟢 Buscar exame por ID
* 🟢 Atualizar exame
* 🟢 Remover exame
* 🟢 Listar exames por paciente

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

# 📄 Receita

O módulo de receitas ainda não foi implementado.

Os campos definitivos dependem da definição da interface e dos requisitos do sistema.

> ⏳ **Status:** Aguardando definição dos campos no Figma/requisitos do projeto.

A implementação será realizada após a definição do contrato de dados.

---

# 🔐 Segurança

Como o NeuroVita poderá trabalhar com informações sensíveis, segurança será uma das prioridades do backend.

### Recursos planejados

* 🔐 Spring Security
* 🔑 JWT
* Controle de acesso por função
* Hash de senhas
* Proteção dos endpoints
* Controle de permissões
* Aplicação de princípios relacionados à LGPD

> ⚠️ Os recursos de segurança serão implementados gradualmente durante o desenvolvimento.

---

# 👥 Perfis previstos

Os perfis previstos para o sistema incluem:

* ADMINISTRADOR
* PROFISSIONAL
* SECRETÁRIO
* PACIENTE

Cada perfil deverá possuir permissões específicas dentro da aplicação.

---

# 📖 Swagger / OpenAPI

A API possui documentação interativa utilizando **Swagger / OpenAPI**.

Com a aplicação executando localmente, a interface pode ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

Também é possível acessar a especificação OpenAPI através de:

```text
http://localhost:8080/v3/api-docs
```

O Swagger permite visualizar os endpoints disponíveis e realizar requisições diretamente pela interface durante o desenvolvimento.

---

# 🧪 Testes

Durante o desenvolvimento são utilizados diferentes níveis de validação:

* Testes automatizados;
* Testes dos endpoints;
* Validação das regras de negócio;
* Validação da persistência;
* Testes de integração;
* Testes através do Swagger;
* Testes através do Postman.

A implementação dos testes automatizados é realizada conforme a organização definida pela equipe responsável pelo projeto.

Atualmente, o projeto possui a estrutura necessária para execução dos testes através do Maven.

Para executar:

```bash
mvn test
```

---

# 🗄️ Banco de dados

O backend utiliza **MongoDB** como banco de dados.

Os documentos implementados atualmente incluem informações relacionadas a:

* Pacientes;
* Profissionais;
* Disponibilidades;
* Consultas;
* Prontuários;
* Diagnósticos;
* Tratamentos;
* Exames.

A conexão com o banco depende da configuração disponibilizada pela equipe responsável pelo banco de dados.

As credenciais de acesso devem ser configuradas por meio de variáveis de ambiente ou mecanismos seguros de configuração.

> ⚠️ Dados reais de acesso ao banco nunca devem ser enviados para o GitHub.

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
```

A implementação utiliza a documentação e o backend anterior como referência.

O objetivo não é simplesmente traduzir o código JavaScript para Java, mas **reorganizar e adaptar o sistema ao ecossistema Java/Spring Boot**, aplicando uma arquitetura adequada para uma API REST.

---

# 🚀 Executando o projeto localmente

## Pré-requisitos

* Java JDK 21 ou superior;
* Maven;
* Git;
* MongoDB configurado no ambiente de desenvolvimento.

Verificar Java:

```bash
java -version
```

Verificar Maven:

```bash
mvn -version
```

## Clonar o projeto

```bash
git clone https://github.com/victorhborges/NeuroVita.git
```

Entrar na pasta:

```bash
cd NeuroVita
```

## Executar a aplicação

```bash
mvn spring-boot:run
```

Por padrão, a aplicação utiliza:

```text
http://localhost:8080
```

### Endpoint de teste

```text
GET /teste
```

URL:

```text
http://localhost:8080/teste
```

Resposta esperada:

```text
API NeuroVita funcionando!
```

### Swagger

```text
http://localhost:8080/swagger-ui/index.html
```

> ℹ️ A conexão com o MongoDB depende da disponibilidade e configuração do banco no ambiente utilizado.

---

# 🔗 Integração com o Frontend

O backend será consumido pelo frontend através de requisições HTTP utilizando a API REST.

Os endpoints utilizam JSON para comunicação entre as aplicações.

Fluxo previsto:

```text
Frontend Angular
       ↓
HTTP / JSON
       ↓
NeuroVita API
       ↓
Spring Boot
       ↓
MongoDB
```

---

# 📊 Status do desenvolvimento

| Módulo                   | Status                     |
| ------------------------ | -------------------------- |
| Estrutura inicial Java   | 🟢 Concluído               |
| Configuração Spring Boot | 🟢 Concluído               |
| Spring Web MVC           | 🟢 Concluído               |
| Spring Data MongoDB      | 🟢 Concluído               |
| Endpoint de teste        | 🟢 Concluído               |
| Paciente                 | 🟢 Concluído               |
| Profissional             | 🟢 Concluído               |
| Disponibilidade          | 🟢 Concluído               |
| Consulta                 | 🟢 Concluído               |
| Agenda                   | 🟢 Concluído               |
| Prontuário               | 🟢 Concluído               |
| Diagnóstico              | 🟢 Concluído               |
| Tratamento               | 🟢 Concluído               |
| Exame                    | 🟢 Concluído               |
| Receita                  | 🟡 Aguardando definição    |
| Swagger / OpenAPI        | 🟢 Concluído               |
| MongoDB externo          | 🟡 Aguardando configuração |
| Administrador            | ⚪ Planejado                |
| Autenticação             | ⚪ Planejado                |
| Autorização              | ⚪ Planejado                |
| Spring Security / JWT    | ⚪ Planejado                |
| Integração com frontend  | ⚪ Planejado                |
| Deploy no Render         | 🔵 Meta                    |

### Legenda

* 🟢 Concluído
* 🟡 Em andamento / aguardando definição
* 🔵 Meta
* ⚪ Planejado

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

* [x] Implementar Paciente
* [x] Implementar Profissional
* [x] Implementar Disponibilidade
* [x] Implementar Consulta
* [x] Implementar Agenda
* [x] Implementar Prontuário
* [x] Implementar Diagnóstico
* [x] Implementar Tratamento
* [x] Implementar Exame
* [ ] Definir e implementar Receita
* [ ] Implementar Administrador

## Etapa 3 — Documentação e integração

* [x] Configurar Swagger / OpenAPI
* [ ] Validar endpoints com MongoDB externo
* [ ] Testar integração com frontend
* [ ] Refinar documentação da API

## Etapa 4 — Segurança

* [ ] Spring Security
* [ ] JWT
* [ ] Perfis de acesso
* [ ] Hash de senhas
* [ ] Proteção dos endpoints
* [ ] Regras de autorização

## Etapa 5 — Deploy

* [ ] Preparar aplicação para produção
* [ ] Configurar variáveis de ambiente
* [ ] Configurar banco de produção
* [ ] Criar serviço no Render
* [ ] Realizar deploy
* [ ] Testar API em produção

---

# 🌿 Git e GitHub

O projeto utiliza Git para controle de versão e GitHub para armazenamento e colaboração.

### Exemplo de branch

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
```

---

# 📌 Boas práticas

Durante o desenvolvimento serão priorizados:

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
* Commits organizados.

---

# 🎓 Projeto acadêmico

**Projeto:** NeuroVita

**Contexto:** Projeto acadêmico desenvolvido na faculdade

**Área:** Tecnologia / Desenvolvimento de Software

O projeto possui finalidade acadêmica e prática, buscando aplicar conhecimentos de:

* Programação;
* Desenvolvimento de APIs;
* Banco de dados;
* Arquitetura de software;
* Programação Orientada a Objetos;
* Segurança;
* Boas práticas de desenvolvimento.

---

# 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos.

A utilização, distribuição ou modificação do código deverá respeitar as decisões definidas pela equipe responsável pelo projeto.
