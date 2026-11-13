# Contrato de dados MongoDB — NeuroVita

**Status:** contrato derivado do código da branch `develop`; conformidade do Atlas ainda não auditada.  
**Uso:** referência para inspecionar, validar e planejar ajustes no banco. Este documento não autoriza alteração ou migração de dados.

## 1. Escopo e critério de leitura

O backend declara 19 coleções por meio de `@Document`. Os campos abaixo reproduzem nomes e tipos Java dos modelos. Obrigatoriedade é descrita separando:

- **Persistência:** a maioria dos modelos não declara `@NotNull`/`@NotBlank`; campos de referência podem estar ausentes em documentos legados e a leitura nem sempre falha.
- **API/regra:** campos que DTOs e serviços exigem em operações de criação/alteração. Isso não implica que o MongoDB aplique validação de schema.
- **Opcional:** campo nullable ou preenchido apenas em certos fluxos. `null` e ausência podem se comportar de forma diferente em consultas e atualizações.

`@Id String` é o identificador do documento. O Spring Data pode gravá-lo como BSON ObjectId quando a string for um ObjectId válido; IDs não ObjectId podem permanecer strings. IDs de relacionamento (`pacienteId`, `profissionalId` etc.) são campos Java `String` comuns, não DBRefs nem objetos BSON ObjectId. O código não declara alias `@Field`, índices declarativos nem validators MongoDB.

As regras de negócio são aplicadas pelos serviços Java, não por constraints de coleção. A API retorna DTOs em alguns módulos; nomes e conteúdo de DTO não devem ser copiados automaticamente para a estrutura persistida.

## 2. Conversão e convenções BSON

### Tipos escalares

| Java | BSON esperado | Observação |
|---|---|---|
| `String` | String | Inclui IDs de referência e valores enum; não são referências Mongo nativas. |
| `Integer` / `int` | Int32 | `int` default é `0`; wrapper pode ser nulo. |
| `Long` / `long` | Int64 | `tokenVersion` wrapper pode estar ausente; getter interpreta como 0. |
| `Boolean` / `boolean` | Boolean | Wrapper pode ser nulo; default de campo só se aplica em novas instâncias Java. |
| `List<T>` | Array BSON | Elementos complexos viram subdocumentos inline. |
| Enum Java | String | Nome exato da constante, sensível a maiúsculas/minúsculas. |
| `Instant` | BSON Date | Instante UTC, precisão BSON em milissegundos. |
| `LocalDateTime`, `LocalDate`, `LocalTime` | BSON Date via conversão temporal | O conversor JSR-310 padrão usa timezone do sistema. Sem timezone explícito no projeto, o instante BSON pode variar entre ambientes. |

Não foram encontrados `MongoCustomConversions`, converters próprios, `MongoConverter` customizado, `ZoneId`, `TimeZone`, nem configuração de timezone no código/configuração versionados. O `pom.xml` usa Spring Boot **4.0.8**, Java 21; a dependência resolvida localmente é Spring Data MongoDB **5.0.7**. O `application.properties` configura a URI do MongoDB por propriedade, mas não define conversão/timezone. Nenhum código/documento de migração Node/Mongoose foi localizado na árvore analisada.

Spring Data fornece codecs/conversores JSR-310. A documentação oficial alerta que conversões padrão dos tipos temporais locais para `Date` dependem do timezone do sistema; o driver nativo usa UTC quando configurado. Fontes: [Spring Data MongoDB — Custom Conversions](https://docs.spring.io/spring-data/mongodb/reference/mongodb/mapping/custom-conversions.html), [MongoConverterConfigurationAdapter 5.x](https://docs.spring.io/spring-data/mongodb/reference/api/java/org/springframework/data/mongodb/core/convert/MongoCustomConversions.MongoConverterConfigurationAdapter.html), [Spring Data MongoDB — Object Mapping](https://docs.spring.io/spring-data/mongodb/reference/mongodb/mapping/mapping.html).

### Datas e horários: API, armazenamento e interpretação

| Campos/modelos | Tipo Java | API JSON | BSON esperado | Considerações |
|---|---|---|---|---|
| `Consulta.dataHora`; `Consulta.historico[].dataHora/alteradaEm`; datas de criação/alteração, arquivamento e histórico nos modelos clínicos e vínculos; `PreferenciasUsuario.atualizadaEm` | `LocalDateTime` | ISO local `yyyy-MM-dd'T'HH:mm:ss` (sem offset, como usado pela API Angular) | BSON Date convertido segundo timezone do processo | É horário civil sem zona explícita. O contrato atual não prova UTC nem `America/Sao_Paulo`. Não converter registros sem decidir qual zona histórica os valores representam. |
| Nascimento, validade de plano, admissão, data da sessão/realização, data em histórico de exame | `LocalDate` | `yyyy-MM-dd` | BSON Date representando início do dia após conversão local | Conceitualmente é data civil, não instante. A conversão atual pode deslocar sua representação BSON por timezone. Não misturar com `Instant`. |
| `Disponibilidade.horaInicio/horaFim` | `LocalTime` | `HH:mm:ss` (API pode aceitar hora sem segundos) | BSON Date produzido pelo conversor de hora | É hora do dia sem data/zona. BSON Date não tem tipo time-only; aplicação e Atlas precisam manter o mesmo conversor. |
| `Administrador.criadoEm`, `Usuario.senhaAlteradaEm`, `RecuperacaoSenha.expiraEm` | `Instant` | ISO 8601 UTC com `Z` na API quando exposto | BSON Date UTC | Instantes absolutos. Persistência em ms pode truncar nanossegundos. |

**Convenção recomendada para novos dados:** separar data civil, horário de relógio e instante; para instantes, UTC/BSON Date; para `LocalDate`/`LocalTime`/`LocalDateTime`, fixar explicitamente codecs/conversores UTC ou armazenar formatos nativos de domínio (por exemplo, data ISO `YYYY-MM-DD` e hora `HH:mm:ss`) com conversores de leitura/escrita correspondentes. A escolha exige alteração de código e migração coordenada, portanto **não é o comportamento atual declarado**. Até essa decisão, a equipe deve preservar o comportamento de conversão do backend e não editar BSON manualmente.

Para agenda, não armazenar `dataHora` como string. O Spring Data deriva critérios de comparação de `LocalDateTime` e os converte pelo mapeamento da propriedade; a forma esperada para período é equivalente a `dataHora >= BSON Date(início)` e `dataHora < BSON Date(fim)`. Consulta por igualdade/intervalo não considera strings como datas. Tipos mistos podem causar resultados inconsistentes e, se documentos retornados não puderem ser convertidos ao modelo, erro de leitura. O campo real e a etapa exata da falha ainda não foram verificados.

### Datas temporais identificadas no modelo

| Coleção | Campos temporais |
|---|---|
| `administradores` | `criadoEm` (`Instant`) |
| `consultas` | `dataHora`, `historico[].dataHora`, `historico[].alteradaEm` (`LocalDateTime`) |
| `pacientes` | `dataNascimento`, `validadePlano` (`LocalDate`) |
| `profissionais` | `dataNascimento` (`LocalDate`) |
| `responsaveis` | `dataNascimento` (`LocalDate`) |
| `secretarias` | `dataNascimento`, `admissao` (`LocalDate`) |
| `usuarios` | `senhaAlteradaEm` (`Instant`) |
| `recuperacoes_senha` | `expiraEm` (`Instant`) |
| `documentos_clinicos` | `dataSessao` (`LocalDate`); `criadoEm`, `atualizadoEm`, `arquivadoEm` (`LocalDateTime`) |
| `diagnosticos` | `arquivadoEm` (`LocalDateTime`) |
| `tratamentos` | `arquivadoEm` (`LocalDateTime`) |
| `exames` | `dataRealizacao`, `historicoResultados[].dataRealizacao` (`LocalDate`); demais datas e histórico (`LocalDateTime`) |
| `prescricoes` | `criadaEm`, `atualizadaEm`, `historico[].atualizadaEm/registradaEm` (`LocalDateTime`) |
| `prontuarios` | `criadoEm`, `atualizadoEm` (`LocalDateTime`) |
| `preferencias_usuarios` | `atualizadaEm` (`LocalDateTime`) |
| `vinculos_profissional_paciente` | `iniciadoEm`, `encerradoEm` (`LocalDateTime`) |
| `evolucoes` | `dataAtendimento`, `criadoEm`, `atualizadoEm` (`LocalDateTime`) |
| `disponibilidades`, `vinculosResponsaveis` | sem campos temporais |

API usa ISO-8601 por `java.time`/Jackson; endpoints de agenda usam `LocalDate` (`yyyy-MM-dd`) e filtros de consulta recebem `LocalDateTime`. Não há offset incluído no `LocalDateTime`, e a zona efetiva de `LocalDateTime.now()` depende da JVM. Esse é um ponto de contrato a normalizar antes de se comparar valores de diferentes ambientes.

## 3. Coleções e schemas

Tipos apresentados como `Date` são BSON Date. Campos marcados **condicional** são necessários apenas nos fluxos indicados. Nenhum schema validator de Mongo foi encontrado.

Os exemplos temporais abaixo ilustram BSON Date na convenção UTC. O código atual não fixa UTC para `LocalDateTime`, `LocalDate` ou `LocalTime`; portanto, o instante BSON exato produzido no ambiente pode variar conforme timezone da JVM. Os exemplos definem a forma BSON esperada, mas não afirmam o valor/tipo atualmente presente no Atlas.

### 3.1 `administradores` — `Administrador`

Perfil administrativo. `nome`, `email` e `cpf` são exigidos no cadastro; e-mail/CPF são verificados por serviço, sem índice único declarado. `perfil` é setado pelo serviço como `ADMINISTRADOR`. `criadoEm` é preenchido com `Instant.now()`. Telefone opcional.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String id` | Gerado pelo Mongo/Spring. |
| `nome`, `email`, `perfil`, `cpf`, `telefone` | String / `String` | Nome/e-mail/CPF requeridos no cadastro; perfil `ADMINISTRADOR`; telefone opcional. |
| `criadoEm` | Date / `Instant` | Preenchido na criação; pode faltar em legado. |

```json
{ "_id": {"$oid":"671000000000000000000001"}, "nome":"Admin exemplo", "email":"admin@example.invalid", "perfil":"ADMINISTRADOR", "cpf":"00000000000", "telefone":null, "criadoEm":{"$date":"2026-11-11T12:00:00Z"} }
```

### 3.2 `usuarios` — `Usuario`

Credencial e associação de login com entidade de domínio. `senha` deve conter somente hash produzido pelo `PasswordEncoder`, nunca senha em claro. `perfil` observado: `ADMINISTRADOR`, `SECRETARIA`, `PROFISSIONAL`, `PACIENTE`. `entidadeId` é ID string da coleção associada ao perfil. `ativo` controla login; `senhaProvisoria` é booleano.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `nome`, `email`, `senha`, `perfil`, `entidadeId` | String | Requeridos em cadastro de conta; e-mail não deve duplicar. |
| `senhaProvisoria`, `ativo` | Boolean / `boolean` | Obrigatórios semanticamente; Java default false. |
| `senhaAlteradaEm` | Date / `Instant` | Opcional até troca de senha. |
| `tokenVersion` | Int64 / `Long` | Opcional em legado; ausência/null é interpretada como zero. |

```json
{ "_id":{"$oid":"671000000000000000000002"}, "nome":"Usuário exemplo", "email":"usuario@example.invalid", "senha":"$2a$...hash...", "perfil":"SECRETARIA", "senhaProvisoria":false, "ativo":true, "entidadeId":"671000000000000000000003", "senhaAlteradaEm":{"$date":"2026-11-11T12:00:00Z"}, "tokenVersion":{"$numberLong":"0"} }
```

### 3.3 `pacientes` — `Paciente`

Cadastro de paciente. Nome e nascimento são obrigatórios; e-mail obrigatório para conta de paciente. Para menor de 18 anos, serviço exige responsável vinculado. `possuiPlanoSaude=true` exige `planoSaude`; false permite limpar os dados do plano. Campos plano/carteirinha/validade opcionais segundo regra.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `nome`, `dataNascimento` | String, Date / `String`, `LocalDate` | Obrigatórios na criação. |
| `email`, `cpf`, `telefone`, `endereco`, `cep`, `cidade` | String | E-mail requerido para a conta; demais podem ser nulos conforme DTO/regra. |
| `ativo` | Boolean / `Boolean` | Default Java `true`; ausência antiga é interpretada como ativo no serviço/modelo. |
| `possuiPlanoSaude` | Boolean / `Boolean` | Optional/nullable no schema; regra de plano condicionada ao valor. |
| `planoSaude`, `numeroCarteirinha` | String | Plano requerido quando possui plano; número opcional. |
| `validadePlano` | Date / `LocalDate` | Opcional. |

```json
{ "_id":{"$oid":"671000000000000000000004"}, "nome":"Paciente exemplo", "dataNascimento":{"$date":"2012-03-15T00:00:00Z"}, "email":"paciente@example.invalid", "cpf":null, "telefone":null, "endereco":null, "cep":null, "cidade":null, "ativo":true, "possuiPlanoSaude":false, "planoSaude":null, "numeroCarteirinha":null, "validadePlano":null }
```

### 3.4 `responsaveis` — `Responsavel`

Pessoa responsável por paciente; vários responsáveis podem se vincular a um paciente. Nome é obrigatório. CPF/e-mail são normalizados e verificados quando fornecidos; telefone, nascimento e endereço opcionais. Responsável não é apagado se ainda houver vínculo.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `nome` | String | Requerido. |
| `cpf`, `email`, `telefone`, `endereco` | String | Opcionais; CPF/e-mail únicos são verificados pela aplicação quando não nulos. |
| `dataNascimento` | Date / `LocalDate` | Opcional. |

```json
{ "_id":{"$oid":"671000000000000000000005"}, "nome":"Responsável exemplo", "cpf":null, "email":null, "telefone":null, "dataNascimento":null, "endereco":null }
```

### 3.5 `vinculosResponsaveis` — `VinculoResponsavel`

Relação many-to-many de paciente/responsável. Identificadores são `String` simples. O service verifica existência dos dois registros e evita duplicação lógica; no unlink, remove a relação (o modelo não guarda histórico/status).

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `responsavelId`, `grauParentesco` | String | Obrigatórios pela API; IDs referem `pacientes` e `responsaveis`. |

```json
{ "_id":{"$oid":"671000000000000000000006"}, "pacienteId":"671000000000000000000004", "responsavelId":"671000000000000000000005", "grauParentesco":"Mãe" }
```

### 3.6 `profissionais` — `Profissional`

Cadastro profissional, ligado à conta `usuarios` por `entidadeId`; mantém IDs de `disponibilidades` em array. Nome e categoria exigidos; e-mail da conta necessário no fluxo de criação. `ativo`/atende* controlam estados e modalidades.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `nome`, `email`, `emailContato`, `cpf`, `telefone`, `categoria`, `formacaoAcademica`, `numeroRegistro` | String | Nome/categoria requeridos; e-mail conta requerido; campos demais opcionais pelo DTO. |
| `dataNascimento` | Date / `LocalDate` | Opcional. |
| `anoConclusao` | Int32 / `Integer` | Opcional. |
| `atendeOnline`, `atendePresencial`, `ativo` | Boolean | `atendePresencial` default Java `true`; outros podem ser nulos/definidos pelo serviço. |
| `disponibilidadeIds` | Array de String / `List<String>` | IDs de `disponibilidades`; pode ser ausente/vazio. |

```json
{ "_id":{"$oid":"671000000000000000000007"}, "nome":"Profissional exemplo", "email":"prof@example.invalid", "emailContato":null, "dataNascimento":null, "cpf":null, "telefone":null, "categoria":"Psicólogo", "formacaoAcademica":null, "numeroRegistro":null, "anoConclusao":null, "atendeOnline":false, "atendePresencial":true, "ativo":true, "disponibilidadeIds":["671000000000000000000008"] }
```

### 3.7 `secretarias` — `Secretaria`

Cadastro da equipe de secretaria, associado a conta de mesmo perfil. `nome` e `email` são requeridos; datas e demais dados conforme DTO podem ser opcionais. `ativo` pode estar ausente em dados legados.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `nome`, `email`, `telefone`, `emailContato`, `cpf`, `endereco`, `cep`, `cidade`, `cargo` | String | Nome/e-mail requeridos; demais opcionais. |
| `dataNascimento`, `admissao` | Date / `LocalDate` | Opcionais. |
| `ativo` | Boolean / `Boolean` | O estado acompanha conta quando alterado pelo serviço. |

```json
{ "_id":{"$oid":"671000000000000000000009"}, "nome":"Secretaria exemplo", "email":"secretaria@example.invalid", "telefone":null, "emailContato":null, "dataNascimento":null, "cpf":null, "endereco":null, "cep":null, "cidade":null, "cargo":null, "admissao":null, "ativo":true }
```

### 3.8 `disponibilidades` — `Disponibilidade`

Janela semanal de atendimento referenciada pelo profissional via `disponibilidadeIds`; não tem `profissionalId` próprio. Dia permitido pelo serviço: `SEGUNDA_FEIRA`, `TERCA_FEIRA`, `QUARTA_FEIRA`, `QUINTA_FEIRA`, `SEXTA_FEIRA`, `SABADO`, `DOMINGO` (sem acentos e com underscore). Início/fim requeridos e devem formar intervalo válido; `ativo` controla uso.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `diaSemana` | String | Requerido; `SEGUNDA_FEIRA`, `TERCA_FEIRA`, `QUARTA_FEIRA`, `QUINTA_FEIRA`, `SEXTA_FEIRA`, `SABADO`, `DOMINGO`. |
| `horaInicio`, `horaFim` | Date / `LocalTime` | Requeridos; `horaFim` após início. |
| `ativo` | Boolean / `Boolean` | Usar true/false explícito; janela inativa não agenda. |

```json
{ "_id":{"$oid":"671000000000000000000008"}, "diaSemana":"QUARTA_FEIRA", "horaInicio":{"$date":"1970-01-01T08:00:00Z"}, "horaFim":{"$date":"1970-01-01T12:00:00Z"}, "ativo":true }
```

### 3.9 `vinculos_profissional_paciente` — `VinculoProfissionalPaciente`

Histórico de associação profissional/paciente; vínculo ativo quando `encerradoEm == null`. Não apagar vínculo encerrado. IDs referem `pacientes` e `profissionais`; `iniciadoPor`/`encerradoPor` são identificadores textuais do ator (normalmente identidade autenticada), não DBRefs.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `profissionalId` | String | Obrigatórios pela criação do vínculo. |
| `iniciadoEm`, `encerradoEm` | Date / `LocalDateTime` | Início preenchido no vínculo; encerramento nulo enquanto ativo. |
| `iniciadoPor`, `encerradoPor` | String | Metadados de auditoria; encerradoPor só ao encerrar. |

```json
{ "_id":{"$oid":"671000000000000000000010"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "iniciadoEm":{"$date":"2026-11-11T12:00:00Z"}, "iniciadoPor":"operador@example.invalid", "encerradoEm":null, "encerradoPor":null }
```

### 3.10 `consultas` — `Consulta`

Agenda é derivada desta coleção. Paciente e profissional obrigatórios e validados pelo serviço. `dataHora` é início; `duracao` em minutos (padrão operacional 50, podendo preferências existentes definir outro valor). Conflitos verificam intervalo completo; canceladas não bloqueiam. Status ausente em documentos antigos deve ser tratado pela compatibilidade do backend e não deve ser convertido automaticamente sem validação.

`StatusConsulta`: `PENDENTE`, `CONFIRMADA`, `REMARCADA`, `CANCELADA`. `historico` guarda snapshots embutidos, inclusive status e horário anterior.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `profissionalId` | String | Obrigatórios; referências às respectivas coleções. |
| `dataHora` | Date / `LocalDateTime` | Obrigatório; início de consulta, não string. |
| `tipoAtendimento`, `local`, `modalidade` | String | Obrigatórios no request. |
| `duracao` | Int32 / `Integer` | Requerido no request atual; minutos. Não assumir que todos documentos antigos têm o campo. |
| `status` | String / `StatusConsulta` | Default Java `PENDENTE`; enum acima. Ausência em legado é possível. |
| `historico` | Array de subdocumentos | Default lista vazia; snapshot conforme estrutura abaixo. |
| `historico[].profissionalId`, `tipoAtendimento`, `local`, `modalidade`, `alteradaPor` | String | Valores do estado anterior/ator; alguns podem ser nulos em histórico legado. |
| `historico[].dataHora`, `alteradaEm` | Date / `LocalDateTime` | Data/hora antiga e instante local da alteração. |
| `historico[].duracao` | Int32 / `Integer` | Minutos do estado anterior. |
| `historico[].status` | String / enum | Estado anterior. |

```json
{ "_id":{"$oid":"671000000000000000000011"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "dataHora":{"$date":"2026-11-11T14:00:00Z"}, "tipoAtendimento":"Psicoterapia", "local":"Unidade", "modalidade":"Presencial", "duracao":50, "status":"CONFIRMADA", "historico":[] }
```

### 3.11 `prontuarios` — `Prontuario`

Resumo clínico associado ao paciente, profissional e, opcionalmente, consulta. Serviço mantém prontuário único por paciente (verifica existência antes de criar). Conteúdos clínicos são strings; acesso é controlado na API.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `profissionalId` | String | Requeridos no request; referências validadas. |
| `consultaId` | String | Opcional; referência a `consultas`. |
| `queixaPrincipal`, `avaliacao`, `planoTratamento`, `prescricao` | String | Opcionais na persistência, podem guardar informação clínica. |
| `criadoEm`, `atualizadoEm` | Date / `LocalDateTime` | Preenchidos/atualizados pelo service. |

```json
{ "_id":{"$oid":"671000000000000000000012"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "consultaId":null, "queixaPrincipal":"Conteúdo ilustrativo", "avaliacao":null, "planoTratamento":null, "prescricao":null, "criadoEm":{"$date":"2026-11-11T12:00:00Z"}, "atualizadoEm":{"$date":"2026-11-11T12:00:00Z"} }
```

### 3.12 `documentos_clinicos` — `DocumentoClinico`

Documento clínico versionado logicamente por `ativo`/`arquivadoEm`; não excluir fisicamente via fluxo de arquivamento. Serve para seções como registros clínicos e documentos; `secao` e `tipoAtendimento` são strings, sem enum persistente fechado. Não há modelos separados no código para laudo, encaminhamento, atestado, acolhimento ou avaliação psicológica; sua representação específica deve ser confirmada pela equipe funcional, sem inferir schemas próprios.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `secao`, `titulo`, `dataSessao`, `tipoAtendimento`, `conteudo` | String/Date | Requeridos no DTO; paciente validado. `conteudo` até 250.000 caracteres. |
| `profissionalId` | String | Definido pela autoria autenticada; requerido na criação após validação. |
| `criadoEm`, `atualizadoEm`, `arquivadoEm` | Date / `LocalDateTime` | `criadoEm`/`atualizadoEm` geridos pelo service; arquivamento opcional. |
| `ativo` | Boolean / `Boolean` | Default Java true; ausência/null é interpretada como ativo. |
| `anexos` | Array de subdocumentos | Default vazio; `{nome:String, conteudoBase64:String, tipoMime:String}`. Limites e MIME são validados no service. |

```json
{ "_id":{"$oid":"671000000000000000000013"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "secao":"EVOLUCAO", "titulo":"Registro ilustrativo", "dataSessao":{"$date":"2026-11-11T00:00:00Z"}, "tipoAtendimento":"Psicoterapia", "conteudo":"Texto fictício", "criadoEm":{"$date":"2026-11-11T12:00:00Z"}, "atualizadoEm":{"$date":"2026-11-11T12:00:00Z"}, "ativo":true, "arquivadoEm":null, "anexos":[] }
```

### 3.13 `diagnosticos` — `Diagnostico`

Diagnóstico ligado a paciente e profissional. `pacienteId`, `profissionalId` e `descricao` requeridos pelo DTO; `cid` opcional, máximo 20 caracteres; exclusão lógica.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `profissionalId`, `descricao`, `cid` | String | IDs/descrição requeridos; CID opcional. |
| `ativo` | Boolean / `Boolean` | Default true; null/ausência lido como ativo. |
| `arquivadoEm` | Date / `LocalDateTime` | Nulo enquanto ativo; preenchido ao arquivar. |

```json
{ "_id":{"$oid":"671000000000000000000014"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "descricao":"Descrição fictícia", "cid":null, "ativo":true, "arquivadoEm":null }
```

### 3.14 `tratamentos` — `Tratamento`

Plano de tratamento ligado a paciente/profissional. IDs e descrição requeridos pelo request. Exclusão é lógica. `frequencia`, `duracao`, `objetivo`, `observacoes` são texto, não valores normalizados/enums.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `profissionalId`, `descricao`, `objetivo`, `frequencia`, `duracao`, `observacoes` | String | IDs/descrição requeridos; demais opcionais. |
| `ativo` | Boolean / `Boolean` | Default true; null/ausência interpretado como ativo. |
| `arquivadoEm` | Date / `LocalDateTime` | Preenchido no arquivamento. |

```json
{ "_id":{"$oid":"671000000000000000000015"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "descricao":"Plano ilustrativo", "objetivo":null, "frequencia":null, "duracao":null, "observacoes":null, "ativo":true, "arquivadoEm":null }
```

### 3.15 `exames` — `Exame`

Pedido/resultado de exame associado a paciente e profissional. Nome e IDs requeridos; resultado pode ser atualizado preservando histórico. Arquivamento lógico.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `profissionalId`, `nome`, `tipo`, `resultado`, `observacoes` | String | IDs/nome requeridos; demais opcionais conforme request. |
| `dataRealizacao` | Date / `LocalDate` | Opcional na solicitação; requerida para envio de resultado. |
| `solicitadoEm`, `criadoEm`, `resultadoAtualizadoEm`, `arquivadoEm` | Date / `LocalDateTime` | Controlados pelo service, alguns ausentes até evento correspondente. |
| `anexosResultado` | Array de `{nome, conteudoBase64, tipoMime}` | Default vazio. |
| `historicoResultados` | Array de subdocumentos | Default vazio; snapshots de data, resultado, observações, anexos e autoria/datas de atualização. |
| `ativo` | Boolean / `Boolean` | Default true; ausente/null interpretado ativo. |

```json
{ "_id":{"$oid":"671000000000000000000016"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "nome":"Exame ilustrativo", "tipo":null, "dataRealizacao":null, "resultado":null, "observacoes":null, "solicitadoEm":{"$date":"2026-11-11T12:00:00Z"}, "criadoEm":{"$date":"2026-11-11T12:00:00Z"}, "resultadoAtualizadoEm":null, "anexosResultado":[], "historicoResultados":[], "ativo":true, "arquivadoEm":null }
```

### 3.16 `prescricoes` — `Prescricao`

Prescrição ligada a paciente/profissional. `pacienteId` e conteúdo textual requeridos no DTO; `profissionalId` vem da autenticação/contexto. `medicamentos` são subdocumentos com campos textuais; atualizações que omitem a lista preservam itens existentes. `historico` contém revisões anteriores.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `pacienteId`, `profissionalId`, `conteudo` | String | Paciente e conteúdo requeridos; profissional atribuído/validado. |
| `criadaEm`, `atualizadaEm` | Date / `LocalDateTime` | Preenchidas pelo serviço. |
| `medicamentos` | Array `{medicamento,dosagem,posologia,frequencia,duracao}` | Default vazio; campos são strings. |
| `historico` | Array `{conteudo,medicamentos,atualizadaEm,registradaEm,registradaPor}` | Default vazio; revisões anteriores devem ser preservadas. |

```json
{ "_id":{"$oid":"671000000000000000000017"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "conteudo":"Texto de prescrição fictício", "criadaEm":{"$date":"2026-11-11T12:00:00Z"}, "atualizadaEm":{"$date":"2026-11-11T12:00:00Z"}, "medicamentos":[], "historico":[] }
```

### 3.17 `preferencias_usuarios` — `PreferenciasUsuario`

Preferências armazenadas por e-mail, não por referência `usuarioId`. Valores permitidos nos DTOs: `tema` `CLARO|ESCURO`; `idioma` `PORTUGUES|INGLES`; `formatoData` `DD/MM/AAAA|MM/DD/AAAA|AAAA/MM/DD`; `inicioSemana` `SEGUNDA-FEIRA|DOMINGO`. Duração de consulta é Integer/minutos e pode não estar definida; serviço preserva os padrões atuais quando ausência. Flags booleanas.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `email` | String | Chave de busca; required semanticamente. |
| `lembretesConsultaEmail`, `alertasConsultasPendentes`, `notificacoesSistema` | Boolean / `Boolean` | Flags opcionais. |
| `tema`, `idioma`, `formatoData`, `inicioSemana` | String | Valores enumerados acima. |
| `duracaoPadraoConsulta` | Int32 / `Integer` | Minutos; request aceita 30, 40, 50 ou 60. Sem preferência aplicada, regra operacional padrão é 50. |
| `atualizadaEm` | Date / `LocalDateTime` | Definida a cada atualização. |

```json
{ "_id":{"$oid":"671000000000000000000018"}, "email":"usuario@example.invalid", "lembretesConsultaEmail":false, "alertasConsultasPendentes":true, "notificacoesSistema":true, "tema":"CLARO", "idioma":"PORTUGUES", "formatoData":"DD/MM/AAAA", "duracaoPadraoConsulta":50, "inicioSemana":"SEGUNDA-FEIRA", "atualizadaEm":{"$date":"2026-11-11T12:00:00Z"} }
```

### 3.18 `recuperacoes_senha` — `RecuperacaoSenha`

Estado temporário para recuperação. O service armazena hashes (`codigoHash`, `tokenHash`), expiração (`Instant`) e tentativas; não armazenar código/token em claro. Há reposição/consumo pelo fluxo de recuperação.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Identificador. |
| `email`, `codigoHash` | String | Necessários para solicitação. |
| `tokenHash` | String | Pode ser nulo antes/depois da etapa de verificação. |
| `expiraEm` | Date / `Instant` | Expiração absoluta; validade conferida pelo service. |
| `tentativas` | Int32 / `int` | Contador; default Java 0, fluxo limita tentativas. |

```json
{ "_id":{"$oid":"671000000000000000000019"}, "email":"usuario@example.invalid", "codigoHash":"hash-do-codigo", "tokenHash":null, "expiraEm":{"$date":"2026-11-11T12:10:00Z"}, "tentativas":0 }
```

### 3.19 `evolucoes` — `Evolucao`

Registro clínico append-only. A API permite criação e leitura por paciente, sem atualização ou exclusão física. Profissionais precisam de vínculo ativo com o paciente; administradores precisam informar o profissional responsável. `tipoEvolucao` é string extensível (por exemplo, `SESSAO`, `ACOLHIMENTO`, `DEVOLUTIVA`, `REAVALIACAO`, `CONTATO`), não enum fechado. As listas podem estar vazias. Registros são criados com `status=FINALIZADA` e `versao=1`; retificação ainda não possui endpoint.

| Campo | BSON / Java | Obrigatoriedade e regra |
|---|---|---|
| `_id` | ObjectId ou String / `String` | Gerado pelo Spring Data. |
| `pacienteId`, `profissionalId` | String / `String` | Obrigatórios na criação; referências textuais. |
| `consultaId`, `prontuarioId` | String ou null / `String` | Opcionais; quando informados, o serviço verifica existência e vínculo com paciente/profissional. |
| `tipoEvolucao`, `titulo`, `conteudo` | String / `String` | Obrigatórios na API; sem enum fechado. |
| `areaAtendimento`, `observacoes`, `conduta`, `retificadaPorId`, `motivoRetificacao` | String ou null / `String` | Opcionais. Campos de retificação estão reservados e não são preenchidos atualmente. |
| `dataAtendimento`, `criadoEm`, `atualizadoEm` | Date / `LocalDateTime` | `dataAtendimento` obrigatório; datas de persistência são definidas pelo servidor, sujeitas às notas de timezone da seção 2. |
| `objetivosTrabalhados`, `encaminhamentos` | Array de String / `List<String>` | Opcionais; vazios quando omitidos. |
| `status` | String / `String` | Atualmente `FINALIZADA` na criação. |
| `versao` | Int32 / `Integer` | Atualmente `1` na criação. |

```json
{ "_id":{"$oid":"671000000000000000000099"}, "pacienteId":"671000000000000000000004", "profissionalId":"671000000000000000000007", "consultaId":null, "prontuarioId":"671000000000000000000014", "tipoEvolucao":"SESSAO", "areaAtendimento":"Psicologia", "titulo":"Evolução de atendimento", "dataAtendimento":{"$date":"2026-11-11T13:00:00Z"}, "conteudo":"Conteúdo ilustrativo, sem dados pessoais.", "objetivosTrabalhados":[], "observacoes":null, "conduta":null, "encaminhamentos":[], "status":"FINALIZADA", "criadoEm":{"$date":"2026-11-11T13:00:00Z"}, "atualizadoEm":{"$date":"2026-11-11T13:00:00Z"}, "versao":1, "retificadaPorId":null, "motivoRetificacao":null }
```

## 4. Relacionamentos e integridade

Todos os campos abaixo são IDs String ordinários. O banco não aplica foreign keys nem cascade. Serviços fazem verificações de existência nos fluxos cobertos; a equipe deve validar referências órfãs antes de limpeza. Não converter IDs para DBRef/ObjectId em campos de relacionamento sem uma migração e alteração coordenada de código.

| Origem.campo | Destino | Cardinalidade/obrigatório | Comportamento |
|---|---|---|---|
| `usuarios.entidadeId` | coleção do perfil (`administradores`, `secretarias`, `profissionais`, `pacientes`) | Uma conta para entidade; requerido em contas normais | Login/perfil e services resolvem entidade por ID. |
| `profissionais.disponibilidadeIds[]` | `disponibilidades._id` | Profissional 0..N janelas | Sincronização mantém IDs; `Disponibilidade` não guarda volta para profissional. |
| `vinculosResponsaveis.pacienteId` | `pacientes._id` | N vínculos por paciente | Verifica paciente. |
| `vinculosResponsaveis.responsavelId` | `responsaveis._id` | N vínculos por responsável | Verifica responsável; remoção de responsável bloqueada se ainda relacionado. |
| `vinculos_profissional_paciente.pacienteId` | `pacientes._id` | N períodos por paciente | Valida referências; histórico encerrado é preservado. |
| `vinculos_profissional_paciente.profissionalId` | `profissionais._id` | N períodos por profissional | Vínculo ativo se `encerradoEm` nulo. |
| `consultas.pacienteId` | `pacientes._id` | N consultas por paciente, requerido | Serviço valida paciente e conflito independente de profissional. |
| `consultas.profissionalId` | `profissionais._id` | N consultas por profissional, requerido | Serviço valida profissional, conflito e disponibilidade. Histórico mantém profissional antigo. |
| `prontuarios.pacienteId` | `pacientes._id` | Um por paciente segundo service | `existsByPacienteId`; recomendável unique após auditoria. |
| `prontuarios.profissionalId` | `profissionais._id` | Um profissional atual do registro | Verificado em fluxos de escrita/acesso. |
| `prontuarios.consultaId` | `consultas._id` | Opcional | String id, pode ser nulo. |
| `documentos_clinicos`, `diagnosticos`, `tratamentos`, `exames`, `prescricoes` `.pacienteId` | `pacientes._id` | N por paciente, normalmente requerido | Serviços verificam paciente e autorizam profissional conforme módulo; clínicos arquivam logicamente. |
| Mesmos documentos clínicos `.profissionalId` | `profissionais._id` | N por profissional | Autoria/histórico; não reescrever ao trocar vínculo atual. |
| `preferencias_usuarios.email` | `usuarios.email` | Preferências 0..1 por usuário em intenção | Serviço busca por email case-insensitive; modelo não guarda usuárioId. |
| `recuperacoes_senha.email` | `usuarios.email` | Estado temporário por usuário | Busca case-insensitive e expiração validada pelo service. |

## 5. Índices

O código não declara índices de aplicação (`@Indexed`, `@CompoundIndex`) nem schema/index bootstrap. MongoDB cria automaticamente `_id_`. As recomendações abaixo são de desempenho/consistência; não crie sem primeiro verificar duplicatas e carga atual.

| Prioridade | Coleção e índice | Consulta justificadora | Único? / risco |
|---|---|---|---|
| Recomendado | `consultas {profissionalId:1, dataHora:1}` | agenda e listagem profissional por período | Não; verificar BSON Date homogêneo primeiro. |
| Recomendado | `consultas {pacienteId:1, dataHora:1}` | agenda/listagem paciente por período | Não. |
| Recomendado | `consultas {dataHora:1}` | listagem geral por período / agenda | Não; pode estar coberto parcialmente pelos compostos, mas prefixo muda. |
| Recomendado | `profissionais {disponibilidadeIds:1}` | lookup inverso `findByDisponibilidadeIdsContaining` | Não; campo array cria multikey. |
| Recomendado | `vinculos_profissional_paciente {pacienteId:1, encerradoEm:1, iniciadoEm:-1}` | vínculos ativos e histórico do paciente | Não; múltiplos profissionais ativos permitidos. |
| Recomendado | `vinculos_profissional_paciente {profissionalId:1, encerradoEm:1}` | pacientes ativos por profissional | Não. |
| Recomendado | `vinculosResponsaveis {pacienteId:1, responsavelId:1}` | listagem e verificação de duplicata | Candidato a único após deduplicação; unlink trabalha com par. |
| Recomendado | `vinculosResponsaveis {responsavelId:1}` | localizar vínculos antes de exclusão | Não. |
| Recomendado | `prontuarios {pacienteId:1}` | buscar/existir prontuário | Candidato a unique, pois service assume um por paciente; checar duplicatas antes. |
| Recomendado | `documentos_clinicos {pacienteId:1, dataSessao:-1}` | histórico cronológico | Não. |
| Recomendado | `evolucoes {pacienteId:1, dataAtendimento:-1}` | histórico de evoluções em ordem decrescente | Não. |
| Recomendado | `documentos_clinicos {pacienteId:1, secao:1, dataSessao:-1}` | histórico por seção | Não; collation/case insensitive deve alinhar serviço. |
| Recomendado | `documentos_clinicos {profissionalId:1, dataSessao:-1}` | documentos do profissional | Não. |
| Recomendado | `diagnosticos {pacienteId:1}`; `tratamentos {pacienteId:1}`; `exames {pacienteId:1}` | listagens por paciente | Não. |
| Recomendado | `prescricoes {pacienteId:1, criadaEm:-1}` e `{profissionalId:1, criadaEm:-1}` | listagens cronológicas | Não. |
| Recomendado | `preferencias_usuarios {email:1}` | busca case-insensitive | Possível único apenas após normalizar e deduplicar e alinhar collation; service não exige índice. |
| Recomendado | `recuperacoes_senha {email:1}` | busca da recuperação | Avaliar TTL em `expiraEm` somente após definir lifecycle e garantir expiração Instant; atualmente não há TTL declarado. Não excluir dados ativos via índice sem decisão do service. |
| Recomendado | `usuarios {email:1}` | login | Candidato a único, mas comparação case-insensitive varia no serviço; deduplicar e escolher collation/normalização. |
| Recomendado | `usuarios {entidadeId:1}` | lookup da conta pela entidade | Candidato a unique no domínio; verificar contas legadas/perfis. |
| Recomendado | perfis `{email:1}` e pacientes `{email:1}`; responsáveis `{email:1}`/`{cpf:1}` | validação de duplicidade/cadastros | Unique apenas após normalização/checagem; valores nulos/ausentes e case devem ser tratados com índices parciais/collation adequados. |

Índices simples por `_id` já existem implicitamente. A existência e efetividade dos demais índices no Atlas é **pendente**. Não adicionar índice temporal antes de confirmar tipo BSON de `consultas.dataHora`.

## 6. Incompatibilidades e migrações a investigar

Não há script/documento de migração Node.js encontrado no checkout; portanto, não é possível declarar a forma dos dados históricos. Antes de qualquer correção/migração:

1. Classificar o tipo BSON de cada `consultas.dataHora`: Date, String, null, ausente ou outro; contar tipos sem retornar conteúdo pessoal.
2. Para String ISO, verificar se contém `Z`, offset local, segundos e frações; interpretar cada variante segundo a timezone de origem, não apenas converter lexicalmente.
3. Detectar mistura de BSON Date/String no mesmo campo. Um range com Date pode não corresponder às strings e os tipos mistos podem causar ordenação/resultado inesperado.
4. Para `LocalDateTime` antigo, determinar se o valor representa hora local da clínica ou UTC convertido para texto. O código atual não guarda zona no valor.
5. Inspecionar `LocalDate` e `LocalTime` separadamente; converter “meia-noite”/data base sem zone pode mudar o dia/hora.
6. Comparar nomes de campos e histórico Node. Por exemplo, camelCase é esperado (`dataHora`, `pacienteId`); alias alternativo não foi encontrado no modelo Java.
7. IDs `_id` como ObjectId são legíveis por `String id`; referências guardadas como strings devem preservar exatamente a representação textual aceita pelo Spring. Não converter campos de referência para BSON ObjectId isoladamente.
8. Auditar campos ausentes/null: status, duração, listas, ativo e datas. Defaults Java não atualizam documentos antigos no banco automaticamente. Os modelos clínicos interpretam `ativo` nulo como true; `Usuario` primitivos ausentes viram false/0; tokenVersion nulo é tratado como 0.
9. Enumerar status desconhecidos: enum inválido pode falhar na leitura. Não normalizar sem entender semântica e histórico.
10. Verificar duplicidade de e-mail/CPF, vínculo, prontuário por paciente e perfil antes de propor unique indexes.

**Nenhuma migração foi executada ou especificada neste documento.** Recomenda-se preparar relatório de contagens/tipos e cópia de segurança aprovada antes de qualquer operação em dados reais.

## 7. Diagnóstico separado: HTTP 500 das consultas por período

Em reprodução anterior no serviço publicado, as três rotas com `dataInicio`/`dataFim` retornaram HTTP 500: consultas por profissional, por paciente e listagem geral. As mesmas rotas sem período retornaram HTTP 200 com lista vazia; login e permissões da Secretaria funcionaram. O corpo publicado foi genérico e não revelou a exceção. Não houve acesso aos logs privados do Render nem consulta autorizada ao Atlas nesta investigação; logo:

- Não se confirmou o tipo BSON real de `consultas.dataHora`.
- Não se confirmou incompatibilidade de `LocalDateTime` com os documentos históricos.
- Não se sabe se a falha ocorre ao construir/converter o critério, executar a consulta, ou materializar um documento retornado.
- A assinatura dos métodos e `LocalDateTime` no modelo são válidos em princípio; isso não prova compatibilidade dos dados armazenados.

Para comprovar a origem, equipe com acesso deve coletar no log privado do Render a stack trace da requisição correlacionada e tipo da exceção (sem dados pessoais), com logging temporário restrito de `org.springframework.data.mongodb.core`/driver. No Atlas, usar agregação somente de leitura `$type`/contagem por tipo de `dataHora`, verificar amostras redigidas e índices. Também reproduzir no mesmo ambiente: filtro `{dataHora: {$gte: ISODate(...), $lt: ISODate(...)}}` e leitura dos documentos correspondentes. Não habilitar logs de comandos com payload sensível em produção sem janela e controle de acesso.

### Avaliação do fallback atualmente no `ConsultaService`

A versão anterior capturava `RuntimeException` em torno da query temporal, então capturava não apenas erro de query/conversão, mas também erros de acesso a dados e outros bugs runtime. Depois chamava `findAll()` ou consulta não temporal por pessoa e filtrava em memória. Isso podia:

- mascarar a falha original e tornar a rota aparentemente saudável;
- disparar segunda falha quando conexão/Atlas está indisponível;
- carregar a coleção toda para lista geral, com custo e memória crescentes;
- continuar falhando na desserialização se o problema estiver em documentos retornados;
- não resolver tipo BSON que não corresponda a Date e ainda elevar custo de leitura.

O fallback amplo foi removido do código local: consultas temporais propagam a falha original, sem carregar a coleção inteira. Isso evita mascarar indisponibilidade e não comprova a causa raiz do HTTP 500. Obter stack trace e tipo BSON continua necessário; não liberar agendamento se qualquer verificação falhar.

## 8. Testes de integração recomendados

O `pom.xml` já inclui `spring-boot-starter-data-mongodb-test` no escopo test, mas não declara Testcontainers nem MongoDB embedded. Um teste de repositório deve usar MongoDB real da mesma linha compatível com produção (preferencialmente container efêmero/Testcontainers, se Docker CI/local estiver disponível), sem apontar para Atlas de produção.

Cobrir:

1. `save(Consulta LocalDateTime)` e verificar via leitura raw BSON que `dataHora` é BSON Date.
2. Ler a entidade salva e comparar o valor após precisão em milissegundos e a zona definida.
3. Consultar intervalos por geral, `pacienteId` e `profissionalId`.
4. Verificar limite inicial inclusivo e fim exclusivo.
5. Verificar comportamento de uma propriedade ausente/null e documentar explicitamente se não deve ser elegível em range.
6. Usar fixtures antigos de tipo String/misto para confirmar se falham na execução, não encontram documentos ou quebram materialização; nunca migrar esses fixtures no Atlas.
7. Garantir conversão idêntica para parâmetros e documentos sob `TZ`/timezone controlado.

Não foi adicionado Testcontainers porque não existe dependência declarada e a causa real ainda não foi confirmada. Uma dependência deve ser justificada pela estratégia de CI/Docker da equipe. Nenhum teste foi executado nesta etapa de documentação.

## 9. Verificação do Atlas e checklist da equipe de banco

### Estado desta auditoria

Não havia conexão de banco/credencial de leitura disponibilizada nesta tarefa; não consultei Atlas. A documentação descreve o esperado pelo código, não prova conformidade das coleções reais. Nomes de coleção e dados reais, índices, enums e BSON efetivo permanecem sujeitos a validação manual.

### Checklist somente leitura antes de propor mudança

- [ ] Confirmar banco/cluster e ambiente corretos sem compartilhar URI ou credenciais.
- [ ] Inventariar as 19 coleções declaradas e outras coleções desconhecidas separadamente.
- [ ] Para `consultas.dataHora`, contar BSON Date/String/null/ausente/outros; inspecionar mistura sem exportar PII.
- [ ] Confirmar timezone e representação de amostras de data/hora em cada collection/geração do sistema.
- [ ] Conferir IDs `_id` e referências string; contar referências órfãs.
- [ ] Conferir status de consulta e valores enum desconhecidos.
- [ ] Conferir ausências de campos/defaults, documentos clínicos arquivados e preservação de histórico.
- [ ] Conferir documentos duplicados antes de índices unique.
- [ ] Comparar índices atuais com a seção de recomendações e registrar explain plans para consultas de agenda.
- [ ] Obter stack trace privada do HTTP 500 e correlacioná-la com query, tipo de parâmetro e leitura dos resultados.
- [ ] Definir convenção de timezone e plano de migração/rollback antes de alterar documentos ou conversores.
- [ ] Realizar backup e aprovação explícita antes de qualquer escrita em dados reais.

## 10. Pendências confirmadas

1. Tipo BSON real dos campos e conformidade das 19 coleções no Atlas.
2. Stack trace e exceção original do HTTP 500 nos logs privados do Render.
3. Origem e timezone de dados legados eventualmente migrados de Node/Mongoose.
4. Convenção temporal explícita e compatível com os valores históricos.
5. Índices existentes e possíveis duplicidades antes de índices unique.
6. Definição funcional de valores `secao`/tipos para laudos, atestados, encaminhamentos, acolhimento e avaliações: atualmente são tratados genericamente por `DocumentoClinico`, sem enum/coleção específica. A tela de prontuário existente já consulta e cadastra registros próprios de `Evolucao`; documentos clínicos legados continuam carregados separadamente e não são convertidos automaticamente.
