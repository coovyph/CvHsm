---
name: plan
description: Plan a feature using spec-driven workflow — explore, define test cases, write ADRs, produce plan file
---

# /plan — Planning Workflow

You are in planning mode. Follow the [Planning Rules](../rules/planning.md), [Git Rules](../rules/git.md), and [General Rules](../rules/general.md).

## What This Command Does

Invokes the full planning workflow:
1. **Git init** if the project isn't already a repo
2. Understand the goal (ask clarifying questions — never assume)
3. Explore the codebase (read existing patterns, configs, related code)
4. Write test cases first → save to `.cursor/test-cases/<feature-name>.md`
5. Write ADRs for architectural decisions → save to `.cursor/adr/<NNNN>-<slug>.md`
6. Produce a plan file → save to `.cursor/plans/<feature-name>.md`

## Process

### Step 0: Git Setup
```bash
# Check if this is a git repo; if not, init it
git status 2>&1 || (git init && git add -A && git commit -m "chore: initial commit")
```
Every project must be git-initialized. If you just inited, confirm the initial commit exists before proceeding.

### Step 1: Clarify
Ask the user — what feature? What problem does it solve? What are the constraints? Use the question tool when multiple options exist. **Do not guess.**

### Step 2: Explore
Read the codebase: AGENTS.md, README.md, package.json, existing patterns, related code. Load relevant rules from `.cursor/rules/`.

### Step 3: Write Test Cases
Before any design, define what "done" means. Create `.cursor/test-cases/<feature-name>.md` using the template at `.cursor/test-cases/_template.md`. Each test case should use Given/When/Then format and map to a future task.

### Step 4: Write ADRs
For each non-trivial architectural decision, create an ADR using the template at `.cursor/adr/_template.md`. At minimum, ADRs are required for:
- Technology choices (framework, library, database)
- Architectural patterns (microservices, monolith, event-driven)
- Data models and schema decisions
- API design patterns
- Authentication/authorization strategies

### Step 5: Produce Plan File
Create `.cursor/plans/<feature-name>.md` with:
- Goal (1 paragraph)
- Link to test cases
- Architecture decisions (links to ADRs)
- Architecture overview
- Files to change
- Ordered task list (each task mapped to test cases)
- Risk assessment

### Step 6: Validate
- Does it follow existing project patterns?
- Are there simpler approaches?
- Is it testable?
- Is it secure? (check `.cursor/rules/security.md`)

## Agent Delegation

For complex planning, delegate to specialized agents:
- `@pm` — task breakdown and progress tracking (produces frontend/backend plan files)
- `@architect` — system-wide architecture decisions
- `@architect-fe` — frontend architecture, component patterns, design systems
- `@architect-be` — backend architecture, API design, database schemas

## Output Checklist

Before completing, ensure the following files exist:
- [ ] `.cursor/test-cases/<feature-name>.md`
- [ ] `.cursor/adr/<NNNN>-<slug>.md` (if applicable)
- [ ] `.cursor/plans/<feature-name>.md`

Then present a summary to the user with links to all generated files.
