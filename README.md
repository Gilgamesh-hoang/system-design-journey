# System Design Journey — Java Backend, Junior → Middle

Personal learning repo following the System Design roadmap (see `system-design-backend-roadmap.md`).
Full roadmap details, trade-offs, and interview questions per topic live in that file — this README is just the map.

## Structure

- `ridenow-app/` — Flagship project (ride-hailing system), evolved module by module. Has its own README with architecture notes and an ADR (decision) log.
- `katas/` — Small, isolated exercises, one per mechanism. Each folder has a `NOTES.md`: problem, approach, trade-offs, related interview questions.
- `design-docs/` — Paper-only case studies (diagram + trade-offs, no code) for classic interview questions.

## Kata index

| Folder | Module | Concept |
|---|---|---|
| `m0-01-capacity-estimation` | M0 | Back-of-envelope estimation for RideNow |
| `m1-01-idempotency-key` | M1 | Idempotency key for POST endpoints |
| `m2-01-mysql-replication` | M2 | Master-slave replication with Docker Compose |
| `m2-02-consistent-hashing` | M2 | Consistent hashing ring with virtual nodes |
| `m2-03-n-plus-one-and-indexing` | M2 | Detect & fix N+1, read EXPLAIN, indexing |
| `m3-01-lru-cache` | M3 | LRU cache from scratch |
| `m3-02-cache-stampede` | M3 | Cache stampede — lock / request coalescing |
| `m4-01-idempotent-kafka-consumer` | M4 | Idempotent consumer + dead-letter queue |
| `m5-01-load-balancer-nginx` | M5 | Nginx load balancing + health check |
| `m6-01-rate-limiter` | M6 | Token bucket / sliding window, Redis-backed |
| `m6-02-circuit-breaker-resilience4j` | M6 | Circuit breaker + retry + fallback |
| `m7-01-observability-prometheus-grafana` | M7 | Metrics, dashboard, alerting |
| `m8-01-cqrs-read-model` | M8 | CQRS with a materialized read model |

## Design-doc index (interview case studies)

`url-shortener` · `rate-limiter` · `notification-service` · `news-feed` · `chat-app` · `ride-hailing` · `ticket-booking` · `distributed-message-queue`

## Status

Not started yet — repo structure only. Update this section as modules are completed.
