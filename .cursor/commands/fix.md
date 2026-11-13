---
name: fix
description: Debug and fix a bug — reproduce, diagnose root cause, write regression test, fix, verify
---

# /fix — Bug Fixing Workflow

You are in debugging mode. Follow the [Fixing Rules](../rules/fixing.md), [Git Rules](../rules/git.md), [General Rules](../rules/general.md), and [Security Rules](../rules/security.md).

## What This Command Does

Investigates and fixes a bug using a rigorous 4-phase process:
1. **Git init** if needed; create a `fix/` or `bugfix/` branch
2. **Reproduce** — Confirm and isolate the bug
3. **Diagnose** — Find the root cause (not the symptom)
4. **Fix** — Write a regression test, then implement the minimal fix
5. **Prevent** — Check for similar patterns, add tests
6. **Do NOT merge** unless user explicitly asks

## Process

### Phase 0: Git Setup
```bash
# Init if needed
git status 2>&1 || (git init && git add -A && git commit -m "chore: initial commit")

# Create fix branch
git checkout main
git checkout -b fix/<bug-description>    # or bugfix/<urgent-bug>
```
Never fix on `main`. Always on a branch. **Do not merge** without explicit approval.

### Phase 1: Reproduce (Understand)

1. **Confirm the bug is real** — Verify the reported behavior is actually unintended
2. **Reproduce it** — Get the exact steps to trigger the bug consistently
3. **Narrow the scope** — Find the minimal reproduction case
4. **Collect evidence**:
   - Error messages and stack traces
   - Logs (request logs, error logs, console output)
   - Network traces (request/response payloads)
   - Environment details (version, OS, browser)

### Phase 2: Diagnose (Root Cause)

1. **Read the relevant code** — Understand what the code is *supposed* to do
2. **Trace the execution path** — Follow the data from input to output
3. **Identify the root cause** — Use the 5 Whys technique:
   - Ask "why did this happen?" five times to drill past symptoms
   - Is it a logic error? (off-by-one, wrong algorithm)
   - Is it a data error? (unexpected null, race condition, wrong type)
   - Is it an integration error? (API contract mismatch, timeout)
   - Is it an environment error? (missing config, version mismatch)

### Phase 3: Fix (Solution)

1. **Add a test case** to `.cursor/test-cases/<feature-name>.md` (or create if doesn't exist)
2. **Write a failing regression test** — This proves the bug exists
3. **Implement the minimal fix** — Smallest change that addresses the root cause
4. **Consider edge cases** — Does the fix handle adjacent scenarios?
5. **Check for side effects** — Could this break callers or consumers?
6. **Verify**:
   - The new regression test passes
   - The original reproduction steps no longer trigger the bug
   - All existing tests still pass
   - No new warnings or errors

### Phase 4: Prevent (Regression)

1. **Review similar code** — Are there other places with the same pattern?
2. **Consider hardening** — Would input validation prevent this class of bug?
3. **Add a comment** — If the fix is non-obvious, document *why* it works
4. **Update test cases** — Mark the new test case as implemented

## Debugging Tools

Adapt to the project's stack:

**JavaScript/TypeScript:**
- `node --inspect-brk` + Chrome DevTools
- `debugger` statement
- `console.trace()` for stack traces
- VS Code debugger with breakpoints
- React DevTools / Redux DevTools

**Python:**
- `pdb` / `ipdb` / `breakpoint()`
- `traceback` module
- `logging` with DEBUG level

**General:**
- Git bisect for regressions: `git bisect start && git bisect bad && git bisect good <commit>`
- Binary search the problem (comment out half the code to narrow it down)

## Agent Delegation

- Investigating complex bugs → `@debugger`
- Writing regression tests → `@test-writer`
- Reviewing the fix → `@reviewer`
- Security implications → `@security`

## Output

After fixing, report:
- Root cause (one sentence)
- The fix (one sentence)
- Files changed
- Test added
- Verification result (all tests passing)

## Anti-patterns

- Do NOT fix the symptom (e.g., add a null check without understanding WHY it's null)
- Do NOT fix without adding a test case and a regression test
- Do NOT make sweeping changes — fix the root cause, not everything around it
- Do NOT assume the bug is "just a one-off" — look for similar patterns
