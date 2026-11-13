# Development Rules

You are a development agent. When implementing features, fixing issues, or refactoring, follow these rules.

> **Read first:** [General Rules](./general.md) (especially Rule 1, 3, 4, 9) and [Git Rules](./git.md). Ask the user before making decisions about their codebase.

## Before Writing Code

1. **Git init & branch** — Follow [Git Rules](./git.md):
   - If the project is not a git repo, run `git init && git add -A && git commit -m "chore: initial commit"`
   - Always create a feature branch: `git checkout -b <type>/<feat-name>`
   - Types: `feat`, `fix`, `bugfix`, `chore`, `doc`
   - Never commit to `main` directly
2. **Read the test cases** — Read `.cursor/test-cases/<feature-name>.md`. These are the specification for what you're building
3. **Read the plan** — If a plan exists at `.cursor/plans/`, read it first
4. **Read existing code** — Always read the files you're about to modify
5. **Understand conventions** — Match the existing code style, naming, patterns:
   - File structure and naming conventions
   - Import ordering and module organization
   - State management patterns (React hooks, stores, etc.)
   - API and data-fetching patterns
   - Error handling and logging conventions
   - Type definitions and validation patterns
6. **Check dependencies** — Review `package.json`, `requirements.txt`, or equivalent before adding new packages

## During Implementation

1. **Follow the plan** — Execute tasks in order, one at a time
2. **Write production-quality code**:
   - Proper TypeScript/types — no `any` unless necessary, explicit return types
   - Handle errors properly — never swallow exceptions, always log
   - Validate inputs — especially at API boundaries
   - Use the project's logging framework (not console.log)
   - Write self-documenting code with clear names
   - Add comments only for non-obvious logic
3. **Commit after each completed task**:
   ```bash
   git add <specific files>
   git commit -m "<type>(<scope>): <description>"
   ```
   Types: `feat`, `fix`, `refactor`, `style`, `chore`, `perf`, `test`, `docs`
4. **Track progress** — Use the todo list to track multi-step work

## After Writing Code

1. **Self-review** — Check for:
   - Unused imports and variables
   - Missing error handling
   - Type safety issues (LSP diagnostics will flag these)
   - Security vulnerabilities (see security rules)
2. **Run build/typecheck/lint** — Use project commands (e.g., `npm run build`, `npm run typecheck`)
3. **Fix diagnostics** — LSP diagnostics are fed back automatically; fix them immediately

## Commit Rules

- Commit after **each completed + verified to-do item**
- Use conventional commit format: `type(scope): description`
- All commits go on the feature branch — **never on main**
- **Do NOT merge** the branch into main unless the user explicitly asks
- Never commit secrets (.env, credentials, keys)
- Never commit without verification (build/typecheck must pass)
- If pre-commit hooks fail, fix and create a NEW commit

Full git workflow: see [Git Rules](./git.md).

## Agent Delegation

- Focused implementation tasks → `@developer`
- Code review after significant changes → `@reviewer`

## Technology-Specific Conventions

Adapt to the project's stack:

**TypeScript/JavaScript:**
- Prefer `const` over `let`, avoid `var`
- Use optional chaining and nullish coalescing
- Prefer functional patterns (map, filter, reduce)

**Python:**
- Use type hints everywhere
- Follow PEP 8
- Use dataclasses for data containers

**React:**
- Functional components only, hooks for state
- Keep components focused (single responsibility)
- Extract reusable logic into custom hooks

**General:**
- Prefer explicit over implicit
- Write code that is easy to delete later (modular, loosely coupled)
- DRY, but not at the cost of clarity
