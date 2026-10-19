# 🧠 NeuroVita - Backend

Backend da plataforma **NeuroVita**, desenvolvido como projeto acadêmico da **Faculdade Brasília**, no curso de **Análise e Desenvolvimento de Sistemas (ADS)**.

A aplicação disponibiliza uma **API REST** para gerenciamento de pacientes, responsáveis, profissionais, consultas, agenda e informações relacionadas ao atendimento psicológico.

> 🚧 **Status:** Em desenvolvimento. A API está hospedada no Render. A autenticação, as permissões de acesso, a integração com o banco de dados e as novas operações de responsáveis e vínculos com pacientes continuam em validação.

## 🛠️ Tecnologias

| Tecnologia | Versão / uso |
|---|---|
| ☕ Java | 21 |
| 🌱 Spring Boot | 4.0.8 |
| 🗄️ MongoDB | Banco de dados NoSQL |
| 🔗 Spring Data MongoDB | Persistência de dados |
| 🔐 Spring Security | Autenticação e autorização |
| 🎟️ JWT | Autenticação por token |
| 🔑 BCrypt | Hash e verificação de senhas |
| ✅ Jakarta Bean Validation | Validação de dados de entrada |
| 📖 SpringDoc OpenAPI | Documentação da API |
| 🐳 Docker | Containerização |
| ☁️ Render | Hospedagem |
| 📦 Maven | Gerenciamento e construção do projeto |
| 🧪 Maven Wrapper | Execução do Maven sem instalação global obrigatória |

---

## 📌 Funcionalidades

O backend possui os seguintes módulos e recursos:

- 👤 Pacientes
- 👥 Responsáveis
- 🔗 Vínculos entre pacientes e responsáveis
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
- 👤 Modelo centralizado de usuários
- ✅ Validação de dados de entrada
- 🔄 Status e cancelamento de consultas, em validação local

O módulo de **Receitas** aguarda a definição dos campos e das regras de negócio no Figma.

A existência dos módulos não substitui a validação funcional de cada operação, das permissões e da integração com o banco de dados.

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
           Model
             ↓
           MongoDB
```

### Controller

Recebe as requisições HTTP, aplica as validações de entrada quando configuradas e retorna as respostas da API.

### Service

Implementa as regras de negócio e as operações da aplicação.

### Repository

Responsável pelo acesso aos dados utilizando Spring Data MongoDB.

### Model

Representa as entidades e os documentos persistidos no MongoDB.

### DTO

Separa os dados recebidos e enviados pela API dos modelos de persistência.

### Security

Contém os componentes responsáveis pela autenticação e pela aplicação das regras de segurança, incluindo o filtro JWT.

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
│   │   │   │   └── Controllers dos módulos
│   │   │   ├── dto/
│   │   │   │   └── DTOs de requisição e resposta
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/
│   │   │   │   └── Modelos dos documentos MongoDB
│   │   │   ├── repository/
│   │   │   │   └── Repositórios dos módulos
│   │   │   ├── security/
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── service/
│   │   │   │   └── Serviços dos módulos
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

A estrutura apresenta os principais componentes. Os arquivos específicos de cada módulo foram omitidos para facilitar a leitura.

---

## 👤 Pacientes

O módulo de pacientes oferece operações de cadastro, consulta, atualização e exclusão.

### Endpoints

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

As validações de formato de CPF e telefone não substituem a verificação completa dos dígitos nem a confirmação da existência desses dados.

O cadastro também participa do fluxo de criação de credenciais de acesso, conforme a implementação do serviço.

### Responsáveis vinculados

O cadastro e a atualização de pacientes recebem uma lista de responsáveis. Cada item contém o identificador do responsável já cadastrado e o grau de parentesco.

Exemplo ilustrativo de requisição:

```json
{
  "nome": "Paciente Exemplo",
  "dataNascimento": "2015-05-20",
  "email": "paciente.exemplo@email.com",
  "cpf": "123.456.789-00",
  "telefone": "(61) 99999-0000",
  "possuiPlanoSaude": false,
  "planoSaude": null,
  "numeroCarteirinha": null,
  "validadePlano": null,
  "responsaveis": [
    {
      "responsavelId": "ID_DO_RESPONSAVEL_CADASTRADO",
      "grauParentesco": "Mãe"
    }
  ]
}
```

Os valores são exemplos para ilustrar a estrutura esperada. O identificador precisa corresponder a um responsável existente no banco de dados.

O campo `responsaveis` é uma lista, permitindo representar mais de um responsável para o mesmo paciente.

---

## 👥 Responsáveis

O módulo de responsáveis permite cadastrar, consultar, atualizar e excluir os dados pessoais e de contato dos responsáveis pelos pacientes.

### Endpoints

```text
POST   /api/responsaveis
GET    /api/responsaveis
GET    /api/responsaveis/{id}
PUT    /api/responsaveis/{id}
DELETE /api/responsaveis/{id}
```

### Dados do responsável

O modelo contempla os seguintes campos:

| Campo | Descrição |
|---|---|
| `id` | Identificador do responsável |
| `nome` | Nome do responsável |
| `cpf` | CPF, quando informado |
| `email` | E-mail, quando informado |
| `telefone` | Telefone, quando informado |

O nome é obrigatório. CPF e e-mail possuem verificações de duplicidade quando informados, de acordo com as regras implementadas no serviço.

### Vínculos com pacientes

Os dados pessoais do responsável são armazenados separadamente dos dados do paciente. A associação entre eles é registrada na coleção `vinculosResponsaveis`.

Cada vínculo contém:

| Campo | Descrição |
|---|---|
| `id` | Identificador do vínculo |
| `pacienteId` | Identificador do paciente |
| `responsavelId` | Identificador do responsável |
| `grauParentesco` | Relação entre o paciente e o responsável |

Essa estrutura permite:

- Um paciente possuir vários responsáveis.
- Um responsável estar associado a vários pacientes.
- O grau de parentesco ser definido individualmente em cada vínculo.
- Evitar a duplicação dos dados pessoais do responsável dentro do documento do paciente.

### Regras implementadas localmente

- O nome do responsável é obrigatório.
- CPF e e-mail são normalizados antes das verificações de duplicidade.
- Não é permitido excluir um responsável que ainda possua vínculos com pacientes.
- Cada vínculo precisa apontar para um responsável existente.
- Não são permitidos vínculos duplicados para o mesmo responsável e paciente.
- O grau de parentesco é obrigatório.
- O cadastro de pacientes menores de idade exige pelo menos um responsável.
- Na atualização do paciente, a lista recebida é utilizada para reconciliar os vínculos existentes.

As regras e a persistência dos dados ainda precisam ser verificadas por meio de requisições reais após a publicação da implementação.

### Permissões

As operações de responsáveis estão configuradas para os perfis `ADMINISTRADOR` e `SECRETARIA`. A autorização efetiva deve ser confirmada com requisições autenticadas e com os diferentes perfis.

**Importante:** o vínculo com um responsável não concede automaticamente acesso a prontuários ou a outras informações clínicas do paciente. Essa autorização deve ser tratada separadamente.

---

## 🧑‍⚕️ Profissionais

Gerenciamento de profissionais e suas disponibilidades.

### Endpoints

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

O cadastro também participa do fluxo de criação de credenciais de acesso, conforme a implementação do serviço.

---

## ⏰ Disponibilidades

### Endpoints

```text
POST   /api/disponibilidades
GET    /api/disponibilidades
GET    /api/disponibilidades/{id}
PUT    /api/disponibilidades/{id}
DELETE /api/disponibilidades/{id}
```

O serviço possui validações para os dados obrigatórios e para a ordem dos horários. As regras funcionais completas ainda devem ser verificadas.

---

## 📅 Consultas

O módulo de consultas permite cadastrar, consultar, atualizar e excluir atendimentos. A implementação local também contempla status para representar a situação de cada consulta.

### Endpoints

```text
POST   /api/consultas
GET    /api/consultas
GET    /api/consultas/{id}
PUT    /api/consultas/{id}
DELETE /api/consultas/{id}
PATCH  /api/consultas/{id}/cancelar
```

### Consultas específicas

```text
GET /api/consultas/paciente/{pacienteId}
GET /api/consultas/profissional/{profissionalId}
```

### Status das consultas

| Status | Significado |
|---|---|
| `CONFIRMADA` | Consulta cadastrada e confirmada. É o status inicial de uma nova consulta. |
| `REMARCADA` | Consulta cuja data ou horário foi alterado. |
| `CANCELADA` | Consulta cancelada, mantida no histórico quando utilizado o endpoint de cancelamento. |

O status é controlado pelo backend e não deve ser definido livremente no corpo da requisição de cadastro ou atualização.

### Cancelamento

O endpoint `PATCH /api/consultas/{id}/cancelar` altera o status da consulta para `CANCELADA`, preservando o registro para consulta posterior.

O endpoint `DELETE /api/consultas/{id}` permanece disponível para exclusão definitiva do registro.

### Regras de agendamento

A implementação local contempla validações relacionadas a:

- Existência do paciente e do profissional.
- Data, horário e duração da consulta.
- Disponibilidade cadastrada para o profissional.
- Conflitos de horário do profissional.
- Conflitos de horário do paciente.
- Permissões de acesso às consultas.
- Desconsideração de consultas canceladas na verificação de conflitos.

Consultas canceladas não devem bloquear o horário para novos agendamentos. Consultas antigas sem status explícito são tratadas como confirmadas na resposta e na verificação de conflitos.

A alteração de data ou horário por meio do endpoint de atualização marca a consulta como `REMARCADA`. Consultas canceladas não podem ser atualizadas pela operação normal de alteração.

**Atenção:** essas regras foram implementadas localmente e ainda precisam ser validadas funcionalmente, inclusive após a publicação no Render.

---

## 🗓️ Agenda

A agenda é construída a partir das consultas cadastradas e não possui um documento próprio no MongoDB, conforme a arquitetura atual.

### Endpoints

```text
GET /api/agenda

GET /api/agenda/dia?data=2026-09-20

GET /api/agenda/profissional/{id}?data=2026-09-20

GET /api/agenda/paciente/{id}
```

A apresentação dos status e o tratamento das consultas canceladas na agenda devem ser conferidos durante a validação funcional.

---

## 📋 Prontuários

Armazenam informações relacionadas ao acompanhamento clínico.

### Endpoints

```text
POST /api/prontuarios
GET  /api/prontuarios/{id}
PUT  /api/prontuarios/{id}
GET  /api/prontuarios/paciente/{pacienteId}
```

O acesso aos prontuários deve respeitar as permissões de cada perfil e os vínculos de atendimento entre profissionais e pacientes. A validação completa dessas restrições permanece necessária.

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

### Endpoints

```text
POST   /api/administradores
GET    /api/administradores
GET    /api/administradores/{id}
PUT    /api/administradores/{id}
DELETE /api/administradores/{id}
```

As senhas não devem ser retornadas pelos DTOs de resposta e são armazenadas utilizando BCrypt.

O perfil `ADMINISTRADOR` é utilizado nas regras de autorização da API.

A integração entre o cadastro de administradores e o modelo centralizado de usuários faz parte da implementação de autenticação em revisão.

---

## 🔐 Autenticação e segurança

O projeto utiliza:

- Spring Security.
- JWT (JSON Web Token).
- BCrypt.
- Filtro de autenticação JWT.
- Modelo centralizado `Usuario`.
- Verificação do estado ativo da conta.
- Identificação do perfil do usuário autenticado.
- Fluxo de troca de senha no primeiro acesso.
- Regras de autorização configuradas no Spring Security.

### Modelo centralizado de usuários

A autenticação utiliza a entidade `Usuario`, responsável pelas credenciais e pelas informações necessárias à identificação dos usuários.

O modelo contempla:

- Nome.
- E-mail.
- Senha armazenada com BCrypt.
- Perfil de acesso.
- Indicador de senha provisória.
- Estado ativo da conta.
- Identificador da entidade relacionada, quando aplicável.

O repositório oferece busca de usuários por e-mail sem diferenciar maiúsculas e minúsculas.

A unicidade dos e-mails e a consistência dos vínculos com as entidades de domínio devem ser garantidas pela implementação e pela configuração do banco de dados.

### Login

```text
POST /api/auth/login
```

Exemplo de requisição:

```json
{
  "email": "admin@email.com",
  "senha": "senha-de-exemplo"
}
```

Após a validação das credenciais, a API retorna um token JWT e os dados previstos no DTO de resposta.

O exemplo é ilustrativo. Não utilize credenciais reais em documentação pública.

### Envio do token

Nas requisições protegidas, o token deve ser enviado no cabeçalho HTTP:

```text
Authorization: Bearer <token>
```

O filtro JWT verifica o token e consulta o usuário correspondente. Contas inexistentes ou inativas não devem autenticar as requisições.

### Primeiro acesso e troca de senha

```text
POST /api/auth/primeiro-acesso/alterar-senha
```

Esse endpoint é destinado à troca obrigatória de uma senha provisória.

A requisição autenticada utiliza o token de primeiro acesso. O e-mail do usuário é obtido a partir da autenticação, em vez de ser informado livremente no corpo da requisição.

Exemplo de corpo:

```json
{
  "novaSenha": "NovaSenhaSegura123",
  "confirmarSenha": "NovaSenhaSegura123"
}
```

A implementação local contempla:

- Token com validade reduzida para o primeiro acesso.
- Restrição do token de primeiro acesso ao endpoint de troca de senha.
- Validação de preenchimento e tamanho da nova senha.
- Confirmação de igualdade entre as senhas.
- Armazenamento da nova senha com BCrypt.
- Atualização do indicador de senha provisória.

Após a alteração, um novo login deve utilizar a senha definitiva.

### Perfis e autorização

Os perfis previstos no modelo de autenticação são:

- **Administrador:** administração geral do sistema.
- **Profissional:** acesso aos atendimentos e aos dados necessários dos pacientes sob sua responsabilidade.
- **Secretaria:** operações administrativas e de agendamento, respeitando as restrições de informações clínicas.
- **Paciente:** acesso aos próprios dados e informações de atendimento, conforme as regras do sistema.

A configuração local contém regras específicas para rotas de administradores, cadastro e consulta de pacientes e profissionais, além da troca de senha no primeiro acesso e das operações de responsáveis.

As permissões de todos os endpoints ainda precisam ser revisadas e validadas. A existência de um perfil no modelo centralizado não significa, por si só, que todas as suas permissões já estejam implementadas.

O acesso a prontuários, diagnósticos, exames e demais informações clínicas deverá respeitar as permissões de cada perfil e o vínculo entre profissionais e pacientes. Essas restrições devem ser verificadas no backend, e não apenas na interface do usuário.

O vínculo entre um paciente e um responsável não concede automaticamente acesso aos dados clínicos.

A consulta ao perfil individual de um profissional permite o acesso ao próprio perfil pelo usuário autenticado com perfil `PROFISSIONAL`, respeitando a validação do vínculo entre a conta e a entidade. O acesso administrativo permanece permitido. A validação dessa regra em produção está pendente.

### Tratamento de erros

O tratamento de erros contempla exceções para recursos inexistentes e credenciais inválidas.

Comportamentos esperados:

- Recursos inexistentes: resposta `404`, conforme o tratamento da rota.
- Credenciais inválidas: resposta `401`, sem diferenciar e-mail inexistente de senha incorreta.
- Token inválido ou expirado: requisição não autenticada.
- Conta inativa: requisição não autenticada.
- Token de primeiro acesso em rota não permitida: acesso negado.

As respostas de validação e os códigos de erro devem ser conferidos durante os testes funcionais.

---

## 📖 Swagger / OpenAPI

A API possui documentação interativa por meio do Swagger/OpenAPI, permitindo consultar os endpoints e executar requisições HTTP.

### Localmente

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Documentação OpenAPI:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Em produção

- **Swagger UI:** [https://neurovita.onrender.com/swagger-ui/index.html](https://neurovita.onrender.com/swagger-ui/index.html)
- **API:** [https://neurovita.onrender.com](https://neurovita.onrender.com)

### Autenticação no Swagger

A documentação está configurada para utilizar autenticação HTTP Bearer com JWT nos endpoints protegidos.

**Como autenticar:**

1. Execute `POST /api/auth/login` com um e-mail e uma senha válidos.
2. Copie o token JWT retornado pela API.
3. Clique no botão **Authorize**, identificado pelo cadeado no Swagger.
4. Informe o token no campo de autenticação, utilizando o formato `Bearer SEU_TOKEN` caso o campo não acrescente o prefixo automaticamente.
5. Confirme a autorização e execute um endpoint que exija autenticação.

O endpoint `POST /api/auth/login` não exige um token prévio. Os demais endpoints seguem as regras de autenticação e autorização configuradas no Spring Security.

> **Importante:** não compartilhe tokens JWT, senhas ou credenciais reais em documentação pública.

A presença do botão Authorize confirma a configuração da documentação OpenAPI, mas o funcionamento do login e das permissões deve ser validado por meio de requisições reais.

---

## 🐳 Docker

O projeto possui um `Dockerfile` para preparar a aplicação para execução em um container com Java 21.

A aplicação utiliza a variável de ambiente `PORT`, com `8080` como valor padrão:

```text
PORT=8080
```

O Dockerfile é utilizado na configuração de hospedagem em serviços compatíveis com containers.

---

## ☁️ Deploy

O backend está hospedado no **Render**, utilizando Docker.

### URL da API

[https://neurovita.onrender.com](https://neurovita.onrender.com)

### Endpoints públicos de verificação

```text
GET /
GET /teste
```

### Swagger

[https://neurovita.onrender.com/swagger-ui/index.html](https://neurovita.onrender.com/swagger-ui/index.html)

A resposta dos endpoints de verificação confirma que a rota está respondendo, mas não comprova, por si só, a conectividade com o MongoDB.

As alterações desenvolvidas localmente só estarão disponíveis no ambiente hospedado depois do commit, do push e da conclusão bem-sucedida do novo deploy.

---

## 🗄️ Banco de dados

O projeto utiliza **MongoDB** com Spring Data MongoDB.

### Coleções principais

A aplicação utiliza documentos MongoDB para persistir as entidades dos módulos. A estrutura final depende da configuração e da migração coordenada com a equipe responsável pelo banco.

As coleções relacionadas à funcionalidade de responsáveis são:

| Coleção | Finalidade |
|---|---|
| `responsaveis` | Dados pessoais e de contato dos responsáveis |
| `vinculosResponsaveis` | Relação entre pacientes e responsáveis, incluindo o grau de parentesco |
| `pacientes` | Dados cadastrais dos pacientes |

O relacionamento entre paciente e responsável é armazenado separadamente, evitando duplicar os dados pessoais do responsável em cada paciente.

### Configuração da conexão

A aplicação utiliza a variável de ambiente:

```text
MONGODB_URI
```

A propriedade configurada em `application.properties` é:

```properties
spring.mongodb.uri=${MONGODB_URI}
```

Em produção, a URI de conexão é fornecida pela variável `MONGODB_URI`, configurada no ambiente do Render.

A aplicação também utiliza a variável `JWT_SECRET`, que fornece a chave usada para assinar e validar os tokens JWT.

### Segurança das configurações

- Não armazenar senhas ou tokens no código-fonte.
- Não versionar URIs do MongoDB que contenham credenciais.
- Não publicar os valores reais de `MONGODB_URI` ou `JWT_SECRET`.
- Configurar as variáveis no ambiente de execução.

### Status da conexão

A configuração da integração com MongoDB está presente no projeto. A conectividade com o MongoDB Atlas, bem como a leitura e a gravação de documentos em produção, deve ser confirmada por meio de verificações específicas.

Um teste automatizado de contexto aprovado localmente não comprova, isoladamente, a conexão real com o banco de dados.

A estrutura de responsáveis e vínculos precisa ser compatibilizada com a migração do MongoDB coordenada pela equipe responsável pelo banco antes de considerar a funcionalidade validada em produção.

---

## 🚀 Executando localmente

### Pré-requisitos

- Java JDK 21, compatível com a versão configurada no projeto.
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

O Maven Wrapper incluído no projeto também pode ser utilizado sem depender de uma instalação global do Maven.

### Clonar o repositório

```bash
git clone https://github.com/victorhborges/NeuroVita.git

cd NeuroVita
```

### Configurar as variáveis de ambiente

Configure `MONGODB_URI` com uma URI válida do MongoDB e `JWT_SECRET` com uma chave secreta forte.

A chave JWT deve atender ao tamanho mínimo exigido pela implementação de autenticação.

Não inclua os valores reais dessas variáveis em arquivos versionados ou em exemplos públicos.

### Executar a aplicação

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

A resposta esperada deve ser conferida na implementação atual do endpoint.

A resposta desse endpoint confirma que a rota está respondendo, mas não comprova, por si só, a conectividade com o MongoDB.

---

## 🧪 Testes

Os testes são executados por meio do Maven.

### Windows

```cmd
mvn test
```

Ou, utilizando o Maven Wrapper:

```cmd
mvnw.cmd test
```

### Linux/macOS

```bash
./mvnw test
```

A execução local mais recente informada terminou com `BUILD SUCCESS`.

Esse resultado indica que a execução dos testes Maven terminou sem falhas reportadas. Ele não comprova a conectividade real com o MongoDB Atlas nem a validação funcional de todos os endpoints, perfis e permissões.

Os testes funcionais completos são responsabilidade da equipe designada para essa atividade. A cobertura de testes poderá ser ampliada conforme a evolução do projeto e o planejamento da equipe.

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
Java 21
Spring Boot
Spring Web MVC
Spring Data MongoDB
MongoDB
Spring Security
JWT
BCrypt
Jakarta Bean Validation
```

A implementação utiliza o backend anterior como referência, mas reorganiza a aplicação de acordo com a arquitetura e o ecossistema Java/Spring Boot.

---

## 📊 Status do projeto

| Módulo / recurso | Status conhecido |
|---|---|
| Estrutura Java | 🟢 Implementada |
| Spring Boot | 🟢 Implementado |
| Spring Web MVC | 🟢 Implementado |
| Spring Data MongoDB | 🟢 Configurado |
| Pacientes | 🟡 CRUD implementado; novos vínculos em validação |
| Responsáveis | 🟡 CRUD implementado localmente; aguardando validação funcional |
| Vínculos entre pacientes e responsáveis | 🟡 Implementados localmente; aguardando validação funcional e compatibilidade com o banco |
| Profissionais | 🟢 CRUD implementado |
| Validação de pacientes e profissionais | 🟢 Implementada localmente |
| Disponibilidades | 🟢 Implementadas |
| Consultas | 🟡 CRUD e regras de agendamento implementados; status e cancelamento em validação local |
| Agenda | 🟢 Implementada; comportamento dos status a validar |
| Prontuários | 🟢 CRUD implementado |
| Diagnósticos | 🟢 CRUD implementado |
| Tratamentos | 🟢 CRUD implementado |
| Exames | 🟢 CRUD implementado |
| Administradores | 🟢 CRUD implementado |
| Modelo centralizado `Usuario` | 🟡 Implementado localmente, pendente de validação funcional |
| Login centralizado | 🟡 Implementado localmente, pendente de validação funcional |
| JWT | 🟡 Implementação local em revisão |
| BCrypt | 🟢 Utilizado no fluxo de autenticação |
| Troca de senha no primeiro acesso | 🟡 Implementada localmente, pendente de validação funcional |
| Spring Security | 🟡 Configuração e permissões em revisão |
| Autorização por perfil para todos os módulos | 🟡 Em desenvolvimento |
| Proteção por vínculo profissional-paciente | ⚪ Pendente de validação |
| Swagger/OpenAPI | 🟢 Implementado |
| Tratamento global de exceções | 🟢 Implementado, com melhorias pendentes |
| Docker | 🟢 Configurado |
| Deploy Render | 🟢 Implantação configurada |
| MongoDB Atlas em produção | 🟡 Conexão e operações reais pendentes de confirmação |
| Receitas | ⚪ Aguardando requisitos |
| Testes completos | 🟡 Sob responsabilidade da equipe de testes |
| Integração com frontend | ⚪ Planejada |

Os status indicam o estado de desenvolvimento conhecido. Não substituem a validação funcional dos módulos, a revisão de segurança ou a confirmação do ambiente de produção.

---

## 🗺️ Próximas etapas

- [x] Adicionar validações iniciais aos DTOs de pacientes e profissionais.
- [x] Aplicar `@Valid` nos endpoints de cadastro e atualização desses módulos.
- [x] Executar os testes Maven localmente com resultado `BUILD SUCCESS`.
- [x] Preparar a implementação local de autenticação centralizada e troca de senha no primeiro acesso.
- [x] Implementar localmente os status das consultas e o endpoint de cancelamento.
- [x] Implementar localmente o CRUD de responsáveis.
- [x] Implementar localmente os vínculos entre pacientes e responsáveis.
- [ ] Revisar o diff e publicar as alterações no GitHub.
- [ ] Confirmar o deploy das alterações no Render.
- [ ] Validar o CRUD de responsáveis pelo Swagger.
- [ ] Validar o cadastro de pacientes com um ou mais responsáveis.
- [ ] Validar a atualização e a remoção de vínculos.
- [ ] Validar as regras de menores de idade sem responsável.
- [ ] Compatibilizar a funcionalidade com a estrutura final do MongoDB Atlas.
- [ ] Validar a criação e a associação das contas de usuários às entidades de domínio.
- [ ] Confirmar a conexão com o MongoDB Atlas em produção.
- [ ] Validar operações reais de leitura e gravação no banco.
- [ ] Validar as regras de status, remarcação, cancelamento e conflitos de horário.
- [ ] Validar o login e a troca de senha no primeiro acesso.
- [ ] Revisar as permissões dos endpoints para cada perfil.
- [ ] Padronizar as respostas de erro de validação.
- [ ] Implementar e validar as restrições de acesso aos dados clínicos.
- [ ] Definir o módulo de Receitas após a especificação no Figma.
- [ ] Validar a API em produção com a equipe responsável.
- [ ] Integrar com o frontend.
- [ ] Ampliar a cobertura de testes conforme o planejamento da equipe.

---

## 🌿 Git e GitHub

O projeto utiliza Git e GitHub para controle de versão.

### Fluxo básico

```bash
git status

git diff

git add README.md

git diff --cached

git commit -m "docs: atualiza README"

git push origin main
```

Esse é um exemplo de fluxo para uma alteração exclusivamente documental. Para commits que incluem código, adicione também os arquivos específicos da funcionalidade revisada.

Antes de executar `git add`, revise as alterações para evitar incluir arquivos temporários, credenciais ou mudanças não relacionadas.

Atualize o README quando houver alterações relevantes na arquitetura, nas funcionalidades, nos endpoints ou na configuração do projeto.

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
- Validação dos dados de entrada.
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
