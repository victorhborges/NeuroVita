# 🧠 NeuroVita — Backend

Backend da plataforma **NeuroVita**, desenvolvida para auxiliar no gerenciamento de atendimentos psicológicos, pacientes, profissionais, consultas, agendas e informações relacionadas ao atendimento clínico.

O projeto está sendo desenvolvido como parte das atividades acadêmicas da faculdade, com foco na aplicação prática de conceitos de **Programação Orientada a Objetos, desenvolvimento de APIs REST, arquitetura de software, banco de dados, segurança e boas práticas de desenvolvimento**.

> 🚧 **Status:** Em desenvolvimento
> ☕ **Linguagem:** Java 21
> 🌱 **Framework:** Spring Boot 4.0.8
> 🗄️ **Banco:** MongoDB
> ☁️ **Hospedagem planejada:** Render

---

## 📌 Sobre o projeto

O NeuroVita é uma plataforma voltada para instituições e profissionais da área de psicologia, com o objetivo de centralizar e organizar informações relacionadas aos atendimentos.

O backend será responsável por disponibilizar uma **API REST**, permitindo a comunicação entre o frontend e os serviços da aplicação.

Entre os principais módulos previstos estão:

* 👤 Pacientes
* 🧑‍⚕️ Profissionais
* 📅 Consultas
* 🗓️ Agendas
* ⏰ Disponibilidades
* 🔐 Usuários e permissões
* 📋 Registros clínicos
* 📄 Documentos e relatórios

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

### Testes e documentação — planejado

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
GET    /pacientes
POST   /pacientes
PUT    /pacientes/{id}
DELETE /pacientes/{id}
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
* Agenda
* Disponibilidade
* Administrador

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
│   │   │           │   ├── TesteController.java
│   │   │           │   └── PacienteController.java
│   │   │           │
│   │   │           ├── service/
│   │   │           │   └── PacienteService.java
│   │   │           │
│   │   │           ├── repository/
│   │   │           │   └── PacienteRepository.java
│   │   │           │
│   │   │           ├── model/
│   │   │           │   └── Paciente.java
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

## 👤 Paciente

O módulo de pacientes é o primeiro módulo em desenvolvimento.

### Funcionalidades

* 🟡 Cadastrar paciente
* 🟡 Listar pacientes
* ⚪ Buscar paciente por ID
* ⚪ Atualizar paciente
* ⚪ Remover paciente

### Endpoints

```text
POST   /pacientes
GET    /pacientes
GET    /pacientes/{id}
PUT    /pacientes/{id}
DELETE /pacientes/{id}
```

---

## 🧑‍⚕️ Profissional

Módulo responsável pelo gerenciamento dos profissionais que realizam os atendimentos.

### Funcionalidades planejadas

* Cadastrar profissional
* Listar profissionais
* Buscar profissional
* Atualizar profissional
* Remover profissional

---

## 📅 Consulta

Módulo responsável pelo agendamento e gerenciamento das consultas.

### Funcionalidades planejadas

* Agendar consulta
* Listar consultas
* Buscar consulta
* Atualizar consulta
* Cancelar consulta
* Controlar status da consulta
* Evitar conflitos de horários

### Status previstos

```text
AGENDADO
CONFIRMADO
EM_ATENDIMENTO
REALIZADO
FALTA
CANCELADO
```

---

## 🗓️ Agenda

Responsável pelo gerenciamento da agenda dos profissionais.

### Funcionalidades planejadas

* Criar agenda
* Consultar agenda
* Atualizar agenda
* Remover agenda
* Consultar horários disponíveis

---

## ⏰ Disponibilidade

Responsável pelos horários em que cada profissional está disponível para atendimento.

### Funcionalidades planejadas

* Cadastrar disponibilidade
* Consultar disponibilidade
* Atualizar disponibilidade
* Remover disponibilidade
* Vincular disponibilidade a profissionais

---

## 🔐 Segurança

Como o NeuroVita poderá trabalhar com informações sensíveis, segurança será uma das prioridades do backend.

### Recursos planejados

* Autenticação com JWT;
* Controle de acesso por função;
* Proteção de endpoints;
* Hash de senhas;
* Controle de permissões;
* Registro de acessos;
* Auditoria;
* Proteção de informações clínicas;
* Aplicação de princípios relacionados à LGPD;
* Autenticação em dois fatores para profissionais.

> ⚠️ Os recursos de segurança serão implementados gradualmente durante o desenvolvimento.

---

## 👥 Perfis previstos

* ADMINISTRADOR
* PROFISSIONAL
* SECRETÁRIO
* PACIENTE

Cada perfil deverá possuir permissões específicas dentro da aplicação.

---

## 🧪 Testes

Durante o desenvolvimento serão realizados testes para validar:

* Endpoints;
* Regras de negócio;
* Persistência de dados;
* Validações;
* Tratamento de erros;
* Autenticação;
* Autorização;
* Integração entre as camadas.

Ferramentas previstas:

* JUnit
* Postman
* Swagger / OpenAPI

---

## 🗄️ Banco de dados

O backend será desenvolvido utilizando **MongoDB**.

Os documentos previstos incluem informações relacionadas a:

* Administradores;
* Profissionais;
* Pacientes;
* Consultas;
* Agendas;
* Disponibilidades;
* Registros clínicos;
* Outros módulos definidos durante o desenvolvimento.

A configuração definitiva da conexão com o banco dependerá do ambiente disponibilizado pela equipe responsável pelo banco de dados.

> ⚠️ Dados reais de acesso ao banco nunca devem ser enviados para o GitHub.

---

## 🔄 Reimplementação em Java

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

## 🚀 Executando o projeto localmente

### Pré-requisitos

* Java JDK 21 ou superior;
* Maven;
* Git;
* MongoDB disponível no ambiente de desenvolvimento.

Verificar Java:

```bash
java -version
```

Verificar Maven:

```bash
mvn -version
```

### Clonar o projeto

```bash
git clone https://github.com/victorhborges/NeuroVita.git
```

Entrar na pasta:

```bash
cd NeuroVita
```

### Executar a aplicação

```bash
mvn spring-boot:run
```

Por padrão, a aplicação utiliza:

```text
http://localhost:8080
```

Endpoint de teste:

```text
GET http://localhost:8080/teste
```

Resposta esperada:

```text
API NeuroVita funcionando!
```

> ℹ️ A conexão com o MongoDB depende da disponibilidade e configuração do banco no ambiente utilizado.

---

## 🔗 Integração com o Frontend

O backend será consumido pelo frontend através de requisições HTTP utilizando a API REST.

A estrutura da API será desenvolvida considerando a integração futura com o frontend do NeuroVita.

---

## 📊 Status do desenvolvimento

| Módulo                   | Status                |
| ------------------------ | --------------------- |
| Estrutura inicial Java   | 🟢 Concluído          |
| Configuração Spring Boot | 🟢 Concluído          |
| Spring Web MVC           | 🟢 Concluído          |
| Spring Data MongoDB      | 🟢 Concluído          |
| Endpoint de teste        | 🟢 Concluído          |
| Model Paciente           | 🟢 Concluído          |
| Repository Paciente      | 🟢 Concluído          |
| Service Paciente         | 🟢 Concluído          |
| Controller Paciente      | 🟡 Em desenvolvimento |
| CRUD Paciente            | 🟡 Em desenvolvimento |
| Profissionais            | ⚪ Planejado           |
| Consultas                | ⚪ Planejado           |
| Agenda                   | ⚪ Planejado           |
| Disponibilidades         | ⚪ Planejado           |
| Administradores          | ⚪ Planejado           |
| Autenticação             | ⚪ Planejado           |
| Autorização              | ⚪ Planejado           |
| Testes                   | ⚪ Planejado           |
| Swagger/OpenAPI          | ⚪ Planejado           |
| Integração com frontend  | ⚪ Planejado           |
| Deploy no Render         | 🔵 Meta               |

### Legenda

* 🟢 Concluído
* 🟡 Em desenvolvimento
* 🔵 Meta
* ⚪ Planejado

---

## 🗺️ Roadmap

### Etapa 1 — Estrutura

* [x] Criar projeto Spring Boot
* [x] Configurar Maven
* [x] Configurar Spring Web MVC
* [x] Configurar Spring Data MongoDB
* [x] Criar estrutura de pacotes
* [x] Criar primeiro endpoint

### Etapa 2 — API

* [x] Criar Model Paciente
* [x] Criar Repository Paciente
* [x] Criar Service Paciente
* [x] Criar Controller Paciente
* [ ] Finalizar CRUD de Paciente
* [ ] Implementar Profissional
* [ ] Implementar Disponibilidade
* [ ] Implementar Consulta
* [ ] Implementar Agenda
* [ ] Implementar demais módulos

### Etapa 3 — Segurança

* [ ] Spring Security
* [ ] JWT
* [ ] Perfis de acesso
* [ ] Hash de senhas
* [ ] Proteção dos endpoints
* [ ] Regras de autorização

### Etapa 4 — Testes e documentação

* [ ] Testes unitários
* [ ] Testes da API
* [ ] Swagger / OpenAPI
* [ ] Testes de integração
* [ ] Integração com frontend

### Etapa 5 — Deploy

* [ ] Preparar aplicação para produção
* [ ] Configurar variáveis de ambiente
* [ ] Configurar banco de produção
* [ ] Criar serviço no Render
* [ ] Realizar deploy
* [ ] Testar API em produção

---

## 🌿 Git e GitHub

O projeto utiliza Git para controle de versão e GitHub para armazenamento e colaboração.

Exemplo de branch:

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

## 📌 Boas práticas

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

## 🎓 Projeto acadêmico

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

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos.

A utilização, distribuição ou modificação do código deverá respeitar as decisões definidas pela equipe responsável pelo projeto.
