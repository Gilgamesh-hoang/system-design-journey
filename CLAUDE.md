# CLAUDE.md

Context for Claude Code sessions in this repo. Read `learning-log.md` before starting work — it has the latest decisions, trade-offs, and next steps from theory sessions (done in Cowork/Chat, not here).

## What this repo is

Personal learning project: System Design for a Java/Spring Boot backend engineer, junior → middle.
Full roadmap, rationale, and interview questions per topic: `system-design-backend-roadmap.md`.

## Structure

- `ridenow-app/` — flagship project (ride-hailing system), evolved module by module. Has its own `README.md` with current architecture + an ADR (decision) log.
- `katas/mN-*/NOTES.md` — small isolated exercises, one per mechanism. Not production code; OK to be rough/thrown away.
- `design-docs/*.md` — paper-only interview case studies (diagram + trade-offs), no code.
- `learning-log.md` — chronological log of theory-session takeaways. Update this file after each Cowork/Chat theory session; this file is the bridge so Claude Code doesn't need the chat history.

## Conventions

- Package `ridenow-app` by domain (`trip`, `driver`, `pricing`, `notification`, `common`), not by layer — it's meant to split into microservices from Module 5 onward.
- Commit messages prefixed by module: `[M2] kata: LRU cache`, `[M6] flagship: circuit breaker for pricing`.
- Kata code can be quick/rough. `ridenow-app` code should be held to a higher bar — it's the portfolio piece.
- When starting a new module's flagship work, check `ridenow-app/README.md` for the current architecture before changing it.

## When helping with kata or flagship work

Assume the goal is deep understanding for interviews, not just a working demo. Prefer explaining trade-offs over silently picking one, and flag when a "quick" implementation is skipping something a real interview answer would need to mention.

## Proactive logging — do this without being asked

- After finishing meaningful work on a kata or a flagship step (not every small edit — when a concept has actually been implemented or a real decision made), append a short entry to `learning-log.md` yourself, following the format already in that file. Don't wait to be told.
- If the work involved a real trade-off (why Kafka over RabbitMQ, why this shard key, why CQRS here but not there — the kind of thing an interviewer would ask "why not X?" about), create a new ADR in `ridenow-app/docs/adr/` following the format in `0001-record-architecture-decisions.md`, and update `ridenow-app/README.md`'s "Current stage" / "Architecture snapshot" sections to match.
- Mention what you logged/wrote at the end of your reply so it's visible, not silent.
- This does NOT apply to theory-only discussion that happens in Cowork/Chat — Claude Code has no visibility into those sessions. The user (or the Cowork session, when asked) is responsible for logging theory takeaways into `learning-log.md`.
