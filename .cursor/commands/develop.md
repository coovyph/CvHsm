---
name: develop
description: Implement a feature — read test cases first, follow the plan, commit after each task
---

# /develop — Development Workflow

You are in development mode. Follow the [Development Rules](../rules/development.md), [Git Rules](../rules/git.md), [General Rules](../rules/general.md), and [Security Rules](../rules/security.md).

## What This Command Does

Implements a planned feature or fix following the spec-driven workflow:
1. Read test cases from `.cursor/test-cases/<feature-name>.md`
2. Read the plan from `.cursor/plans/<feature-name>.md`
3. Read ADRs from `.cursor/adr/` for context on architectural decisions
4. Implement tasks in order, one at a time

## Process

### Step 1: Read the Specification
- **Read `.cursor/test-cases/<feature-name>.md`** — these are the acceptance criteria
- **Read `.cursor/plans/<feature-name>.md`** — the implementation plan
- **Read relevant ADRs** — architectural decisions that guide implementation
- **Read existing code** — all files that will be modified

### Step 2: Load Rules
Always cross-reference:
- `.cursor/rules/development.md` — coding conventions, commit format
- `.cursor/rules/security.md` — OWASP top 10, input validation, parameterized queries
- `.cursor/rules/general.md` — no assumptions, follow patterns, small changes

### Step 3: Implement (Task by Task)
For each task in the plan:
1. Mark it as in_progress
2. Write the code following existing project patterns
3. Self-review: type safety, error handling, security
4. Run build/typecheck/lint
5. Mark task as completed

### Step 4: Verify Against Test Cases
After implementing, verify your code against each test case:
- Happy path: does it work?
- Edge cases: does it handle empty, null, boundary values?
- Error states: does it fail gracefully?

### Step 5: Write Unit Tests
If a dedicated `@test-writer` hasn't been dispatched, write unit tests for the code you wrote. Follow `.cursor/rules/testing.md`.

## Agent Delegation

- Focused implementation → `@developer`
- Code review after changes → `@reviewer`
- Writing tests → `@test-writer`

## Anti-patterns

- Do NOT skip reading test cases — they are the specification
- Do NOT implement without a plan (ask for `/plan` first if no plan exists)
