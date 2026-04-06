# Frontend Quality Checklist

Use this checklist when the task touches shared components, page-level flows, complex forms, async state, or visual polish.

## Scope and Context

- Confirm the affected route, page, component ownership, and user goal.
- Confirm whether the task is a new feature, a regression fix, a refactor, or a UX polish pass.
- Check existing patterns before inventing a new state, layout, hook, or API wrapper.

## State Coverage

- Handle loading, empty, error, disabled, success, and permission states when relevant.
- Check null, undefined, partial, stale, and slow responses.
- Verify filter, search, sort, pagination, tab, and modal state interactions.
- Prevent async race conditions, duplicate submits, and stale closures where applicable.

## Data and Integration

- Verify request and response field names, nullability, defaults, and enum values.
- Check optimistic updates, rollback behavior, retry paths, and error messaging.
- Reset or preserve state intentionally after route changes, modal close, or submit success.
- Make sure placeholders and mock values do not leak into production code.

## Layout and Responsive Behavior

- Check mobile, tablet, and desktop breakpoints that matter for the page.
- Verify overflow, sticky areas, scroll containers, long text, empty cards, and dynamic height changes.
- Confirm spacing, alignment, and visual hierarchy against nearby screens or the design system.

## Accessibility

- Prefer semantic elements before adding ARIA.
- Ensure labels, accessible names, and helper text exist for interactive controls.
- Check tab order, focus visibility, dialog focus management, and keyboard escape paths.
- Avoid color-only status communication when a text cue is easy to add.

## Performance

- Watch for avoidable re-renders, expensive derived state, and repeated network requests.
- Use debouncing, lazy loading, pagination, or virtualization when list size or interaction frequency justifies it.
- Size images and media intentionally to avoid layout shift.

## Verification

- Run the lightest meaningful validation available: lint, typecheck, build, tests, or focused manual review.
- If tooling is unavailable, state what was not verified and what regression risk remains.
