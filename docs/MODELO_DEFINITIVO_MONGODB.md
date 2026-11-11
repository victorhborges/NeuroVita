# Orientação de adequação do MongoDB Atlas ao NeuroVita

**Decisão de arquitetura:** neste trabalho, o backend Java da branch `develop` é a fonte da verdade. A equipe do banco deve adequar as coleções ao código existente; não se deve mudar classes Java, DTOs ou contratos REST para acomodar um desenho diferente do banco.

Este documento orienta a preparação de ambientes vazios e a conferência do Atlas. O esquema campo a campo e os exemplos BSON estão em [CONTRATO_DADOS_MONGODB.md](CONTRATO_DADOS_MONGODB.md). Não foram consultados nem alterados dados Atlas nesta tarefa.

## 1. Decisão sobre a proposta recebida

O texto de proposta contém recomendações úteis, mas não representa o contrato atualmente consumido pelo Java. Aplicá-lo diretamente criaria incompatibilidades. Para manter o sistema funcional sem alterações de código, **não adotar** nesta etapa:

| Proposta do texto | Contrato atual do Java | Consequência se aplicado sem mudança de código |
|---|---|---|
| `LocalDate` como String `YYYY-MM-DD` | `LocalDate` convertido para BSON Date | Leitura/conversão incompatível. |
| `LocalTime` como String `HH:mm:ss` | `LocalTime` convertido para BSON Date | `horaInicio`/`horaFim` não desserializam como o esperado. |
| Eventos como `Instant` e conversão UTC explícita em geral | Diversos eventos são `LocalDateTime`; apenas alguns campos usam `Instant` | A conversão, comparação de intervalos e DTOs deixariam de corresponder. |
| `disponibilidades.profissionalId` | Profissional guarda `disponibilidadeIds[]`; disponibilidade não tem `profissionalId` | Consulta e sincronização Angular/Java não encontrariam as referências esperadas. |
| `consultas.duracaoMinutos`, `responsavelId`, `unidadeId`, `salaId` | Campo atual é `duracao`; não há os demais campos | O Java ignora os novos campos e não aplica suas regras. |
| `preferencias_usuarios.usuarioId` | Busca atual usa `email` | Preferências não seriam localizadas pelo serviço. |
| `recuperacoes_senha.usuarioId` | Fluxo atual persiste e busca por `email` | Recuperação de conta incompatível. |
| Novas coleções `evolucoes`, `avaliacoes_clinicas`, `arquivos_clinicos`, `auditoria_eventos`, `unidades_atendimento`, `salas` | Não há modelos `@Document`, repositories ou services correspondentes no código atual | Coleções vazias não ativam funcionalidades; API não as utiliza. |
| Campos adicionais como `criadoEm`/`atualizadoEm` em entidades onde o modelo não os define | Ausentes nos modelos correspondentes | Não serão persistidos nem atualizados pelo backend atual. |

Essas podem ser evoluções futuras, mas exigem desenvolvimento Java, integração de telas, autorização e testes antes de serem parte do contrato do banco. A existência de uma tela protótipo no MVP, isoladamente, não torna um campo parte do modelo implementado.

## 2. Coleções a provisionar

O código contém 18 modelos `@Document`. São as coleções exatas:

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
13. `diagnosticos`
14. `tratamentos`
15. `exames`
16. `prescricoes`
17. `preferencias_usuarios`
18. `recuperacoes_senha`

O Spring Data pode criar uma coleção na primeira gravação. Não é obrigatório criar previamente coleções vazias. Se a equipe optar por provisioná-las manualmente, usar os nomes acima, manter MongoDB padrão de `_id` e não inserir documentos inventados como seed. Nenhuma outra coleção está ligada a um modelo persistente nesta versão.

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
3. Criar as 18 coleções listadas (ou deixar Spring criá-las no primeiro save), sem as coleções propostas que ainda não existem no Java.
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
| Modalidade e local da consulta | `Consulta.modalidade`, `Consulta.local` | Campos textuais genéricos; não há entidades de unidade ou sala. O protótipo que mostra sala/unidade não está coberto como relacionamento de dados. |
| Seleção e histórico de responsável | `responsaveis`, `vinculosResponsaveis` | Cadastro e vínculo existem; `Consulta` não grava qual responsável foi selecionado no agendamento e o vínculo de responsável atual não possui encerramento/histórico. |
| Prontuário e histórico clínico | `prontuarios`, `consultas`, `documentos_clinicos` | Cobertura básica. A estrutura não é ainda um prontuário multidisciplinar com tipos e autoria independentes para cada evolução. |
| Devolutivas, rastreio, acolhimento e evolução social mostrados nas páginas clínicas | Campo `DocumentoClinico.secao`/`conteudo` pode guardar texto genérico | Persistência genérica possível, mas não há modelos nem regras específicas para esses processos. Não afirmar como fluxos estruturados concluídos. |
| Laudos, atestados psicológicos, declarações e relatórios mostrados no protótipo | `documentos_clinicos` com `secao`, `titulo`, `dataSessao`, `tipoAtendimento`, `conteudo`, `anexos` | Pode representar documento em termos genéricos; não há categoria fechada, campos próprios, emissão formal ou versionamento completo de cada tipo. |
| Diagnósticos, tratamentos, exames e prescrições | Coleções homônimas | Há persistência própria e regras presentes no código; anexos e histórico seguem estruturas existentes, não um serviço geral de arquivo externo. |
| Preferências/configurações | `preferencias_usuarios` | Persistidas e localizadas por e-mail, conforme contrato atual; não trocar para `usuarioId` sem alterar serviço Java. |
| Upload/visualização de documentos | `documentos_clinicos.anexos` e anexos de exame | O modelo atual armazena conteúdo Base64 embutido com limites aplicados no serviço; armazenamento externo de arquivos não está implementado. |
| Salas, unidades, auditoria clínica append-only e notificações persistentes | Nenhum modelo/coleção correspondente no código atual | Não coberto. Não criar essas coleções vazias como se habilitassem a funcionalidade. |

**Conclusão da comparação:** o contrato MongoDB descrito aqui e em `CONTRATO_DADOS_MONGODB.md` é compatível com o backend atual e considera o MVP ao registrar seus módulos e lacunas. Ele **não representa integralmente todos os fluxos visuais do PDF como funcionalidades persistidas e estruturadas**. Para a orientação desta etapa, o banco deve continuar seguindo o código. Se o responsável funcional decidir que campos/fluxos do protótipo são obrigatórios além do backend atual, isso será uma decisão de escopo e precisará vir acompanhada da implementação correspondente antes de alterar o banco.

## 10. O que este documento não conclui

- Não confirma que o Atlas atual tem as 18 coleções, os tipos descritos ou índices recomendados.
- Não identifica a exceção original do HTTP 500 de consultas por período. No ambiente publicado as rotas filtradas falharam com 500, mas sem logs privados e inspeção BSON não é possível provar se o problema é tipo armazenado, conversão, execução ou leitura.
- Não declara os módulos adicionais do texto colado (evoluções independentes, avaliações, armazenamento externo, auditoria, unidades/salas ou notificações) implementados.
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
