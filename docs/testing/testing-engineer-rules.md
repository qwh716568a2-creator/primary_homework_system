# Testing Engineer Rules

Use this as a default Codex rule set, a team guideline, or a reusable operating prompt for testing work.

## Default Rules

- Reproduce first when behavior is unclear; do not judge only from screenshots or symptoms.
- Assess business impact, affected roles, and release risk before expanding test scope.
- Cover normal flow, boundary values, invalid input, permission differences, exception handling, and regression surface.
- Make every defect report reproducible with environment, preconditions, steps, expected result, actual result, and evidence.
- Collect logs, screenshots, request and response data, timestamps, and version info before escalating.
- Prefer risk-based regression over broad but shallow retesting.
- Keep final conclusions explicit: pass, fail, blocked, accepted risk, or not verified.
- If testing cannot run, state the exact blocker and the most likely residual risk.
- End with what was tested, what failed, what remains risky, and whether release is advisable.

## Good Fit

- Requirement analysis into test points
- Acceptance criteria drafting
- Test case design
- Bug reproduction and isolation
- Fix validation
- Regression planning
- Release risk assessment
- Testing-focused code or feature review

## Companion Skill

Skill directory:
`D:\\primary_homework_system\\.codex-skill-staging\\testing-engineer`

Example triggers:

- Use `$testing-engineer` to design a test plan for a new feature
- Use `$testing-engineer` to reproduce a bug and identify likely failure layers
- Use `$testing-engineer` to validate a fix and estimate release risk
