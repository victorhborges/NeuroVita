# 🧠 NeuroVita - Plataforma Integrada

Plataforma **NeuroVita**, desenvolvida como projeto acadêmico da **Faculdade Brasília**, no curso de **Análise e Desenvolvimento de Sistemas (ADS)**.

O projeto possui uma arquitetura integrada, composta por frontend Angular, backend Java com Spring Boot e banco de dados MongoDB Atlas.

O backend disponibiliza uma API REST para gerenciamento de pacientes, responsáveis, profissionais, consultas, agenda e informações relacionadas ao atendimento psicológico. O frontend Angular é compilado e servido pela própria aplicação Spring Boot.

> 🚧 **Status:** A integração frontend e backend está implementada localmente. A aplicação integrada foi compilada e executada localmente, com conexão ao MongoDB Atlas. O login real com JWT foi verificado pelo frontend, incluindo armazenamento da sessão, exibição do nome do usuário autenticado e logout. O interceptor JWT e a proteção de navegação por perfil estão implementados. A nova arquitetura integrada ainda não foi publicada no Render. A validação funcional completa permanece sob responsabilidade da equipe designada para os testes.

---

## 🛠️ Tecnologias

| Tecnologia                | Versão / uso                                        |
| ------------------------- | --------------------------------------------------- |
| ☕ Java                    | 21                                                  |
| 🌱 Spring Boot            | 4.0.8                                               |
| 🅰️ Angular               | 21                                                  |
| 🟢 Node.js / npm          | Dependências e compilação do frontend               |
| 🗄️ MongoDB               | Banco de dados NoSQL                                |
| 🔗 Spring Data MongoDB    | Persistência de dados                               |
| 🔐 Spring Security        | Autenticação e autorização                          |
| 🎟️ JWT                   | Autenticação por token                              |
| 🔑 BCrypt                 | Hash e verificação de senhas                        |
| ✅ Jakarta Bean Validation | Validação de dados de entrada                       |
| 📖 SpringDoc OpenAPI      | Documentação da API                                 |
| 🐳 Docker                 | Containerização e build multi-stage                 |
| ☁️ Render                 | Hospedagem                                          |
| 📦 Maven                  | Gerenciamento e construção do projeto               |
| 🧪 Maven Wrapper          | Execução do Maven sem instalação global obrigatória |

---

## 📌 Funcionalidades

O projeto possui os seguintes módulos e recursos:

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
- 🔄 Status, remarcação e cancelamento de consultas
- 🖥️ Frontend Angular integrado à aplicação Spring Boot

O módulo de **Receitas** aguarda a definição dos campos e das regras de negócio no Figma.

A existência dos módulos não substitui a validação funcional de cada operação, das permissões e das regras de negócio.

---

## 🏗️ Arquitetura

O NeuroVita utiliza uma arquitetura integrada, na qual o Spring Boot disponibiliza a API REST e também serve os arquivos estáticos compilados do Angular.

### Visão geral

```text
                  Usuário / Navegador
                          |
                          v
                   Frontend Angular
                          |
                          v
                  Aplicação Spring Boot
                          |
             +------------+------------+
             |                         |
             v                         v
       Arquivos estáticos          API REST
             |                         |
             |                    Controller
             |                         |
             |                      Service
             |                         |
             |                    Repository
             |                         |
             |                       Model
             |                         |
             |                      MongoDB
             |                    MongoDB Atlas
             |
             v
          Interface
```

### Frontend

Desenvolvido em Angular, responsável pela interface utilizada pelos administradores, profissionais, secretarias e pacientes.

O frontend é compilado e seus arquivos gerados são incorporados aos recursos estáticos do Spring Boot, em `src/main/resources/static/`.

A autenticação do frontend utiliza a API do backend. O token JWT é armazenado em `sessionStorage` e enviado nas requisições protegidas por meio de um interceptor HTTP.

### Controller

Recebe as requisições HTTP, aplica as validações de entrada configuradas e retorna as respostas da API.

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
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SpaRoutingConfig.java
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── NeurovitaApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       └── application.properties
│   │
│   └── test/
│
├── .dockerignore
├── .gitignore
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

A estrutura apresenta os principais componentes. Os arquivos específicos de cada módulo foram omitidos para facilitar a leitura.

O diretório `frontend/` contém o projeto Angular. O diretório `src/` contém a aplicação Java Spring Boot.

Os arquivos compilados do Angular são incorporados aos recursos estáticos do Spring Boot. A pasta `src/main/resources/static/` contém a versão compilada utilizada pela aplicação integrada.

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

| Campo      | Descrição                    |
| ---------- | ---------------------------- |
| `id`       | Identificador do responsável |
| `nome`     | Nome do responsável          |
| `cpf`      | CPF, quando informado        |
| `email`    | E-mail, quando informado     |
| `telefone` | Telefone, quando informado   |

O nome é obrigatório. O DTO utiliza validações para os campos aplicáveis, incluindo e-mail, CPF e telefone. CPF e e-mail também são verificados quanto à duplicidade, conforme as regras implementadas no serviço.

### Vínculos com pacientes

Os dados pessoais do responsável são armazenados separadamente dos dados do paciente. A associação entre eles é registrada na coleção `vinculosResponsaveis`.

Cada vínculo contém:

| Campo            | Descrição                                |
| ---------------- | ---------------------------------------- |
| `id`             | Identificador do vínculo                 |
| `pacienteId`     | Identificador do paciente                |
| `responsavelId`  | Identificador do responsável             |
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

Essas regras precisam ser validadas funcionalmente por meio de requisições reais.

### Permissões

As operações de responsáveis estão configuradas para os perfis `ADMINISTRADOR` e `SECRETARIA`. A autorização efetiva deve ser confirmada com requisições autenticadas e com os diferentes perfis.

**Importante:** o vínculo com um responsável não concede automaticamente acesso a prontuários ou a outras informações clínicas do paciente. Essa autorização deve ser tratada separadamente.

---

## 🧑‍⚕️ Profissionais

O módulo permite gerenciar profissionais e suas disponibilidades.

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

O módulo permite cadastrar, consultar, atualizar e excluir disponibilidades.

### Endpoints

```text
POST   /api/disponibilidades
GET    /api/disponibilidades
GET    /api/disponibilidades/{id}
PUT    /api/disponibilidades/{id}
DELETE /api/disponibilidades/{id}
```

### Validações implementadas

O serviço valida os campos obrigatórios de horário e verifica se o horário final é posterior ao horário inicial.

O dia da semana é normalizado para padronizar a comparação dos valores. A normalização remove espaços nas extremidades, converte o texto para letras maiúsculas e substitui espaços e hífens por sublinhados.

### Permissões

As permissões das rotas globais de disponibilidades ainda precisam ser revisadas para garantir que cada perfil possa executar somente as operações autorizadas. As rotas vinculadas ao profissional também precisam respeitar as regras de acesso correspondentes.

---

## 📅 Consultas

O módulo de consultas permite cadastrar, consultar, atualizar e excluir atendimentos. A implementação local contempla status, regras de agendamento e cancelamento.

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

| Status       | Significado                                                                           |
| ------------ | ------------------------------------------------------------------------------------- |
| `CONFIRMADA` | Consulta cadastrada e confirmada. É o status inicial de uma nova consulta.            |
| `REMARCADA`  | Consulta cuja data ou horário foi alterado.                                           |
| `CANCELADA`  | Consulta cancelada, mantida no histórico quando utilizado o endpoint de cancelamento. |

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

A comparação dos dias da semana das disponibilidades foi normalizada no serviço de consultas, para reduzir inconsistências causadas por diferenças de espaços, maiúsculas, minúsculas e separadores.

**Atenção:** as regras foram implementadas localmente e precisam ser validadas funcionalmente, inclusive após a publicação no Render.

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

Os DTOs possuem validações para campos obrigatórios. O acesso aos prontuários deve respeitar as permissões de cada perfil e os vínculos de atendimento entre profissionais e pacientes. A validação completa dessas restrições permanece necessária.

---

## 🧠 Diagnósticos

### Endpoints

```text
POST   /api/diagnosticos
GET    /api/diagnosticos
GET    /api/diagnosticos/{id}
PUT    /api/diagnosticos/{id}
DELETE /api/diagnosticos/{id}

GET    /api/diagnosticos/paciente/{pacienteId}
```

### Validação dos dados

O cadastro e a atualização de diagnósticos utilizam Jakarta Bean Validation para validar os campos obrigatórios:

- ID do paciente.
- ID do profissional.
- Descrição do diagnóstico.

As validações são aplicadas pelo controller com `@Valid`. O serviço continua responsável por verificar as regras de negócio, incluindo os vínculos necessários entre paciente e profissional.

---

## 💊 Tratamentos

O módulo permite gerenciar tratamentos relacionados aos pacientes e profissionais.

### Endpoints

```text
POST   /api/tratamentos
GET    /api/tratamentos
GET    /api/tratamentos/{id}
PUT    /api/tratamentos/{id}
DELETE /api/tratamentos/{id}

GET    /api/tratamentos/paciente/{pacienteId}
```

### Validação dos dados

O DTO de requisição possui validações para os campos obrigatórios:

- ID do paciente.
- ID do profissional.
- Descrição do tratamento.

Os endpoints de cadastro e atualização aplicam `@Valid` para executar as validações de entrada.

O cadastro retorna `201 Created` quando a operação é concluída com sucesso.

O serviço mantém as verificações de existência do paciente e do profissional e as regras de associação com a consulta correspondente, conforme a implementação do módulo.

As regras de negócio e as permissões ainda precisam ser validadas funcionalmente.

---

## 🧪 Exames

O módulo permite gerenciar exames associados a pacientes e profissionais.

### Endpoints

```text
POST   /api/exames
GET    /api/exames
GET    /api/exames/{id}
PUT    /api/exames/{id}
DELETE /api/exames/{id}

GET    /api/exames/paciente/{pacienteId}
```

### Validação dos dados

O DTO de requisição possui validações para os campos obrigatórios:

- ID do paciente.
- ID do profissional.
- Nome do exame.

Os endpoints de cadastro e atualização aplicam `@Valid` para executar as validações de entrada.

O cadastro retorna `201 Created` quando a operação é concluída com sucesso.

Os demais campos do exame, como tipo, data de realização, resultado e observações, são tratados conforme a estrutura do DTO.

As regras de negócio, as permissões e a persistência devem ser verificadas funcionalmente.

---

## 👨‍💼 Administradores

O módulo permite gerenciar administradores.

### Endpoints

```text
POST   /api/administradores
GET    /api/administradores
GET    /api/administradores/{id}
PUT    /api/administradores/{id}
DELETE /api/administradores/{id}
```

### Credenciais e modelo centralizado de usuários

A autenticação utiliza o modelo centralizado `Usuario` para armazenar as credenciais.

As senhas são armazenadas utilizando BCrypt e não devem ser retornadas pelos DTOs de resposta de administradores.

O serviço de administradores foi ajustado para centralizar as credenciais no modelo `Usuario`, sincronizando os dados necessários entre o administrador e a conta de acesso relacionada.

O perfil `ADMINISTRADOR` é utilizado nas regras de autorização da API.

A consistência entre o administrador e sua conta de usuário precisa ser preservada durante as operações de cadastro e atualização.

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

A resposta do login contém os dados necessários para identificar o usuário autenticado, incluindo nome, e-mail, perfil e, quando aplicável, categoria profissional.

O exemplo é ilustrativo. Não utilize credenciais reais em documentação pública.

### Envio do token

Nas requisições protegidas, o token deve ser enviado no cabeçalho HTTP:

```text
Authorization: Bearer <token>
```

O filtro JWT verifica o token e consulta o usuário correspondente. Contas inexistentes ou inativas não devem autenticar as requisições.

### Integração da autenticação com o frontend

O frontend Angular utiliza o serviço de autenticação para realizar o login por meio de `POST /api/auth/login`.

Após a resposta bem-sucedida:

- O token JWT é armazenado em `sessionStorage`, na chave `neurovita_token`.
- Os dados mínimos do usuário são armazenados em `sessionStorage`, na chave `neurovita_usuario`.
- O perfil recebido do backend é mapeado para o perfil utilizado na navegação do Angular.
- O nome do usuário autenticado é exibido no cabeçalho.
- O botão de saída encerra a sessão e retorna à tela de login.

O interceptor HTTP adiciona o cabeçalho `Authorization: Bearer` às requisições destinadas às rotas da API que necessitam de autenticação.

O guard Angular protege a navegação das áreas dos perfis e verifica se o usuário possui o perfil esperado para acessar determinada rota.

A proteção de navegação no frontend não substitui a autorização no backend. O Spring Security continua responsável por permitir ou negar o acesso aos endpoints e aos dados.

### Perfis e mapeamento no frontend

O backend utiliza os perfis:

- `ADMINISTRADOR`
- `SECRETARIA`
- `PROFISSIONAL`
- `PACIENTE`

No frontend, o perfil `PROFISSIONAL` é diferenciado pela categoria profissional retornada no login:

- `psicologo`
- `psiquiatra`

O frontend utiliza os perfis `admin`, `secretaria`, `psicologo` e `psiquiatra` para organizar a navegação.

O perfil `PACIENTE` existe no backend, mas não foi identificada uma área correspondente no frontend integrado. Por isso, não deve receber acesso indevido às áreas dos demais perfis.

A validação do login com contas dos demais perfis depende da disponibilidade de contas válidas para os testes.

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

A implementação contempla:

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

A configuração do Spring Security contém regras específicas para as rotas de administradores, pacientes, profissionais, consultas, agenda, disponibilidades, prontuários, diagnósticos, tratamentos, exames e responsáveis.

As permissões de todos os endpoints ainda precisam ser revisadas e validadas. A existência de um perfil no modelo centralizado não significa, por si só, que todas as suas permissões já estejam implementadas.

O acesso a prontuários, diagnósticos, exames e demais informações clínicas deverá respeitar as permissões de cada perfil e o vínculo entre profissionais e pacientes. Essas restrições devem ser verificadas no backend, e não apenas na interface do usuário.

O vínculo entre um paciente e um responsável não concede automaticamente acesso aos dados clínicos.

A revisão das permissões específicas das rotas globais de disponibilidades permanece entre as próximas etapas.

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

A configuração OpenAPI está disponível. O login com JWT já foi verificado em produção na versão anterior do backend e localmente na aplicação integrada. A autorização de todos os perfis e endpoints ainda precisa ser validada.

---

## 🐳 Docker

O projeto utiliza um Dockerfile multi-stage para compilar e executar o frontend Angular e o backend Java na mesma aplicação.

O processo possui três etapas:

1. **Frontend:** utiliza Node.js para instalar as dependências e compilar o Angular.
2. **Backend:** utiliza Java 21 e Maven para compilar o Spring Boot e incorporar os arquivos gerados pelo Angular em `src/main/resources/static/`.
3. **Execução:** utiliza uma imagem Java 21 JRE para executar a aplicação integrada.

A aplicação utiliza a variável de ambiente `PORT`, com `8080` como valor padrão.

As configurações sensíveis são fornecidas por variáveis de ambiente, incluindo:

- `MONGODB_URI`
- `JWT_SECRET`
- `PORT`

O arquivo `.env` é utilizado somente no ambiente local e não deve ser versionado.

O arquivo `.dockerignore` exclui dependências, arquivos gerados, configurações locais e informações sensíveis do contexto de build.

### Construção da imagem

```bash
docker build -t neurovita-integrado .
```

### Execução local

```bash
docker run --rm --name neurovita-local --env-file .env -p 8080:8080 neurovita-integrado
```

A aplicação estará disponível em:

[http://localhost:8080](http://localhost:8080)

---

## ☁️ Deploy e ambientes

O projeto está sendo organizado em quatro ambientes de desenvolvimento e entrega, conforme a organização definida para o projeto acadêmico.

### Estrutura dos repositórios

| Repositório                           | Finalidade      |
| ------------------------------------- | --------------- |
| `victorhborges/NeuroVita`             | Desenvolvimento |
| `victorhborges/NeuroVita-Testes`      | Testes          |
| `victorhborges/NeuroVita-Homologacao` | Homologação     |
| `victorhborges/NeuroVita-Producao`    | Produção        |

Os três últimos repositórios serão criados posteriormente.

### Fluxo de promoção

```text
DESENVOLVIMENTO
       |
       v
     TESTES
       |
       v
  HOMOLOGAÇÃO
       |
       v
    PRODUÇÃO
```

Cada etapa deverá receber uma versão revisada e identificável.

A promoção entre ambientes deverá preservar o histórico de alterações e ocorrer após a aprovação da etapa anterior.

### Ambiente de desenvolvimento

O repositório de desenvolvimento será utilizado para implementar novas funcionalidades, corrigir problemas e integrar o frontend e o backend.

### Ambiente de testes

Receberá versões disponibilizadas para a equipe responsável pela validação funcional.

### Ambiente de homologação

Receberá versões aprovadas nos testes para validação final antes da publicação.

### Ambiente de produção

Receberá somente versões aprovadas para disponibilização aos usuários.

### Produção atual

A versão anterior do backend está hospedada no Render, utilizando Docker.

- **URL atual:** [https://neurovita.onrender.com](https://neurovita.onrender.com)
- **Swagger:** [https://neurovita.onrender.com/swagger-ui/index.html](https://neurovita.onrender.com/swagger-ui/index.html)

O login autenticado e as consultas de administradores e pacientes foram verificados na versão anterior em produção. As operações retornaram respostas HTTP `200` nos cenários verificados.

A nova arquitetura integrada ainda precisa ser publicada e validada no ambiente de produção.

### Integração com o frontend

O frontend Angular é servido pela própria aplicação Spring Boot por meio dos arquivos estáticos compilados e incorporados em `src/main/resources/static/`.

Essa arquitetura permite que a interface e a API utilizem a mesma origem, simplificando a comunicação entre frontend e backend.

O Spring Security possui configuração específica para permitir o acesso aos arquivos estáticos e às rotas do frontend, mantendo as regras de segurança aplicáveis às rotas `/api/**`.

A autenticação do frontend utiliza o endpoint `POST /api/auth/login`. Após o login bem-sucedido, o token JWT e os dados mínimos do usuário são armazenados em `sessionStorage`.

O frontend utiliza um interceptor HTTP para enviar o token JWT no cabeçalho Bearer das requisições destinadas à API. Um guard controla a navegação conforme o perfil autenticado.

Foram verificados localmente:

- Login real integrado à API.
- Recebimento e armazenamento do token JWT.
- Exibição do nome do usuário autenticado no cabeçalho.
- Encerramento da sessão pelo botão de logout.
- Execução da interface Angular pela aplicação Spring Boot.

A autorização efetiva dos recursos permanece sob responsabilidade do Spring Security no backend. A validação completa dos perfis, permissões e módulos depende de testes funcionais adicionais.

A configuração de CORS permanece disponível para origens externas autorizadas. A permissão de CORS não substitui a autenticação JWT nem as regras de autorização dos endpoints.

---

## 🗄️ Banco de dados

O projeto utiliza **MongoDB** com Spring Data MongoDB e MongoDB Atlas.

### Coleções

A aplicação utiliza coleções MongoDB para persistir os documentos dos módulos, incluindo:

- `administradores`
- `consultas`
- `diagnosticos`
- `disponibilidades`
- `exames`
- `pacientes`
- `profissionais`
- `prontuarios`
- `responsaveis`
- `secretarias`
- `tratamentos`
- `usuarios`
- `vinculosResponsaveis`

A estrutura efetivamente utilizada depende dos modelos e repositórios implementados e da configuração do banco.

### Responsáveis e vínculos

| Coleção                | Finalidade                                                             |
| ---------------------- | ---------------------------------------------------------------------- |
| `responsaveis`         | Dados pessoais e de contato dos responsáveis                           |
| `vinculosResponsaveis` | Relação entre pacientes e responsáveis, incluindo o grau de parentesco |
| `pacientes`            | Dados cadastrais dos pacientes                                         |

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

A URI precisa incluir o nome do banco de dados, além dos parâmetros de conexão necessários.

Em produção, a URI de conexão é fornecida pela variável `MONGODB_URI`, configurada no ambiente do Render.

A aplicação também utiliza a variável `JWT_SECRET`, que fornece a chave usada para assinar e validar os tokens JWT.

### Segurança das configurações

- Não armazenar senhas ou tokens no código-fonte.
- Não versionar URIs do MongoDB que contenham credenciais.
- Não publicar os valores reais de `MONGODB_URI` ou `JWT_SECRET`.
- Configurar as variáveis no ambiente de execução.
- Manter o arquivo `.env` fora do Git.
- Não incluir arquivos de configuração sensíveis nas imagens ou publicações do projeto.

### Status da conexão

A conexão com o MongoDB Atlas foi estabelecida em produção na versão anterior do backend. Os logs do Render indicaram a conexão com o cluster, e consultas autenticadas de administradores e pacientes retornaram dados reais pela API.

Na nova estrutura integrada, a conexão também foi verificada localmente. O login JWT e a consulta autenticada de profissionais retornaram resultados esperados nos cenários verificados.

Esses resultados confirmam a conectividade e a leitura de dados nos cenários verificados. **Ainda não comprovam que todas as operações de cadastro, atualização e exclusão de todos os módulos estejam funcionando corretamente.**

A compatibilidade da funcionalidade de responsáveis e vínculos com a estrutura definitiva do banco deve ser confirmada em conjunto com a equipe responsável pelo banco de dados.

---

## 🚀 Executando localmente

### Pré-requisitos

- Java JDK 21, compatível com a versão configurada no projeto.
- Node.js e npm, para trabalhar diretamente com o frontend.
- Git.
- Docker Desktop, para executar a aplicação integrada em container.
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

### Verificar Node.js e npm

```bash
node -v
npm -v
```

### Clonar o repositório

```bash
git clone https://github.com/victorhborges/NeuroVita.git

cd NeuroVita
```

### Configurar as variáveis de ambiente

Configure `MONGODB_URI` com uma URI válida do MongoDB e `JWT_SECRET` com uma chave secreta forte.

A URI deve conter o nome do banco de dados. A chave JWT deve atender ao tamanho mínimo exigido pela implementação de autenticação.

Não inclua os valores reais dessas variáveis em arquivos versionados ou em exemplos públicos.

### Executar somente o backend

**Windows**

```cmd
mvnw.cmd spring-boot:run
```

**Linux/macOS**

```bash
./mvnw spring-boot:run
```

### Compilar o frontend

Acesse o diretório do frontend:

```bash
cd frontend
```

Instale as dependências:

```bash
npm ci
```

Compile a aplicação:

```bash
npm run build
```

O resultado da compilação é gerado em:

```text
frontend/dist/neurovita/browser/
```

### Incorporar o frontend ao Spring Boot

Após compilar o Angular, os arquivos gerados precisam ser copiados para a pasta de recursos estáticos do backend.

No Windows, execute o comando a partir da raiz do projeto:

```cmd
xcopy /E /I /Y frontend\dist\neurovita\browser src\main\resources\static
```

Essa operação atualiza os arquivos que serão servidos pelo Spring Boot.

Sempre que houver alterações no frontend, execute novamente a compilação e a cópia antes de iniciar uma nova execução integrada.

A pasta `src/main/resources/static/` contém os arquivos compilados utilizados pela aplicação integrada e deve acompanhar as alterações do frontend.

Os diretórios `frontend/node_modules` e `frontend/dist` são gerados durante o desenvolvimento e permanecem ignorados pelo Git.

### Executar a aplicação integrada localmente

Na raiz do projeto, configure as variáveis de ambiente necessárias na sessão do terminal.

No Windows, por exemplo:

```cmd
set "MONGODB_URI=SUA_URI_LOCAL_DO_MONGODB"
set "JWT_SECRET=SUA_CHAVE_JWT_LOCAL"
```

Não compartilhe esses valores nem os inclua em arquivos versionados.

Depois, execute:

```cmd
mvnw.cmd spring-boot:run
```

A aplicação integrada estará disponível em:

[http://localhost:8080](http://localhost:8080)

Mantenha o terminal aberto enquanto estiver utilizando a aplicação. Para encerrar, pressione `Ctrl + C`.

### Executar a aplicação integrada com Docker

Na raiz do projeto:

```bash
docker build -t neurovita-integrado .
```

Depois:

```bash
docker run --rm --name neurovita-local --env-file .env -p 8080:8080 neurovita-integrado
```

A aplicação estará disponível em:

[http://localhost:8080](http://localhost:8080)

### Endpoints locais

- **Frontend:** [http://localhost:8080](http://localhost:8080)
- **Swagger:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Documentação OpenAPI:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🧪 Verificação e testes

A compilação e os testes Maven podem ser executados localmente.

### Compilação

Para verificar se as classes do projeto compilam:

**Windows**

```cmd
mvnw.cmd compile
```

**Linux/macOS**

```bash
./mvnw compile
```

### Testes Maven

**Windows**

```cmd
mvnw.cmd test
```

**Linux/macOS**

```bash
./mvnw test
```

A execução local mais recente informada terminou com `BUILD SUCCESS` nas verificações realizadas antes da integração.

A integração também foi compilada e executada localmente, com conexão ao MongoDB Atlas.

O login JWT pelo frontend integrado, a exibição do nome do usuário e o logout foram verificados localmente.

O resultado da compilação confirma que as classes foram compiladas sem falhas reportadas. O resultado dos testes Maven se limita aos testes executados nessa execução.

Nenhum desses resultados, isoladamente, comprova a validação funcional de todos os endpoints, perfis, regras de negócio e operações com o MongoDB Atlas.

Os testes funcionais completos são responsabilidade da equipe designada para essa atividade. As verificações de compilação e as correções do backend são realizadas durante o desenvolvimento.

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

A integração atual incorpora o frontend Angular à aplicação Java, mantendo as responsabilidades separadas entre interface, API e persistência.

---

## 📊 Status do projeto

| Módulo / recurso                             | Status conhecido                                                                                                                        |
| -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| Estrutura Java                               | 🟢 Implementada                                                                                                                         |
| Spring Boot                                  | 🟢 Implementado                                                                                                                         |
| Spring Web MVC                               | 🟢 Implementado                                                                                                                         |
| Spring Data MongoDB                          | 🟢 Configurado                                                                                                                          |
| Pacientes                                    | 🟡 CRUD implementado; vínculos e regras precisam de validação funcional                                                                 |
| Responsáveis                                 | 🟡 CRUD implementado localmente; aguardando validação funcional                                                                         |
| Vínculos entre pacientes e responsáveis      | 🟡 Implementados localmente; aguardando validação funcional e compatibilidade com o banco                                               |
| Profissionais                                | 🟢 CRUD implementado                                                                                                                    |
| Validação de pacientes e profissionais       | 🟢 Implementada                                                                                                                         |
| Disponibilidades                             | 🟡 Validações de horários e normalização do dia implementadas; permissões globais em revisão                                            |
| Consultas                                    | 🟡 Regras de agendamento, normalização de disponibilidade, status e cancelamento implementados localmente; validação funcional pendente |
| Agenda                                       | 🟢 Implementada; comportamento dos status a validar                                                                                     |
| Prontuários                                  | 🟡 Implementados; validação das permissões e regras de acesso pendente                                                                  |
| Diagnósticos                                 | 🟢 Validação de campos obrigatórios com `@NotBlank` e aplicação de `@Valid` no cadastro e na atualização                                |
| Tratamentos                                  | 🟢 Validação de campos obrigatórios com `@NotBlank`, aplicação de `@Valid` e resposta `201 Created` no cadastro                         |
| Exames                                       | 🟢 Validação de campos obrigatórios com `@NotBlank`, aplicação de `@Valid` e resposta `201 Created` no cadastro                         |
| Administradores                              | 🟢 Credenciais centralizadas no modelo `Usuario`; senhas armazenadas com BCrypt                                                         |
| Modelo centralizado `Usuario`                | 🟢 Implementado; validação funcional completa pendente                                                                                  |
| Login centralizado                           | 🟢 Login com JWT verificado em produção na versão anterior e localmente na aplicação integrada                                          |
| JWT                                          | 🟢 Utilizado na autenticação; interceptor implementado no frontend; revisão das permissões pendente                                     |
| BCrypt                                       | 🟢 Utilizado no fluxo de autenticação                                                                                                   |
| Troca de senha no primeiro acesso            | 🟡 Implementada; validação funcional pendente                                                                                           |
| Spring Security                              | 🟡 Configurado; revisão das permissões por endpoint em andamento                                                                        |
| Autorização por perfil para todos os módulos | 🟡 Em revisão e validação                                                                                                               |
| Proteção por vínculo profissional-paciente   | 🟡 Regras existentes precisam de validação funcional completa                                                                           |
| Swagger/OpenAPI                              | 🟢 Implementado                                                                                                                         |
| Tratamento global de exceções                | 🟢 Implementado, com melhorias pendentes                                                                                                |
| Frontend Angular                             | 🟢 Compilação local realizada                                                                                                           |
| Integração Angular + Spring Boot             | 🟢 Aplicação executada localmente                                                                                                       |
| Navegação Angular e rotas SPA                | 🟢 Verificadas localmente                                                                                                               |
| Docker multi-stage                           | 🟢 Configurado e compilado                                                                                                              |
| Login JWT na aplicação integrada             | 🟢 Verificado localmente                                                                                                                |
| Interceptor JWT e guard Angular              | 🟢 Implementados localmente                                                                                                             |
| Nome do usuário e logout                     | 🟢 Verificados localmente                                                                                                               |
| MongoDB Atlas na aplicação integrada         | 🟢 Conexão e consulta verificadas localmente                                                                                            |
| Deploy Render da nova arquitetura            | ⚪ Pendente                                                                                                                              |
| MongoDB Atlas em produção                    | 🟢 Conexão e leitura verificadas na versão anterior                                                                                     |
| Receitas                                     | ⚪ Aguardando requisitos                                                                                                                 |
| Testes funcionais completos                  | 🟡 Sob responsabilidade da equipe designada                                                                                             |

Os status indicam o estado de desenvolvimento conhecido. Não substituem a validação funcional dos módulos, a revisão de segurança ou a confirmação de todas as operações no ambiente de produção.

---

## 🗺️ Próximas etapas

- [x] Implementar validações iniciais nos DTOs de pacientes e profissionais.
- [x] Aplicar `@Valid` nos endpoints de cadastro e atualização desses módulos.
- [x] Implementar localmente o CRUD de responsáveis.
- [x] Implementar localmente os vínculos entre pacientes e responsáveis.
- [x] Implementar localmente status, remarcação e cancelamento de consultas.
- [x] Normalizar os dias da semana na comparação de disponibilidades durante o agendamento.
- [x] Aplicar validações com `@NotBlank` e `@Valid` aos diagnósticos.
- [x] Aplicar validações com `@NotBlank` e `@Valid` aos tratamentos.
- [x] Aplicar validações com `@NotBlank` e `@Valid` aos exames.
- [x] Configurar o cadastro de tratamentos e exames para retornar `201 Created`.
- [x] Centralizar as credenciais de administradores no modelo `Usuario`.
- [x] Verificar o login com JWT em produção na versão anterior.
- [x] Verificar consultas autenticadas de administradores e pacientes em produção na versão anterior.
- [x] Confirmar a conexão com o MongoDB Atlas e a leitura de dados nos cenários verificados.
- [x] Compilar o frontend Angular localmente.
- [x] Configurar o Docker multi-stage para frontend e backend.
- [x] Incorporar os arquivos compilados do Angular aos recursos estáticos do Spring Boot.
- [x] Configurar o tratamento de rotas SPA.
- [x] Executar a aplicação integrada localmente.
- [x] Verificar o login JWT na aplicação integrada.
- [x] Verificar a conexão com MongoDB Atlas na aplicação integrada.
- [x] Implementar o armazenamento do token JWT no frontend.
- [x] Implementar o envio do token nas requisições destinadas à API.
- [x] Implementar a proteção de navegação por perfil no Angular.
- [x] Verificar a exibição do nome do usuário autenticado no cabeçalho.
- [x] Verificar o encerramento da sessão pelo botão de logout.
- [ ] Revisar o diff das alterações locais e preparar o primeiro commit da integração.
- [ ] Atualizar e revisar a documentação antes da publicação.
- [ ] Publicar a branch de integração no repositório de desenvolvimento.
- [ ] Validar o login e o redirecionamento com contas dos diferentes perfis.
- [ ] Confirmar a autorização efetiva dos endpoints para cada perfil.
- [ ] Revisar as permissões das rotas globais de disponibilidades.
- [ ] Validar o CRUD de disponibilidades e suas regras de horário.
- [ ] Validar o CRUD de tratamentos e exames.
- [ ] Validar o CRUD de responsáveis pelo Swagger.
- [ ] Validar o cadastro de pacientes com um ou mais responsáveis.
- [ ] Validar a atualização e a remoção de vínculos.
- [ ] Validar as regras de menores de idade sem responsável.
- [ ] Compatibilizar a funcionalidade de responsáveis com a estrutura final do MongoDB Atlas.
- [ ] Validar a criação e a associação das contas de usuários às entidades de domínio.
- [ ] Validar operações de leitura, cadastro, atualização e exclusão no banco para os módulos aplicáveis.
- [ ] Validar as regras de status, remarcação, cancelamento e conflitos de horário.
- [ ] Validar o login e a troca de senha no primeiro acesso.
- [ ] Revisar as permissões dos endpoints para cada perfil.
- [ ] Padronizar e validar as respostas de erro.
- [ ] Validar as restrições de acesso aos dados clínicos.
- [ ] Definir o módulo de Receitas após a especificação no Figma.
- [ ] Disponibilizar a aplicação para a validação funcional da equipe responsável.
- [ ] Criar e configurar o repositório de testes.
- [ ] Criar e configurar o repositório de homologação.
- [ ] Criar e configurar o repositório de produção.
- [ ] Definir o processo de promoção de versões entre os quatro ambientes.
- [ ] Configurar os serviços Render correspondentes aos ambientes.
- [ ] Validar a nova arquitetura no ambiente de testes.
- [ ] Promover uma versão aprovada para homologação.
- [ ] Publicar em produção somente após aprovação da homologação.

---

## 🌿 Git e GitHub

O projeto utiliza Git e GitHub para controle de versão, revisão de código e organização das entregas.

### Organização dos repositórios

O projeto será organizado em quatro repositórios independentes, conforme a exigência acadêmica:

| Repositório                           | Responsabilidade |
| ------------------------------------- | ---------------- |
| `victorhborges/NeuroVita`             | Desenvolvimento  |
| `victorhborges/NeuroVita-Testes`      | Testes           |
| `victorhborges/NeuroVita-Homologacao` | Homologação      |
| `victorhborges/NeuroVita-Producao`    | Produção         |

Os repositórios de testes, homologação e produção serão criados durante a organização dos ambientes.

### Estratégia de branches

No repositório de desenvolvimento, será adotada uma organização baseada em branches:

| Branch      | Finalidade                                    |
| ----------- | --------------------------------------------- |
| `main`      | Versão integrada e estável do desenvolvimento |
| `develop`   | Integração contínua das funcionalidades       |
| `feature/*` | Desenvolvimento de funcionalidades            |
| `fix/*`     | Correções de problemas                        |
| `release/*` | Preparação de versões para entrega            |

Nos repositórios de testes, homologação e produção, a branch `main` deverá representar a versão promovida e aprovada para aquele ambiente.

As regras definitivas de proteção e promoção serão configuradas durante a criação dos repositórios.

### Fluxo de promoção

```text
Desenvolvimento
      |
      v
Testes
      |
      v
Homologação
      |
      v
Produção
```

A promoção deverá preservar o histórico Git e identificar as versões entregues.

Uma versão somente deverá ser promovida após a aprovação da etapa anterior.

### Fluxo de revisão

Antes de publicar as alterações:

```bash
git status

git diff

git diff -- README.md
```

Antes de realizar um commit:

1. Conferir os arquivos modificados.
2. Revisar as diferenças.
3. Confirmar que não existem credenciais ou arquivos temporários.
4. Atualizar o README quando houver alterações relevantes.
5. Adicionar somente os arquivos relacionados à alteração.
6. Criar o commit com uma mensagem descritiva.
7. Enviar para a branch correspondente.

### Padrão de commits

```text
feat: nova funcionalidade

fix: correção de problema

docs: alteração na documentação

test: alteração nos testes

refactor: melhoria estrutural

chore: configuração ou manutenção
```

Antes de executar `git add`, revise as alterações para evitar incluir arquivos temporários, credenciais ou mudanças não relacionadas.

O arquivo `.env` não deve ser versionado.

Atualize o README quando houver alterações relevantes na arquitetura, nas funcionalidades, nos endpoints ou na configuração do projeto.

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
- Revisão de código.
- Separação dos ambientes de desenvolvimento, testes, homologação e produção.
- Preservação do histórico de alterações.

---

## 🎓 Projeto acadêmico

**Projeto:** NeuroVita
**Instituição:** Faculdade Brasília
**Curso:** Análise e Desenvolvimento de Sistemas
**Área:** Desenvolvimento de Software

O projeto possui finalidade acadêmica e prática, aplicando conhecimentos de programação, desenvolvimento backend, APIs REST, frontend, banco de dados, arquitetura de software, segurança, integração e boas práticas de desenvolvimento.

---

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos.

A utilização, distribuição ou modificação do código deverá respeitar as decisões definidas pela equipe responsável pelo projeto.