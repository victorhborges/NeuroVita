# 🧠 NeuroVita — Backend

Backend da plataforma **NeuroVita**, desenvolvida para auxiliar no gerenciamento de atendimentos psicológicos, pacientes, profissionais, consultas e informações relacionadas ao atendimento clínico.

O projeto está sendo desenvolvido como parte das atividades acadêmicas da faculdade, com foco na aplicação prática de conceitos de desenvolvimento de software, banco de dados, APIs e arquitetura de sistemas.

> 🚧 **Status:** Em desenvolvimento
> 🔄 **Tecnologia:** Migração/reimplementação de Node.js/JavaScript para Java
> ☁️ **Hospedagem planejada:** Render

---

## 📌 Sobre o projeto

O **NeuroVita** é uma plataforma voltada para instituições e profissionais da área de psicologia, com o objetivo de centralizar e organizar informações relacionadas aos atendimentos.

A aplicação busca facilitar o gerenciamento de:

* 👤 Pacientes
* 🧑‍⚕️ Profissionais
* 📅 Consultas
* 🗓️ Agendas
* ⏰ Disponibilidades
* 🔐 Usuários e permissões
* 📋 Registros clínicos
* 📄 Relatórios e documentos

O backend será responsável por disponibilizar os serviços e regras de negócio através de uma **API REST**, permitindo a comunicação com o frontend da aplicação.

---

## 🎯 Objetivos

O backend tem como principais objetivos:

* Disponibilizar uma API REST para o sistema NeuroVita;
* Gerenciar pacientes, profissionais e consultas;
* Controlar agendas e disponibilidades;
* Implementar autenticação e autorização;
* Aplicar as regras de negócio do sistema;
* Proteger informações sensíveis;
* Seguir boas práticas de desenvolvimento;
* Permitir integração com o frontend;
* Disponibilizar a aplicação em ambiente de produção através do **Render**.

---

# 🛠️ Tecnologias

## Backend

* ☕ **Java**
* 🌱 **Spring Boot**
* 🌐 **Spring Web**
* 🗄️ **Spring Data JPA**
* 🔐 **Spring Security**
* 🔑 **JWT**
* 📦 **Maven**

## Banco de dados

* 🐬 **MySQL**

## Testes e documentação

* 🧪 **JUnit**
* 📬 **Postman**
* 📖 **Swagger / OpenAPI**

## Versionamento e hospedagem

* 🔀 **Git**
* 🐙 **GitHub**
* ☁️ **Render**

---

# 🏗️ Arquitetura

O backend será desenvolvido utilizando uma arquitetura organizada em camadas, baseada nos conceitos de **MVC**, **REST** e princípios de organização inspirados em **Clean Architecture**.

A estrutura inicial será organizada da seguinte forma:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
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

Responsável pelas regras de negócio da aplicação.

Exemplos:

* validar dados;
* verificar conflitos de horários;
* controlar regras de agendamento;
* aplicar regras de acesso.

### Repository

Responsável pela comunicação com o banco de dados utilizando **Spring Data JPA**.

### Model

Representa as entidades do sistema.

Exemplos:

```text
Paciente
Profissional
Consulta
Agenda
Disponibilidade
Administrador
```

---

# 📁 Estrutura planejada

```text
neurovita-backend/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/
│   │   │       └── com/
│   │   │           └── neurovita/
│   │   │               │
│   │   │               ├── controller/
│   │   │               ├── service/
│   │   │               ├── repository/
│   │   │               ├── model/
│   │   │               ├── dto/
│   │   │               ├── security/
│   │   │               ├── exception/
│   │   │               │
│   │   │               └── NeuroVitaApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── ...
│   │
│   └── test/
│       └── java/
│
├── .gitignore
├── pom.xml
└── README.md
```

A estrutura poderá ser ajustada conforme o desenvolvimento do projeto.

---

# 👥 Principais módulos

## 👤 Paciente

Responsável pelo gerenciamento dos pacientes cadastrados.

Funcionalidades planejadas:

* Cadastrar paciente
* Listar pacientes
* Buscar paciente por ID
* Atualizar paciente
* Remover paciente

Endpoints planejados:

```text
POST   /api/pacientes
GET    /api/pacientes
GET    /api/pacientes/{id}
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
```

---

## 🧑‍⚕️ Profissional

Responsável pelo gerenciamento dos profissionais que realizam os atendimentos.

Funcionalidades planejadas:

* Cadastrar profissional
* Listar profissionais
* Buscar profissional
* Atualizar profissional
* Remover profissional

Endpoints planejados:

```text
POST   /api/profissionais
GET    /api/profissionais
GET    /api/profissionais/{id}
PUT    /api/profissionais/{id}
DELETE /api/profissionais/{id}
```

---

## 📅 Consulta

Responsável pelo agendamento e gerenciamento das consultas.

Funcionalidades planejadas:

* Agendar consulta
* Listar consultas
* Buscar consulta
* Atualizar consulta
* Cancelar consulta
* Controlar status da consulta
* Evitar conflitos de horários

Endpoints planejados:

```text
POST   /api/consultas
GET    /api/consultas
GET    /api/consultas/{id}
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}
```

Status previstos:

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

Funcionalidades planejadas:

* Criar agenda
* Consultar agenda
* Atualizar agenda
* Remover agenda
* Consultar horários disponíveis

Endpoint planejado:

```text
/api/agendas
```

---

## ⏰ Disponibilidade

Responsável pelos horários em que cada profissional está disponível para atendimento.

Funcionalidades planejadas:

* Cadastrar disponibilidade
* Consultar disponibilidade
* Atualizar disponibilidade
* Remover disponibilidade
* Vincular disponibilidade a profissionais

Endpoint planejado:

```text
/api/disponibilidades
```

---

## 🔐 Administrador

Responsável pelo gerenciamento administrativo do sistema.

Funcionalidades planejadas:

* Cadastro
* Consulta
* Atualização
* Remoção
* Gerenciamento de permissões

Endpoint planejado:

```text
/api/administradores
```

---

# 🔐 Segurança

O NeuroVita trabalha com informações potencialmente sensíveis. Por isso, segurança será uma das prioridades do backend.

Entre os recursos planejados estão:

* Autenticação utilizando JWT;
* Controle de acesso baseado em funções;
* Proteção de endpoints;
* Senhas armazenadas utilizando hash seguro;
* Controle de sessão;
* Registro de tentativas de login;
* Controle de permissões;
* Proteção de informações clínicas;
* Auditoria de acessos;
* Aplicação de princípios relacionados à LGPD.

### 👥 Perfis

Os principais perfis previstos são:

```text
ADMINISTRADOR
PROFISSIONAL
SECRETÁRIO
PACIENTE
```

Cada perfil deverá possuir permissões específicas dentro da aplicação.

---

# 🛡️ Regras de segurança previstas

Entre as regras documentadas para o sistema estão:

* Senhas fortes;
* Bloqueio temporário após múltiplas tentativas de login inválidas;
* Expiração de sessão por inatividade;
* Autenticação em dois fatores para profissionais;
* Registro de acessos;
* Controle de acesso aos registros clínicos;
* Imutabilidade de registros clínicos finalizados;
* Registro de auditoria;
* Proteção de dados sensíveis.

> ⚠️ Essas funcionalidades serão implementadas gradualmente durante o desenvolvimento.

---

# 📋 Requisitos funcionais

| Código | Requisito                              |
| ------ | -------------------------------------- |
| RF01   | Cadastro de pacientes                  |
| RF02   | Prontuário/registro clínico eletrônico |
| RF03   | Anexos clínicos                        |
| RF04   | Gerenciamento da agenda                |
| RF05   | Lembretes de consultas                 |
| RF06   | Lista de espera                        |
| RF07   | Controle de pagamentos                 |
| RF08   | Cálculo de honorários profissionais    |

---

# ⚙️ Requisitos não funcionais

Entre os principais requisitos estão:

* Segurança e proteção dos dados;
* Conformidade com a LGPD;
* Controle de acesso por função;
* Registro de auditoria;
* Disponibilidade do sistema;
* Bom desempenho;
* Facilidade de manutenção;
* Backup periódico;
* Proteção das informações clínicas.

---

# 🔄 Migração para Java

O backend original do projeto foi desenvolvido utilizando:

```text
JavaScript
Node.js
Express.js
MySQL
```

O novo backend será desenvolvido utilizando:

```text
Java
Spring Boot
Spring Data JPA
Spring Security
MySQL
```

A migração será realizada como uma **reimplementação do backend**, utilizando a documentação e o código anterior como referência quando este estiver disponível.

O objetivo não é simplesmente traduzir código JavaScript para Java, mas adaptar a aplicação para uma estrutura adequada ao ecossistema Java/Spring Boot.

---

# 🧪 Testes

Durante o desenvolvimento serão realizados testes para validar:

* Endpoints;
* Regras de negócio;
* Persistência de dados;
* Autenticação;
* Autorização;
* Validações;
* Tratamento de erros;
* Integração entre as camadas.

Ferramentas previstas:

```text
JUnit
Postman
Swagger / OpenAPI
```

---

# 📖 Documentação da API

A API será documentada utilizando **Swagger/OpenAPI**.

Exemplo de acesso local:

```text
http://localhost:8080/swagger-ui/index.html
```

A URL poderá mudar conforme a configuração final do projeto.

---

# 🗄️ Banco de dados

O banco de dados utilizado será o **MySQL**.

Principais entidades previstas:

```text
Administrador
Profissional
Paciente
Consulta
Agenda
Disponibilidade
Profissional_Disponibilidade
```

O modelo do banco poderá ser ajustado durante a implementação conforme as necessidades encontradas no backend anterior e na integração com o frontend.

---

# ☁️ Hospedagem

Uma das metas do projeto é disponibilizar o backend em ambiente de produção utilizando o **Render**.

### Objetivo

Ao final da primeira etapa de desenvolvimento, a API deverá estar acessível através de uma URL pública para permitir a integração com o frontend.

Exemplo:

```text
https://neurovita-backend.onrender.com
```

> ⚠️ A URL acima é apenas um exemplo. A URL definitiva será adicionada após a criação do serviço no Render.

### Planejamento

A hospedagem será preparada considerando:

* Build da aplicação Java;
* Configuração do ambiente de produção;
* Variáveis de ambiente;
* Configuração do banco de dados;
* Configuração de conexão segura;
* Deploy automático através do GitHub;
* Testes da API em produção.

### Prazo

A primeira versão hospedada do backend deverá ser disponibilizada **dentro do prazo de aproximadamente 1 mês**.

Por esse motivo, a estratégia de desenvolvimento prioriza inicialmente uma versão funcional da API, deixando funcionalidades mais avançadas para etapas posteriores.

---

# 🚀 Executando o projeto localmente

## Pré-requisitos

É necessário possuir instalado:

* Java JDK
* Maven
* MySQL
* Git

Verificar o Java:

```bash
java -version
```

Verificar o Maven:

```bash
mvn -version
```

---

## 📥 Clonar o projeto

```bash
git clone URL_DO_REPOSITORIO
```

Entrar na pasta:

```bash
cd neurovita-backend
```

---

## 🗄️ Configuração do banco

Criar um banco MySQL para o projeto.

Exemplo:

```sql
CREATE DATABASE neurovita;
```

As informações de conexão deverão ser configuradas através de propriedades ou variáveis de ambiente.

Exemplo conceitual:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/neurovita
spring.datasource.username=usuario
spring.datasource.password=senha
```

> ⚠️ Dados reais de acesso ao banco nunca devem ser enviados para o GitHub.

---

# ▶️ Executando a aplicação

Com o projeto configurado:

```bash
mvn spring-boot:run
```

A aplicação deverá iniciar, por padrão, em:

```text
http://localhost:8080
```

---

# 🔗 Integração com o Frontend

O backend será consumido pelo frontend através de requisições HTTP utilizando a API REST.

Frontend documentado atualmente:

[Projeto NeuroVita — Frontend](https://github.com/Ryyann777/Projeto_NeuroSpectrum?utm_source=chatgpt.com)

> O frontend poderá sofrer alterações durante o desenvolvimento da nova versão do backend.

---

# 📊 Status do desenvolvimento

| Módulo                   | Status                |
| ------------------------ | --------------------- |
| Estrutura inicial Java   | 🟡 Em desenvolvimento |
| Configuração Spring Boot | 🟡 Em desenvolvimento |
| Banco de dados           | 🟡 Em desenvolvimento |
| Pacientes                | ⚪ Planejado           |
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
| Deploy no Render         | 🔵 Meta do projeto    |

### Legenda

```text
🟢 Concluído
🟡 Em desenvolvimento
🔵 Próxima etapa / Meta
⚪ Planejado
```

---

# 📅 Estratégia de desenvolvimento

Para cumprir o prazo de hospedagem, o desenvolvimento será dividido em etapas.

### Etapa 1 — Estrutura

* Criar projeto Spring Boot;
* Configurar Maven;
* Configurar banco;
* Criar estrutura de pacotes;
* Criar primeiro endpoint;
* Executar aplicação localmente.

### Etapa 2 — API

* Implementar entidades;
* Criar repositories;
* Criar services;
* Criar controllers;
* Implementar CRUDs principais;
* Validar respostas HTTP.

### Etapa 3 — Segurança

* Spring Security;
* JWT;
* Perfis de acesso;
* Hash de senha;
* Proteção dos endpoints.

### Etapa 4 — Testes e integração

* Testes unitários;
* Testes da API;
* Swagger;
* Postman;
* Integração com frontend.

### Etapa 5 — Deploy

* Preparar aplicação para produção;
* Configurar variáveis de ambiente;
* Configurar banco;
* Criar serviço no Render;
* Realizar deploy;
* Testar API hospedada.

---

# 🌿 Git e GitHub

O projeto utiliza Git para controle de versão.

### Criar uma branch

```bash
git checkout -b feature/nome-da-feature
```

### Adicionar alterações

```bash
git add .
```

### Criar commit

```bash
git commit -m "feat: adiciona cadastro de pacientes"
```

### Enviar para o GitHub

```bash
git push origin feature/nome-da-feature
```

### Exemplos de commits

```text
feat: adiciona entidade paciente
feat: implementa cadastro de pacientes
feat: adiciona autenticação JWT
fix: corrige validação de consulta
docs: atualiza README
test: adiciona testes de paciente
```

---

# 📌 Boas práticas

Durante o desenvolvimento serão priorizados:

* Código organizado;
* Separação de responsabilidades;
* Programação Orientada a Objetos;
* Princípios SOLID;
* Tratamento de exceções;
* Validação de dados;
* Uso de DTOs;
* Padronização dos endpoints;
* Código legível;
* Commits organizados;
* Segurança das informações;
* Documentação da API.

---

# 🎓 Projeto acadêmico

**Projeto:** NeuroVita
**Contexto:** Projeto acadêmico desenvolvido na faculdade
**Área:** Tecnologia / Desenvolvimento de Software

O projeto está sendo desenvolvido com finalidade acadêmica e prática, buscando aplicar conhecimentos de programação, desenvolvimento de APIs, banco de dados, arquitetura de software e boas práticas de desenvolvimento.

---

# 📚 Documentação relacionada

A documentação do projeto contém informações sobre:

* Requisitos funcionais;
* Requisitos não funcionais;
* Regras de negócio;
* Casos de uso;
* Diagramas UML;
* Modelo entidade-relacionamento;
* Fluxos do sistema;
* Segurança;
* Agenda;
* Consultas;
* Registros clínicos.

---

# 🚧 Status atual

O backend está em fase inicial de desenvolvimento.

A implementação está sendo realizada em **Java com Spring Boot**, tendo como referência a documentação existente e o backend anterior desenvolvido em **Node.js/JavaScript**.

O foco inicial é construir uma API funcional, testável e preparada para integração com o frontend e posterior hospedagem no **Render**, considerando o prazo de aproximadamente **1 mês para disponibilização da aplicação em ambiente de produção**.

---

## 👨‍💻 Desenvolvimento

Backend desenvolvido para o projeto acadêmico **NeuroVita**.

O desenvolvimento seguirá uma abordagem incremental, priorizando inicialmente as funcionalidades necessárias para disponibilizar uma primeira versão funcional da API.

---

# 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos.

A utilização, distribuição ou modificação do código deverá respeitar as decisões definidas pela equipe responsável pelo projeto.
