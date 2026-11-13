# Git Rules

These rules govern how git is used in every project. They apply to ALL workflows.

> **Read first:** [General Rules](./general.md) — especially Rule 1 (Never Assume).

---

## Rule 1: Auto-Initialize Git

**Every project MUST be a git repository.** Before doing any work:

```bash
# Check if git is initialized
git status 2>&1

# If NOT a git repo, init it immediately
git init
git add -A
git commit -m "chore: initial commit"
```

Never start development in a project that isn't git-initialized.

---

## Rule 2: Always Branch — Never Commit to Main

**Every change goes on a feature branch.** Never commit directly to `main` (or `master`).

### Branch Creation

Before making any changes:

```bash
# Fetch latest main
git checkout main
git pull origin main  # if remote exists

# Create feature branch
git checkout -b <type>/<feat-name>
```

### Branch Naming Convention

```
<type>/<descriptive-name>
```

| Type | When to use | Example |
|------|------------|---------|
| `feat` | New feature or enhancement | `feat/user-registration` |
| `fix` | Bug fix (non-critical, non-urgent) | `fix/login-redirect-loop` |
| `bugfix` | Urgent/hotfix bug | `bugfix/crash-on-empty-cart` |
| `chore` | Maintenance, deps, config, refactor | `chore/update-dependencies` |
| `doc` | Documentation changes | `doc/api-endpoint-reference` |

**Naming rules:**
- Use lowercase with hyphens as word separators (`feat/two-factor-auth`)
- Keep it short but descriptive (3-5 words max)
- No special characters except hyphens
- Be specific: `fix/null-pointer-checkout` not `fix/bug123`

### Before Creating a Branch

1. **Always pull latest main** first
2. **Confirm the branch name** with the user if the task description is ambiguous
3. **Never reuse old branches** — one branch per logical change

---

## Rule 3: Commit on the Feature Branch

All work is committed to the feature branch. Follow conventional commit format:

```
<type>(<scope>): <description>
```

| Commit type | When to use |
|-------------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code restructuring (no behavior change) |
| `style` | Formatting, linting (no behavior change) |
| `chore` | Maintenance, deps, config |
| `perf` | Performance improvement |
| `test` | Adding or updating tests |
| `docs` | Documentation only |

**Commit rules:**
- One logical change per commit
- Commit after each completed + verified task
- Commit message describes "why", not "what"
- Never commit secrets, `.env`, or credentials
- Never commit without build/typecheck passing

Reference: [Development Rules](./development.md) for full commit workflow.

---

## Rule 4: NEVER Merge Without Explicit Approval

**This is a hard rule.** You MUST NOT merge a feature branch into main unless:

1. The user explicitly asks you to merge (e.g., "merge it", "ship it", "merge to main")
2. The user invokes a merge-related command

### When in Doubt, Ask

```
"I've completed the implementation on branch feat/user-auth.
 All tests pass and the build is green.
 Would you like me to merge this into main?"
```

### Merge Checklist (Before Asking User)

Before even asking the user to merge, verify:
- [ ] All tests pass
- [ ] Build/typecheck/lint passes
- [ ] No unresolved merge conflicts
- [ ] Branch is up-to-date with latest main (rebased or merged)
- [ ] Security audit is clean (see `/audit`)
- [ ] Test cases are satisfied (see `.cursor/test-cases/`)
- [ ] ADRs are written (if applicable)
- [ ] No debug code, commented-out code, or console.log

### Merge Workflow (Only When User Approves)

```bash
git checkout main
git pull origin main
git merge --no-ff feat/user-registration   # always use --no-ff to preserve branch history
git push origin main
git branch -d feat/user-registration       # clean up local
git push origin --delete feat/user-registration  # clean up remote
```

---

## Rule 5: Pull Before Push

Always pull (rebase or merge) before pushing to avoid conflicts:

```bash
# On the feature branch
git pull --rebase origin main   # rebase onto latest main
git push origin feat/user-registration
```

If there are conflicts, resolve them, then:

```bash
git add <resolved-files>
git rebase --continue
git push origin feat/user-registration
```

---

## Full Workflow Example

```bash
# 1. Initialize if needed
git status || (git init && git add -A && git commit -m "chore: initial commit")

# 2. Branch off main
git checkout main
git checkout -b feat/user-registration

# 3. Implement task-by-task
# ... write code ...
# ... run build, tests, lint ...
git add src/auth/register.ts
git commit -m "feat(auth): add user registration endpoint"

# ... next task ...
git add src/auth/register.test.ts
git commit -m "test(auth): add registration unit tests"

# 4. Push branch (rebase first)
git pull --rebase origin main
git push origin feat/user-registration

# 5. Wait for user to say "merge"
# DO NOT merge until explicitly told.
```

## Anti-patterns

- **Do NOT** commit to main directly — always use a feature branch
- **Do NOT** merge without explicit user approval
- **Do NOT** create branches with generic names like `fix/bug` or `feat/stuff`
- **Do NOT** skip pulling latest main before branching
- **Do NOT** reuse old branches for new work
