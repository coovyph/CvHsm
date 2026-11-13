---
name: test
description: Write and run tests — read test cases, convert to test code, verify coverage
---

# /test — Testing Workflow

You are in testing mode. Follow the [Testing Rules](../rules/testing.md), [Git Rules](../rules/git.md), and [General Rules](../rules/general.md).

## What This Command Does

Converts test case specifications into actual test code and verifies coverage:
1. **Git init** if needed; create a `test/` or `chore/` branch for test changes
2. Read test cases from `.cursor/test-cases/<feature-name>.md`
3. Convert each TC-* into actual test code (unit, integration, or e2e)
4. Run tests and verify they pass
5. Report coverage and any gaps

## Process

### Step 0: Git Setup
```bash
# Init if needed
git status 2>&1 || (git init && git add -A && git commit -m "chore: initial commit")

# If adding new tests for a feature, create a branch
git checkout main
git checkout -b test/<feature-name>   # or chore/<task>
```
All test changes go on a branch. Never commit test changes directly to main.

### Step 1: Read the Specification
- **Read `.cursor/test-cases/<feature-name>.md`** — the test case specification
- **Read `.cursor/plans/<feature-name>.md`** — for context on the implementation
- **Read the code to be tested** — understand the interfaces, types, and behaviors

### Step 2: Convert Test Cases to Code
For each TC-* in the test case file:

1. Determine the test type:
   - `unit` → test single function/component in isolation
   - `integration` → test multiple units working together
   - `e2e` → test full user flow (browser or API)

2. Write the test following **AAA pattern**:
   ```
   // Arrange — set up the Given conditions
   // Act — execute the When action
   // Assert — verify the Then outcome
   ```

3. Follow naming convention:
   ```
   describe('<Unit>', () => {
     it('should <expected behavior> when <condition>', () => { ... });
   });
   ```

4. Always include test cases from all categories:
   - Happy path
   - Empty/null input
   - Boundary values
   - Error states (validation, auth, network, server)
   - State transitions (if applicable)

### Step 3: Run & Verify
- Run the specific test file first
- Run the full test suite to check for regressions
- Verify coverage meets thresholds (from `.cursor/rules/testing.md`)
- If tests fail: fix the test if it's wrong, or flag the implementation if the test is correct

### Step 4: Report
Output a summary:
- Number of test cases from specification
- Number of tests written
- Coverage percentage
- Any uncovered cases or gaps
- Test results (pass/fail)

## Agent Delegation

- Writing comprehensive tests → `@test-writer` (self-validates coverage and logic)
- Running tests and reporting regressions → `@qa` (read-only, reports with reproduction steps)

## Coverage Thresholds

| Layer | Min Coverage |
|-------|-------------|
| Utility/pure functions | 95%+ |
| Business logic | 90%+ |
| API endpoints | 85%+ |
| UI components | 80%+ |

## Anti-patterns

- Do NOT write tests without reading the test case specification
- Do NOT test third-party internals
- Do NOT write tests that depend on order
- Do NOT skip flaky tests — fix or remove them
