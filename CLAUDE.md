# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Gov-Case-Flow is a Maven multi-module Spring Boot 3.2 (Java 17; CI also runs JDK 21) backend for public-administration case management. It is built from microservices that talk to each other over RabbitMQ. Each service with a database owns its own PostgreSQL instance, and its schema is managed by Liquibase (`ddl-auto=validate`, so every schema change needs a changelog).

Modules:
- `auth-service`: registration, login, refresh tokens, brute-force lockout, admin promotion. Issues JWTs.
- `case-service`: case CRUD, status workflow, officer assignment, status history.
- `audit-service`: consumes domain events and stores the audit trail. Exposes a read API.
- `notification-service`: consumes events and currently only logs them. It has no database.
- `gov-case-flow-events`: shared event DTOs (`com.govcaseflow.events.{auth,cases}`).
- `gov-case-flow-tracing`: a shared infrastructure library with correlation-ID propagation and a base error-handling model (`BaseGlobalExceptionHandler`, `ErrorResponse`, `ErrorCode`). It is wired in through Spring Boot auto-configuration (`META-INF/spring/...AutoConfiguration.imports`).

## Commands

Use `./mvnw` (or `mvnw.cmd` on Windows) from the repo root.

```bash
./mvnw clean verify                        # what CI runs: unit tests (surefire) + *IT tests (failsafe)
./mvnw test                                # unit tests only
./mvnw -pl case-service -am verify         # one service plus the modules it depends on
./mvnw -pl case-service -am test -Dtest=CaseServiceTest -Dsurefire.failIfNoSpecifiedTests=false
./mvnw -pl case-service -am verify -Dit.test=CaseServiceRabbitIT -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false   # single IT only
./mvnw -pl case-service -am -DskipTests package

cp .env.example .env && docker compose up --build   # full local stack
```

- Classes named `*IT` are Testcontainers integration tests (Postgres + RabbitMQ) and run in the `verify` phase, so Docker must be running. Plain `*Test` classes are unit or MockMvc tests.
- Run `-am` whenever you change `gov-case-flow-events` or `gov-case-flow-tracing`. Services depend on the SNAPSHOT artifacts.
- Each Dockerfile builds from the repo root context (`mvn -pl <service> -am package`).
- Local ports: case 8080, auth 8081, audit 8082, RabbitMQ UI 15672. Swagger is at `/swagger-ui/index.html` on each HTTP service.
- `ansible/` holds the deployment playbooks (`setup.yml`, `deploy.yml --ask-vault-pass`).

## Architecture

### Event publishing (transactional outbox-lite)
Services never publish to RabbitMQ directly inside a transaction. The flow is:
1. The domain service (for example `CaseService`) calls Spring's `ApplicationEventPublisher.publishEvent(event)` inside its `@Transactional` method.
2. A `@TransactionalEventListener(phase = AFTER_COMMIT)` component (`case-service/.../listener/CaseStatusChangedEventListener`, `auth-service/.../messaging/*EventListener`) forwards the event to a Rabbit publisher (`RabbitCaseEventPublisher`, `RabbitAuthEventPublisher`).

Follow this same pattern when you add a new event.

### Exchanges, routing keys and queues
- `case.events.exchange` (topic), routing key `case.status.changed`, consumed by `audit.case-status.queue` and `notification.case-status.queue`.
- `auth.events.exchange` (topic), routing keys `auth.user.registered`, `auth.user.promoted` and `auth.account.locked`, consumed by the `audit.*.queue` queues and `notification.user-registered.queue`.

Producers declare only the exchange. Consumers declare their own queues and bindings (`audit-service/.../config/*Amqp*Config`, `notification-service/.../config/*AmqpConfig`). Exchange and routing-key names are duplicated as constants in each service, so keep them in sync.

Messages are JSON (`Jackson2JsonMessageConverter` with `JavaTimeModule`). Consumers restrict deserialization to the trusted package `com.govcaseflow.events`, so event classes must live in `gov-case-flow-events`.

### Correlation ID (`gov-case-flow-tracing`)
- HTTP: `CorrelationIdFilter` runs at highest precedence, before Spring Security. It reads or creates `X-Correlation-Id` and puts it into the MDC under `correlationId`. Only canonical UUIDs are accepted; any other value is replaced.
- AMQP outbound: `CorrelationIdMessagePostProcessor` is applied automatically to Boot's auto-configured `RabbitTemplate` through a customizer. **A service that defines its own `RabbitTemplate` bean must add the post-processor manually.** `case-service`'s `caseRabbitTemplate` in `RabbitConfig` is an example.
- AMQP inbound: a `BeanPostProcessor` prepends `CorrelationIdListenerAdvice` to every listener container factory's advice chain, so Boot's retry interceptor stays in place.
- Log patterns include `[%X{correlationId}]`.

### Security
`auth-service` signs JWTs with the shared `JWT_SECRET`. The token carries the claims `userId` and `roles`. Every other HTTP service has its own copy of `JwtService` and `JwtAuthenticationFilter`, validates tokens with the same secret, and maps roles to `ROLE_<role>` authorities (`USER`, `OFFICER`, `ADMIN`). `case-service` splits its endpoints by role across `CaseUserController`, `CaseOfficerController` and `CaseAdminController`.

### Case workflow
Allowed status transitions are defined in `case-service/.../domain/CaseStatusTransitions`: DRAFT → SUBMITTED → IN_REVIEW → DECISION_PENDING → APPROVED/REJECTED → CLOSED. Each change is persisted to `CaseStatusHistory` and emits `CaseStatusChangedEvent`. `case-service` has its own `CaseStatus` enum, separate from the one in `gov-case-flow-events`. They are mapped through `CaseStatusEventMapper`.

### Configuration
Each service reads `application.properties` with env-var overrides; the defaults target the docker-compose hostnames. The `test` profile lives in `src/main/resources/application-test.properties`, not under `src/test`. Integration tests use `@ActiveProfiles("test")` plus `@DynamicPropertySource` to point at the Testcontainers instances.

## Conventions

- Work happens on branches named `feature/SCRUM-<n>-<slug>`, with Conventional Commits that carry the ticket key, for example `feat(tracing): SCRUM-7 ...` or `fix(build): SCRUM-7 ...`.
- CI (`.github/workflows/ci.yml`) runs `mvn -B -ntp clean verify` on JDK 17 and 21, then builds the Docker image of every service. It runs on pushes and PRs to `main` and `develop`.

## Ticket workflow rules

### Scope
- Implement only what the ticket and its linked Confluence specification describe.
- Report anything you notice outside that scope as a proposal for a separate ticket; do not implement it in the current change. This applies in particular to Liquibase migrations and refactors.

### Build files
- Changes to `pom.xml` (root or module), CI workflows and `docker-compose.yml` need my explicit confirmation first. State which modules are affected and how far the change reaches.

### Before committing
- Always run a full `./mvnw clean verify` across all modules, not only the one you changed.
- Do not filter the build output — Maven warnings locally can be errors in CI.
- Show me the build result before committing.
- Stage only the module directories the ticket covers. Never use `git add -A`.

docs: add ticket workflow rules to CLAUDE.md