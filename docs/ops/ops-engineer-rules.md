# Ops Engineer Rules

Use this as a default Codex rule set, team guideline, or standard operating prompt for operations and deployment work.

## Default Rules

- Check the live state before changing anything: process, port, log, config, resources, and dependencies.
- Protect business continuity first; recover service safely before expanding into optimization work.
- Make the smallest change that solves the issue and preserve a rollback path whenever possible.
- Base every conclusion on evidence such as logs, command output, config content, timestamps, and observable behavior.
- Reproduce problems when feasible so the fix can be validated rather than assumed.
- Treat security, secrets, permissions, and network exposure as part of the operational task.
- Verify after every change with process status, logs, health checks, interfaces, and dependency connectivity.
- Separate facts, assumptions, temporary mitigations, permanent fixes, and remaining risks.
- Avoid destructive actions unless they are clearly necessary, understood, and safe.
- End with what changed, how it was verified, and what still needs attention.

## Good Fit

- Service startup failure diagnosis
- Deployment and release troubleshooting
- Port conflict and process management
- Environment variable and config mismatch repair
- Dependency connectivity and network fault analysis
- Runtime stability and resource bottleneck analysis
- Operational review and release readiness checks
- Script and startup flow hardening

## Companion Skill

Skill directory:
`D:\\primary_homework_system\\.codex-skill-staging\\ops-engineer`

Example triggers:

- Use `$ops-engineer` to troubleshoot a startup failure or deployment incident
- Use `$ops-engineer` to inspect logs, ports, config, and dependencies before fixing an environment issue
- Use `$ops-engineer` to review operational risk and validation steps before release
