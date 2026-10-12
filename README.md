# 🧠 NeuroVita - Backend

Backend da plataforma **NeuroVita**, desenvolvido como projeto acadêmico da **Faculdade Brasília — Análise e Desenvolvimento de Sistemas**.

A aplicação disponibiliza uma **API REST** para gerenciamento de pacientes, profissionais, consultas, agenda e informações relacionadas ao atendimento psicológico.

> 🚧 **Status:** Em desenvolvimento.

## 🛠️ Tecnologias

| Tecnologia | Versão / Uso |
|---|---|
| ☕ Java | 21 |
| 🌱 Spring Boot | 4.0.8 |
| 🗄️ MongoDB | Banco de dados |
| 🐳 Docker | Containerização |
| ☁️ Render | Hospedagem |
| 📦 Maven | Gerenciamento do projeto |
| 🔐 Spring Security | Segurança e autorização |
| 🎟️ JWT | Autenticação por token |
| 🔑 BCrypt | Codificação e verificação de senhas |
| ✅ Jakarta Bean Validation | Validação dos dados de entrada |

---

## 📌 Funcionalidades

Atualmente, o backend possui os seguintes módulos:

- 👤 Pacientes
- 🧑‍⚕️ Profissionais
- ⏰ Disponibilidades
- 📅 Consultas
- 🗓️ Agenda
- 📋 Prontuários
- 🧠 Diagnósticos
- 💊 Tratamentos
- 🧪 Exames
- 👨‍💼 Administradores
- 🔐 Autenticação e segurança
- ✅ Validação de dados de entrada para pacientes e profissionais, com Jakarta Bean Validation.

O módulo de **Receitas** aguarda a definição dos campos e das regras no Figma.

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

Responsável pelas requisições HTTP e pelas respostas da API.

### Service

Responsável pelas regras de negócio e pelas operações da aplicação.

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

## 👤 Pacientes

CRUD de pacientes com operações de cadastro, consulta, atualização e exclusão.

```text
POST   /api/pacientes
GET    /api/pacientes
GET    /api/pacientes/{id}
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
```

### Validação dos dados

O cadastro e a atualização utilizam Jakarta Bean Validation para verificar regras aplicáveis aos campos recebidos.

Entre as validações implementadas estão:

- Nome obrigatório.
- Data de nascimento obrigatória e anterior à data atual.
- Formato de e-mail.
- Formato de CPF.
- Formato de telefone.

As validações de formato de CPF e telefone não substituem uma verificação completa dos dígitos ou a confirmação da existência desses dados.

---

## 🧑‍⚕️ Profissionais

Gerenciamento de profissionais e suas disponibilidades.

```text
POST   /api/profissionais
GET    /api/profissionais
GET    /api/profissionais/{id}
PUT    /api/profissionais/{id}
DELETE /api/profissionais/{id}
```

### Validação dos dados

O cadastro e a atualização utilizam Jakarta Bean Validation para verificar regras aplicáveis aos campos recebidos.

Entre as validações implementadas estão:

- Nome obrigatório.
- Formato de e-mail.
- Formato de CPF.
- Formato de telefone.
- Categoria profissional obrigatória.
- Limites para o ano de conclusão.

### Disponibilidades vinculadas

```text
POST   /api/profissionais/{profissionalId}/disponibilidades/{disponibilidadeId}
GET    /api/profissionais/{profissionalId}/disponibilidades
DELETE /api/profissionais/{profissionalId}/disponibilidades/{disponibilidadeId}
```

---

## ⏰ Disponibilidades

```text
POST   /api/disponibilidades
GET    /api/disponibilidades
GET    /api/disponibilidades/{id}
PUT    /api/disponibilidades/{id}
DELETE /api/disponibilidades/{id}
```

---

## 📅 Consultas

Gerenciamento e consulta dos atendimentos.

```text
POST   /api/consultas
GET    /api/consultas
GET    /api/consultas/{id}
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}
```

### Consultas específicas

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

## 📋 Prontuários

Armazenam informações relacionadas ao acompanhamento clínico.

```text
POST /api/prontuarios

GET  /api/prontuarios/{id}

PUT  /api/prontuarios/{id}

GET  /api/prontuarios/paciente/{pacienteId}
```

O acesso aos prontuários deverá respeitar as permissões de cada perfil e os vínculos de atendimento entre profissionais e pacientes.

---

## 🧠 Diagnósticos

```text
POST   /api/diagnosticos
GET    /api/diagnosticos
GET    /api/diagnosticos/{id}
PUT    /api/diagnosticos/{id}
DELETE /api/diagnosticos/{id}

GET    /api/diagnosticos/paciente/{pacienteId}
```

---

## 💊 Tratamentos

```text
POST   /api/tratamentos
GET    /api/tratamentos
GET    /api/tratamentos/{id}
PUT    /api/tratamentos/{id}
DELETE /api/tratamentos/{id}

GET    /api/tratamentos/paciente/{pacienteId}
```

---

## 🧪 Exames

```text
POST   /api/exames
GET    /api/exames
GET    /api/exames/{id}
PUT    /api/exames/{id}
DELETE /api/exames/{id}

GET    /api/exames/paciente/{pacienteId}
```

---

## 👨‍💼 Administradores

Gerenciamento de administradores.

```text
POST   /api/administradores
GET    /api/administradores
GET    /api/administradores/{id}
PUT    /api/administradores/{id}
DELETE /api/administradores/{id}
```

As senhas não são retornadas pelos DTOs de resposta e são armazenadas utilizando **BCrypt**.

Os administradores possuem um perfil de acesso utilizado no controle de autorização da API.

---

## 🔐 Autenticação e segurança

O projeto utiliza:

- Spring Security.
- JWT (JSON Web Token).
- BCrypt.
- Filtro de autenticação JWT.
- Controle de acesso aos endpoints.
- Controle de acesso por perfil, em evolução.
- Tratamento global de exceções.

### Login

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

Após a validação das credenciais, a API retorna um token JWT.

Nas requisições protegidas, o token deve ser enviado no cabeçalho:

```text
Authorization: Bearer <token>
```

### Perfis e autorização

Atualmente, o fluxo de autenticação está implementado para administradores, com validação de credenciais utilizando BCrypt e emissão de token JWT.

O perfil `ADMINISTRADOR` é utilizado no controle de acesso aos endpoints de gerenciamento de administradores.

A autenticação dos perfis **profissional, secretaria e paciente**, assim como as regras específicas de autorização para cada perfil, ainda precisa ser implementada e validada.

As permissões previstas para os perfis deverão considerar as responsabilidades de cada usuário e a proteção dos dados pessoais e clínicos.

- **Administrador:** administração geral do sistema.
- **Profissional:** acesso aos atendimentos e aos dados necessários dos pacientes sob sua responsabilidade.
- **Secretaria:** acesso aos dados necessários às operações administrativas, respeitando as restrições de informações clínicas.
- **Paciente:** acesso aos próprios dados e informações de atendimento, conforme as regras do sistema.

Essas permissões são uma proposta inicial e ainda precisam ser implementadas e validadas no backend.

O acesso a prontuários, diagnósticos, exames e demais informações clínicas deverá respeitar as permissões de cada perfil e o vínculo entre profissionais e pacientes. A autorização deverá ser verificada no backend, não apenas na interface do usuário.

### Tratamento de erros

- Recursos inexistentes retornam `404`.
- Credenciais inválidas retornam `401`, sem diferenciar e-mail inexistente de senha incorreta.
- Tokens inválidos ou expirados não autenticam a requisição.

O tratamento de erros de validação dos campos também deverá ser padronizado para fornecer respostas claras e consistentes à API.

---

## 📖 Swagger / OpenAPI

A API possui documentação através do Swagger.

### Localmente

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### Documentação OpenAPI

[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🐳 Docker

O projeto possui um `Dockerfile` utilizando Java 21 e Maven Wrapper.

A aplicação utiliza a porta definida pela variável de ambiente `PORT`, com `8080` como valor padrão.

```text
PORT=8080
```

O Dockerfile é utilizado para preparar a aplicação para hospedagem em serviços compatíveis com containers.

---

## ☁️ Deploy

O backend está configurado para hospedagem no **Render** utilizando Docker.

### URL

[https://neurovita.onrender.com](https://neurovita.onrender.com)

### Endpoints públicos para verificação

```text
GET /
GET /teste
```

### Swagger

[https://neurovita.onrender.com/swagger-ui/index.html](https://neurovita.onrender.com/swagger-ui/index.html)

> ⚠️ A aplicação utiliza a variável de ambiente `MONGODB_URI` para conexão com o MongoDB Atlas. A conectividade com o banco em produção permanece em validação.

---

## 🗄️ Banco de dados

O projeto utiliza **MongoDB** e Spring Data MongoDB.

A conexão é configurada por meio da variável de ambiente:

```text
MONGODB_URI
```

A propriedade utilizada pela aplicação é:

```properties
spring.mongodb.uri=${MONGODB_URI}
```

Em produção, a aplicação utiliza a URI fornecida pela variável `MONGODB_URI` configurada no Render.

A chave utilizada para assinatura dos tokens JWT também é configurada por meio de:

```text
JWT_SECRET
```

Informações sensíveis, como senhas, tokens e URIs com credenciais, não devem ser armazenadas no código-fonte ou enviadas para o GitHub.

**Status da conexão em produção:** em validação. O teste de contexto automatizado passou localmente, mas ainda é necessário confirmar que a aplicação consegue estabelecer conexão real com o MongoDB Atlas e executar operações de leitura e gravação.

---

## 🚀 Executando localmente

### Pré-requisitos

- Java JDK 21 ou superior, com compatibilidade com a versão definida no projeto.
- Git.
- MongoDB Atlas ou outra instância MongoDB acessível.
- Variáveis de ambiente necessárias configuradas.

### Verificar Java

```bash
java -version
```

### Verificar Maven

```bash
mvn -version
```

### Clonar o repositório

```bash
git clone https://github.com/victorhborges/NeuroVita.git

cd NeuroVita
```

### Configurar as variáveis de ambiente

Configure `MONGODB_URI` com a URI de conexão válida do MongoDB e `JWT_SECRET` com uma chave secreta forte.

Não inclua os valores reais dessas variáveis em arquivos versionados ou em exemplos públicos.

### Executar

**Windows**

```cmd
mvnw.cmd spring-boot:run
```

**Linux/macOS**

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada na porta configurada, normalmente:

[http://localhost:8080](http://localhost:8080)

### Endpoint de teste

```text
GET /teste
```

Resposta esperada:

```text
API NeuroVita funcionando!
```

A resposta desse endpoint confirma que a rota está respondendo, mas não comprova, por si só, a conectividade com o MongoDB.

---

## 🧪 Testes

Os testes são executados por meio do Maven.

### Windows

```cmd
mvnw.cmd test
```

### Linux/macOS

```bash
./mvnw test
```

O projeto possui um teste básico de contexto da aplicação.

A execução local mais recente do teste automatizado terminou com `BUILD SUCCESS`, sem falhas ou erros no teste executado.

Esse resultado não comprova a conectividade real com o MongoDB Atlas nem a validação funcional de todos os endpoints.

Testes mais abrangentes serão desenvolvidos conforme a evolução do projeto e a organização da equipe responsável pelos testes.

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
Jakarta Bean Validation
```

A implementação utiliza o backend anterior como referência, mas reorganiza a aplicação de acordo com a arquitetura e o ecossistema Java/Spring Boot.

---

## 📊 Status do projeto

| Módulo / Recurso | Status |
|---|---|
| Estrutura Java | 🟢 Implementada |
| Spring Boot | 🟢 Implementado |
| Spring Web MVC | 🟢 Implementado |
| Spring Data MongoDB | 🟢 Implementado |
| Validação de pacientes e profissionais | 🟢 Implementada localmente |
| Pacientes | 🟢 CRUD implementado |
| Profissionais | 🟢 CRUD implementado |
| Disponibilidades | 🟢 Implementadas |
| Consultas | 🟢 CRUD implementado |
| Agenda | 🟢 Implementada |
| Prontuários | 🟢 CRUD implementado |
| Diagnósticos | 🟢 CRUD implementado |
| Tratamentos | 🟢 CRUD implementado |
| Exames | 🟢 CRUD implementado |
| Administradores | 🟢 CRUD implementado |
| Login de administrador | 🟡 Implementado, pendente de validação funcional |
| Login dos quatro perfis | ⚪ Pendente |
| JWT | 🟢 Implementado no fluxo atual |
| BCrypt | 🟢 Implementado no fluxo atual |
| Spring Security | 🟡 Configuração em revisão |
| Autorização por perfil para todos os módulos | 🟡 Em desenvolvimento |
| Proteção de informações clínicas por vínculo profissional-paciente | ⚪ Pendente de implementação e validação |
| Swagger/OpenAPI | 🟢 Implementado |
| Tratamento global de exceções | 🟢 Implementado, com melhorias pendentes |
| Docker | 🟢 Configurado |
| Deploy Render | 🟢 Implantação configurada |
| MongoDB de produção | 🟡 Em validação |
| Receita | ⚪ Aguardando requisitos |
| Testes completos | 🟡 Em desenvolvimento pela equipe responsável |
| Integração com frontend | ⚪ Planejada |

Os status indicam o estado de desenvolvimento conhecido e não substituem a validação funcional dos módulos.

---

## 🗺️ Próximas etapas

- [x] Adicionar validações iniciais aos DTOs de pacientes e profissionais.
- [x] Aplicar `@Valid` nos endpoints de cadastro e atualização desses módulos.
- [ ] Padronizar as respostas de erro de validação.
- [ ] Revisar a configuração automática do Spring Security.
- [ ] Implementar e validar o login dos perfis profissional, secretaria e paciente.
- [ ] Refinar as regras de autorização dos perfis.
- [ ] Implementar a proteção de dados clínicos com base no vínculo profissional-paciente.
- [ ] Definir o módulo de Receitas após a especificação no Figma.
- [ ] Validar a conexão com o MongoDB Atlas em produção.
- [ ] Validar as operações de leitura e gravação no banco de produção.
- [ ] Validar a API em produção.
- [ ] Integrar com o frontend.
- [ ] Ampliar a cobertura de testes com a equipe responsável.

---

## 🌿 Git e GitHub

O projeto utiliza Git e GitHub para controle de versão.

### Fluxo básico

```bash
git status

git add .

git commit -m "feat: mensagem descritiva"

git push origin main
```

Antes de executar `git add .`, revise as alterações para evitar incluir arquivos temporários, credenciais ou mudanças não relacionadas.

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

- Programação Orientada a Objetos.
- Separação de responsabilidades.
- Princípios SOLID.
- DTOs.
- Validação de dados de entrada.
- Tratamento de exceções.
- Segurança das informações.
- Variáveis de ambiente.
- Código legível.
- Documentação.
- Controle de versão.

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