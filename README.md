# Financial Invoice Analytics Platform





https://github.com/user-attachments/assets/ed8cffab-7116-450e-b582-1f97b24aa023



**OCR invoice extraction + automation**, end to end: ingest scanned vendor
invoice images (as structured JSON, as if output by a legacy OCR/back-office system), validate the
extraction, land it in a Snowflake star schema, and expose AP analytics (spend by seller, invoice
aging, extraction-quality exceptions) through a Spring Boot API and a React dashboard.

## High-level architecture

<img width="1485" height="981" alt="image" src="https://github.com/user-attachments/assets/562e2950-0ea2-4eba-b10c-86eeefac1999" />


- **Single entrypoint**: nginx is the only service bound to a host port. `frontend` and `backend`
  are internal-only 
- **CI/CD runs on the same box it deploys to** — Jenkins detects which of `backend/`/`frontend`
  changed per commit and only rebuilds/redeploys that image.


https://github.com/user-attachments/assets/8caba077-7eb7-4bf0-8ed1-5d5ebab8744a

## Tech stack

| Layer | Choice | Why |
|---|---|---|
| Backend | Java 21, Spring Boot 3, `NamedParameterJdbcTemplate` | Snowflake is OLAP — an ORM adds indirection, not value |
| Frontend | React 19 + TypeScript, Vite, amCharts 5 | Typed API layer, imperative charting kept out of components |
| Warehouse | Snowflake — star schema + dynamic tables + streams/tasks | Semi-structured JSON via `VARIANT`, declarative refresh DAG |
| Object storage | AWS S3 (SDK v2) | Immutable audit copy of every uploaded file, independent of the warehouse write |
| Gateway | nginx | Single entrypoint, reverse proxy, rate limiting |
| CI/CD | Jenkins (on the same EC2 host) | Path-based builds, `docker compose` deploy, free-tier disk/executor constraints |
| Container runtime | Docker Compose | Whole stack reproducible with one command, no orchestrator needed at this scale |

## Repo layout

```
financial-invoice-analytics-platform/
├── backend/           # Spring Boot API — controller → service → repository, DTOs at the boundary
├── frontend/          # React + TypeScript dashboard — own Dockerfile, served by its own nginx
├── nginx/             # gateway config — reverse proxy + rate limiting, no static assets baked in
├── snowflake/          # schema, stage, pipe, dynamic-table, stream, task, sample-query
├── sample-data/        # real OCR invoice-extraction sample CSV/JSON
├── docker-compose.yml
└── Jenkinsfile
```

## Running it locally

```bash
docker compose up -d --build
```

Backend needs Snowflake + AWS credentials as env vars (`SNOWFLAKE_ACCOUNT`, `SNOWFLAKE_USER`,
`SNOWFLAKE_PASSWORD`, `SNOWFLAKE_WAREHOUSE`, `SNOWFLAKE_DB`, `SNOWFLAKE_SCHEMA`,
`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`) — put them in a `.env` next to `docker-compose.yml`
(gitignored; never commit it). Without them, the backend falls back to no-op repository/writer
beans rather than failing to start.

Once up: the dashboard is at `http://localhost/`, the API at `http://localhost/api/v1/...`.
