# code-flash

A personal spaced repetition system for LeetCode problems. Import your problem lists, solve problems, rate your confidence, and let the SRS engine schedule what to review next — so you stop forgetting approaches and start recognising patterns.

---

## What it does

- **Imports** problems from LeetCode lists, study plans, or individual slugs via the unofficial GraphQL API
- **Schedules** review sessions using SM-2 with a velocity adjustment — problems you struggle with come back sooner, problems you ace get pushed back
- **Tracks** solve history with personal notes, confidence ratings, and SRS state per problem
- **Surfaces** weakest patterns on the dashboard so you know where to focus
- **Supports** three review modes: Due Only, Sequential, and Mixed

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Database | PostgreSQL |
| ORM | Spring Data JPA (Hibernate 7) |
| Migrations | Flyway |
| HTTP client | Spring WebFlux WebClient |
| Frontend | Thymeleaf + Bootstrap 5 |
| Build | Gradle |

### Design patterns used

| Pattern | Where |
|---|---|
| Strategy | SRS algorithm (`SM2Algorithm`, `VelocityAdjustedAlgorithm`) |
| Factory | Problem importers (`ImporterFactory`) |
| Observer | `SolvedEvent` via `ApplicationEventPublisher` |
| Template Method | `ProblemImporter` base class |
| Builder | All entities and DTOs |
| DTO / Mapper | Clean separation between API response, domain, and DB entity |

---

## Prerequisites

- Java 21+
- PostgreSQL 14+
- Gradle 8+ (or use the included `./gradlew` wrapper)
- A LeetCode account (for list imports)

---

## Setup

### 1. Create the database

```bash
psql -U postgres -c "CREATE DATABASE codeflash;"
```

### 2. Configure `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/codeflash
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        jdbc:
          batch_size: 100
        order_inserts: true
        order_updates: true
        batch_versioned_data: true

  flyway:
    enabled: false   # Flyway is run manually — set to true to auto-migrate on startup

leetcode:
  session-cookie: "YOUR_LEETCODE_SESSION"
  csrf-token: "YOUR_CSRF_TOKEN"

algorithm:
  type: velocity     # Options: sm2 | velocity

enrichment:
  enabled: true
  batch-size: 100
  interval-days: 1

backup:
  directory: ${user.home}/codeflash-backups
  db-name: codeflash
  db-user: postgres
  retention-days: 30
  pg-dump-path: /opt/homebrew/opt/postgresql@16/bin/pg_dump      # macOS Homebrew default
  pg-restore-path: /opt/homebrew/opt/postgresql@16/bin/psql
  restore:
    enabled: false
```

> Adjust `pg-dump-path` and `pg-restore-path` to match your PostgreSQL installation path. On Linux these are typically `/usr/bin/pg_dump` and `/usr/bin/psql`.

### 3. Get your LeetCode session cookie

1. Log in to leetcode.com
2. Open DevTools → Application → Cookies → `leetcode.com`
3. Copy `LEETCODE_SESSION` and `csrftoken` values into `application.yml`

> The session cookie expires every few weeks. Paste a new one into `application.yml` when imports start failing.

### 4. Run

```bash
./gradlew bootRun
```

Or build a jar and run it:

```bash
./gradlew build
java -jar build/libs/code-flash-0.0.1-SNAPSHOT.jar
```

Flyway runs migrations automatically on startup. The app is available at `http://localhost:8080`.

---

## Project structure

```
src/main/java/com/codeflash/
├── client/                  # LeetCode GraphQL client + URL parser
├── config/                  # Spring config classes
├── controller/
│   ├── rest/                # REST API controllers
│   └── ui/                  # Thymeleaf view controllers
├── domain/                  # Pure domain models (no Spring/JPA)
├── dto/                     # Request/response DTOs
├── entity/                  # JPA entities
├── repository/              # Spring Data JPA repositories
└── service/
    ├── importer/            # Import pipeline (Template Method)
    └── srs/                 # SRS algorithm implementations

src/main/resources/
├── db/migration/            # Flyway SQL migrations (V1–V4)
└── templates/               # Thymeleaf HTML templates
```

---

## SRS algorithm

Two implementations of `SRSAlgorithm` (Strategy pattern):

**SM2Algorithm** — the classic Anki algorithm:
- `AGAIN` → interval resets to 1, ease −0.20
- `HARD` → interval × 1.2, ease −0.15
- `GOOD` → interval × ease
- `EASY` → interval × ease × 1.3, ease +0.15
- Ease clamped between 1.3 and 2.5

**VelocityAdjustedAlgorithm** — wraps SM2 and compresses intervals when you're solving frequently:
- Computes a velocity factor from solves in the last 3 days vs a baseline of 3/day
- Factor ≥ 1.0 — intervals only ever compress, never expand
- High velocity → more frequent review; low velocity → SM2 baseline

---

## Enrichment

A scheduled job (`PatternListEnricher`) runs periodically to fetch topic tags and company tags for all problems via the LeetCode GraphQL API. It:
- Runs on a configurable interval (default 24h)
- Processes problems in batches with `entityManager.flush()+clear()` per batch
- Is idempotent — safe to re-run, uses `addAll()` on the existing tag collection
- Tracks last run time in `app_settings` table

To force a re-run:
```bash
psql -U postgres -d codeflash -c "UPDATE app_settings SET value = '2000-01-01T00:00:00' WHERE key = 'last_enriched_at';"
```

---

## API endpoints

### Problems
| Method | Path | Description |
|---|---|---|
| GET | `/problems` | Problems page with filters |
| GET | `/problems/{id}` | Problem detail + solve history |
| GET | `/problems/random` | Redirect to a weighted random problem |

### Review
| Method | Path | Description |
|---|---|---|
| GET | `/review` | Review session (mode selector or active session) |
| POST | `/api/problems/{id}/solve` | Record a solve with confidence rating + notes |

### Import
| Method | Path | Description |
|---|---|---|
| GET | `/import` | Import page |
| POST | `/api/import` | Trigger an import |
| GET | `/api/import/lists/available` | Fetch your LeetCode lists |

### Lists
| Method | Path | Description |
|---|---|---|
| GET | `/api/lists` | All imported lists with problem counts |
| PATCH | `/api/lists/{id}/rename` | Rename a list |
| GET | `/api/lists/{id}/topics` | Topic breakdown for a list |

### Stats
| Method | Path | Description |
|---|---|---|
| GET | `/stats` | Stats page |
| GET | `/api/stats/heatmap` | Activity heatmap data |

---

## Database migrations

| Version | Description |
|---|---|
| V1 | Initial schema — problems, tags, lists, srs_states, solve_records |
| V2 | App settings table |
| V3 | Company tags support (`tag_type` column) |
| V4 | Position column on `problem_list_items` for sequential ordering |

---

## Known limitations

- LeetCode session cookie must be manually refreshed every few weeks
- Company tags are populated by the enrichment job, not at import time
- No authentication — single-user personal tool only
- React frontend migration is planned (see `USEME.md` for roadmap)