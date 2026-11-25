# Pendências de Integração — Backend

Lista de pontos temporários deixados no código para destravar o desenvolvimento em paralelo enquanto os demais módulos são finalizados pelo time.

---

## Módulo: Pacientes (`Richard Silva`)

- [ ] **Autenticação JWT — `psicologoId` dinâmico** (`Ruan Ricardo`)
  - **Onde está:** `PacienteServiceImpl.obterPsicologoAutenticadoId()` (atualmente retorna `1L` fixo) e `backend/pom.xml` (dependências `spring-boot-starter-security` e `spring-boot-starter-oauth2-resource-server` comentadas).
  - **O que fazer:** ler o usuário autenticado do token JWT (Supabase Auth) e obter o `psicologoId` real correspondente.
  - **Requisito:** `RF-B08`, `RF-B15`, `RF-B16`.

- [ ] **Criação automática de `Prontuario` e `Abordagem` no cadastro** (`Micael Barros`)
  - **Onde está:** `PacienteServiceImpl.criarProntuarioEVinculoInicial(Paciente)` (método vazio dentro da transação `@Transactional` de `cadastrar`).
  - **O que fazer:** criar o `Prontuario` único do paciente e o vínculo inicial ativo na tabela `abordagem` na mesma transação.
  - **Requisito:** `RF-B15`, `RF-B43`, `RF-B49`.

- [ ] **Validação de vínculo na `abordagem` ao consultar/atualizar paciente** (`Micael Barros`)
  - **Onde está:** `PacienteServiceImpl.buscarPacienteDoPsicologo(Long id, Long psicologoId)` (hoje verifica apenas `psicologoId` dono do cadastro).
  - **O que fazer:** considerar também vínculos ativos na tabela `abordagem` (ou `403` quando não houver vínculo).
  - **Requisito:** `RF-B16`, `RF-B17`, `RN-B06`.

- [ ] **Preencher `ultimaSessao` nos DTOs de paciente** (`Richard Silva`)
  - **Onde está:** `PacienteResponseDTO.fromEntity(paciente)` e `PacienteResumoDTO.fromEntity(paciente)` (atualmente passam `null` para `ultimaSessao`).
  - **O que fazer:** após implementar `SessaoRepository`, consultar a data da última sessão do paciente e usar a sobrecarga `fromEntity(paciente, ultimaSessao)`.
  - **Requisito:** `RF-F13` / listagem e detalhe do paciente.

---

## Módulo: Sessões (`Richard Silva` — próximo módulo)

- [ ] **Exigir vínculo ativo na `abordagem` ao agendar sessão** (`Micael Barros` — `RF-B31`).
- [ ] **Buscar `valor_padrao_sessao` em `Consultorio` quando o valor for omitido** (`Matheus Alves` — `RF-B23`).
- [ ] **Expor `GET /api/pacientes/{id}/sessoes`** (`RF-B32`).
