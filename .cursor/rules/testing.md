# Testing Rules

You are a testing agent. When writing, running, or maintaining tests, follow these rules.

> **Read first:** [General Rules](./general.md) — especially Rule 1 (Never Assume), Rule 3 (Follow Patterns), and Rule 5 (Always Verify). Ask before setting coverage thresholds or choosing test frameworks the project doesn't already use.

## Testing Philosophy

- **Test behavior, not implementation** — Tests should survive refactors
- **Every bug gets a test** — Fix a bug → write a regression test first
- **Tests are documentation** — A test suite should explain what the code does
- **Fast feedback loop** — Unit tests < 1s, integration < 10s, e2e < 5min

## Test Structure

Follow **Arrange-Act-Assert (AAA)** pattern:

```
// Arrange — set up test data and mocks
// Act — execute the code under test
// Assert — verify the outcome
```

## Coverage Requirements

| Layer | Minimum Coverage | Type |
|-------|-----------------|------|
| Utility/pure functions | 95%+ | Unit |
| Business logic | 90%+ | Unit + Integration |
| API endpoints | 85%+ | Integration |
| UI components | 80%+ | Unit (render + interaction) |
| Configuration/routes | Spot check | Unit |

## What to Test

### Always Test
- **Edge cases** — empty input, null/undefined, boundary values, large inputs
- **Error paths** — invalid input, network failures, timeouts, auth failures
- **Happy path** — the primary use case
- **State transitions** — loading → success, loading → error, initial → modified
- **Race conditions** — concurrent operations, debounced inputs

### Never Test
- Third-party library internals (test your usage of them)
- Trivial getters/setters
- Framework boilerplate (e.g., React component rendering from a library)

## Test Types

1. **Unit tests** — Single function/component in isolation
   - Mock external dependencies
   - Fast (< 1s per test file)
   - Framework: Jest, Vitest, pytest, etc.

2. **Integration tests** — Multiple units working together
   - Real database (test instance), real HTTP calls (test server)
   - Verify contracts between modules
   - Framework: Supertest, React Testing Library, pytest + fixtures

3. **E2E tests** — Full user flows through the application
   - Real browser or API client
   - Test critical user journeys only
   - Framework: Playwright, Cypress

4. **Snapshot tests** — Visual/structural regression
   - Use sparingly (only for stable, non-volatile output)
   - Never snapshot large component trees

## Naming Convention

```
describe('<Unit Name>', () => {
  it('should <expected behavior> when <condition>', () => {
    // ...
  });
});
```

Example:
```
describe('calculateTotal', () => {
  it('should return 0 when cart is empty', () => { ... });
  it('should apply discount when promo code is valid', () => { ... });
  it('should throw ValidationError when quantity is negative', () => { ... });
});
```

## Mocking

- Mock at boundaries (HTTP, database, filesystem, third-party APIs)
- Don't mock what you don't own (wrap third-party libs in your own adapter)
- Use spies to verify interactions, stubs to control dependencies
- Clear mocks between tests (`beforeEach(() => jest.clearAllMocks())`)

## Running Tests

- Run the specific test file first, then the full suite
- Use `--watch` mode during development
- CI must run the full test suite + lint + typecheck
- Never merge with failing tests

## Test-First (Spec-Driven Development)

**Test cases are written during planning — before any code is written.** They serve as the specification that drives development, unit testing, and endpoint testing.

### The Workflow

```
PLAN → WRITE TEST CASES → DEVELOP → UNIT TEST → ENDPOINT TEST → QA
  ↑                                                                    ↓
  └──────────────────── ALL REFERENCE SAME TEST CASES ─────────────────┘
```

1. **Planning phase** — Test cases are created and saved to `.cursor/test-cases/<feature-name>.md`
2. **Development phase** — Developer reads test cases before implementing; each task maps to one or more test cases
3. **Unit testing** — `@test-writer` converts test cases into actual unit/integration tests
4. **Endpoint testing** — `@qa` uses the same test cases to verify the feature end-to-end
5. **Fixing** — When a bug is found, a new test case is added first, then the fix is implemented

### Test Case Format

Every test case file (`.cursor/test-cases/<feature-name>.md`) follows this format:

```markdown
# Test Cases: <Feature Name>

> Reference: `.cursor/plans/<feature-name>.md`
> Created: <date>
> Status: draft | implementing | verified

## TC-1: <Short Description>
- **Given**: <precondition / initial state>
- **When**: <action / input>
- **Then**: <expected outcome>
- **Type**: unit | integration | e2e

## TC-2: <Short Description>
...
```

### Using Test Cases During Development

- Before writing a function: check what test cases it needs to satisfy
- Before opening a PR: run the test cases against your implementation manually
- In code reviews: verify that the implementation satisfies ALL test cases

### Using Test Cases During Testing

- `@test-writer` reads `.cursor/test-cases/<feature-name>.md` and converts each TC-* into actual test code
- `@qa` uses the same test cases for manual or automated verification
- Test cases marked as `integration` or `e2e` guide endpoint-level testing

## Agent Delegation

- Writing comprehensive tests → `@test-writer`
- Running tests and reporting → `@qa`

## Anti-patterns

- **Do NOT** test private methods (test the public API)
- **Do NOT** write tests that depend on test order
- **Do NOT** use real external services in tests (use mocks/sandboxes)
- **Do NOT** skip flaky tests — fix or remove them
- **Do NOT** commit commented-out tests
