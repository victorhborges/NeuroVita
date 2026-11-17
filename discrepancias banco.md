# Discrepâncias e compatibilidades do contrato MongoDB

Atualizado em 17/11/2026. Documento para a equipe responsável pelo banco. Nenhuma operação foi executada no MongoDB Atlas.

## `pacientes`: endereço estruturado do formulário FBR

A referência visual do MVP (PDF `NEUROVITA (fbr atual).pdf`, p. 94) apresenta CEP, cidade, endereço/logradouro, número, complemento e bairro. A coleção/modelo `Paciente` já possuía `cep`, `cidade` e `endereco`, mas não possuía `numero`, `complemento` ou `bairro`. O modelo e o contrato da aplicação Java passaram a aceitar e devolver os três campos faltantes.

**Ajuste solicitado à equipe do banco:** permitir `numero`, `complemento` e `bairro` como campos string opcionais em documentos de `pacientes`. Não criar coleção. Não exigir esses campos em documentos legados, não realizar backfill e não excluir ou reformatar o `endereco` existente. Se houver validator que rejeite campos desconhecidos, incluir esses três campos como opcionais.

O backend exige CEP com oito dígitos, número e bairro no cadastro iniciado pelo formulário FBR. Nas atualizações, campo omitido preserva o valor existente; texto vazio explícito limpa os campos opcionais. CEP continua validado pelo formato da API. Essa diferença de operação é regra da API, não validator rígido de todos os documentos existentes.

## Referências

- [Contrato de dados](docs/CONTRATO_DADOS_MONGODB.md), seção `pacientes`.
- [Modelo definitivo](docs/MODELO_DEFINITIVO_MONGODB.md), nota de compatibilidade do formulário FBR.
- [Referência funcional e visual FBR](docs/MVP-FBR-REFERENCIA.md), cadastro de paciente.
