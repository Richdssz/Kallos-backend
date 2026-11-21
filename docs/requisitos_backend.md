# Documento de Requisitos de Back-end

## Sistema de Gestão para Consultório Psicológico — Kallos Psi

| Campo | Informação |
|---|---|
| **Projeto** | Kallos Psi |
| **Documento** | Requisitos de back-end |
| **Versão** | 1.0 |
| **Data** | 19 de novembro de 2026 |
| **Equipe** | Ruan Ricardo, Richard Silva, Matheus Alves e Micael Barros |

> Versão em Markdown organizada a partir do documento oficial de requisitos de back-end.

## Sumário

1. [Introdução](#1-introdução)
2. [Atendimento às exigências do projeto](#2-atendimento-às-exigências-do-projeto)
3. [Equipe e entidades](#3-equipe-e-entidades)
4. [Arquitetura e diretrizes](#4-arquitetura-e-diretrizes)
5. [Princípios SOLID e padrões GRASP](#5-princípios-solid-e-padrões-grasp)
6. [Regras de negócio](#6-regras-de-negócio)
7. [Requisitos funcionais por entidade](#7-requisitos-funcionais-por-entidade)
8. [Requisitos não funcionais](#8-requisitos-não-funcionais-de-back-end)

---

## 1. Introdução

### 1.1 Objetivo

Este documento especifica os requisitos do back-end do Kallos Psi, sistema de gestão para consultório psicológico. Ele define a API REST, as regras de negócio e a estrutura do código, servindo como referência para o desenvolvimento e a avaliação do projeto.

### 1.2 Escopo

O documento cobre as 8 entidades do banco de dados, a arquitetura em camadas, o padrão da API, a autenticação, a persistência e a documentação com Swagger. As telas e a experiência de uso são tratadas em documento próprio de front-end.

### 1.3 Tecnologias de back-end

| Tecnologia | Uso no projeto |
|---|---|
| Java e Spring Boot | Linguagem e framework da API REST e das regras de negócio. |
| Spring Data JPA | Mapeamento das entidades e acesso ao banco por repositórios. |
| PostgreSQL (Supabase) | Banco relacional persistente hospedado no Supabase. |
| Supabase Auth | Autenticação por e-mail e senha; emite o token JWT validado pela API. |
| Bean Validation | Validação dos dados recebidos pelos DTOs. |
| Swagger (`springdoc-openapi`) | Documentação interativa e testes dos endpoints. |
| Render | Hospedagem da API. |

## 2. Atendimento às exigências do projeto

| Exigência | Como é atendida | Referência |
|---|---|---|
| SOLID e GRASP | Justificativa de 4 princípios SOLID (S, O, I e D) e 4 padrões GRASP (Especialista na Informação, Controlador, Criador e Baixo Acoplamento). | Seção 5 |
| Entidades modeladas com JPA | As 8 tabelas são mapeadas como entidades `@Entity`. | Seções 4.1 e 7 |
| Repositórios para acesso ao banco | Um repositório Spring Data JPA por entidade. | Seção 4.1 |
| Serviços com regras de negócio | Cada entidade possui um serviço que concentra regras como CRP, CPF, conflito de horário, vínculo e imutabilidade. | Seções 4.1 e 6 |
| Controladores REST | Um controlador REST por entidade, sob o prefixo `/api`. | Seções 4.3 e 7 |
| Cadastro, consulta, atualização e exclusão | CRUD por entidade; exclusão lógica ou restrita quando o histórico deve ser preservado. | Seção 3.3 |
| Integração com banco de dados | Conexão da API ao PostgreSQL do Supabase. | Seção 4.6 |
| Banco persistente, sem H2 | PostgreSQL no Supabase em todos os ambientes; nenhum banco em memória. | Seção 4.6 |
| Pelo menos um DTO | DTOs de entrada e saída em todas as entidades, como `PacienteRequestDTO`. | Seção 4.4 |
| Organização adequada dos pacotes | Pacotes por camada, com responsabilidade única. | Seção 4.2 |
| Swagger | Swagger UI com todos os endpoints documentados e suporte a token JWT. | Seção 4.7 |

## 3. Equipe e entidades

### 3.1 Responsabilidades

| Integrante | Entidades sob responsabilidade |
|---|---|
| Ruan Ricardo | Usuários e Psicólogos |
| Richard Silva | Pacientes e Sessões |
| Matheus Alves | Consultório e Evoluções |
| Micael Barros | Prontuários e Abordagem |

### 3.2 Mapa de entidades

| Entidade | Tabela | Rota base | Classe principal | Responsável |
|---|---|---|---|---|
| Usuários | `usuarios` | `/api/usuarios` | `Usuario` | Ruan Ricardo |
| Psicólogos | `psicologos` | `/api/psicologos` | `Psicologo` | Ruan Ricardo |
| Pacientes | `pacientes` | `/api/pacientes` | `Paciente` | Richard Silva |
| Sessões | `sessoes` | `/api/sessoes` | `Sessao` | Richard Silva |
| Consultório | `consultorio` | `/api/consultorio` | `Consultorio` | Matheus Alves |
| Evoluções | `evolucoes` | `/api/evolucoes` | `Evolucao` | Matheus Alves |
| Prontuários | `prontuarios` | `/api/prontuarios` | `Prontuario` | Micael Barros |
| Abordagem | `abordagem` | `/api/abordagens` | `Abordagem` | Micael Barros |

### 3.3 Cobertura de CRUD

Em cada entidade, a exclusão segue a regra de negócio correspondente: é lógica quando o histórico deve ser preservado e não é exposta quando o registro é permanente.

| Entidade | Cadastro | Consulta | Atualização | Exclusão |
|---|---|---|---|---|
| Usuários | `POST` | `GET` | `PUT` | `DELETE`, desativação lógica |
| Psicólogos | `POST` | `GET` | `PUT` | `DELETE`, somente sem vínculos |
| Pacientes | `POST` | `GET` | `PUT` | `DELETE`, desativação lógica |
| Sessões | `POST` | `GET` | `PUT` | `DELETE`, cancelamento lógico |
| Consultório | `POST`, registro único | `GET` | `PUT` | Não exposta |
| Evoluções | `POST` | `GET` | Não exposta, registro imutável | Não exposta |
| Prontuários | Automático no cadastro do paciente | `GET` | `PUT` | Não exposta |
| Abordagem | `POST` | `GET` | `PUT` | `DELETE`, desvinculação lógica |

## 4. Arquitetura e diretrizes

As diretrizes abaixo valem para todas as entidades.

### 4.1 Arquitetura em camadas

| Camada | Responsabilidade | Classes |
|---|---|---|
| Controller REST | Recebe a requisição HTTP, valida o DTO e delega ao serviço. Não contém regra de negócio. | `@RestController` |
| Service | Concentra regras de negócio, orquestra repositórios, controla transações e converte DTOs e entidades. | `@Service`, `@Transactional` |
| Repository | Persistência e consultas ao banco. | Interfaces `JpaRepository` |
| Entity | Mapeamento objeto-relacional das 8 tabelas. | `@Entity` |
| DTO | Contrato de entrada e saída da API, separado das entidades. | `*RequestDTO`, `*ResponseDTO` |
| Exception handler | Traduz exceções em respostas de erro padronizadas. | `@RestControllerAdvice` |

Fluxo de uma requisição:

```text
Controller → Service → Repository → PostgreSQL
```

### 4.2 Organização dos pacotes

| Pacote | Conteúdo |
|---|---|
| `com.kallospsi.controller` | Controladores REST de todas as entidades. |
| `com.kallospsi.service` | Serviços com as regras de negócio. |
| `com.kallospsi.repository` | Repositórios Spring Data JPA. |
| `com.kallospsi.entity` | Entidades JPA e enums de modalidade, status e papel. |
| `com.kallospsi.dto` | DTOs de entrada e saída organizados por entidade. |
| `com.kallospsi.exception` | Exceções de negócio e tratador global de erros. |
| `com.kallospsi.security` | Validação do token JWT e regras de acesso. |
| `com.kallospsi.integration` | Comunicação com o Supabase Auth. |
| `com.kallospsi.config` | Configuração do Swagger, segurança e CORS. |

### 4.3 Padrão da API REST

- Todas as rotas usam o prefixo `/api`, formato JSON e nomes de recursos no plural.
- Listagens são paginadas com `page`, `size` e `sort`, e aceitam filtros por *query string*.
- Erros seguem um formato único com `timestamp`, `status`, `erro`, `mensagem`, `caminho` e, quando houver falha de validação, a lista `campos`.

| Código | Uso |
|---:|---|
| `200` | Consulta ou atualização realizada. |
| `201` | Recurso criado, com cabeçalho `Location`. |
| `204` | Exclusão lógica concluída, sem corpo. |
| `400` | Dados inválidos no DTO. |
| `401` | Requisição sem autenticação válida. |
| `403` | Usuário sem permissão ou sem vínculo com o paciente. |
| `404` | Recurso não encontrado. |
| `405` | Operação não exposta para a entidade. |
| `409` | Conflito de regra de negócio, como CRP, CPF ou horário duplicado. |

### 4.4 DTOs e validação

Nenhum endpoint recebe ou devolve uma entidade JPA diretamente. Cada entidade possui DTOs de entrada (`*RequestDTO`) e saída (`*ResponseDTO`). A listagem de pacientes usa `PacienteResumoDTO`, com CPF mascarado.

O `PacienteRequestDTO` é o exemplo de referência: recebe nome, CPF, data de nascimento, telefone, e-mail, endereço e contato de emergência e usa Bean Validation, como `@NotBlank`, `@Email` e `@Past`. A conversão entre DTO e entidade ocorre no serviço.

### 4.5 Autenticação e autorização

- O Supabase Auth autentica o usuário e emite o token JWT, validado pela API em cada requisição.
- A API identifica o usuário pelo ID da tabela `usuarios`.
- A API nunca armazena senhas. No cadastro, apenas as repassa ao Supabase Auth.
- Os papéis são `ADMINISTRADOR` e `PSICOLOGO`.
- O administrador gerencia consultório e contas; o psicólogo gerencia seus pacientes, sessões e registros clínicos.
- Dados clínicos exigem vínculo ativo entre psicólogo e paciente na tabela `abordagem`; sem vínculo, a API responde `403`.
- Contas com `ativo = false` não acessam a API.

### 4.6 Persistência

- O banco é PostgreSQL no Supabase, acessado por JDBC. H2 ou outro banco em memória não é usado, nem em testes.
- O esquema é criado pelo script de implementação do modelo das 8 tabelas; o JPA apenas valida e não altera o banco automaticamente.
- Credenciais e chaves ficam em variáveis de ambiente, nunca no repositório.
- `criado_em` e `atualizado_em` usam `timestamptz` e são preenchidos automaticamente.
- O endereço do paciente, em `jsonb`, é mapeado para um objeto Java.
- Operações que alteram várias tabelas, como cadastrar paciente, executam em uma única transação.

### 4.7 Documentação com Swagger

A API é documentada com Swagger UI (`springdoc-openapi`), publicado junto à aplicação. Todo endpoint informa resumo, parâmetros, corpo e códigos de resposta, agrupados por entidade. O botão **Authorize** permite informar o token JWT para testar rotas protegidas.

### 4.8 Tratamento de erros

Um tratador global (`@RestControllerAdvice`) converte exceções de negócio, validação e acesso nos códigos da seção 4.3 e no formato padrão. Nenhuma mensagem expõe pilhas de execução, consultas SQL ou outros detalhes internos.

## 5. Princípios SOLID e padrões GRASP

### 5.1 SOLID

| Princípio | Como a estrutura atende | Onde se vê |
|---|---|---|
| **S — Responsabilidade única** | Controlador cuida de HTTP, serviço das regras e repositório da persistência. | `PacienteController`, `PacienteService`, `PacienteRepository` |
| **O — Aberto e fechado** | Novas regras entram como componentes injetados no serviço, sem alterar o código existente. | Validadores de agendamento usados por `SessaoService` |
| **I — Segregação de interfaces** | Interfaces pequenas e específicas expõem somente as operações necessárias. | `PacienteService`, `SessaoRepository`, `AuthGateway` |
| **D — Inversão de dependência** | Controladores dependem de interfaces de serviço e serviços de interfaces de repositório, injetadas por construtor. | Injeção por construtor e `AuthGateway` |

### 5.2 GRASP

| Padrão | Como a estrutura atende | Onde se vê |
|---|---|---|
| **Especialista na Informação** | A responsabilidade fica na classe que possui os dados necessários. | `Sessao.sobrepoe(...)`, `Prontuario` |
| **Controlador** | Controladores REST recebem, validam e delegam, sem regras de negócio. | `*Controller` |
| **Criador** | Quem agrega ou relaciona objetos também os cria. | `PacienteService.cadastrar(...)` cria prontuário e vínculo inicial. |
| **Baixo acoplamento** | DTOs isolam o contrato da API do modelo e camadas se comunicam por interfaces. | Pacotes `dto`, `service` e `repository` |

## 6. Regras de negócio

| ID | Regra | Aplicada em |
|---|---|---|
| **RN-B01** | O sistema atende a um único consultório. | RF-B33 a RF-B35 |
| **RN-B02** | Cada psicólogo tem uma conta individual; o administrador não tem cadastro profissional. | RF-B01, RF-B09 |
| **RN-B03** | O CRP é único no sistema. | RF-B09, RF-B14 |
| **RN-B04** | O CPF é único por psicólogo responsável. | RF-B15, RF-B21 |
| **RN-B05** | Cada paciente tem um prontuário único, criado no cadastro. | RF-B15, RF-B43 |
| **RN-B06** | Somente psicólogos com vínculo ativo atendem o paciente e acessam seus dados clínicos. | RF-B17, RF-B31, RF-B42, RF-B44 |
| **RN-B07** | Um psicólogo não pode ter duas sessões com horários sobrepostos. | RF-B30 |
| **RN-B08** | A sessão pode estar agendada, concluída ou cancelada. | RF-B27, RF-B28 |
| **RN-B09** | O pagamento pode estar pendente ou pago, via Pix ou cartão. | RF-B29 |
| **RN-B10** | A evolução é vinculada a uma sessão concluída e não pode ser alterada após o registro. | RF-B38, RF-B41 |
| **RN-B11** | A desativação de pacientes é lógica e preserva os registros clínicos. | RF-B19 |
| **RN-B12** | Informações clínicas têm acesso restrito às permissões dos profissionais. | RF-B42, RF-B44 |

## 7. Requisitos funcionais por entidade

Os identificadores seguem o padrão `RF-B`.

### 7.1 Usuários

| Campo | Definição |
|---|---|
| **Responsável** | Ruan Ricardo |
| **Tabela** | `usuarios` (`id uuid`, `psicologo_id`, `email`, `papel`, `ativo`) |
| **Rota base** | `/api/usuarios` |
| **Acesso** | Administrador gerencia todas as contas; psicólogo consulta a própria. |
| **Classes** | `Usuario`, `UsuarioRepository`, `UsuarioService`, `UsuarioController`, `UsuarioRequestDTO`, `UsuarioResponseDTO`, `CadastroContaDTO` |

| ID | Requisito | Endpoint | Descrição |
|---|---|---|---|
| **RF-B01** | Cadastrar conta | `POST /api/usuarios/cadastro` | Recebe nome, CRP, e-mail e senha. Cria conta no Supabase Auth, psicólogo e usuário `PSICOLOGO` em uma transação. |
| **RF-B02** | Consultar usuário autenticado | `GET /api/usuarios/me` | Devolve usuário, papel e psicólogo vinculado. |
| **RF-B03** | Listar usuários | `GET /api/usuarios` | Lista paginada, restrita ao administrador, com filtros por papel e situação. |
| **RF-B04** | Buscar usuário por ID | `GET /api/usuarios/{id}` | Devolve uma conta; psicólogo acessa somente a própria. |
| **RF-B05** | Atualizar usuário | `PUT /api/usuarios/{id}` | Atualiza e-mail e situação; somente administrador altera papel. E-mail deve ser único. |
| **RF-B06** | Desativar usuário | `DELETE /api/usuarios/{id}` | Define `ativo = false`, mantém o registro e bloqueia o acesso. |
| **RF-B07** | Reativar usuário | `PATCH /api/usuarios/{id}/reativar` | Restabelece o acesso de uma conta desativada. |
| **RF-B08** | Delegar autenticação | — | Supabase Auth realiza login, logout e recuperação. A API valida JWT e nunca armazena senhas. |

### 7.2 Psicólogos

| Campo | Definição |
|---|---|
| **Responsável** | Ruan Ricardo |
| **Tabela** | `psicologos` (`id`, `consultorio_id`, `nome`, `crp`, `telefone`, `especialidade`) |
| **Rota base** | `/api/psicologos` |
| **Acesso** | Usuários autenticados consultam; psicólogo edita o próprio perfil; administrador gerencia todos. |
| **Classes** | `Psicologo`, `PsicologoRepository`, `PsicologoService`, `PsicologoController`, `PsicologoRequestDTO`, `PsicologoResponseDTO` |

| ID | Requisito | Endpoint | Descrição |
|---|---|---|---|
| **RF-B09** | Cadastrar psicólogo | `POST /api/psicologos` | Cria perfil ligado ao consultório; CRP deve ser único. |
| **RF-B10** | Listar psicólogos | `GET /api/psicologos` | Lista paginada com busca por nome. |
| **RF-B11** | Buscar psicólogo por ID | `GET /api/psicologos/{id}` | Devolve os dados profissionais. |
| **RF-B12** | Atualizar perfil profissional | `PUT /api/psicologos/{id}` | Psicólogo atualiza o próprio perfil e administrador qualquer perfil; revalida o CRP. |
| **RF-B13** | Excluir psicólogo | `DELETE /api/psicologos/{id}` | Permitido somente sem pacientes, sessões ou vínculos; caso contrário responde `409`. |
| **RF-B14** | Validar CRP | — | Valida formato e unicidade no cadastro e atualização; duplicidade responde `409`. |

### 7.3 Pacientes

| Campo | Definição |
|---|---|
| **Responsável** | Richard Silva |
| **Tabela** | `pacientes` (`id`, `psicologo_id`, `nome`, `cpf`, `data_nascimento`, `telefone`, `email`, `endereco`, `contato_emergencia`, `telefone_emergencia`, `status`) |
| **Rota base** | `/api/pacientes` |
| **Acesso** | Psicólogo autenticado, aos pacientes sob sua responsabilidade ou vinculados a ele. |
| **Classes** | `Paciente`, `PacienteRepository`, `PacienteService`, `PacienteController`, `PacienteRequestDTO`, `PacienteResponseDTO`, `PacienteResumoDTO` |

| ID | Requisito | Endpoint | Descrição |
|---|---|---|---|
| **RF-B15** | Cadastrar paciente | `POST /api/pacientes` | Define o psicólogo autenticado como responsável e o status ativo; cria prontuário e vínculo inicial na mesma transação. |
| **RF-B16** | Listar pacientes | `GET /api/pacientes` | Lista paginada com busca por nome ou CPF e filtro por status, restrita ao psicólogo. |
| **RF-B17** | Buscar paciente por ID | `GET /api/pacientes/{id}` | Devolve cadastro completo; responde `403` sem vínculo. |
| **RF-B18** | Atualizar paciente | `PUT /api/pacientes/{id}` | Atualiza dados pessoais, endereço e contato de emergência, revalidando CPF. |
| **RF-B19** | Desativar paciente | `DELETE /api/pacientes/{id}` | Exclusão lógica; preserva prontuário, sessões e evoluções. |
| **RF-B20** | Reativar paciente | `PATCH /api/pacientes/{id}/reativar` | Retorna o paciente ao status ativo. |
| **RF-B21** | Validar CPF | — | Valida formato, dígitos e unicidade por psicólogo; duplicidade responde `409`. |
| **RF-B22** | Proteger CPF nas listagens | — | `PacienteResumoDTO` devolve CPF mascarado; CPF completo aparece somente no detalhe. |

### 7.4 Sessões

| Campo | Definição |
|---|---|
| **Responsável** | Richard Silva |
| **Tabela** | `sessoes` (`id`, `paciente_id`, `psicologo_id`, `inicio_em`, `fim_em`, `modalidade`, `status`, `valor`, `forma_pagamento`, `status_pagamento`, `pago_em`, `observacoes`) |
| **Rota base** | `/api/sessoes` |
| **Acesso** | Psicólogo autenticado, às próprias sessões. |
| **Classes** | `Sessao`, `SessaoRepository`, `SessaoService`, `SessaoController`, `SessaoRequestDTO`, `SessaoResponseDTO`, `PagamentoDTO` |

| ID | Requisito | Endpoint | Descrição |
|---|---|---|---|
| **RF-B23** | Agendar sessão | `POST /api/sessoes` | Cria sessão com paciente, intervalo, modalidade e valor; usa valor padrão se omitido. Nasce agendada e pendente. |
| **RF-B24** | Listar sessões | `GET /api/sessoes` | Lista sessões do psicólogo com filtros por período, paciente e status. |
| **RF-B25** | Buscar sessão por ID | `GET /api/sessoes/{id}` | Devolve os dados da sessão. |
| **RF-B26** | Atualizar e remarcar | `PUT /api/sessoes/{id}` | Altera horário, modalidade, valor e observações; somente sessões agendadas podem ser remarcadas. |
| **RF-B27** | Cancelar sessão | `DELETE /api/sessoes/{id}` | Define status cancelada e preserva o registro. |
| **RF-B28** | Concluir sessão | `PATCH /api/sessoes/{id}/concluir` | Define status concluída e habilita o registro de evolução. |
| **RF-B29** | Registrar pagamento | `PATCH /api/sessoes/{id}/pagamento` | Informa Pix ou cartão, define pagamento como pago e grava `pago_em`. |
| **RF-B30** | Impedir conflito de horário | — | Rejeita com `409` sobreposição com sessão não cancelada do mesmo psicólogo. |
| **RF-B31** | Exigir vínculo do paciente | — | Só agenda paciente com vínculo ativo na abordagem. |
| **RF-B32** | Consultar sessões do paciente | `GET /api/pacientes/{id}/sessoes` | Devolve histórico com data, modalidade, status e pagamento. |

### 7.5 Consultório

| Campo | Definição |
|---|---|
| **Responsável** | Matheus Alves |
| **Tabela** | `consultorio` (`id`, `razao_social`, `endereco`, `telefone`, `valor_padrao_sessao`, `logo`) |
| **Rota base** | `/api/consultorio` |
| **Acesso** | Usuários autenticados consultam; somente administrador altera. |
| **Classes** | `Consultorio`, `ConsultorioRepository`, `ConsultorioService`, `ConsultorioController`, `ConsultorioRequestDTO`, `ConsultorioResponseDTO` |

| ID | Requisito | Endpoint | Descrição |
|---|---|---|---|
| **RF-B33** | Consultar consultório | `GET /api/consultorio` | Devolve razão social, endereço, telefone, valor padrão e logotipo do registro único. |
| **RF-B34** | Atualizar consultório | `PUT /api/consultorio` | Somente administrador; exige valor padrão maior ou igual a zero. |
| **RF-B35** | Cadastrar consultório inicial | `POST /api/consultorio` | Cria o registro único; se já existir, responde `409`. |
| **RF-B36** | Atualizar logotipo | `PUT /api/consultorio/logo` | Somente administrador; grava a referência no campo `logo`. |
| **RF-B37** | Proteger o registro único | — | A exclusão do consultório não é exposta. |

### 7.6 Evoluções

| Campo | Definição |
|---|---|
| **Responsável** | Matheus Alves |
| **Tabela** | `evolucoes` (`id`, `prontuario_id`, `sessao_id`, `conteudo`, `criado_em`) |
| **Rota base** | `/api/evolucoes` |
| **Acesso** | Psicólogos com vínculo ativo com o paciente. |
| **Classes** | `Evolucao`, `EvolucaoRepository`, `EvolucaoService`, `EvolucaoController`, `EvolucaoRequestDTO`, `EvolucaoResponseDTO` |

| ID | Requisito | Endpoint | Descrição |
|---|---|---|---|
| **RF-B38** | Registrar evolução | `POST /api/evolucoes` | Cria evolução para sessão concluída que pertença ao paciente do prontuário. |
| **RF-B39** | Listar evoluções do prontuário | `GET /api/evolucoes?prontuarioId={id}` | Devolve histórico paginado em ordem cronológica. |
| **RF-B40** | Buscar evolução por ID | `GET /api/evolucoes/{id}` | Devolve uma evolução. |
| **RF-B41** | Garantir imutabilidade | — | Edição e exclusão não são expostas; tentativas respondem `405`. |
| **RF-B42** | Restringir acesso | — | Somente psicólogos com vínculo ativo consultam e registram; demais recebem `403`. |

### 7.7 Prontuários

| Campo | Definição |
|---|---|
| **Responsável** | Micael Barros |
| **Tabela** | `prontuarios` (`id`, `paciente_id`, `aberto_em`, `anamnese`, `observacoes_gerais`, `cid_principal`, `descricao_cid_principal`, `cid_secundario`, `descricao_cid_secundario`) |
| **Rota base** | `/api/prontuarios` |
| **Acesso** | Psicólogos com vínculo ativo com o paciente. |
| **Classes** | `Prontuario`, `ProntuarioRepository`, `ProntuarioService`, `ProntuarioController`, `ProntuarioRequestDTO`, `ProntuarioResponseDTO` |

| ID | Requisito | Endpoint | Descrição |
|---|---|---|---|
| **RF-B43** | Criar prontuário automaticamente | — | `PacienteService` cria o prontuário no cadastro do paciente; relação 1:1 com `paciente_id` único. |
| **RF-B44** | Consultar prontuário do paciente | `GET /api/prontuarios/paciente/{pacienteId}` | Devolve anamnese, observações e CIDs; responde `403` sem vínculo. |
| **RF-B45** | Buscar prontuário por ID | `GET /api/prontuarios/{id}` | Devolve o prontuário com o mesmo controle de acesso. |
| **RF-B46** | Atualizar registro clínico | `PUT /api/prontuarios/{id}` | Atualiza anamnese, observações, CIDs e data de atualização. |
| **RF-B47** | Proteger o prontuário | — | Exclusão não exposta; prontuário acompanha o paciente, que só é desativado logicamente. |
| **RF-B48** | Buscar CID | `GET /api/cid?termo={texto}` | Pesquisa códigos e descrições em catálogo de referência, que não integra as 8 tabelas. |

### 7.8 Abordagem

| Campo | Definição |
|---|---|
| **Responsável** | Micael Barros |
| **Tabela** | `abordagem` (`id`, `psicologo_id`, `paciente_id`, `vinculado_em`, `ativo`) |
| **Rota base** | `/api/abordagens` |
| **Acesso** | Psicólogo responsável pelo paciente e administrador. |
| **Classes** | `Abordagem`, `AbordagemRepository`, `AbordagemService`, `AbordagemController`, `AbordagemRequestDTO`, `AbordagemResponseDTO` |

| ID | Requisito | Endpoint | Descrição |
|---|---|---|---|
| **RF-B49** | Vincular psicólogo a paciente | `POST /api/abordagens` | Cria vínculo ativo com data atual; o par psicólogo-paciente é único e sustenta a relação N:N. |
| **RF-B50** | Listar vínculos | `GET /api/abordagens` | Lista paginada com filtros por paciente, psicólogo e situação. |
| **RF-B51** | Buscar vínculo por ID | `GET /api/abordagens/{id}` | Devolve um vínculo. |
| **RF-B52** | Atualizar vínculo | `PUT /api/abordagens/{id}` | Atualiza data de vínculo e situação. |
| **RF-B53** | Desvincular psicólogo | `DELETE /api/abordagens/{id}` | Define `ativo = false`, preserva o histórico e impede novas sessões e acesso clínico. |
| **RF-B54** | Reativar vínculo | `PATCH /api/abordagens/{id}/reativar` | Restabelece um vínculo desativado. |
| **RF-B55** | Restringir quem vincula | — | Somente psicólogo responsável ou administrador cria e desativa vínculos. |

## 8. Requisitos não funcionais de back-end

| ID | Requisito | Descrição |
|---|---|---|
| **RNF-B01** | Persistência real | Todos os dados ficam no PostgreSQL do Supabase; nenhum banco em memória em qualquer ambiente. |
| **RNF-B02** | Segurança de acesso | Todas as rotas, exceto cadastro de conta, exigem JWT válido; dados clínicos exigem vínculo ativo. |
| **RNF-B03** | Proteção de dados | Segredos em variáveis de ambiente, comunicação HTTPS e senhas nunca armazenadas pela API. |
| **RNF-B04** | Integridade | Chaves primárias e estrangeiras, campos únicos e transações em operações que alteram várias tabelas. |
| **RNF-B05** | Desempenho | Listagens paginadas e filtros executados no banco, sem carregar tabelas inteiras em memória. |
| **RNF-B06** | Documentação | Todos os endpoints descritos no Swagger UI. |
| **RNF-B07** | Manutenibilidade | Código em camadas e pacotes, conforme SOLID e GRASP. |
| **RNF-B08** | Rastreabilidade | Datas de criação e atualização registradas automaticamente nas tabelas. |
