---
name: audit
description: Security audit — check for vulnerabilities, review against OWASP Top 10, scan dependencies
---

# /audit — Security Audit Workflow

You are in security audit mode. Follow the [Security Rules](../rules/security.md), [Git Rules](../rules/git.md), and [General Rules](../rules/general.md).

**Security rules override convenience.** Never ship insecure code for the sake of speed.

## What This Command Does

Performs a comprehensive security audit:
1. Scan dependencies for known vulnerabilities
2. Review code against OWASP Top 10
3. Check for exposed secrets and sensitive data
4. Validate authentication/authorization patterns
5. Produce an audit report with findings and fixes

## Process

### Step 0: Git Setup
```bash
# Init if needed
git status 2>&1 || (git init && git add -A && git commit -m "chore: initial commit")
```
If you need to apply fixes based on audit findings, create a branch:
```bash
git checkout -b fix/<issue>   # or bugfix/<urgent-issue>
```
**Never apply fixes on main.** Always use a branch. **Do not merge** unless the user explicitly asks.

### Step 1: Dependency Scan
Run the project's vulnerability scanner:
```bash
npm audit        # Node.js
pip audit        # Python
cargo audit      # Rust
bundle audit     # Ruby
```
Report any critical/high vulnerabilities with their fix versions.

### Step 2: OWASP Top 10 Review
Check the codebase for each OWASP category:

1. **Broken Access Control** — Are all endpoints authorization-checked? Is deny-by-default enforced?
2. **Cryptographic Failures** — Are bcrypt/argon2 used for passwords? Is TLS enforced? Are keys hardcoded?
3. **Injection** — Are all DB queries parameterized? Is user input sanitized? Are shell commands safe?
4. **Insecure Design** — Do new features have threat models? Are security assumptions documented?
5. **Security Misconfiguration** — Is debug mode disabled in prod? Are secure headers set? Are defaults changed?
6. **Vulnerable Components** — Are dependencies up to date? Are CVEs tracked?
7. **Authentication Failures** — Is rate limiting on auth? Are passwords complex? Is MFA available?
8. **Data Integrity Failures** — Are lockfiles present? Are checksums verified in CI?
9. **Logging & Monitoring** — Are auth events logged? Are sensitive fields excluded from logs?
10. **SSRF** — Are user-supplied URLs validated? Are internal IPs blocked?

### Step 3: Secrets Check
- Scan the codebase for hardcoded secrets (API keys, tokens, passwords, private keys)
- Verify `.env` is in `.gitignore`
- Check that no `.env` files are committed
- Look for secrets in config files, test files, and comments

### Step 4: Auth & Session Review
- Verify password hashing (bcrypt cost factor >= 10)
- Check session cookie attributes (httpOnly, secure, SameSite=Strict)
- Verify CSRF protection on state-changing operations
- Check JWT configuration (algorithm, expiration, signing)
- Review rate limiting on auth endpoints

### Step 5: Input Validation Review
- Are all user inputs validated at the boundary?
- Are file uploads validated (MIME type, size, content)?
- Is HTML sanitized (DOMPurify)?
- Are SQL/NoSQL queries parameterized?
- Are shell commands using execFile/spawn (not exec)?

### Step 6: Produce Audit Report
Create `.cursor/audits/<date>-audit.md` with:

```markdown
# Security Audit — YYYY-MM-DD

## Summary
- Critical: X | High: X | Medium: X | Low: X

## Findings

### [CRITICAL] <Title>
- **Category**: OWASP #X — <Category>
- **Location**: `path/to/file.ts:42`
- **Description**: <what's wrong>
- **Impact**: <what an attacker could do>
- **Fix**: <specific, actionable fix>
- **Verified**: yes/no

## Dependency Vulnerabilities
| Package | Current | Patched | Severity | CVE |
|---------|---------|---------|----------|-----|

## Actions Required
1. [ ] <action> — priority: critical
2. [ ] <action> — priority: high
```

## Agent Delegation

- Comprehensive security audit → `@security` (read-only, no file modifications)
- Fixing vulnerabilities → `@developer` or `/fix`

## When No Project Exists

If you're setting up a new project, add these security defaults:
- Add `.env` to `.gitignore`
- Add a pre-commit hook for secret scanning
- Configure secure HTTP headers
- Set up `npm audit` in CI

## Anti-patterns

- Do NOT fix vulnerabilities without the user's approval — present findings first
- Do NOT disable security measures to "get things working"
- Do NOT ignore low-severity issues — they compound
