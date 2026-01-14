# Gov-Case-Flow (CaseFlow) — Backend

![CI](https://github.com/bartlomiejlorenowicz/Gov-Case-Flow-backend/actions/workflows/ci.yml/badge.svg)

**Gov-Case-Flow** is a **case management system** for public administration (GovTech-like).  
It supports creating and tracking official cases (**sprawy urzędowe**) with **status workflow**, **audit trail**, and **event-driven communication**.

The backend is implemented as **microservices** using **Spring Boot**, **PostgreSQL**, **RabbitMQ**, and **Docker Compose**.

---

## 🧩 Microservices

This repository contains a Maven multi-module backend:

- **auth-service** — registration, login, JWT authentication & authorization
- **case-service** — case CRUD, status workflow, assigning cases to users, status history
- **audit-service** — audit trail (stores received domain events)
- **notification-service** — receives events (currently logs them, planned: real notifications)
- **gov-case-flow-events** — shared module with event contracts (DTOs) used between services

---

## 🏗️ Architecture

Services communicate using **RabbitMQ (event-driven architecture)**.

### Mermaid diagram (microservices + RabbitMQ)

```mermaid
flowchart LR
  subgraph Client
    U[User / Frontend]
  end

  U -->|REST + JWT| AUTH[auth-service]
  U -->|REST + JWT| CASE[case-service]

  subgraph Messaging
    R[(RabbitMQ)]
  end

  CASE -->|publish events| R
  R -->|CaseStatusChangedEvent| AUDIT[audit-service]
  R -->|CaseStatusChangedEvent| NOTIF[notification-service]

  subgraph Databases
    ADB[(auth_db)]
    CDB[(case_db)]
    DDB[(audit_db)]
  end

  AUTH --> ADB
  CASE --> CDB
  AUDIT --> DDB 
```
##  ⚙️ Tech stack
Java 17

Spring Boot

Spring Security + JWT

PostgreSQL 17

RabbitMQ 3 (management)

Liquibase

Maven multi-module

Docker + Docker Compose

GitHub Actions CI

Build & Test on JDK 17 and JDK 21 (matrix)

Docker image build for all services (Buildx + cache)

## ✅ Features
Auth
user registration

user login

JWT-based authentication / authorization

Case management
create case

update case (PATCH)

delete case

get single case / list cases (pagination)

assign case to user

validated status transitions (workflow)

status history persistence

publishes domain events (RabbitMQ)

Audit
listens for domain events

stores audit trail in PostgreSQL

Notifications
listens for domain events

currently logs received events (planned: email/in-app notifications)

## 🐳 Run locally (Docker Compose)
✅ Recommended: run everything using Docker Compose.

Start
bash
Copy code
docker compose up --build
Services & ports
Service	Port (host → container)	Notes
case-service	8080 → 8080	REST API + Actuator
auth-service	8081 → 8080	REST API + Actuator
audit-service	-	internal service + Actuator
notification-service	-	internal service + Actuator
postgres (case_db)	5432 → 5432	case-service DB
audit-postgres (audit_db)	5433 → 5432	audit-service DB
auth-postgres (auth_db)	5434 → 5432	auth-service DB
rabbitmq	5672, 15672	AMQP + Management UI

Healthchecks
Docker Compose includes healthchecks:

PostgreSQL: pg_isready

Services: GET /actuator/health

## 🔑 Configuration
Currently configuration is provided directly in docker-compose.yml.
Planned improvement: .env support.

RabbitMQ
Host: rabbitmq

Port: 5672

Management UI: http://localhost:15672

🔌 API (high-level)
Auth (auth-service)
POST /auth/register — register user

POST /auth/login — login and obtain JWT

Cases (case-service)
POST /cases — create case

GET /cases — list cases (pagination)

GET /cases/{id} — get case by id

PATCH /cases/{id} — update case / change status

DELETE /cases/{id} — delete case

PATCH /cases/{id}/assign — assign case to user

Exact endpoints may vary depending on controller implementation.

## 🧪 CI / Quality
This repository uses GitHub Actions CI pipeline:

build & test on JDK 17 and JDK 21

caching enabled for Maven dependencies

Docker images built for all microservices (Buildx + GitHub Actions cache)

Workflow file:

.github/workflows/ci.yml

## 🗺️ Roadmap
 add .env + .env.example

 add OpenAPI/Swagger documentation

 add Testcontainers integration tests (Postgres + RabbitMQ)

 improve observability (Actuator metrics)

 correlation IDs / tracing in logs

 implement real notification delivery (email / in-app)

## 👤 Author
Created by Bartłomiej Lorenowicz