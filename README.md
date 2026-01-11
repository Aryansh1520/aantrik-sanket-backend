# Aantrik Sanket – Backend

Aantrik Sanket is a psychology-focused SaaS platform.
This repository contains the **backend service**, built with Spring Boot and PostgreSQL, designed to evolve incrementally following real-world SaaS engineering practices.

The goal of this project is to balance **correctness, clarity, and extensibility** while avoiding premature optimization.

---

## Tech Stack

- **Java 21**
- **Spring Boot**
- **PostgreSQL**
- **JPA / Hibernate**
- **Gradle**
- **Swagger / OpenAPI**

---

## Current Features

- Modular Spring Boot application structure
- PostgreSQL integration with connection pooling
- Centralized configuration via environment-based properties
- Health monitoring endpoint
- OpenAPI / Swagger documentation
- Production-ready project layout

---

## Planned Features

- User management and authentication
- JWT-based security
- OAuth integration (Google)
- Secure password hashing
- Rate limiting and abuse protection
- Redis integration for caching and ephemeral data
- SaaS-oriented domain modeling
- Multi-tenant groundwork

---

## Project Philosophy

- Build incrementally, not prematurely
- Favor explicit configuration over magic
- Keep infrastructure and business logic separated
- Follow production-grade patterns even at small scale
- Treat this as both a real backend and a portfolio artifact

---

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL
- Gradle

### Run locally

```bash
./gradlew bootRun
