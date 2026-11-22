# Modelo definitivo do MongoDB para NeuroVita

**Decisão de arquitetura:** neste trabalho, o backend Java da branch `develop` é a fonte da verdade. A equipe do banco deve adequar as coleções ao código existente; não se deve mudar classes Java, DTOs ou contratos REST para acomodar um desenho diferente do banco.

**Compatibilidade do formulário FBR de paciente:** a referência atual apresenta endereço separado em CEP, cidade, logradouro/endereço, número, complemento e bairro. O contrato de aplicação preserva os campos existentes `cep` e `cidade` e acrescenta `numero`, `complemento` e `bairro`, todos opcionais em documentos legados. A equipe do banco deve permitir esses três campos string opcionais na coleção existente `pacientes`; não é necessária coleção nova, migração nem backfill. O cadastro pela API exige CEP válido, número e bairro; atualização parcial mantém os campos omitidos e texto vazio explícito limpa campos opcionais (CEP continua sujeito à validação do formato). Registrar em [discrepancias banco.md](../discrepancias%20banco.md).

Este documento orienta a preparação de ambientes vazios e a conferência do Atlas. O esquema campo a campo e os exemplos BSON estão em [CONTRATO_DADOS_MONGODB.md](CONTRATO_DADOS_MONGODB.md). Não foram consultados nem alterados dados Atlas nesta tarefa.

### Conciliação com a orientação encaminhada à equipe do banco

A orientação DOCX está parcialmente alinhada ao código, mas precisa separar o que já é persistido pelo backend do que deve ser preparado para as próximas versões. O plano de banco solicitado considera **24 coleções: 19 usadas pelo backend atual e cinco planejadas para módulos futuros**. `evolucoes` já é uma coleção atual, portanto deve sair do grupo futuro; a contagem correta é **19 atuais + 5 futuras = 24**.

As cinco coleções `avaliacoes_clinicas`, `arquivos_clinicos`, `auditoria_eventos`, `unidades_atendimento` e `salas` não possuem modelos persistentes nem APIs correspondentes no backend atual. Elas devem constar no modelo definitivo e podem ser provisionadas vazias, com estrutura preparada pela equipe do banco, para reduzir ajustes em futuras entregas. Sua presença **não significa que as funcionalidades estejam disponíveis**: até haver módulos Java/API, avaliações textuais e documentos atuais continuam usando `documentos_clinicos`, anexos permanecem nas estruturas existentes e `consultas.local` continua texto. Para a futura associação de local, prever `consultas.unidadeId` e `consultas.salaId` opcionais como IDs String; o backend atual não os lê nem persiste. Não aplicar validators rígidos nem inserir dados operacionais nessas coleções antes da implementação e validação dos respectivos contratos.

Outros ajustes necessários no contrato são: incluir `exames.descricaoSolicitacao` como campo opcional; tratar `administradores.cpf` como opcional; e usar para `evolucoes` todos os campos da classe Java atual, não o schema resumido da proposta. A orientação recomenda `America/Sao_Paulo`, mas o backend não fixa esse timezone nos conversores de `LocalDate`, `LocalTime` ou `LocalDateTime`; a equipe Java e a equipe do banco precisam acordar a convenção antes de validar intervalos ou regravar dados temporais. Nenhuma dessas diferenças justifica alterar dados ou estruturas diretamente no Atlas nesta etapa.

## 1. Decisão sobre a proposta recebida

O texto de proposta contém recomendações úteis, mas não representa o contrato atualmente consumido pelo Java. Aplicá-lo diretamente criaria incompatibilidades. Para manter o sistema funcional sem alterações de código, **não adotar** nesta etapa:

| Proposta do texto | Contrato atual do Java | Consequência se aplicado sem mudança de código |
|---|---|---|
| `LocalDate` como String `YYYY-MM-DD` | `LocalDate` convertido para BSON Date | Leitura/conversão incompatível. |
| `LocalTime` como String `HH:mm:ss` | `LocalTime` convertido para BSON Date | `horaInicio`/`horaFim` não desserializam como o esperado. |
| Eventos como `Instant` e conversão UTC explícita em geral | Diversos eventos são `LocalDateTime`; apenas alguns campos usam `Instant` | A conversão, comparação de intervalos e DTOs deixariam de corresponder. |
| `disponibilidades.profissionalId` | Profissional guarda `disponibilidadeIds[]`; disponibilidade não tem `profissionalId` | Consulta e sincronização Angular/Java não encontrariam as referências esperadas. |
| `consultas.duracaoMinutos`, `responsavelId` | Campo atual é `duracao`; não há `responsavelId` | O Java ignora esses campos e não aplica regras associadas. `unidadeId` e `salaId` foram separados como campos futuros opcionais no plano MongoDB; o Java atual ainda não os persiste. |
| `preferencias_usuarios.usuarioId` | Busca atual usa `email` | Preferências não seriam localizadas pelo serviço. |
| `recuperacoes_senha.usuarioId` | Fluxo atual persiste e busca por `email` | Recuperação de conta incompatível. |
| `administradores.cpf` obrigatório | DTO, serviço e modelo Java permitem CPF ausente; somente nome/e-mail são exigidos pelo serviço | Um validator que exija CPF pode rejeitar cadastros válidos. Manter o campo opcional. |
| Ausência de `exames.descricaoSolicitacao` | Campo opcional usado pelo contrato atual para separar a solicitação do resultado | Um validator que rejeite campos extras ou não reconheça o campo impede persistir a solicitação atualizada. Permitir o campo sem exigir backfill. |
| Coleções futuras `avaliacoes_clinicas`, `arquivos_clinicos`, `auditoria_eventos`, `unidades_atendimento`, `salas` | Não há modelos `@Document`, repositories ou services correspondentes no código atual | Manter no plano do banco e provisionar como estrutura futura conforme esta orientação; elas não são usadas pela API atual. A coleção `evolucoes` já existe no backend e integra as 19 atuais. |
| Campos adicionais como `criadoEm`/`atualizadoEm` em entidades onde o modelo não os define | Ausentes nos modelos correspondentes | Não serão persistidos nem atualizados pelo backend atual. |

As estruturas futuras podem ser provisionadas para reduzir mudanças posteriores, mas exigem desenvolvimento Java, integração de telas, autorização e testes antes de integrarem o contrato operacional da aplicação. A existência de uma tela protótipo no MVP, isoladamente, não torna um campo parte do modelo Java atual.

## 2. Coleções do plano definitivo

O código contém 19 modelos `@Document`. Estas são as coleções ativas no backend atual:

1. `usuarios`
2. `administradores`
3. `secretarias`
4. `pacientes`
5. `responsaveis`
6. `vinculosResponsaveis` — preservar exatamente o camelCase
7. `profissionais`
8. `vinculos_profissional_paciente`
9. `disponibilidades`
10. `consultas`
11. `prontuarios`
12. `documentos_clinicos`
13. `evolucoes`
14. `diagnosticos`
15. `tratamentos`
16. `exames`
17. `prescricoes`
18. `preferencias_usuarios`
19. `recuperacoes_senha`

O Spring Data pode criar coleções ativas na primeira gravação. A equipe pode provisionar as 19 manualmente ou deixá-las ser criadas pela aplicação; usar os nomes acima, manter `_id` padrão e não inserir documentos inventados como seed.

O modelo definitivo também prevê cinco coleções futuras, cujos schemas preliminares constam da orientação de banco recebida: `avaliacoes_clinicas`, `arquivos_clinicos`, `auditoria_eventos`, `unidades_atendimento` e `salas`. A pedido do responsável pelo projeto, a equipe do banco pode provisioná-las antecipadamente, vazias, como preparação estrutural. Elas ainda não possuem `@Document`, repositories, services, controllers ou operações Angular; portanto não devem ser anunciadas como funcionalidades operacionais. A equipe do banco deve confirmar os campos e tipos preliminares antes de aplicar validators. São **24 coleções planejadas**, mas somente **19 integradas ao backend atual**.

## 3. Tipos e nomes que devem ser respeitados

- Os nomes de campos são os nomes Java em camelCase descritos no contrato associado. Não converter para snake_case nem renomear campos.
- `String` é BSON String; `Integer` é Int32; `Long` é Int64; Boolean é BSON Boolean; listas são arrays e classes internas são subdocumentos.
- `@Id String` é compatível com o `_id` gerado como ObjectId pelo Spring Data. Campos de relacionamento são String simples contendo a representação textual do ID, e não DBRef. Não trocar referências para ObjectId BSON.
- Enums são strings com valores exatos; `StatusConsulta`: `PENDENTE`, `CONFIRMADA`, `REMARCADA`, `CANCELADA`.
- Defaults como `ativo=true`, `status=PENDENTE` e listas vazias são aplicados ao instanciar novos objetos Java; não corrigem nem completam documentos já existentes automaticamente.
- Campos opcionais podem ser omitidos ou nulos, salvo quando o fluxo de negócio exige valor. Não impor required indiscriminadamente via `$jsonSchema`: campos legados, patches e fluxos diferentes podem depender de ausência/null.

## 4. Datas e horário — preservar os tipos usados pelo backend

Para esta adequação, os formatos BSON são os do mapeamento Spring Data atual, detalhados na tabela temporal do [contrato de dados](CONTRATO_DADOS_MONGODB.md):

- `Instant` → BSON Date como instante UTC.
- `LocalDateTime`, `LocalDate` e `LocalTime` → BSON Date via conversores JSR-310 atuais, dependentes do timezone da JVM.
- JSON da API continua usando valores ISO compatíveis com os tipos Java, por exemplo `LocalDate` `YYYY-MM-DD`, `LocalTime` `HH:mm:ss` e `LocalDateTime` sem offset `YYYY-MM-DDTHH:mm:ss`. O formato JSON **não** significa que o BSON seja String.
- Não gravar datas como String no Atlas somente porque a API as recebe como ISO String.

Há uma limitação importante: o código não fixa timezone e usa `LocalDateTime.now()` em vários serviços. Assim, a representação de `LocalDateTime`/`LocalDate`/`LocalTime` em BSON depende do timezone do processo Java. A equipe do banco deve preservar o formato BSON Date; a zona de conversão precisa ser alinhada com o responsável do backend antes de carga ou regravação temporal. Não aplicar deslocamento de horas nem migração de data sem identificar a zona de origem.

Nas consultas de agenda, `consultas.dataHora` deve ser BSON Date homogêneo. Os repositories comparam intervalo por `dataHora >= início` e `dataHora < fim`; strings não são equivalentes a BSON Date em consultas de faixa.

## 5. Relacionamentos a preservar

As referências são campos String, validados pela aplicação. Não criar foreign keys, DBRefs ou objetos aninhados em substituição às referências atuais.

| Coleção/campo | Referência |
|---|---|
| `usuarios.entidadeId` | ID string do cadastro correspondente a `perfil`. |
| `profissionais.disponibilidadeIds[]` | IDs de `disponibilidades._id`; essa é a direção atual do relacionamento. |
| `vinculosResponsaveis.pacienteId`, `.responsavelId` | `pacientes` e `responsaveis`. |
| `vinculos_profissional_paciente.pacienteId`, `.profissionalId` | `pacientes` e `profissionais`; `encerradoEm=null` indica vínculo ativo, manter históricos encerrados. |
| `consultas.pacienteId`, `.profissionalId` | `pacientes` e `profissionais`. |
| `prontuarios.pacienteId`, `.profissionalId`, `.consultaId` | `pacientes`, `profissionais`, opcionalmente `consultas`. |
| `evolucoes.prontuarioId` | `prontuarios` (opcional). |
| `documentos_clinicos`, `diagnosticos`, `tratamentos`, `exames`, `prescricoes` `.pacienteId`/`.profissionalId` | `pacientes` e `profissionais`. |
| `preferencias_usuarios.email`, `recuperacoes_senha.email` | Localização do usuário pelo e-mail no fluxo atual; não alterar para `usuarioId` sem alteração do backend. |

O backend valida existência em fluxos específicos, mas MongoDB não impõe integridade referencial. Antes de recriar qualquer banco com dados, gerar inventário de órfãos e aprovar seu tratamento.

## 6. Índices e validação

O código não declara índices de aplicação. O MongoDB mantém o índice `_id_`; os demais são recomendações de desempenho descritas no contrato de dados. Antes de criar índices únicos:

1. contar duplicados de e-mail/CPF e de pares de vínculo;
2. definir normalização e collation igual à comparação feita pelo Java;
3. decidir índices parciais para campos opcionais;
4. para vínculo profissional, permitir diversos profissionais ativos quando as regras do domínio permitirem;
5. não usar índice único `profissionalId + dataHora` como substituto da validação de sobreposição de duração.

O Java valida regras de negócio; não há `$jsonSchema` configurado ou contrato de validator versionado no projeto. Não aplicar validators estritos nas coleções antes de testar os requests existentes e documentos opcionais/legados. Este documento não inclui scripts de índices ou validators para execução direta porque a estrutura precisa ser validada em ambiente de desenvolvimento primeiro.

## 7. Ambientes e URI

Manter desenvolvimento, testes, homologação e produção separados por database/credencial conforme configuração já aprovada pelo projeto. Nomes sugeridos no texto colado são propostas, não foram confirmados no código nem na configuração externa. O `application.properties` versionado contém configuração por propriedade; confirmar com a equipe de deploy qual variável do Render é mapeada para a versão atual do Spring Boot. Não compartilhar URI, usuário, senha ou token em tickets/documentos.

Nunca apontar testes automatizados para produção. Não usar cópia de produção para popular desenvolvimento sem anonimização e autorização. O banco de desenvolvimento pode ser recriado somente após confirmar cluster/database alvo e autorização da equipe responsável; esta orientação não executou nem autoriza um `dropDatabase`.

## 8. Sequência segura para a equipe de banco

1. Confirmar por canal seguro o cluster e o nome de cada database de ambiente; conferir URI sem registrá-la em documentação.
2. Começar por um database de desenvolvimento vazio, separado dos demais.
3. Preparar as 24 coleções do modelo definitivo: 19 usadas pelo backend (ou criadas no primeiro save do Spring) e cinco coleções futuras vazias, sem presumir que estas últimas tenham APIs operacionais.
4. Aplicar nomes/tipos BSON segundo `CONTRATO_DADOS_MONGODB.md`; guardar IDs de referência como strings.
5. Usar apenas documentos sintéticos de teste e os endpoints da aplicação para semear cadastros; não montar usuários com senha manual em claro.
6. Confirmar persistência e leitura de um documento de cada módulo por meio da API.
7. Confirmar no Compass/Atlas que `consultas.dataHora` é BSON Date e que as três consultas de intervalo usam datas compatíveis.
8. Comparar tipos BSON gerados pelos endpoints com os exemplos do contrato; registrar diferenças sem alterar produção.
9. Só então revisar índices recomendados, medir consultas e criar índices aprovados.
10. Replicar configuração somente após validação e plano de rollback específico por ambiente.

## 9. Cobertura do MVP FBR visível no PDF

O PDF apresenta protótipos e fluxos de interface. A matriz abaixo compara o que é visível no material com as estruturas Java atuais. Ela não transforma, por si só, protótipos em novos campos obrigatórios.

| Área demonstrada no MVP | Suporte atual no modelo Java | Cobertura real |
|---|---|---|
| Login, recuperação de senha e perfis de usuário | `usuarios`, `recuperacoes_senha`, `administradores`, `secretarias`, `profissionais`, `pacientes` | Implementado para os perfis e fluxos presentes no código. |
| Cadastro e consulta de pacientes, profissionais e secretarias | Coleções homônimas | Implementado, sujeito aos campos efetivamente existentes nos modelos. |
| Agendamento, agenda e disponibilidade | `consultas`, `disponibilidades`, `profissionais.disponibilidadeIds` | Implementado com janelas semanais, conflitos e histórico; não usar um relacionamento alternativo `disponibilidades.profissionalId` sem mudar o código. |
| Modalidade e local da consulta | `Consulta.modalidade`, `Consulta.local`; `unidadeId`/`salaId` planejados apenas no banco | Hoje a API persiste campos textuais genéricos e não relaciona unidade/sala. O schema futuro pode aceitar IDs opcionais, mas dependem da implementação Java e integração da tela. |
| Seleção e histórico de responsável | `responsaveis`, `vinculosResponsaveis` | Cadastro e vínculo existem; `Consulta` não grava qual responsável foi selecionado no agendamento e o vínculo de responsável atual não possui encerramento/histórico. |
| Prontuário e histórico clínico | `prontuarios`, `consultas`, `documentos_clinicos`, `evolucoes` | Cobertura básica com evolução clínica estruturada, autoria e registro preservado; não há edição/retificação de evolução nesta versão nem prontuário multidisciplinar completo. |
| Evoluções clínicas | Coleção `evolucoes` (`Evolucao`) e API `/api/evolucoes` | Criação finalizada e listagem por paciente, protegidas por vínculo ativo para profissionais. Não há edição/retificação nesta versão. |
| Devolutivas, rastreio, acolhimento e evolução social mostrados nas páginas clínicas | Campo `DocumentoClinico.secao`/`conteudo` pode guardar texto genérico | API de documentos permite criar, consultar por paciente/seção, editar e arquivar conteúdo genérico. Não há modelos nem regras de processo específicas para esses fluxos. |
| Laudos, atestados psicológicos, declarações, encaminhamentos, avaliações psicológicas e relatórios mostrados no protótipo | `documentos_clinicos` com `secao`, `titulo`, `dataSessao`, `tipoAtendimento`, `conteudo`, `anexos` | O backend persiste o conteúdo textual editável nas operações genéricas de documento. Não há categoria fechada, campos clínicos próprios, geração/assinatura de documento formal ou versionamento completo por tipo. |
| Diagnósticos, tratamentos, exames e prescrições | Coleções homônimas | Há persistência própria e regras presentes no código; anexos e histórico seguem estruturas existentes, não um serviço geral de arquivo externo. |
| Conteúdo da solicitação de exame (exames solicitados, justificativa clínica e CID) | `exames.descricaoSolicitacao` | Campo opcional novo no contrato da aplicação. A equipe responsável pelo banco deve incluí-lo no schema/validator utilizado; não é necessário backfill e documentos antigos podem permanecer sem ele. |
| Preferências/configurações | `preferencias_usuarios` | Persistidas e localizadas por e-mail, conforme contrato atual; não trocar para `usuarioId` sem alterar serviço Java. |
| Opção visual de autenticação em duas etapas | Nenhum campo, endpoint ou fluxo correspondente | O PDF atualizado mostra somente o controle para ativar 2FA. Fator, ativação/verificação, recuperação e fluxo de login não foram definidos; implementação depende de decisão funcional. |
| Upload/visualização de documentos | `documentos_clinicos.anexos` e anexos de exame | O modelo atual armazena conteúdo Base64 embutido com limites aplicados no serviço; armazenamento externo de arquivos não está implementado. |
| Salas, unidades, auditoria clínica append-only e notificações persistentes | Cinco coleções planejadas no banco; sem modelos/APIs correspondentes no código atual | As coleções de avaliações, arquivos, auditoria, unidades e salas podem ser preparadas vazias, sem habilitar operações. Notificações persistentes não estão no plano de 24 coleções. |

**Conclusão da comparação:** o contrato MongoDB descrito aqui e em `CONTRATO_DADOS_MONGODB.md` representa o backend e suas lacunas conhecidas. Para a solicitação de exame descrita no PDF atualizado, o projeto agora persiste o texto clínico em `exames.descricaoSolicitacao`. A equipe responsável pelo banco deve adicionar esse campo opcional ao schema/validator que utiliza. Não é necessário backfill, e não foi feita alteração direta no banco.

## 10. O que este documento não conclui

- Não confirma que o Atlas atual tem as 19 coleções, os tipos descritos ou índices recomendados.
- Não identifica a exceção original do HTTP 500 de consultas por período. No ambiente publicado as rotas filtradas falharam com 500, mas sem logs privados e inspeção BSON não é possível provar se o problema é tipo armazenado, conversão, execução ou leitura.
- Não declara os módulos adicionais do texto recebido (avaliações, armazenamento externo, auditoria, unidades/salas) implementados. Suas coleções podem ser preparadas vazias para evolução futura. Evoluções possuem modelo, repositório, service e API próprios no backend atual. Notificações persistentes não foram incluídas entre as cinco coleções do plano.
- Não substitui aprovação do contrato pelo responsável do backend e da equipe que opera Atlas.

## 11. Checklist de aceite do database de desenvolvimento

- [ ] Branch/código usado como referência é `develop` e versão implantada corresponde ao checkout aprovado.
- [ ] Database de desenvolvimento separado, identificado e sem apontamento para produção.
- [ ] Nomes das coleções conferidos (incluindo `vinculosResponsaveis`).
- [ ] Campos e tipos BSON comparados com o contrato atual, não com a proposta incompatível.
- [ ] IDs de relações são strings e todas as referências sintéticas resolvem.
- [ ] `consultas.dataHora` e demais datas/horas salvas pela aplicação foram inspecionadas como BSON Date.
- [ ] Datas não foram transformadas de BSON Date em String.
- [ ] Não há seed com credenciais em claro nem dados pessoais reais.
- [ ] API persiste e lê entidades sem mudança de código.
- [ ] Range de agenda avaliado com datas de limite; falha 500 investigada por stack trace, não mascarada por suposição.
- [ ] Índices únicos só foram propostos após análise de duplicidade e decisão de collation.
- [ ] Nenhuma alteração foi replicada a homologação/produção sem aprovação e plano de rollback.
