# Test Cases: <Feature Name>

> **Reference plan**: `.cursor/plans/<feature-name>.md`
> **Created**: YYYY-MM-DD
> **Status**: draft | implementing | verified
> **Owner**: <developer/team>

---

## Overview

Brief description of what this feature does and the key behaviors to verify.

---

## Scenarios

<!-- 
Copy this block for each test case (TC-1, TC-2, ...).
Map each test case to a task in the plan file.
-->

### TC-1: <Short Description>

- **Type**: unit | integration | e2e
- **Given**: <precondition / initial state / setup>
- **When**: <action / input / API call>
- **Then**: <expected outcome / response / state change>
- **Notes**: <any additional context, edge cases to watch for>

---

### TC-N: <Error case / edge case>

- **Type**: unit | integration | e2e
- **Given**: <precondition>
- **When**: <invalid action / error input>
- **Then**: <expected error response / graceful handling>
- **Notes**: <boundary conditions>

---

## Coverage Checklist

Verify that test cases cover:

- [ ] Happy path (primary use case)
- [ ] Empty/null input
- [ ] Boundary values (min, max, just-over)
- [ ] Error states (validation failure, auth failure, network failure, server error)
- [ ] State transitions (loading → success, loading → error, empty → populated)
- [ ] Authorization (unauthorized user, wrong role)
- [ ] Concurrency / race conditions (if applicable)
