# Backend - Programação Orientada a Objetos (Java / Spring Boot)

API desenvolvida em **Java 21** e **Spring Boot 3** para a cadeira de POO e integrada ao projeto **PI3**.

## 🚀 Tecnologias
- Java 21 LTS
- Spring Boot 3
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Docker

## 🛠️ Como Executar Localmente

### Pré-requisitos
- JDK 21 instalado (ou Docker)

### Executando com Maven Wrapper:
No Windows (PowerShell / CMD):
```bash
.\mvnw.cmd spring-boot:run
```

No Linux / macOS:
```bash
./mvnw spring-boot:run
```

A API estará disponível em: `http://localhost:8080/api/health`

## 🐳 Executando com Docker
```bash
docker build -t pi3-backend .
docker run -p 8080:8080 pi3-backend
```
