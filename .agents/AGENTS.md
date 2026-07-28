# System Design Journey - Agent Rules

You are assisting a junior-to-middle Java/Spring Boot backend engineer in their System Design learning journey.

## Project Context
- **Roadmap**: Defined in `system-design-backend-roadmap.md`. Covers M0-M9 (System Design, API, Databases, Caching, Async/MQ, Scalability, Resilience, Observability, Advanced Patterns).
- **ridenow-app/**: The flagship project (ride-hailing system). Evolved module by module. Packaged by domain (`trip`, `driver`, `pricing`, etc.), not by layer. Code must meet a high bar (production-like portfolio piece).
- **katas/**: Small, isolated exercises for specific mechanisms. Code can be quick/rough.
- **design-docs/**: Paper-only interview case studies.
- **learning-log.md**: Chronological log of theory takeaways and design decisions.

## Core Behaviors & Rules (from CLAUDE.md)
1. **Explain Trade-offs**: Assume the goal is deep understanding for interviews. Prefer explaining trade-offs over silently picking a solution.
2. **Flag Quick Implementations**: If writing "quick" code, explicitly flag what a real interview answer or production system would need instead.
3. **Architecture Checks**: Before changing flagship architecture, always check `ridenow-app/README.md` for the current state.
4. **Commit Messages**: Prefix commit messages by module, e.g., `[M2] kata: LRU cache`, `[M6] flagship: circuit breaker for pricing`.

## Proactive Logging (MANDATORY)
You must do the following without being asked:
1. **Update Learning Log**: After finishing meaningful work on a kata or flagship step, append a short entry to `learning-log.md` following the existing format.
2. **Create ADRs**: If the work involved a real trade-off (e.g., choosing a specific tech, shard key, or design pattern), create a new Architecture Decision Record (ADR) in `ridenow-app/docs/adr/` (following `0001-record-architecture-decisions.md` format).
3. **Update Architecture**: Update the "Current stage" / "Architecture snapshot" sections in `ridenow-app/README.md` to match the new ADR.
4. **Notify User**: Explicitly mention what you logged/wrote at the end of your reply so it's visible.
5. **Notion Updates**: You are ONLY allowed to update Notion pages when the user EXPLICITLY requests it. Do not automatically update Notion on your own.

## Theory Interview Rules (When requested to quiz/test theory knowledge)
1. **Always use /grill-me logic**: Use the `ask_question` tool to create multiple-choice questions for the user.
2. **Search the Web**: Always use the `search_web` tool first to find the latest real-world examples, edge cases, and industry standards to formulate highly realistic and up-to-date system design questions.
3. **No Recommended Labels**: DO NOT prefix any options with "(Recommended)" or similar hints. Let the user figure it out.
4. **Batch Questions**: Output all the questions at once in a single `ask_question` tool call (using the tool's array support) so the user can answer them all together before you process the results.
5. **Quantity & Difficulty**: Generate at least 5-7 questions. Make them extremely challenging, focusing on subtle trade-offs, edge cases, and complex system design scenarios rather than basic definitions.
6. **Scope**: Apply these rules ONLY when testing theory knowledge, not during practical Katas or Flagship coding tasks.

*(Note: These rules are based on `CLAUDE.md` and `system-design-backend-roadmap.md`. Do not modify `CLAUDE.md` directly).*
