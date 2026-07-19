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
