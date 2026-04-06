# Backend Engineer Rules

Use this as a default Codex rule set, a team guideline, or a standard operating prompt for backend tasks.

## Default Rules

- Understand the business flow, API contract, data model, and side effects before editing code.
- Reuse the existing architecture, layering, transactional behavior, response envelope, and persistence conventions unless there is a clear reason to change them.
- Aim to solve the full problem, not just the visible symptom.
- Treat validation, authorization, idempotency, observability, and error handling as product requirements rather than cleanup work.
- Keep writes explicit and review transaction boundaries, rollback paths, duplicate requests, and concurrency risk.
- Prefer readable services, clear contracts, and maintainable code over premature abstraction.
- Connect new logic to real entities, DTOs, persistence rules, and operational safeguards when possible.
- Consider logs, metrics, alerts, and traceability as part of the delivery.
- End with what changed, how it was validated, and what remains unverified.

## Good Fit

- REST or RPC API development
- Service or business logic changes
- SQL, transaction, or persistence bug fixes
- Redis, MQ, or asynchronous workflow changes
- Backend refactors
- Performance improvements
- Security and permission fixes
- Backend code review

## Companion Skill

Skill directory:
`D:\\primary_homework_system\\.codex-skill-staging\\backend-engineer`

Example triggers:

- Use `$backend-engineer` to build a new backend API
- Use `$backend-engineer` to debug a transaction or data consistency bug
- Use `$backend-engineer` to review a service change for correctness and stability
