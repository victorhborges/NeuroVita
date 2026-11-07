# 🧠 NeuroVita - Plataforma Integrada

Plataforma **NeuroVita**, desenvolvida como projeto acadêmico da **Faculdade Brasília**, no curso de **Análise e Desenvolvimento de Sistemas (ADS)**.

O projeto possui arquitetura integrada, composta por frontend Angular, backend Java com Spring Boot e banco de dados MongoDB Atlas.

O backend disponibiliza uma API REST para gerenciamento de pacientes, responsáveis, profissionais, consultas, agenda e informações relacionadas ao atendimento psicológico. O frontend Angular é compilado e servido pela própria aplicação Spring Boot.

> 🚧 **Status do projeto:** a integração entre frontend Angular e backend Spring Boot está implementada localmente. A última validação Maven executou 36 testes, todos aprovados, sem falhas ou erros. A geração do JAR e a compilação de desenvolvimento do Angular já foram realizadas em etapas anteriores. A validação da implementação mais recente com Docker e MongoDB Atlas real, assim como a publicação da nova versão integrada, permanece pendente.

---

## 🛠️ Tecnologias

| Tecnologia | Versão / uso |
|---|---|
| ☕ Java | 21 |
| 🌱 Spring Boot | 4.0.8 |
| 🅰️ Angular | 21 |
| 🟢 Node.js / npm | Dependências e compilação do frontend |
| 🗄️ MongoDB | Banco de dados NoSQL |
| 🔗 Spring Data MongoDB | Persistência de dados |
| 🔐 Spring Security | Autenticação e autorização |
| 🎟️ JWT | Autenticação por token |
| 🔑 BCrypt | Hash e verificação de senhas |
| ✅ Jakarta Bean Validation | Validação de dados de entrada |
| 📖 SpringDoc OpenAPI | Documentação da API |
| 🐳 Docker | Containerização e build multi-stage |
| ☁️ Render | Hospedagem |
| 📦 Maven | Gerenciamento e construção do projeto |
| 🧪 Maven Wrapper | Execução do Maven |

---

## 📌 Funcionalidades

O projeto contempla os seguintes módulos e recursos:

- 👤 Pacientes.
- 👥 Responsáveis.
- 🔗 Vínculos entre pacientes e responsáveis.
- 🧑‍⚕️ Profissionais.
- 🔗 Vínculos entre profissionais e pacientes, com histórico de atendimento.
- ⏰ Disponibilidades.
- 📅 Consultas.
- 🗓️ Agenda.
- 📋 Prontuários.
- 🧠 Diagnósticos.
- 💊 Tratamentos.
- 🧪 Exames.
- 📄 Documentos clínicos.
- 👨‍💼 Administradores.
- 🏢 Secretarias.
- 🔐 Autenticação e segurança.
- 👤 Modelo centralizado de usuários.
- ✅ Validação de dados de entrada.
- 🔄 Status, remarcação e cancelamento de consultas.
- 🖥️ Frontend Angular integrado à aplicação Spring Boot.
- 📄 Documentos clínicos e anexos persistidos pela API.
- ✉️ Recuperação de senha por código temporário enviado por e-mail.
- 🏥 Validação das informações de plano de saúde.
- 🏠 Atendimento presencial e online.
- 👥 Suporte a múltiplos responsáveis por paciente.
- 🚫 Inativação de cadastros sem exclusão do histórico.
- 🔒 Bloqueio de novos agendamentos para pacientes e profissionais inativos.
- 🔐 Controle de leitura compartilhada de informações clínicas conforme vínculo ativo.
- 📝 Preservação da autoria dos registros clínicos.

O módulo de **Receitas** aguarda a definição dos campos e das regras de negócio no Figma.

A existência de um módulo implementado não substitui a validação funcional de suas operações, permissões e regras de negócio.

---

## 🏗️ Arquitetura

O NeuroVita utiliza uma arquitetura integrada. O Spring Boot disponibiliza a API REST e também serve os arquivos estáticos compilados do Angular.

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

### Frontend

Desenvolvido em Angular, responsável pela interface utilizada pelos administradores, profissionais e secretarias.

O frontend é compilado e seus arquivos gerados são incorporados aos recursos estáticos do Spring Boot, em `src/main/resources/static/`.

A autenticação utiliza a API do backend. O token JWT é armazenado em `sessionStorage` e enviado nas requisições protegidas por meio de um interceptor HTTP.

### Backend

A aplicação Java utiliza Spring Boot e separa as responsabilidades entre:

- **Controller:** recebe requisições HTTP e retorna respostas da API.
- **Service:** implementa regras de negócio.
- **Repository:** realiza o acesso aos dados utilizando Spring Data MongoDB.
- **Model:** representa as entidades e documentos persistidos.
- **DTO:** separa os dados recebidos e enviados pela API dos modelos de persistência.
- **Security:** contém os componentes de autenticação e autorização, incluindo o filtro JWT.

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
│   │   │   ├── service/
│   │   │   └── NeurovitaApplication.java
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

A estrutura apresenta os principais componentes. Os arquivos específicos de cada módulo foram omitidos para facilitar a leitura.

O diretório `frontend/` contém o projeto Angular. O diretório `src/` contém a aplicação Java Spring Boot.

Os arquivos compilados do Angular são incorporados aos recursos estáticos do Spring Boot. A pasta `src/main/resources/static/` contém os arquivos utilizados pela aplicação integrada.

---

## 👤 Pacientes

O módulo de pacientes oferece operações de cadastro, consulta, atualização e inativação.

### Endpoints

```text
POST   /api/pacientes
GET    /api/pacientes
GET    /api/pacientes/{id}
PUT    /api/pacientes/{id}
DELETE /api/pacientes/{id}
```

### Validação dos dados

Entre as validações implementadas estão:

- Nome obrigatório.
- Data de nascimento obrigatória e anterior à data atual.
- Formato de e-mail.
- Formato de CPF.
- Formato de telefone.
- Validação dos dados de plano de saúde.

As validações de formato não substituem a verificação completa dos dígitos de CPF nem a confirmação da existência dos dados informados.

### Plano de saúde

O cadastro contempla a informação de existência de plano de saúde.

Quando `possuiPlanoSaude` é verdadeiro, os seguintes campos são obrigatórios:

- `planoSaude`
- `numeroCarteirinha`
- `validadePlano`

Quando o paciente não possui plano, esses dados não são exigidos.

### Responsáveis vinculados

O cadastro e a atualização de pacientes recebem uma lista de responsáveis.

Cada item contém o identificador do responsável e o grau de parentesco correspondente.

Exemplo ilustrativo:

```json
{
  "nome": "Paciente Exemplo",
  "dataNascimento": "2015-05-20",
  "possuiPlanoSaude": false,
  "planoSaude": null,
  "numeroCarteirinha": null,
  "validadePlano": null,
  "responsaveis": [
    {
      "responsavelId": "ID_DO_RESPONSAVEL",
      "grauParentesco": "Mãe"
    },
    {
      "responsavelId": "ID_DO_OUTRO_RESPONSAVEL",
      "grauParentesco": "Pai"
    }
  ]
}
```

O campo `responsaveis` é uma lista, permitindo representar mais de um responsável para o mesmo paciente.

### Regras implementadas

- Pacientes menores de idade precisam possuir pelo menos um responsável.
- Pacientes maiores de idade não precisam obrigatoriamente de responsável.
- Cada responsável pode estar associado a vários pacientes.
- Cada vínculo possui seu próprio grau de parentesco.
- A atualização reconcilia os vínculos recebidos com os existentes.
- A remoção de um responsável da ficha remove somente o vínculo daquele paciente.
- O cadastro pessoal do responsável não deve ser apagado quando um vínculo é removido.
- A edição de um responsável compartilhado mantém sua identidade, atualizando seus dados cadastrais.
- Responsáveis com vínculos ativos não podem ser excluídos.

O frontend filtra os contatos sem nome antes de enviar os dados para associação.

O serviço também tenta compensar a criação de responsáveis recém-criados quando ocorre falha posterior no salvamento do paciente. Essa compensação não representa uma transação MongoDB e precisa ser validada no banco real.

### Inativação

A operação de exclusão de pacientes foi adaptada para inativação.

O cadastro permanece armazenado, preservando seu histórico e seus documentos relacionados. A conta de acesso vinculada também é inativada.

Pacientes inativos não podem receber novos agendamentos.

---

## 👥 Responsáveis

O módulo permite cadastrar, consultar, atualizar e excluir responsáveis, respeitando os vínculos existentes.

### Endpoints

```text
POST   /api/responsaveis
GET    /api/responsaveis
GET    /api/responsaveis/{id}
PUT    /api/responsaveis/{id}
DELETE /api/responsaveis/{id}
```

### Dados do responsável

| Campo | Descrição |
|---|---|
| `id` | Identificador do responsável |
| `nome` | Nome do responsável |
| `cpf` | CPF, quando informado |
| `email` | E-mail, quando informado |
| `telefone` | Telefone, quando informado |

O nome é obrigatório. Os campos aplicáveis possuem validações de formato, e CPF e e-mail são verificados quanto à duplicidade conforme as regras do serviço.

### Vínculos com pacientes

Os dados pessoais do responsável são armazenados separadamente dos dados do paciente.

A associação entre eles é registrada na coleção `vinculosResponsaveis`.

Cada vínculo contém:

| Campo | Descrição |
|---|---|
| `id` | Identificador do vínculo |
| `pacienteId` | Identificador do paciente |
| `responsavelId` | Identificador do responsável |
| `grauParentesco` | Relação entre paciente e responsável |

Essa estrutura permite:

- Um paciente possuir vários responsáveis.
- Um responsável estar associado a vários pacientes.
- O grau de parentesco ser definido individualmente em cada vínculo.
- Evitar duplicação dos dados pessoais do responsável.

O vínculo com um responsável não concede automaticamente acesso a prontuários ou a outras informações clínicas do paciente.

---

## 🧑‍⚕️ Profissionais

O módulo permite gerenciar profissionais, suas disponibilidades e modalidades de atendimento.

### Endpoints

```text
POST   /api/profissionais
GET    /api/profissionais
GET    /api/profissionais/{id}
PUT    /api/profissionais/{id}
DELETE /api/profissionais/{id}
```

### Validações

Entre as validações implementadas estão:

- Nome obrigatório.
- Formato de e-mail.
- Formato de CPF.
- Formato de telefone.
- Categoria profissional obrigatória.
- Limites para o ano de conclusão.
- Pelo menos uma modalidade de atendimento habilitada.

### Modalidades de atendimento

O profissional pode oferecer:

- Atendimento presencial.
- Atendimento online.
- Ambas as modalidades.

O campo `atendePresencial` possui comportamento padrão verdadeiro quando não informado, preservando a compatibilidade com cadastros anteriores.

O backend valida as modalidades independentemente do frontend. Não é permitido cadastrar ou atualizar um profissional sem nenhuma modalidade de atendimento.

O agendamento também verifica se a modalidade solicitada está entre as oferecidas pelo profissional.

### Disponibilidades vinculadas

```text
POST   /api/profissionais/{profissionalId}/disponibilidades/{disponibilidadeId}
GET    /api/profissionais/{profissionalId}/disponibilidades
DELETE /api/profissionais/{profissionalId}/disponibilidades/{disponibilidadeId}
```

### Inativação

A inativação preserva os dados do profissional, suas consultas e seu histórico.

A conta de usuário associada também é inativada, impedindo a autenticação com aquela conta.

Profissionais inativos não podem receber novos agendamentos nem participar de alterações de consultas que violem essa regra.

---

## 🔗 Vínculos entre profissionais e pacientes

O NeuroVita utiliza vínculos explícitos para controlar o relacionamento de atendimento entre profissionais e pacientes.

Essa estrutura permite que um paciente seja acompanhado por mais de um profissional, mantendo o histórico dos períodos de atendimento.

### Estrutura dos vínculos

Os vínculos são armazenados separadamente na coleção:

`vinculos_profissional_paciente`

Cada registro representa um período de relacionamento entre um profissional e um paciente.

A estrutura contempla a identificação do profissional, a identificação do paciente e os dados necessários para registrar a abertura e o encerramento do vínculo.

O encerramento de um vínculo não apaga seu registro anterior, preservando o histórico de atendimento.

### Gestão dos vínculos

A abertura e o encerramento dos vínculos são restritos aos perfis autorizados de administrador e secretaria.

O profissional não pode conceder acesso a si próprio por meio da criação de um vínculo.

A gestão dos vínculos é realizada por meio de operações específicas do backend, com validação das permissões do usuário autenticado.

### Controle de acesso às informações clínicas

A leitura compartilhada de informações clínicas depende da existência de vínculo ativo entre o profissional autenticado e o paciente.

As regras implementadas contemplam:

- Profissionais com vínculo ativo podem consultar as informações clínicas compartilhadas do paciente.
- Profissionais sem vínculo ativo não possuem acesso de leitura compartilhada.
- O encerramento do vínculo retira o acesso compartilhado correspondente.
- O histórico dos vínculos encerrados permanece preservado.
- O acesso não deve ser concedido somente por conhecer o identificador do paciente ou do documento.

### Preservação da autoria

O compartilhamento de leitura não transfere a autoria dos registros clínicos.

As operações de edição e remoção permanecem restritas ao profissional autor do registro, respeitadas as demais permissões do sistema.

Dessa forma, profissionais vinculados podem consultar informações clínicas compartilhadas sem adquirir automaticamente permissão para modificar registros produzidos por outros profissionais.

A validação funcional completa dessas regras permanece necessária.

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

O dia da semana é normalizado para padronizar a comparação dos valores.

As permissões das rotas globais e vinculadas ao profissional precisam ser verificadas durante os testes funcionais.

---

## 📅 Consultas

O módulo de consultas permite cadastrar, consultar, atualizar, remarcar e cancelar atendimentos.

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
| `CONFIRMADA` | Consulta cadastrada e confirmada. |
| `REMARCADA` | Consulta cuja data ou horário foi alterado. |
| `CANCELADA` | Consulta cancelada, preservada no histórico quando utilizado o endpoint de cancelamento. |

O status é controlado pelo backend e não deve ser definido livremente no corpo da requisição.

### Regras de agendamento

A implementação contempla validações relacionadas a:

- Existência do paciente e do profissional.
- Data, horário e duração da consulta.
- Disponibilidade cadastrada para o profissional.
- Conflitos de horário do profissional.
- Conflitos de horário do paciente.
- Permissões de acesso.
- Modalidade oferecida pelo profissional.
- Estado ativo do paciente.
- Estado ativo do profissional.
- Desconsideração de consultas canceladas na verificação de conflitos.

Pacientes e profissionais inativos não podem participar de novos agendamentos ou de alterações de consultas que violem as regras de atividade.

Consultas canceladas não devem bloquear o horário para novos agendamentos.

O endpoint `PATCH /api/consultas/{id}/cancelar` altera o status para `CANCELADA`, preservando o registro.

O endpoint `DELETE /api/consultas/{id}` permanece disponível para exclusão definitiva, conforme as regras da implementação.

A alteração de data ou horário por meio do endpoint de atualização marca a consulta como `REMARCADA`.

Consultas canceladas não podem ser atualizadas pela operação normal de alteração.

As regras precisam ser validadas com requisições reais e com o banco de dados utilizado pelo projeto.

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

Os prontuários armazenam informações relacionadas ao acompanhamento clínico dos pacientes.

### Endpoints

```text
POST /api/prontuarios
GET  /api/prontuarios/{id}
PUT  /api/prontuarios/{id}
GET  /api/prontuarios/paciente/{pacienteId}
```

### Prontuário centralizado por paciente

O prontuário permanece associado ao paciente, permitindo uma visão centralizada do acompanhamento clínico.

A implementação estabelece a regra de um único prontuário central por paciente, impedindo a criação de um segundo prontuário para o mesmo paciente.

A verificação é realizada pelo backend antes da criação de um novo registro.

### Compatibilidade com os dados existentes

Durante a implementação, foi realizada uma consulta somente de leitura à coleção `prontuarios` no MongoDB Atlas.

O resultado identificou:

- Cinco documentos existentes.
- Nenhum prontuário sem `pacienteId`.
- Nenhuma duplicidade de `pacienteId`.
- Nenhum vínculo profissional-paciente preexistente na nova estrutura.

Também foram identificados os campos `criadoEm` e `atualizadoEm` nos documentos existentes.

Esses campos foram incorporados ao mapeamento do modelo e da resposta para preservar a compatibilidade com os registros já persistidos.

As atualizações preservam a data original de criação do prontuário.

Nenhum prontuário existente foi removido ou alterado pela auditoria.

### Controle de acesso

O acesso aos prontuários deve respeitar as permissões de cada perfil e os vínculos de atendimento entre profissionais e pacientes.

Profissionais com vínculo ativo podem realizar a leitura compartilhada conforme as regras implementadas.

O acesso de escrita e as alterações nos registros permanecem sujeitos às regras de autorização e autoria.

A validação completa dessas restrições permanece necessária.

---

## 📄 Documentos clínicos

O módulo de documentos clínicos permite consultar e gerenciar documentos relacionados ao acompanhamento dos pacientes.

A implementação considera o compartilhamento de leitura entre profissionais vinculados ao paciente.

### Leitura compartilhada

A consulta dos documentos clínicos considera o vínculo ativo entre o profissional autenticado e o paciente.

Dessa forma:

- Profissionais com vínculo ativo podem consultar os documentos clínicos compartilhados do paciente.
- Profissionais sem vínculo ativo não possuem acesso de leitura compartilhada.
- O encerramento do vínculo impede a leitura compartilhada correspondente.
- A autoria original do documento é preservada.

### Edição e remoção

A leitura compartilhada não concede automaticamente permissão de edição ou remoção.

Somente o profissional autor pode editar ou remover seu próprio registro, respeitadas as demais regras de autorização do backend.

Os documentos clínicos existentes e seus dados devem ser preservados durante as alterações da estrutura.

A validação funcional completa do compartilhamento e das permissões permanece pendente.

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

O cadastro e a atualização utilizam Jakarta Bean Validation para verificar os campos obrigatórios, incluindo paciente, profissional e descrição.

O serviço continua responsável pelas verificações de existência e pelas regras de associação entre paciente e profissional.

---

## 💊 Tratamentos

### Endpoints

```text
POST   /api/tratamentos
GET    /api/tratamentos
GET    /api/tratamentos/{id}
PUT    /api/tratamentos/{id}
DELETE /api/tratamentos/{id}

GET    /api/tratamentos/paciente/{pacienteId}
```

O DTO de requisição possui validações para os campos obrigatórios, incluindo paciente, profissional e descrição.

O cadastro retorna `201 Created` quando concluído com sucesso.

As regras de negócio e as permissões ainda precisam ser validadas funcionalmente.

---

## 🧪 Exames

### Endpoints

```text
POST   /api/exames
GET    /api/exames
GET    /api/exames/{id}
PUT    /api/exames/{id}
DELETE /api/exames/{id}

GET    /api/exames/paciente/{pacienteId}
```

O DTO de requisição possui validações para os campos obrigatórios, incluindo paciente, profissional e nome do exame.

O cadastro retorna `201 Created` quando concluído com sucesso.

Os demais campos, como tipo, data de realização, resultado e observações, são tratados conforme a estrutura do DTO.

As regras de negócio e as permissões precisam ser validadas funcionalmente.

---

## 👨‍💼 Administradores e secretarias

Os módulos permitem gerenciar os cadastros administrativos e suas contas de acesso.

As credenciais são centralizadas no modelo `Usuario`.

As senhas são armazenadas utilizando BCrypt e não devem ser retornadas pelos DTOs de resposta.

### Secretarias

O cadastro de secretárias participa do fluxo de criação de credenciais.

A inativação preserva o registro e sincroniza o estado da conta de usuário associada.

Secretárias criadas com estado inativo também devem receber contas inativas.

As permissões administrativas das secretarias devem ser respeitadas nas operações de gerenciamento de vínculos entre profissionais e pacientes.

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

A entidade `Usuario` é responsável pelas credenciais e informações necessárias à identificação dos usuários.

O modelo contempla:

- Nome.
- E-mail.
- Senha armazenada com BCrypt.
- Perfil de acesso.
- Indicador de senha provisória.
- Estado ativo da conta.
- Identificador da entidade relacionada, quando aplicável.

A autenticação deve rejeitar contas inexistentes ou inativas.

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

Após a validação das credenciais, a API retorna um token JWT e os dados previstos no DTO de resposta.

Não utilize credenciais reais em documentação pública.

### Envio do token

Nas requisições protegidas, o token deve ser enviado no cabeçalho HTTP:

```text
Authorization: Bearer <token>
```

### Integração da autenticação com o frontend

O frontend Angular utiliza o serviço de autenticação para realizar o login por meio de `POST /api/auth/login`.

Após a resposta bem-sucedida:

- O token JWT é armazenado em `sessionStorage`, na chave `neurovita_token`.
- Os dados mínimos do usuário são armazenados em `sessionStorage`, na chave `neurovita_usuario`.
- O perfil recebido do backend é mapeado para o perfil utilizado na navegação Angular.
- O nome do usuário autenticado é exibido no cabeçalho.
- O botão de saída encerra a sessão e retorna à tela de login.

O interceptor HTTP adiciona o cabeçalho `Authorization: Bearer` às requisições destinadas às rotas protegidas.

O guard Angular protege a navegação das áreas dos perfis.

A proteção de navegação no frontend não substitui a autorização no backend.

### Perfis

O backend utiliza os perfis:

- `ADMINISTRADOR`
- `SECRETARIA`
- `PROFISSIONAL`
- `PACIENTE`

No frontend, o perfil `PROFISSIONAL` é diferenciado pela categoria profissional:

- `psicologo`
- `psiquiatra`

O frontend utiliza os perfis `admin`, `secretaria`, `psicologo` e `psiquiatra` para organizar a navegação.

O perfil `PACIENTE` existe no backend, mas a existência desse perfil não significa que uma área correspondente esteja disponível no frontend integrado.

### Primeiro acesso e troca de senha

```text
POST /api/auth/primeiro-acesso/alterar-senha
```

Esse endpoint é destinado à troca obrigatória de uma senha provisória.

A implementação contempla:

- Token com validade reduzida para o primeiro acesso.
- Restrição do token ao endpoint de troca de senha.
- Validação de preenchimento e tamanho da nova senha.
- Confirmação de igualdade entre as senhas.
- Armazenamento com BCrypt.
- Atualização do indicador de senha provisória.

### Recuperação de senha

A recuperação utiliza código temporário enviado por e-mail.

O código de seis dígitos possui validade de 10 minutos. O token emitido após a verificação também possui validade de 10 minutos e é de uso único.

O envio real depende de configuração SMTP no ambiente.

Sem SMTP habilitado, a API não simula o envio.

### Tratamento de erros

O tratamento contempla exceções para recursos inexistentes, credenciais inválidas e erros de validação.

Comportamentos esperados:

- Recursos inexistentes: resposta `404`, conforme a rota.
- Credenciais inválidas: resposta `401`.
- Token inválido ou expirado: requisição não autenticada.
- Conta inativa: requisição não autenticada.
- Token de primeiro acesso em rota não permitida: acesso negado.

Os códigos e formatos das respostas devem ser conferidos durante os testes funcionais.

---

## 🔄 Sincronização de entidades e contas de acesso

Pacientes, profissionais e secretárias possuem dados de domínio e podem possuir contas relacionadas no modelo centralizado `Usuario`.

As operações de inativação e reativação procuram manter sincronizados os estados da entidade e da conta de acesso.

Foi implementada a classe `SincronizacaoAtivo.java` para centralizar o processo de sincronização e a tentativa de restauração dos estados anteriores em caso de falha de salvamento.

**Limitação importante:** esse mecanismo utiliza compensação, não uma transação MongoDB.

Se a operação principal falhar e também ocorrer falha na restauração, pode persistir uma inconsistência entre entidade e conta de usuário.

A consistência desse fluxo ainda precisa ser validada com MongoDB Atlas real, inclusive em cenários de falha.

---

## 📖 Swagger / OpenAPI

A API possui documentação interativa por meio do Swagger/OpenAPI.

### Localmente

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Documentação OpenAPI:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Produção anterior

- **Swagger UI:** [https://neurovita.onrender.com/swagger-ui/index.html](https://neurovita.onrender.com/swagger-ui/index.html)
- **API:** [https://neurovita.onrender.com](https://neurovita.onrender.com)

### Autenticação no Swagger

1. Execute `POST /api/auth/login` com credenciais válidas.
2. Copie o token JWT retornado.
3. Clique em **Authorize**.
4. Informe o token no formato `Bearer SEU_TOKEN`, caso o campo não acrescente o prefixo automaticamente.
5. Confirme a autorização e execute um endpoint protegido.

Não compartilhe tokens JWT, senhas ou credenciais reais.

A versão anterior do backend teve login JWT verificado em produção. A nova versão integrada precisa passar por validação completa após sua publicação.

---

## 🐳 Docker

O projeto utiliza um Dockerfile multi-stage para compilar e executar frontend Angular e backend Java na mesma aplicação.

O processo possui três etapas:

1. **Frontend:** utiliza Node.js para instalar dependências e compilar o Angular.
2. **Backend:** utiliza Java 21 e Maven para compilar o Spring Boot e incorporar os arquivos do Angular em `src/main/resources/static/`.
3. **Execução:** utiliza uma imagem Java 21 JRE para executar a aplicação integrada.

A aplicação utiliza a variável de ambiente `PORT`, com `8080` como valor padrão.

### Variáveis de ambiente

As configurações sensíveis são fornecidas por variáveis de ambiente, incluindo:

- `MONGODB_URI`
- `JWT_SECRET`
- `PORT`
- `SMTP_ENABLED`
- `SMTP_HOST`
- `SMTP_PORT`
- `SMTP_USERNAME`
- `SMTP_PASSWORD`
- `SMTP_FROM`
- `SMTP_AUTH`
- `SMTP_STARTTLS`

O SMTP permanece desabilitado por padrão. Para habilitar a recuperação de senha por e-mail, configure o provedor e defina `SMTP_ENABLED=true`.

O arquivo `.env` é utilizado somente no ambiente local e não deve ser versionado.

### Construção da imagem

```bash
docker build -t neurovita-integrado .
```

### Execução local

```bash
docker run --rm --name neurovita-local --env-file .env -p 8080:8080 neurovita-integrado
```

A validação da imagem Docker com as alterações mais recentes ainda está pendente.

---

## ☁️ Deploy e ambientes

O projeto utiliza **um único repositório GitHub**, organizado por branches para separar os ambientes de desenvolvimento, testes, homologação e produção.

Repositório:

[https://github.com/victorhborges/NeuroVita](https://github.com/victorhborges/NeuroVita)

### Organização das branches

| Branch | Finalidade |
|---|---|
| `develop` | Desenvolvimento e integração das funcionalidades |
| `testes` | Versão disponibilizada para validação da equipe de testes |
| `homologacao` | Validação final antes da produção |
| `producao` | Versão aprovada para disponibilização |

A branch `main` não faz parte do fluxo operacional atual.

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
producao
```

A promoção entre ambientes deve ocorrer por Pull Requests, preservando o histórico Git.

Não devem ser realizados pushes diretos nas branches `testes`, `homologacao` e `producao`.

Uma versão só deve ser promovida após revisão e aprovação da etapa anterior.

### Trabalho colaborativo

Os integrantes da equipe podem trabalhar no mesmo repositório, cada um utilizando branches próprias para suas tarefas.

Exemplos:

```text
feature/frontend-pacientes
feature/frontend-agenda
feature/frontend-login
fix/frontend-validacao
feature/backend-consultas
```

Fluxo recomendado:

1. Atualizar a branch `develop`.
2. Criar uma branch própria para a tarefa.
3. Realizar as alterações.
4. Executar as verificações aplicáveis.
5. Fazer commit e push da branch de trabalho.
6. Abrir um Pull Request direcionado a `develop`.
7. Aguardar revisão antes da integração.

As equipes devem alinhar alterações que envolvam contratos da API, modelos compartilhados e serviços de integração para evitar incompatibilidades entre frontend e backend.

### Ambiente de produção anterior

A versão anterior do backend está hospedada no Render.

- **URL:** [https://neurovita.onrender.com](https://neurovita.onrender.com)
- **Swagger:** [https://neurovita.onrender.com/swagger-ui/index.html](https://neurovita.onrender.com/swagger-ui/index.html)

O login autenticado e consultas de administradores e pacientes foram verificados na versão anterior.

A nova arquitetura integrada ainda precisa ser publicada e validada.

### Integração com o frontend

O frontend Angular é servido pela própria aplicação Spring Boot por meio dos arquivos estáticos compilados e incorporados em `src/main/resources/static/`.

Essa arquitetura permite que a interface e a API utilizem a mesma origem, simplificando a comunicação entre frontend e backend.

O Spring Security possui configuração para os arquivos estáticos e para as rotas da aplicação, mantendo as regras de segurança aplicáveis às rotas da API.

A autorização efetiva dos recursos permanece sob responsabilidade do backend.

---

## 🗄️ Banco de dados

O projeto utiliza **MongoDB** com Spring Data MongoDB e MongoDB Atlas.

### Coleções

A aplicação utiliza coleções para persistir os documentos dos módulos, incluindo:

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
- `vinculos_profissional_paciente`

A existência e a estrutura efetiva de cada coleção dependem dos modelos, repositórios e operações executadas.

### Responsáveis e vínculos

| Coleção | Finalidade |
|---|---|
| `responsaveis` | Dados pessoais e de contato dos responsáveis |
| `vinculosResponsaveis` | Relação entre pacientes e responsáveis |
| `pacientes` | Dados cadastrais dos pacientes |

O relacionamento entre paciente e responsável é armazenado separadamente, evitando duplicação dos dados pessoais.

### Vínculos entre profissionais e pacientes

| Coleção | Finalidade |
|---|---|
| `vinculos_profissional_paciente` | Registro dos vínculos entre profissionais e pacientes, incluindo períodos de atendimento e histórico de encerramento |

A estrutura permite registrar mais de um profissional associado a um paciente, mantendo o histórico dos vínculos anteriores.

O acesso de leitura compartilhada às informações clínicas depende da existência de vínculo ativo.

O encerramento do vínculo não remove o registro histórico.

### Configuração da conexão

A aplicação utiliza a variável:

```text
MONGODB_URI
```

A propriedade configurada em `application.properties` utiliza essa variável para estabelecer a conexão.

A URI precisa incluir o nome do banco de dados e os parâmetros necessários.

### Segurança das configurações

- Não armazenar senhas ou tokens no código-fonte.
- Não versionar URIs do MongoDB que contenham credenciais.
- Não publicar os valores reais de `MONGODB_URI` ou `JWT_SECRET`.
- Configurar as variáveis no ambiente de execução.
- Manter o arquivo `.env` fora do Git.

### Status da conexão

A conexão com MongoDB Atlas foi estabelecida e utilizada em verificações anteriores.

Durante a implementação dos vínculos entre profissionais e pacientes, foi realizada uma consulta somente de leitura à coleção `prontuarios`, identificando cinco documentos, sem duplicidades de `pacienteId` e sem registros sem identificação de paciente.

Também foram verificados os nomes dos campos existentes, sem exposição dos valores clínicos.

A auditoria não realizou migração ou escrita nos documentos existentes.

Entretanto, as alterações mais recentes ainda não foram validadas integralmente com MongoDB Atlas real.

Os testes Maven mais recentes utilizam repositórios mockados. Portanto, a aprovação desses testes não comprova a persistência correta de todos os novos fluxos no banco real.

A compatibilidade das operações de responsáveis, vínculos, prontuários, documentos clínicos, inativação e sincronização de contas deve ser confirmada com a equipe responsável pelo banco de dados.

---

## 🚀 Executando localmente

### Pré-requisitos

- Java JDK 21.
- Node.js e npm.
- Git.
- Docker Desktop, para execução em container.
- MongoDB Atlas ou outra instância MongoDB acessível.
- Variáveis de ambiente configuradas.

### Verificar Java

```bash
java -version
```

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

### Atualizar a branch de desenvolvimento

```bash
git switch develop
git pull origin develop
```

### Configurar as variáveis de ambiente

Configure `MONGODB_URI` com uma URI válida do MongoDB e `JWT_SECRET` com uma chave secreta forte.

Não inclua os valores reais dessas variáveis em arquivos versionados.

### Executar somente o backend

**Windows:**

```cmd
mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

### Trabalhar diretamente no frontend

Acesse o diretório:

```bash
cd frontend
```

Instale as dependências:

```bash
npm ci
```

Inicie o servidor de desenvolvimento conforme os scripts disponíveis no `package.json`.

Normalmente:

```bash
npm start
```

ou:

```bash
npx ng serve
```

### Compilar o frontend

```bash
npm run build
```

O resultado da compilação é gerado no diretório de saída configurado no `angular.json`.

Na configuração atual, o caminho esperado é:

```text
frontend/dist/neurovita/browser/
```

### Incorporar o frontend ao Spring Boot

Após compilar o Angular, os arquivos gerados precisam ser copiados para os recursos estáticos do backend.

No Windows, execute a partir da raiz do projeto:

```cmd
xcopy /E /I /Y frontend\dist\neurovita\browser src\main\resources\static
```

Sempre que houver alterações no frontend, execute novamente a compilação e a cópia antes de iniciar uma nova execução integrada.

A pasta `src/main/resources/static/` contém os arquivos compilados utilizados pela aplicação integrada.

Os diretórios `frontend/node_modules` e `frontend/dist` são gerados durante o desenvolvimento e permanecem ignorados pelo Git.

### Executar a aplicação integrada localmente

No Windows, configure as variáveis na sessão do terminal:

```cmd
set "MONGODB_URI=SUA_URI_DO_MONGODB"
set "JWT_SECRET=SUA_CHAVE_JWT"
```

Depois, na raiz do projeto:

```cmd
mvnw.cmd spring-boot:run
```

A aplicação estará disponível em:

[http://localhost:8080](http://localhost:8080)

### Executar com Docker

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
- **OpenAPI:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🧪 Verificação e testes

A compilação e os testes Maven podem ser executados localmente.

### Compilação do backend

**Windows:**

```cmd
mvnw.cmd compile
```

**Linux/macOS:**

```bash
./mvnw compile
```

### Testes Maven

**Windows:**

```cmd
mvnw.cmd test
```

**Linux/macOS:**

```bash
./mvnw test
```

### Empacotamento

**Windows:**

```cmd
mvnw.cmd -DskipTests package
```

**Linux/macOS:**

```bash
./mvnw -DskipTests package
```

### Última validação reportada

Na última execução local das alterações de vínculos entre profissionais e pacientes:

| Verificação | Resultado |
|---|---|
| Testes Maven | 36 aprovados, sem falhas ou erros |
| `NeurovitaApplicationTests` | 1 aprovado |
| `ConsultaServiceTest` | 7 aprovados |
| `DocumentoClinicoServiceTest` | 2 aprovados |
| `PacienteServiceTest` | 3 aprovados |
| `ProfissionalServiceTest` | 3 aprovados |
| `ProntuarioServiceTest` | 2 aprovados |
| `SecretariaServiceTest` | 14 aprovados |
| `VinculoProfissionalPacienteServiceTest` | 4 aprovados |
| Compilação Maven | `BUILD SUCCESS` |
| `git diff --check` | Sem erros |
| Docker com alterações recentes | Pendente |
| MongoDB Atlas com alterações recentes | Pendente |
| Deploy da nova versão | Pendente |

A suíte foi executada por meio de:

```cmd
mvnw.cmd test
```

Resultado:

```text
Tests run: 36, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

Os testes unitários dos serviços utilizam repositórios mockados. O resultado positivo não substitui a validação das operações no MongoDB Atlas real.

**Os resultados acima representam a última execução informada e não garantem que todas as operações estejam validadas em ambiente real.**

Os testes funcionais completos são responsabilidade da equipe designada. As verificações de compilação e as correções técnicas são realizadas durante o desenvolvimento.

---

## 📊 Status do projeto

| Módulo / recurso | Status conhecido |
|---|---|
| Estrutura Java | 🟢 Implementada |
| Spring Boot | 🟢 Implementado |
| Spring Data MongoDB | 🟢 Configurado |
| Frontend Angular | 🟢 Integrado localmente |
| Pacientes | 🟡 CRUD e novas regras implementados localmente; validação real pendente |
| Responsáveis | 🟡 CRUD e associação implementados; validação com banco real pendente |
| Múltiplos responsáveis | 🟢 Implementado localmente |
| Vínculos e graus de parentesco | 🟢 Regras implementadas localmente |
| Plano de saúde | 🟢 Validações implementadas localmente |
| Profissionais | 🟢 CRUD e modalidades implementados localmente |
| Atendimento presencial e online | 🟢 Regras implementadas localmente |
| Vínculos entre profissionais e pacientes | 🟢 Estrutura e serviços implementados localmente |
| Histórico de vínculos | 🟢 Implementado localmente |
| Leitura clínica compartilhada | 🟢 Regras implementadas localmente |
| Preservação da autoria clínica | 🟢 Regras implementadas localmente |
| Disponibilidades | 🟡 Implementadas; revisão de permissões pendente |
| Consultas | 🟢 Regras de atividade, modalidade e agendamento implementadas localmente |
| Agenda | 🟢 Implementada; validação funcional pendente |
| Inativação de pacientes | 🟢 Implementada localmente |
| Inativação de profissionais | 🟢 Implementada localmente |
| Inativação de secretárias | 🟢 Implementada localmente |
| Sincronização entidade/usuário | 🟡 Implementada com compensação; validação com MongoDB real pendente |
| Prontuários | 🟢 Estrutura centralizada e prevenção de duplicidade implementadas localmente |
| Documentos clínicos | 🟢 Leitura compartilhada e preservação de autoria implementadas localmente |
| Diagnósticos | 🟢 Implementados |
| Tratamentos | 🟢 Implementados; validação funcional pendente |
| Exames | 🟢 Implementados; validação funcional pendente |
| Administradores | 🟢 Credenciais centralizadas em `Usuario` |
| Autenticação JWT | 🟢 Integrada localmente |
| BCrypt | 🟢 Utilizado |
| Troca de senha no primeiro acesso | 🟡 Implementada; validação funcional pendente |
| Recuperação de senha por e-mail | 🟡 Implementada; requer SMTP |
| Spring Security | 🟡 Configurado; revisão de permissões pendente |
| Swagger/OpenAPI | 🟢 Implementado |
| Tratamento global de exceções | 🟢 Implementado |
| Testes Maven | 🟢 36 aprovados na última execução informada |
| Docker multi-stage | 🟢 Configurado; validação da versão atual pendente |
| MongoDB Atlas | 🟡 Auditoria somente de leitura realizada; validação integral da versão atual pendente |
| Deploy da nova arquitetura | ⚪ Pendente |
| Receitas | ⚪ Aguardando requisitos |
| Testes funcionais completos | 🟡 Sob responsabilidade da equipe designada |

Os status indicam o estado de desenvolvimento conhecido. Não substituem a validação funcional, a revisão de segurança ou a confirmação de todas as operações no ambiente de produção.

---

## 🗺️ Próximas etapas

### Desenvolvimento e integração

- [x] Implementar validações iniciais nos DTOs.
- [x] Implementar CRUD de responsáveis.
- [x] Implementar vínculos entre pacientes e responsáveis.
- [x] Implementar suporte a múltiplos responsáveis por paciente.
- [x] Implementar validações de plano de saúde.
- [x] Implementar modalidades de atendimento.
- [x] Implementar status, remarcação e cancelamento de consultas.
- [x] Bloquear agendamentos para pacientes e profissionais inativos.
- [x] Implementar inativação de pacientes, profissionais e secretárias.
- [x] Implementar sincronização de estado entre entidades e contas.
- [x] Implementar vínculos entre profissionais e pacientes.
- [x] Implementar histórico de abertura e encerramento de vínculos.
- [x] Implementar leitura compartilhada de informações clínicas conforme vínculo ativo.
- [x] Preservar autoria dos registros clínicos.
- [x] Implementar prevenção de prontuários centrais duplicados por paciente.
- [x] Adicionar testes para vínculos, prontuários e documentos clínicos.
- [x] Executar testes Maven: 36 aprovados na última execução informada.
- [x] Gerar o JAR localmente.
- [x] Executar build de desenvolvimento Angular.
- [x] Verificar `git diff --check`.

### Validação técnica pendente

- [ ] Validar a nova versão com Docker.
- [ ] Validar integralmente a conexão com MongoDB Atlas na versão atual.
- [ ] Verificar persistência de pacientes e responsáveis.
- [ ] Verificar criação, atualização e remoção de vínculos.
- [ ] Verificar abertura e encerramento de vínculos entre profissionais e pacientes.
- [ ] Verificar leitura compartilhada com vínculo ativo.
- [ ] Verificar bloqueio de leitura após encerramento do vínculo.
- [ ] Verificar preservação de autoria em documentos clínicos.
- [ ] Verificar prevenção de prontuários duplicados no banco real.
- [ ] Verificar compensação em cenários de falha de salvamento.
- [ ] Verificar sincronização de entidade e conta de usuário.
- [ ] Verificar reativação e inativação no banco real.
- [ ] Verificar consultas, remarcações e modalidades no banco real.
- [ ] Revisar permissões dos endpoints.
- [ ] Validar o build otimizado Angular em ambiente com rede disponível.

### Validação funcional

- [ ] Validar login com contas dos diferentes perfis.
- [ ] Validar troca de senha no primeiro acesso.
- [ ] Validar recuperação de senha com SMTP.
- [ ] Validar CRUD de pacientes e responsáveis.
- [ ] Validar cadastro de pacientes menores e maiores de idade.
- [ ] Validar múltiplos responsáveis e graus de parentesco.
- [ ] Validar edição e remoção de vínculos.
- [ ] Validar profissionais e modalidades de atendimento.
- [ ] Validar disponibilidades e conflitos de horário.
- [ ] Validar consultas, remarcações e cancelamentos.
- [ ] Validar inativação e reativação de cadastros.
- [ ] Validar prontuários, diagnósticos, tratamentos e exames.
- [ ] Validar acesso compartilhado aos dados clínicos conforme vínculo ativo.
- [ ] Validar edição e remoção de documentos conforme autoria.
- [ ] Definir o módulo de Receitas após a especificação no Figma.
- [ ] Disponibilizar a aplicação para a equipe de testes.

### Entrega e publicação

- [ ] Publicar as alterações revisadas na branch `develop`.
- [ ] Promover para `testes` por Pull Request.
- [ ] Executar testes funcionais.
- [ ] Promover a versão aprovada para `homologacao`.
- [ ] Realizar validação final.
- [ ] Promover para `producao` somente após aprovação.
- [ ] Configurar e validar os serviços Render correspondentes aos ambientes.

---

## 🌿 Git e GitHub

O projeto utiliza Git e GitHub para controle de versão, revisão de código e organização das entregas.

**Repositório oficial:**

[https://github.com/victorhborges/NeuroVita](https://github.com/victorhborges/NeuroVita)

### Estratégia de branches

| Branch | Finalidade |
|---|---|
| `develop` | Desenvolvimento diário e integração |
| `testes` | Validação pela equipe de testes |
| `homologacao` | Validação final |
| `producao` | Versão aprovada |

A branch `main` existe, mas não faz parte do fluxo operacional definido atualmente.

### Fluxo de promoção

```text
develop
   |
   v
testes
   |
   v
homologacao
   |
   v
producao
```

As promoções ocorrem por Pull Requests, preservando o histórico Git.

Não realizar push direto nas branches de testes, homologação ou produção.

### Fluxo de trabalho individual

Para começar a trabalhar:

```bash
git clone https://github.com/victorhborges/NeuroVita.git

cd NeuroVita

git switch develop

git pull origin develop
```

Criar uma branch específica para a tarefa:

```bash
git switch -c feature/nome-da-tarefa
```

Após concluir as alterações:

```bash
git add .

git commit -m "Descrição da alteração"

git push -u origin feature/nome-da-tarefa
```

Depois, abrir um Pull Request com destino a `develop`.

### Revisão antes de publicar

Antes de realizar um commit:

1. Conferir a branch atual.
2. Conferir os arquivos modificados.
3. Revisar as diferenças.
4. Confirmar que não existem credenciais ou arquivos temporários.
5. Atualizar o README quando houver alterações relevantes.
6. Adicionar somente os arquivos relacionados.
7. Criar um commit com mensagem descritiva.
8. Enviar para a branch correspondente.

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

docs: alteração na documentação

test: alteração nos testes

refactor: melhoria estrutural

chore: configuração ou manutenção
```

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
- Separação dos ambientes.
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