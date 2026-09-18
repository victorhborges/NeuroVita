# 🧠 NeuroVita - Backend

Backend da plataforma **NeuroVita**, desenvolvido como projeto acadêmico da **Faculdade Brasília — Análise e Desenvolvimento de Sistemas**.

A aplicação disponibiliza uma **API REST** para gerenciamento de pacientes, profissionais, consultas, agenda e informações relacionadas ao atendimento psicológico.

> 🚧 **Status:** Em desenvolvimento

| Tecnologia     | Versão / Uso             |
| -------------- | ------------------------ |
| ☕ Java         | 21                       |
| 🌱 Spring Boot | 4.0.8                    |
| 🗄️ MongoDB    | Banco de dados           |
| 🐳 Docker      | Containerização          |
| ☁️ Render      | Hospedagem               |
| 📦 Maven       | Gerenciamento do projeto |

---

## 📌 Funcionalidades

Atualmente o backend possui os seguintes módulos:

* 👤 Pacientes
* 🧑‍⚕️ Profissionais
* ⏰ Disponibilidades
* 📅 Consultas
* 🗓️ Agenda
* 📋 Prontuários
* 🧠 Diagnósticos
* 💊 Tratamentos
* 🧪 Exames
* 👨‍💼 Administradores
* 🔐 Autenticação e segurança

O módulo de **Receitas** aguarda a definição dos campos e regras no Figma.

---

## 🏗️ Arquitetura

O projeto utiliza uma arquitetura em camadas:

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

Responsável pelas requisições HTTP e respostas da API.

### Service

Responsável pelas regras de negócio e operações da aplicação.

### Repository

Responsável pelo acesso aos dados utilizando Spring Data MongoDB.

### Model

Representa os documentos persistidos no MongoDB.

### DTO

Utilizado para separar os dados recebidos e enviados pela API dos modelos de persistência.

---

## 📁 Estrutura do projeto

```text
neurovita/
├── src/
│   ├── main/
│   │   ├── java/com/neurovita/
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   │   ├── JwtService.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── service/
│   │   │   └── NeurovitaApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── .gitignore
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

---

## 👤 Paciente

CRUD completo de pacientes.

```text
POST   /api/pacientes
GET    /api/pacientes
GET    /api/pacientes/{id}
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
```

---

## 🧑‍⚕️ Profissional

Gerenciamento de profissionais e suas disponibilidades.

```text
POST   /api/profissionais
GET    /api/profissionais
GET    /api/profissionais/{id}
PUT    /api/profissionais/{id}
DELETE /api/profissionais/{id}
```

Disponibilidades vinculadas:

```text
POST   /api/profissionais/{profissionalId}/disponibilidades/{disponibilidadeId}
GET    /api/profissionais/{profissionalId}/disponibilidades
DELETE /api/profissionais/{profissionalId}/disponibilidades/{disponibilidadeId}
```

---

## ⏰ Disponibilidade

```text
POST   /api/disponibilidades
GET    /api/disponibilidades
GET    /api/disponibilidades/{id}
PUT    /api/disponibilidades/{id}
DELETE /api/disponibilidades/{id}
```

---

## 📅 Consulta

Gerenciamento e consulta dos atendimentos.

```text
POST   /api/consultas
GET    /api/consultas
GET    /api/consultas/{id}
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}
```

Consultas específicas:

```text
GET /api/consultas/paciente/{pacienteId}
GET /api/consultas/profissional/{profissionalId}
```

---

## 🗓️ Agenda

A agenda é construída a partir das consultas cadastradas e **não possui um documento próprio no MongoDB**.

```text
GET /api/agenda
GET /api/agenda/dia?data=2026-09-20
GET /api/agenda/profissional/{id}?data=2026-09-20
GET /api/agenda/paciente/{id}
```

---

## 📋 Prontuário

Armazena informações relacionadas ao acompanhamento clínico.

```text
POST /api/prontuarios
GET  /api/prontuarios/{id}
PUT  /api/prontuarios/{id}
GET  /api/prontuarios/paciente/{pacienteId}
```

---

## 🧠 Diagnóstico

```text
POST   /api/diagnosticos
GET    /api/diagnosticos
GET    /api/diagnosticos/{id}
PUT    /api/diagnosticos/{id}
DELETE /api/diagnosticos/{id}
GET    /api/diagnosticos/paciente/{pacienteId}
```

---

## 💊 Tratamento

```text
POST   /api/tratamentos
GET    /api/tratamentos
GET    /api/tratamentos/{id}
PUT    /api/tratamentos/{id}
DELETE /api/tratamentos/{id}
GET    /api/tratamentos/paciente/{pacienteId}
```

---

## 🧪 Exame

```text
POST   /api/exames
GET    /api/exames
GET    /api/exames/{id}
PUT    /api/exames/{id}
DELETE /api/exames/{id}
GET    /api/exames/paciente/{pacienteId}
```

---

## 👨‍💼 Administrador

Gerenciamento de administradores.

```text
POST   /api/administradores
GET    /api/administradores
GET    /api/administradores/{id}
PUT    /api/administradores/{id}
DELETE /api/administradores/{id}
```

As senhas não são retornadas pelos DTOs de resposta e são armazenadas utilizando **BCrypt**.

---

## 🔐 Autenticação e segurança

O projeto utiliza:

* Spring Security
* JWT
* BCrypt
* Filtro de autenticação
* Controle de acesso aos endpoints
* Tratamento global de exceções

### Login

```text
POST /api/auth/login
```

Exemplo:

```json
{
  "email": "admin@email.com",
  "senha": "123456"
}
```

Após o login, a API retorna um token JWT.

Nas requisições protegidas:

```text
Authorization: Bearer <token>
```

### Tratamento de erros

Recursos inexistentes retornam `404`.

Credenciais inválidas retornam `401`, sem diferenciar e-mail inexistente de senha incorreta.

---

## 📖 Swagger / OpenAPI

A API possui documentação através do Swagger.

Localmente:

```text
http://localhost:8080/swagger-ui/index.html
```

Documentação OpenAPI:

```text
http://localhost:8080/v3/api-docs
```

---

## 🐳 Docker

O projeto possui um `Dockerfile` utilizando Java 21 e Maven Wrapper.

A aplicação é executada na porta:

```text
8080
```

O Dockerfile é utilizado para preparar a aplicação para hospedagem em serviços compatíveis com containers.

---

## ☁️ Deploy

O backend está configurado para hospedagem no **Render** utilizando Docker.

URL atual:

```text
https://neurovita.onrender.com
```

Endpoints públicos disponíveis para verificação:

```text
GET /teste
GET /
```

Swagger:

```text
https://neurovita.onrender.com/swagger-ui/index.html
```

> ⚠️ O banco de produção ainda depende da configuração do MongoDB e da variável `MONGODB_URI`.

---

## 🗄️ Banco de dados

O projeto utiliza **MongoDB**.

A conexão é configurada através da variável de ambiente:

```text
MONGODB_URI
```

A chave utilizada pelo JWT também é configurada através de:

```text
JWT_SECRET
```

Informações sensíveis não devem ser armazenadas no código-fonte ou enviadas para o GitHub.

---

## 🚀 Executando localmente

### Pré-requisitos

* Java JDK 21+
* Maven
* Git
* MongoDB, quando forem utilizadas funcionalidades que dependem de persistência

Verificar Java:

```bash
java -version
```

Verificar Maven:

```bash
mvn -version
```

### Clonar

```bash
git clone https://github.com/victorhborges/NeuroVita.git
cd NeuroVita
```

### Executar

Windows:

```bash
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada em:

```text
http://localhost:8080
```

### Endpoint de teste

```text
GET /teste
```

Resposta:

```text
API NeuroVita funcionando!
```

---

## 🧪 Testes

Os testes são executados através do Maven:

```bash
mvn test
```

Windows:

```bash
mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

O projeto possui testes básicos de contexto. Testes mais abrangentes serão desenvolvidos conforme a evolução do projeto e a organização da equipe responsável.

---

## 🔄 Reimplementação em Java

O backend anterior foi desenvolvido utilizando:

```text
Node.js
JavaScript
Express.js
MySQL
```

O novo backend está sendo desenvolvido utilizando:

```text
Java
Spring Boot
Spring Web MVC
Spring Data MongoDB
MongoDB
Spring Security
JWT
```

A implementação utiliza o backend anterior como referência, mas reorganiza a aplicação de acordo com a arquitetura e o ecossistema Java/Spring Boot.

---

## 📊 Status

| Módulo / Recurso        | Status                  |
| ----------------------- | ----------------------- |
| Estrutura Java          | 🟢 Concluído            |
| Spring Boot             | 🟢 Concluído            |
| Spring Web MVC          | 🟢 Concluído            |
| Spring Data MongoDB     | 🟢 Concluído            |
| Pacientes               | 🟢 Concluído            |
| Profissionais           | 🟢 Concluído            |
| Disponibilidades        | 🟢 Concluído            |
| Consultas               | 🟢 Concluído            |
| Agenda                  | 🟢 Concluído            |
| Prontuários             | 🟢 Concluído            |
| Diagnósticos            | 🟢 Concluído            |
| Tratamentos             | 🟢 Concluído            |
| Exames                  | 🟢 Concluído            |
| Administradores         | 🟢 Concluído            |
| Autenticação            | 🟢 Concluído            |
| JWT                     | 🟢 Concluído            |
| BCrypt                  | 🟢 Concluído            |
| Spring Security         | 🟢 Concluído            |
| Swagger/OpenAPI         | 🟢 Concluído            |
| Tratamento de exceções  | 🟢 Concluído            |
| Docker                  | 🟢 Concluído            |
| Deploy Render           | 🟢 Concluído            |
| Receita                 | ⚪ Aguardando requisitos |
| Testes completos        | 🟡 Em desenvolvimento   |
| Integração com frontend | ⚪ Planejado             |
| MongoDB de produção     | ⚪ Pendente              |

---

## 🗺️ Próximas etapas

* [ ] Revisar Controllers
* [ ] Refinar validações da API
* [ ] Refinar regras de autorização
* [ ] Implementar Receita após definição do Figma
* [ ] Ampliar testes
* [ ] Integrar com frontend
* [ ] Configurar MongoDB de produção
* [ ] Validar API em produção

---

## 🌿 Git e GitHub

O projeto utiliza Git e GitHub para controle de versão.

```bash
git add .
git commit -m "feat: mensagem"
git push origin main
```

### Padrão de commits

```text
feat: nova funcionalidade
fix: correção de problema
docs: alteração na documentação
test: alteração nos testes
refactor: melhoria estrutural
chore: configuração ou manutenção
```

---

## 📌 Boas práticas

Durante o desenvolvimento são priorizados:

* Programação Orientada a Objetos
* Separação de responsabilidades
* Princípios SOLID
* DTOs
* Tratamento de exceções
* Segurança das informações
* Variáveis de ambiente
* Código legível
* Documentação
* Controle de versão

---

## 🎓 Projeto acadêmico

**Projeto:** NeuroVita
**Instituição:** Faculdade Brasília
**Curso:** Análise e Desenvolvimento de Sistemas
**Área:** Desenvolvimento de Software

O projeto possui finalidade acadêmica e prática, aplicando conhecimentos de programação, desenvolvimento backend, APIs REST, banco de dados, arquitetura de software, segurança e boas práticas de desenvolvimento.

---

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos.

A utilização, distribuição ou modificação do código deverá respeitar as decisões definidas pela equipe responsável pelo projeto.
