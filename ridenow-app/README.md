# RideNow — Flagship Project

Ride-hailing backend (Grab/Uber-style), built module by module alongside the System Design roadmap (`../system-design-backend-roadmap.md`).

## Current stage

**Module:** _not started yet_
**Shape:** single Spring Boot module (pre-split monolith)

Update this section whenever the project moves to a new module — one line is enough (e.g. "M3 — added Redis cache-aside for driver info").

## Milestone backlog

Per-module specs (what to build, requirements, Definition of Done, ADR to write, mermaid diagram) live in `docs/milestones/` — one file per module, `M1`–`M8`. Read the milestone file **before** starting a module; check off its Definition of Done as you go. M0 is the paper design doc at `../design-docs/M0-ridenow-v1-design-doc.md`.

## Architecture snapshot

_To be filled in once M0/M1 are underway: services (or lack thereof), datastores, and how they talk to each other. Keep this current — it's what you'll narrate in interviews._

## Decisions (ADR log)

Significant design decisions live in `docs/adr/` as short numbered records (what was decided, why, what was traded off). Start a new ADR whenever you make a choice you'd want to defend in an interview — not for every commit, just the ones with a real trade-off behind them.

See `docs/adr/0001-record-architecture-decisions.md` for the format.

## Running locally

_Fill in once there's something to run: `./gradlew bootRun`, required Docker Compose services, env vars, etc._
