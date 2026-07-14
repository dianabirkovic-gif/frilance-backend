# frilance-os-backend

Spring Boot modular monolith backing Frilance OS — see `CLAUDE.md` for
architecture rules before adding code, and the SRS
(`Frilance_OS_Vymogy_SRS.docx`, kept in the design-reference repo) for the
full requirement set this is built against.

Currently implemented: FR-01 (freelancer register/login), and the Overview
("Огляд") dashboard screen end-to-end (entities, migrations, seed data, API).
Everything else in the SRS has a package reserved (see `package-info.java`
in `tasks/`, `notifications/`, `targets/`, `smmstats/`, `ai/`, `analytics/`,
`reports/`, `onboarding/`) but no code yet.

## Requirements

- Java 21
- Maven 3.9+ (or use the wrapper once generated — see below)
- Docker (for local Postgres via `docker-compose`, and for the Testcontainers
  integration test)

This backend was scaffolded without a local JDK/Maven/Docker available, so
none of it has been compiled or run yet. Before trusting it:

```bash
# generate the Maven wrapper once you have Maven installed, so CI/teammates
# don't need a matching local Maven version:
mvn -N wrapper:wrapper

mvn verify
```

## Running locally

```bash
docker compose up -d          # starts Postgres on localhost:5432
cp .env.example .env           # fill in JWT_SECRET at minimum
set -a && source .env && set +a
mvn spring-boot:run
```

Flyway runs automatically on startup (`V1__init_schema.sql`, then
`V2__seed_dashboard_demo_data.sql`, which seeds one demo agency tenant with
data matching the original `dashboard.html` prototype).

Dev login: `diana@example.com` / `password123`.

```bash
curl -X POST localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"diana@example.com","password":"password123"}'

curl localhost:8080/api/v1/dashboard/overview \
  -H "Authorization: Bearer <accessToken from above>"
```

API docs: `http://localhost:8080/docs` (springdoc/Swagger UI).

## Known simplifications (see CLAUDE.md for the full list)

- Single access token, no refresh-token rotation yet.
- Only FR-01 (freelancer login) is wired up; FR-02 (agency owner/PM email
  login, SMM/targetolog shared team password + anonymous session) is not.
- `attention_item` is a seeded placeholder table, not yet a computed view
  over clients/finance/targets.
- `team_member.loadPercent` / `clientCount` are stored directly, not derived
  from `client.assignee_id` counts.