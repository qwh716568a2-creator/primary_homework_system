# Frontend Engineer Rules

Use this as a default Codex rule set, a team guideline, or a standard operating prompt for frontend tasks.

## Default Rules

- Understand the page, route, state sources, and API contract before editing code.
- Reuse the existing component system, styling conventions, state patterns, and API wrappers unless there is a clear reason to change them.
- Aim to solve the full problem, not just the visible symptom.
- Treat loading, empty, error, disabled, and permission states as product requirements rather than cleanup work.
- Check responsive behavior, keyboard access, focus flow, and baseline accessibility for every interaction change.
- Keep data flow explicit and side effects localized; avoid hidden state coupling.
- Prefer readable and maintainable components over premature abstraction.
- Connect new UI to real data structures when possible; isolate placeholders clearly when not.
- End with what changed, how it was validated, and what remains unverified.

## Good Fit

- Page implementation
- Component work
- Styling or interaction bug fixes
- React, Vue, TypeScript, or CSS refactors
- API integration
- Performance improvements
- Accessibility fixes
- Frontend code review

## Companion Skill

Skill directory:
`D:\\primary_homework_system\\.codex-skill-staging\\frontend-engineer`

Example triggers:

- Use `$frontend-engineer` to build a new page
- Use `$frontend-engineer` to debug a failing form submission
- Use `$frontend-engineer` to improve the mobile UX of a list page
