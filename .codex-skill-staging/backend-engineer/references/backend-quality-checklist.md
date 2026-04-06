# Backend Quality Checklist

Load this checklist when a task affects write paths, external integrations, background jobs, permissions, or shared backend logic.

## Contract Correctness

- Validate required fields, types, enums, ranges, nullability, and default values.
- Confirm response shape, status codes, error codes, and exception mapping stay consistent.
- Check backward compatibility for existing clients, scheduled jobs, and downstream consumers.

## Data Safety

- Verify transaction boundaries around multi-step writes.
- Check idempotency for create, submit, pay, retry, callback, and batch operations.
- Review concurrency risk: duplicate requests, stale writes, lost updates, lock scope, and retry safety.
- Confirm schema, index, migration, and backfill impact when persistence changes.

## Integration Resilience

- Set explicit timeout, retry, and fallback behavior for remote calls.
- Preserve message ordering, deduplication, and dead-letter handling for MQ workflows.
- Verify cache invalidation, TTL, and cache consistency rules when cached data is touched.

## Security And Compliance

- Check authentication, authorization, tenant isolation, and data-scope rules.
- Sanitize input; guard against SQL injection, deserialization issues, and unsafe file handling.
- Avoid leaking secrets, tokens, personal data, or internal stack traces in logs and responses.

## Observability

- Add or preserve structured logs on critical branches and failures.
- Keep traceable identifiers such as request ID, order ID, user ID, or task ID in logs when appropriate.
- Confirm metrics or alerts exist for business-critical failures, retry storms, and downstream dependency issues.

## Performance

- Review query count, N+1 patterns, index usage, pagination, and expensive serialization.
- Check whether hot paths need batching, caching, async processing, or reduced lock contention.

## Validation

- Cover normal, invalid, empty, duplicate, timeout, and downstream failure scenarios.
- Prefer targeted tests for business invariants and edge cases over shallow coverage.
- If testing cannot run, state the exact gap and the most likely residual risk.
