# General Rules (Cross-Cutting)

These rules apply to ALL workflows — planning, development, testing, security, and fixing. No workflow-specific rule overrides these.

---

## Rule 1: Never Assume — Always Confirm

**This is the most important rule.** When you are unsure about anything, ask the user. Do not guess, do not assume, do not fill in gaps with your own preferences.

### When You MUST Ask

- **Ambiguous requirements** — If a request can be interpreted in multiple ways, present the options and ask which is correct
- **Missing context** — If the user didn't specify a technology choice, naming convention, file location, or design preference
- **Trade-off decisions** — Performance vs readability, flexibility vs simplicity, speed vs thoroughness
- **Destructive actions** — Before deleting code, dropping tables, removing features, or force-pushing
- **Non-obvious conventions** — If you can't find a clear pattern in the existing codebase
- **User preferences** — UI copy, color choices, layout decisions, error messages

### How to Ask

- **Be specific** — Don't ask "What do you want?" — present concrete options
- **Explain trade-offs** — Why one option over another matters
- **Provide context** — What you know, what you don't know, and what you need to decide
- **Use the question tool** — For multiple-choice decisions, use structured questions

### Anti-pattern

```
// ❌ WRONG — assuming the user wants TypeScript
"I'll create the component in TypeScript."

// ❌ WRONG — assuming the file location
"Putting this in src/utils/helpers.ts"

// ✅ CORRECT — asking when unclear
"This project has both .js and .ts files. Should I use TypeScript for this feature?"

// ✅ CORRECT — offering options
"Where should this go? I see two possible locations:
 1. src/utils/ — alongside existing utilities
 2. src/lib/ — if this will grow into a dedicated module"
```

---

## Rule 2: Read Before You Write

Never modify a file you haven't read. Before writing or editing:

1. Read the file to understand existing code
2. Read related files (imports, parent components, calling code)
3. Read project config (package.json, tsconfig, etc.) to understand conventions

---

## Rule 3: Follow Existing Patterns

When the codebase has established patterns, follow them — even if you know a "better" way. Consistency beats novelty.

- Match naming conventions (camelCase, PascalCase, snake_case)
- Match code style (semicolons, trailing commas, indentation)
- Match file organization (where things live)
- Match architectural patterns (how state is managed, how APIs are called)

If you think the existing pattern is problematic, **ask** before changing it.

---

## Rule 4: Small, Safe Changes

- **Make the smallest change possible** that accomplishes the goal
- **One logical change per commit** — don't bundle unrelated changes
- **Don't refactor "while you're there"** — unless the refactor is necessary for the task
- **Scope creep is the enemy** — if you notice an unrelated problem, note it and ask if the user wants it addressed separately

---

## Rule 5: Always Verify

After every change, verify it works:
- Run the build/typecheck/lint commands for the project
- Run the relevant tests
- If you can't verify (e.g., no tests exist for this area), mention it to the user
- Never mark a task "done" without verification

---

## Rule 6: Handle Errors, Don't Hide Them

- Never use empty catch blocks
- Never `as any` to bypass type errors (fix the types instead)
- Log errors with enough context to debug them
- Surface errors to the user clearly (not "Something went wrong")

---

## Rule 7: Be Conservative with Dependencies

- Don't add a dependency without a strong reason
- Check if the project already has a suitable library
- Prefer standard library / built-in features over external packages
- If you must add a dependency, explain why to the user

---

## Rule 8: Think About the Full Lifecycle

Every line of code you write will need to be:
- Tested (does it have tests?)
- Reviewed (is it clear?)
- Deployed (does it work in production?)
- Maintained (can someone understand it in 6 months?)
- Debugged (are errors clear and traceable?)

---

## Rule 9: Git-Initialize Every Project

**Every project MUST be a git repository before any work begins.** If the project is not git-initialized:

1. Check with `git status`
2. If not a repo, run `git init && git add -A && git commit -m "chore: initial commit"`
3. All work goes on feature branches — never commit directly to `main`
4. Reference: [Git Rules](./git.md)
