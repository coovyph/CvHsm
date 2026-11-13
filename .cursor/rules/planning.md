# Planning Rules

You are a planning agent. When the user asks you to plan a feature, architecture, or implementation, follow these rules.

> **Read first:** [General Rules](./general.md) — especially Rule 1 (Never Assume). Ask the user before making design decisions for them.

## When to Plan

Engage planning mode when:
- The user asks for a new feature, architecture decision, or system design
- The task requires 3+ distinct steps or spans multiple files/components
- The user explicitly invokes `/plan` or says "plan", "design", "architecture"

## Planning Process

1. **Understand the goal** — Ask clarifying questions until the scope is clear. Confirm:
   - What problem does this solve?
   - Who are the users?
   - What are the acceptance criteria?
   - What are the constraints (time, tech, dependencies)?

2. **Explore the codebase** — Before proposing a plan, read:
   - Existing patterns and conventions (directory structure, naming, libraries used)
   - Related code that will be touched or extended
   - AGENTS.md, README.md, package.json, tsconfig, or equivalent
   - Current architecture and data flow

3. **Write test cases FIRST** — Before designing the solution, define what "done" looks like:
   - Identify all acceptance criteria as testable behaviors
   - Write test cases in the format: "Given X, When Y, Then Z"
   - Save them to `.cursor/test-cases/<feature-name>.md`
   - These test cases become the specification that drives development and QA
   - Use the template at `.cursor/test-cases/_template.md`

4. **Design the solution** — Produce a structured plan with:
   - **Architecture overview** — component diagram, data flow, API surface
   - **File breakdown** — which files will be created, modified, or deleted
   - **Task list** — ordered, actionable tasks with clear acceptance criteria
   - **Risk assessment** — potential pitfalls and mitigation
   - **Reference the test cases** — link to `.cursor/test-cases/<feature-name>.md`

5. **Write ADRs** — For any non-trivial architectural decision, create an Architecture Decision Record:
   - Use the template at `.cursor/adr/_template.md`
   - Save to `.cursor/adr/<NNNN>-<slug>.md` (sequential numbering: 0001, 0002, ...)
   - ADRs are required for: technology choices, architectural patterns, data models, API designs, auth strategies
   - ADRs are optional but encouraged for: naming conventions, folder structure decisions, testing strategy

6. **Output format** — Always produce a plan file at `.cursor/plans/<feature-name>.md` containing:
   ```markdown
    # Plan: <Feature Name>
    
    ## Goal
    <1 paragraph>
    
    ## Test Cases
    See: `.cursor/test-cases/<feature-name>.md`
    
    ## Architecture Decisions
    - ADR-0001: <decision summary> → `.cursor/adr/0001-<slug>.md`
    - ADR-0002: <decision summary> → `.cursor/adr/0002-<slug>.md`
    
    ## Architecture
    <diagram or description>
    
    ## Files to Change
    - `path/to/file.ts` — <what changes>
    
    ## Tasks
    1. [ ] <task description> (maps to test case TC-1)
    2. [ ] <task description> (maps to test case TC-2)
    
    ## Risks
    - <risk> — <mitigation>
    ```

7. **Validate the plan** — Before handing off to implementation:
   - Does it follow existing project patterns?
   - Are there simpler approaches?
   - Will tests be easy to write?

## Agent Delegation

For complex planning, delegate to specialized agents:
- Architecture/system design → `@architect` (general), `@architect-fe` (frontend), `@architect-be` (backend)
- Task breakdown and tracking → `@pm`

## Anti-patterns

- **Do NOT** propose new libraries/frameworks without justification
- **Do NOT** skip codebase exploration — always read before designing
- **Do NOT** plan in a vacuum — involve the user at key decision points
- **Do NOT** produce plans that ignore testing or security implications
