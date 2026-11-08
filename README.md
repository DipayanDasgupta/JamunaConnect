# JamunaConnect

Hostel information, room lookup, and complaint routing for **Jamuna Hostel, IIT Madras**.
CS5013 Team 59 — Dipayan Dasgupta (CE24B059) &amp; S. Saathvik (CS26M036).

A single Spring Boot process serves server-rendered Thymeleaf pages and a REST API over
one PostgreSQL database. Design doc: `design_doc_team59.pdf`.

## Stack

- Java 21, Spring Boot 3.3, Maven
- Thymeleaf + Bootstrap, Leaflet.js + GeoJSON for the campus map
- Spring Data JPA, Spring Security (three roles), Bean Validation
- PostgreSQL with `pg_trgm` fuzzy search; Flyway migrations; H2 for fast local runs
- JUnit 5 + Mockito (unit), Testcontainers (integration on real PostgreSQL)
- GitHub Actions (test → build → staging deploy), Docker, Actuator health

## Modules

| Owner    | Module | Contents |
|----------|--------|----------|
| Dipayan  | A | Public pages: contacts, lookup, map, complaint form |
| Dipayan  | B | `GET /api/rooms` fuzzy search + Leaflet map |
| Dipayan  | C | Staff dashboard, filters, status transitions |
| Saathvik | D | Auth/RBAC, six-entity schema, Flyway, repositories |
| Saathvik | E | Complaint/contacts API, validation, rate limit, state machine |
| Saathvik | F | Async notification on submit + daily 72-hour escalation |

Additional public pages: notice board (`/notices`), emergency/first-aid (`/emergency`)
and WiFi/LAN help (`/wifi`). Staff manage notices, emergency information, the resident
directory and the council budget from `/staff/notices`, `/staff/emergency`,
`/staff/residents` and `/staff/budget`. The budget starts from the council budget sheet
and is editable by office staff; the warden has read-only access.

## Run locally (no external services)

Uses the `local` profile: in-memory H2, plain substring search, no SMTP (notifications
fall back to the in-app queue).

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Open http://localhost:8080. Staff sign-in at `/staff/login`.

## Run against PostgreSQL

```bash
createdb jamunaconnect
cp .env.example .env   # fill in values
export $(grep -v '^#' .env | xargs)
mvn spring-boot:run
```

Flyway applies `db/migration/common` plus `db/migration/postgresql` (which creates the
`pg_trgm` extension and GIN indexes).

## API

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET  | `/api/rooms?query=` | public | Fuzzy room/resident lookup |
| GET  | `/api/contacts` | public | Editable contact directory |
| GET  | `/api/notices` | public | Office-posted notice board |
| GET  | `/api/emergency` | public | Emergency contacts + first-aid card |
| POST | `/api/complaints` | public | File a complaint (validated, rate-limited, idempotent) |
| GET  | `/api/complaints?status=` | staff/warden | List complaints |
| GET  | `/api/complaints/{id}/history` | staff/warden | Audit timeline |
| PATCH| `/api/complaints/{id}` | staff/warden | Append a status transition |

## Status machine

`OPEN → ACKNOWLEDGED → IN_PROGRESS → RESOLVED → WARDEN_REVIEW → CLOSED`. Office staff
move a complaint forward and forward it to the Warden for approval; only the Warden can
close. Every legal move appends a `complaint_status_history` row and is never
overwritten. Other illegal jumps (for example `OPEN → RESOLVED`) are rejected with
`409`, and role violations return `403`.

## Tests

```bash
mvn test        # unit tests (no Docker)
mvn verify      # adds Testcontainers integration tests when Docker is present
```

## Dev credentials

Seeded by `V2__seed.sql` for local development only — `office` / `office123` and
`warden` / `warden123`. Rotate before any deployment; secrets come from environment
variables, never the repository.
