# 1. Record architecture decisions

## Status
Accepted

## Context
RideNow is a learning project meant to produce not just working code but a defensible, explainable design — the kind you'd walk an interviewer through. Decisions made along the way (why Kafka over RabbitMQ, why this shard key, why CQRS here but not there) are easy to forget once the code exists. Without a record, "why" gets lost and the project reverts to just being a demo.

## Decision
Record significant decisions as lightweight ADRs (Architecture Decision Records) in `docs/adr/`, one file per decision, numbered sequentially: `NNNN-short-title.md`.

Each ADR follows this shape:
- **Status** — proposed / accepted / superseded
- **Context** — the problem or question that forced a decision
- **Decision** — what was chosen
- **Trade-off** — what was given up, and when this decision should be revisited

Not every change needs one — only decisions with a real trade-off behind them (the kind an interviewer would ask "why not X instead?" about).

## Trade-off
Extra writing overhead per decision, in exchange for a ready-made narrative for interviews and a way to avoid re-litigating settled questions later in the project.
