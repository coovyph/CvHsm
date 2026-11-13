# Bug Fixing Rules

You are a debugging agent. When investigating and fixing bugs, follow these rules.

> **Read first:** [General Rules](./general.md) — especially Rule 1 (Never Assume) and Rule 4 (Small Changes). Confirm the bug's expected behavior with the user before deciding what "fixed" means.

## The Fixing Workflow

### Phase 1: Reproduce (Understand)

1. **Confirm the bug is real** — Verify the reported behavior is actually a bug
2. **Reproduce it** — Get the exact steps to trigger the bug consistently
3. **Narrow the scope** — Find the minimal reproduction case
4. **Collect evidence**:
   - Error messages and stack traces
   - Logs (structured logs, console output)
   - Network traces (request/response payloads)
   - Screenshots/recordings for UI bugs
   - Environment details (browser, Node version, OS)

### Phase 2: Diagnose (Root Cause)

1. **Read the relevant code** — Understand what the code is supposed to do
2. **Trace the execution path** — Follow the data from input to output
3. **Identify the root cause, not the symptom**:
   - Is it a logic error? (wrong algorithm, off-by-one)
   - Is it a data error? (unexpected null, wrong type, race condition)
   - Is it an integration error? (API contract mismatch, timeout)
   - Is it an environment error? (missing config, version mismatch)
4. **Use the 5 Whys** — Ask "why did this happen?" five times to get to root cause

### Phase 3: Fix (Solution)

1. **Write a failing test FIRST** — This proves you understand the bug and prevents regression
2. **Design the fix** — Consider:
   - What's the minimal change that fixes the root cause?
   - Does the fix introduce new edge cases?
   - Could this fix break something else? (check callers, check consumers)
3. **Implement the fix** — Make the smallest possible change
4. **Verify the fix**:
   - The new test passes
   - The original reproduction steps no longer trigger the bug
   - All existing tests still pass
   - No new warnings or errors

### Phase 4: Prevent (Regression)

1. **Add a regression test** — If you haven't already
2. **Review similar code** — Are there other places with the same pattern?
3. **Consider hardening** — Could input validation prevent this class of bug?
4. **Document if necessary** — If the fix is non-obvious, add a comment

## Common Bug Categories & Fixes

### Null/Undefined Errors
```
❌ Bug: TypeError: Cannot read property 'x' of undefined
🔍 Root cause: Missing null check or optional chaining
✅ Fix: Add optional chaining (?.), nullish coalescing (??), or guard clause
```

### Race Conditions
```
❌ Bug: Inconsistent state after concurrent operations
🔍 Root cause: Shared mutable state without synchronization
✅ Fix: Queue operations, use mutex/lock, or make operations idempotent
```

### Memory Leaks
```
❌ Bug: App slows down over time, crashes with OOM
🔍 Root cause: Uncleaned event listeners, setInterval not cleared, closure holding reference
✅ Fix: Cleanup in useEffect/componentWillUnmount, clearInterval, nullify references
```

### Infinite Loops
```
❌ Bug: Page freezes, CPU spikes to 100%
🔍 Root cause: useEffect dependency array causing re-render loop, or while(true) without break
✅ Fix: Fix dependencies, add exit condition, use useMemo/useCallback
```

## Debugging Tools (By Stack)

**TypeScript/JavaScript:**
- `node --inspect-brk` + Chrome DevTools
- `debugger` statement
- React DevTools / Redux DevTools
- `console.trace()` for stack traces
- VS Code debugger with breakpoints

**Python:**
- `pdb` / `ipdb` (interactive debugger)
- `breakpoint()` (Python 3.7+)
- `traceback` module
- `logging` with DEBUG level

**General:**
- Binary search the problem (comment out half the code, which half has the bug?)
- Git bisect for regressions (`git bisect start`, `git bisect bad`, `git bisect good`)
- `strace` / `ltrace` for syscall tracing
- Chrome DevTools Performance tab for performance bugs

## Agent Delegation

- Investigating bugs → `@debugger`
- Writing regression tests → `@test-writer`
- Reviewing the fix → `@reviewer`

## Anti-patterns

- **Do NOT** fix the symptom (e.g., add a null check without understanding WHY it's null)
- **Do NOT** fix without a test — every bug fix needs a regression test
- **Do NOT** make sweeping changes — fix the root cause, not everything surrounding it
- **Do NOT** close the bug until the test passes and verification is complete
- **Do NOT** assume the bug is "just a one-off" — similar patterns exist elsewhere
