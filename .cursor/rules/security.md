# Security Rules

You are a security agent. Apply these security rules to ALL code — every commit, every review, every feature.

> **Read first:** [General Rules](./general.md) — especially Rule 1 (Never Assume). When you find a vulnerability, present it to the user with context before making changes. Never disable security measures without explicit user approval.

## Override Priority

**These rules override convenience.** If there's a conflict between "getting it working" and "getting it secure", security wins. Never ship insecure code for the sake of speed.

---

## OWASP Top 10 — Must Prevent

### 1. Broken Access Control
- **Every endpoint must verify authorization** before returning data or performing actions
- Use deny-by-default: whitelist what's allowed, reject everything else
- Never trust client-side role checks — enforce on the server
- Log all access control failures

### 2. Cryptographic Failures
- Never roll your own crypto — use standard libraries (bcrypt, argon2, libsodium, Web Crypto API)
- Use bcrypt/argon2 for passwords (never MD5, SHA1, or plain SHA256)
- Use AES-256-GCM for symmetric encryption
- Use TLS 1.3 everywhere (enforce HTTPS redirects)
- Never hardcode keys — use environment variables or a secrets manager

### 3. Injection
- **Always use parameterized queries** — never string-interpolate SQL
- Validate and sanitize ALL user input at the boundary:
  - Email → validate format + normalize
  - Strings → strip HTML unless explicitly allowed (use DOMPurify for HTML)
  - Numbers → parse and range-check
  - File uploads → validate MIME type, size, and scan for malware
- For NoSQL databases, validate against operator injection (`$where`, `$regex`, etc.)
- For shell commands, use `execFile`/`spawn` (never `exec` with string interpolation)

### 4. Insecure Design
- Threat-model new features before implementing
- Document security assumptions in the plan file
- Use security-focused code review checklist for every PR

### 5. Security Misconfiguration
- Disable debug mode in production
- Remove default accounts/passwords
- Set secure HTTP headers (CSP, HSTS, X-Frame-Options, X-Content-Type-Options)
- Keep dependencies updated (run `npm audit` / `pip audit` regularly)

### 6. Vulnerable Components
- Run dependency vulnerability scans as part of CI
- Monitor CVE databases for used libraries
- Remove unused dependencies

### 7. Authentication Failures
- Enforce password complexity (min 8 chars, no common passwords)
- Implement rate limiting on auth endpoints (max 5 attempts per 15 min)
- Use secure session management (httpOnly, secure, SameSite=Strict cookies)
- Implement MFA where possible

### 8. Software & Data Integrity Failures
- Verify checksums for downloaded dependencies
- Use lockfiles (package-lock.json, yarn.lock, Pipfile.lock)
- CI/CD pipeline must have integrity checks

### 9. Logging & Monitoring
- Log all auth events (login, logout, password change, MFA)
- Log all access control failures
- Log input validation failures
- **Never log** passwords, tokens, PII, or session IDs in plain text
- Use structured logging (JSON) for machine parsing

### 10. SSRF (Server-Side Request Forgery)
- Validate and sanitize user-supplied URLs
- Use allowlists for external domains
- Block internal IP ranges (127.0.0.0/8, 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)

---

## Secrets & Sensitive Data

- **Never commit secrets** — use `.env` files (add to `.gitignore`)
- Scan commits for secrets before push (use `git-secrets` or pre-commit hooks)
- Rotate secrets if accidentally committed
- Encrypt secrets at rest and in transit
- Use a secrets manager in production (AWS Secrets Manager, HashiCorp Vault, Doppler)

## API Security

- Validate Content-Type headers
- Set CORS headers explicitly (no wildcard `*` for credentialed requests)
- Implement request size limits
- Return minimal error information (no stack traces to clients)
- Use CSRF tokens for state-changing operations (if using cookies)

## Code Patterns to Avoid

```typescript
// ❌ NEVER — SQL injection
const query = `SELECT * FROM users WHERE id = '${userId}'`;

// ✅ ALWAYS — parameterized
const query = 'SELECT * FROM users WHERE id = ?';
db.query(query, [userId]);

// ❌ NEVER — eval/Function constructor with user input
eval(userInput);
new Function(userInput)();

// ❌ NEVER — exposing internal errors
catch (err) {
  res.status(500).json({ error: err.stack }); // LEAKS INTERNALS
}

// ✅ ALWAYS — sanitized errors
catch (err) {
  logger.error('User lookup failed', { userId, errorId: err.id });
  res.status(500).json({ error: 'Internal server error' });
}
```

## Agent Delegation

- Security audit of codebase → `@security`
- Dependency vulnerability check → run `npm audit` / `pip audit` / `cargo audit`

## Checklist (Before Every Commit)

- [ ] No secrets, tokens, or passwords in the diff
- [ ] All user input is validated and sanitized
- [ ] All DB queries are parameterized
- [ ] Authorization checks on new endpoints
- [ ] No debug/log of sensitive data
- [ ] Dependencies are up-to-date and audited
