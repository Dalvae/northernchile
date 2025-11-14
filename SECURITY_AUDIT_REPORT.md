# SECURITY AUDIT REPORT - Northern Chile Backend

**Date:** November 14, 2025
**Environment:** Spring Boot 3.5.7, Java 21, PostgreSQL 15+
**Codebase Location:** `/home/user/northernchile/backend`

---

## EXECUTIVE SUMMARY

The security audit identified **4 CRITICAL**, **9 HIGH**, and **10 MEDIUM** priority security issues. While the backend demonstrates good foundational security practices with Spring Security, JWT authentication, and BCrypt password hashing, several critical issues require immediate remediation before production deployment.

---

## CRITICAL ISSUES - MUST FIX IMMEDIATELY

### 1. Sensitive Data Exposed in toString() Methods - Password Leakage

**Severity:** CRITICAL  
**Impact:** Passwords and password hashes exposed in logs, error messages, and debugging output  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/dto/LoginReq.java` - Lines 54-58
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/dto/RegisterReq.java` - Lines 99-108
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/model/User.java` - Lines 195-210

**Vulnerability Details:**
```java
// LoginReq.java - VULNERABLE
@Override
public String toString() {
    return "LoginReq{" +
            "email='" + email + '\'' +
            ", password='" + password + '\'' +  // PASSWORD EXPOSED!
            '}';
}

// User.java - VULNERABLE
@Override
public String toString() {
    return "User{" +
           "email='" + email + "'" +
           ", passwordHash='" + passwordHash + "'" +  // PASSWORD HASH EXPOSED!
           ...
}
```

**Why This Is Critical:**
- `toString()` is called automatically in logging frameworks when objects are logged
- If any request/response object containing this DTO is logged (error handling, debugging), the password is exposed in logs
- Logs may be stored in plaintext, sent to external systems, or become accessible to attackers
- Password hash exposure allows offline brute-force attacks

**Remediation:**
Remove passwords from `toString()` methods or implement custom `toString()` that excludes sensitive data:
```java
@Override
public String toString() {
    return "LoginReq{" +
            "email='" + email + '\'' +
            '}';
}
```

---

### 2. Missing Input Validation on Authentication Endpoints

**Severity:** CRITICAL  
**Impact:** No validation of login/register requests allows malformed or malicious input  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/AuthController.java` - Lines 24-35

**Vulnerability Details:**
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginReq loginReq) {  // MISSING @Valid
    return ResponseEntity.ok(authService.login(loginReq));
}

@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterReq registerReq) {  // MISSING @Valid
    authService.register(registerReq);
    ...
}
```

**Why This Is Critical:**
- Without `@Valid`, JSR-303 validation constraints are not enforced
- LoginReq has `@NotBlank` and `@Email` annotations that are ignored
- Null, blank, or invalid emails could be processed
- Allows potential injection attacks or unexpected behavior

**Remediation:**
Add `@Valid` annotation:
```java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginReq loginReq) {
    ...
}
```

---

### 3. Payment Token Logging Exposes Authentication Credentials

**Severity:** CRITICAL  
**Impact:** Payment provider tokens (Webpay, Mercado Pago) exposed in application logs  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/payment/PaymentService.java` - Line 69

**Vulnerability Details:**
```java
@Transactional
public PaymentStatusRes confirmPayment(String token) {
    log.info("Confirming payment with token: {}", token);  // TOKEN EXPOSED!
    
    Payment payment = paymentRepository.findByToken(token)
        .orElseThrow(() -> new IllegalArgumentException("Payment not found for token: " + token));
    ...
}
```

**Why This Is Critical:**
- Transbank/Mercado Pago tokens are session identifiers with significant security implications
- If exposed in logs, attackers can potentially:
  - Replay payment transactions
  - Access payment status information
  - Manipulate payment confirmation
- Logs may be stored insecurely or transmitted to third-party services

**Remediation:**
Remove token from logs or mask it:
```java
log.info("Confirming payment with token: {}", token.substring(0, 8) + "***");
```

---

### 4. No Rate Limiting on Authentication Endpoints - Brute Force Vulnerability

**Severity:** CRITICAL  
**Impact:** Attackers can perform unlimited login attempts to guess user passwords  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/AuthController.java` - Lines 24-27
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/AuthController.java` - Lines 55-70 (password reset)

**Vulnerability Details:**
- No rate limiting implementation found in codebase
- Login endpoint accepts unlimited requests from same IP
- Password reset endpoint accepts unlimited requests
- No account lockout mechanism after failed attempts

**Impact:**
- Attackers can perform dictionary attacks to guess passwords
- Brute-force attacks on user enumeration (password reset endpoint leaks user existence)
- Denial of service by exhausting API resources

**Remediation:**
Implement rate limiting using Spring Cloud Resilience4j or Bucket4j:
```xml
<dependency>
    <groupId>io.github.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

---

## HIGH PRIORITY ISSUES - SHOULD FIX SOON

### 5. Missing Security Headers

**Severity:** HIGH  
**Impact:** Browser and client-side protections are not enforced  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/config/SecurityConfig.java` - No header configuration

**Missing Headers:**
```
X-Content-Type-Options: nosniff              // Prevents MIME-sniffing attacks
X-Frame-Options: DENY                        // Prevents clickjacking
Strict-Transport-Security: max-age=31536000  // HTTPS enforcement
X-XSS-Protection: 1; mode=block             // XSS protection
Content-Security-Policy: ...                 // Script injection prevention
```

**Remediation:**
Add to SecurityConfig:
```java
http.headers(headers -> headers
    .contentSecurityPolicy("default-src 'self'")
    .and()
    .xssProtection()
    .and()
    .frameOptions().deny()
    .and()
    .contentTypeOptions()
);
```

---

### 6. Cart Cookie Missing Security Attributes

**Severity:** HIGH  
**Impact:** Session cookies vulnerable to man-in-the-middle and CSRF attacks  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/cart/CartController.java` - Lines 74-79

**Vulnerability Details:**
```java
private void setCartIdCookie(HttpServletResponse response, UUID cartId) {
    Cookie cookie = new Cookie("cartId", cartId.toString());
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
    // MISSING: cookie.setSecure(true);
    // MISSING: cookie.setAttribute("SameSite", "Strict");
    response.addCookie(cookie);
}
```

**Missing Attributes:**
- `Secure` flag - prevents transmission over unencrypted HTTP
- `SameSite=Strict` - prevents CSRF attacks using cross-site requests
- Domain attribute should be explicitly set

**Remediation:**
```java
cookie.setSecure(true);  // Only send over HTTPS
cookie.setAttribute("SameSite", "Strict");  // Prevent CSRF
```

---

### 7. Unvalidated Redirect URLs in Payment Flow

**Severity:** HIGH  
**Impact:** Open redirect vulnerability - attackers can redirect users to phishing sites  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/payment/dto/PaymentInitReq.java` - Lines 35-40
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/payment/PaymentController.java` - Lines 77-91

**Vulnerability Details:**
```java
// PaymentInitReq.java - VULNERABLE
private String returnUrl;    // No validation
private String cancelUrl;    // No validation

// PaymentController.java - accepts user-supplied token
@GetMapping("/confirm")
public ResponseEntity<PaymentStatusRes> confirmPayment(
    @RequestParam(value = "token_ws", required = false) String webpayToken,
    @RequestParam(value = "token", required = false) String genericToken) {
    // No validation that URLs belong to approved domain
}
```

**Attack Scenario:**
1. Attacker initiates payment with `returnUrl=https://attacker.com/phishing`
2. User completes payment on Transbank/Mercado Pago
3. User is redirected to attacker's phishing site
4. Attacker harvests credentials or payment details

**Remediation:**
Validate URLs against whitelist:
```java
private static final List<String> ALLOWED_DOMAINS = 
    Arrays.asList("https://www.northernchile.com", "http://localhost:3000");

private boolean isValidReturnUrl(String url) {
    return ALLOWED_DOMAINS.stream().anyMatch(url::startsWith);
}
```

---

### 8. No Account Lockout After Failed Login Attempts

**Severity:** HIGH  
**Impact:** Enables unlimited login attempts and credential stuffing attacks  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/AuthService.java` - Line 79-107
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/model/User.java` - No lock-related fields

**Missing Implementation:**
- No field to track failed login attempts
- No timestamp for account lockout
- No mechanism to lock account after N failed attempts
- No exponential backoff

**Remediation:**
Add to User model:
```java
@Column(name = "login_attempts", nullable = false)
private int loginAttempts = 0;

@Column(name = "locked_until")
private Instant lockedUntil;

public boolean isLockedOut() {
    return lockedUntil != null && Instant.now().isBefore(lockedUntil);
}
```

---

### 9. HTTPS/TLS Not Enforced

**Severity:** HIGH  
**Impact:** Application can run unencrypted, exposing all traffic to eavesdropping  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/resources/application.properties` - No HTTPS configuration

**Missing Configuration:**
- No `server.ssl.*` properties
- No HTTP to HTTPS redirect
- No HSTS (Strict-Transport-Security) header
- No secure cookie flags for production

**Remediation:**
Add to application.properties:
```properties
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

---

### 10. Webhook Endpoints Not Protected from Replay Attacks

**Severity:** HIGH  
**Impact:** Attackers can replay webhook payloads to trigger multiple payment confirmations  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/payment/WebhookController.java` - Lines 33-51, 69-93

**Vulnerability Details:**
```java
@PostMapping("/mercadopago")
public ResponseEntity<Void> handleMercadoPagoWebhook(@RequestBody Map<String, Object> payload) {
    log.info("Received Mercado Pago webhook: {}", payload);
    
    try {
        PaymentStatusRes response = paymentService.processWebhook("MERCADOPAGO", payload);
        // No verification of webhook signature!
        // No replay attack protection!
        return ResponseEntity.ok().build();
    }
}
```

**Missing Protections:**
- No signature verification from payment provider
- No webhook token validation
- No request ID deduplication
- No timestamp validation

**Remediation:**
Implement signature verification:
```java
private boolean verifyMercadoPagoSignature(String body, String signature) {
    String secret = mercadoPagoSecret;
    String computed = HmacUtils.hmacSha256Hex(secret, body);
    return computed.equals(signature);
}
```

---

### 11. CORS Configuration Too Permissive for Development

**Severity:** HIGH (in production)  
**Impact:** Unnecessary origins allowed; should be restricted in production  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/config/SecurityConfig.java` - Lines 90-103

**Current Configuration:**
```java
configuration.setAllowedOrigins(List.of(
    "http://localhost:3000",          // Development
    "https://northernchile.com",     // Production
    "https://www.northernchile.com"  // Production
));
```

**Issue:** While reasonable, should use environment variables in production and allow credentials. Missing Content-Type validation.

**Remediation:**
```java
@Value("${cors.allowed-origins:http://localhost:3000}")
private String allowedOrigins;

// Parse from CSV and apply per-environment
```

---

## MEDIUM PRIORITY ISSUES - BEST PRACTICES

### 12. JWT Secret Has Insecure Default Value

**Severity:** MEDIUM  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/resources/application.properties` - Line 13

**Issue:**
```properties
jwt.secret=${JWT_SECRET:change-me-in-prod}
```

The default value "change-me-in-prod" is weak and only 256 bits (minimum for HS256).

**Remediation:** Ensure environment variable is always set in production with cryptographically secure key.

---

### 13. Spring DevTools Remote Secret Has Default Value

**Severity:** MEDIUM  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/resources/application.properties` - Line 51

**Issue:** Enables remote code execution if secret is guessed.

```properties
spring.devtools.remote.secret=${SPRING_REMOTE_SECRET:change-me-in-dev}
```

**Remediation:** Disable DevTools in production or ensure strong secret.

---

### 14. Admin User Configuration in application.properties

**Severity:** MEDIUM  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/resources/application.properties` - Lines 23-34

**Issue:** Admin credentials stored in configuration file (even as env variables) is risky.

**Remediation:** Implement admin user provisioning via CLI tool or initial setup wizard.

---

### 15. No Logout Endpoint to Invalidate JWT Tokens

**Severity:** MEDIUM  
**Files Affected:**
- No logout endpoint in `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/`

**Issue:** JWT tokens cannot be invalidated. Users cannot force logout, and stolen tokens remain valid until expiration (24 hours).

**Remediation:** Implement token blacklist:
```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
    jwtBlacklistService.blacklistToken(token);
    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
}
```

---

### 16. No Token Refresh Mechanism

**Severity:** MEDIUM  
**Files Affected:**
- Missing from entire auth system

**Issue:** JWT tokens have 24-hour expiration. Users must re-login after 24 hours.

**Remediation:** Implement refresh token flow with shorter-lived access tokens.

---

### 17. Password Complexity Not Enforced

**Severity:** MEDIUM  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/dto/RegisterReq.java` - Line 16

**Current Validation:**
```java
@Size(min = 8)  // Only minimum length, no complexity rules
private String password;
```

**Missing:**
- Uppercase letters required
- Lowercase letters required
- Numbers required
- Special characters required
- No dictionary words

**Remediation:**
```java
@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
private String password;
```

---

### 18. Weather API Key in Logs (Partially Addressed)

**Severity:** MEDIUM  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/external/WeatherService.java`

**Status:** Partially mitigated with masking, but improvement possible.

---

### 19. Transbank Test Credentials Hardcoded

**Severity:** MEDIUM  
**Files Affected:**
- `/home/user/northernchile/backend/src/main/resources/application.properties` - Lines 135-137
- `/home/user/northernchile/backend/src/main/java/com/northernchile/api/payment/provider/TransbankPaymentService.java` - Line 42

**Issue:**
```properties
transbank.api-key=${TRANSBANK_API_KEY:579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C}
```

These are real Transbank integration credentials that appear in repository.

**Remediation:** Remove defaults and use environment variables only.

---

### 20. No Request Size Validation Beyond File Uploads

**Severity:** MEDIUM  
**Issue:** While file uploads are limited to 10MB, request bodies are not globally validated, allowing potential DoS attacks with large JSON payloads.

**Remediation:**
```properties
server.tomcat.max-http-post-size=1MB
```

---

## BEST PRACTICE RECOMMENDATIONS

### 21. Security Testing in CI/CD Pipeline
- Implement SAST (Static Application Security Testing)
- Add dependency vulnerability scanning (OWASP Dependency-Check)
- Implement DAST (Dynamic Application Security Testing)

### 22. Audit Logging
- Add audit log for all sensitive operations (login, payment, admin actions)
- Log to immutable storage
- Monitor for suspicious patterns

### 23. Input Validation Summary
**Current State:** Good - JSR-303 validation is used appropriately  
**Recommendation:** Add custom validators for business logic (e.g., tour dates, pricing rules)

### 24. SQL Injection Prevention
**Current State:** Safe - Uses JPA with parameterized queries  
**Status:** No issues found

### 25. API Key Management
- Implement key rotation mechanism
- Add API key expiration
- Create separate keys for different environments
- Use API key vault (AWS Secrets Manager, HashiCorp Vault)

### 26. Password Reset Token Security
**Current Status:** Good - Uses 64-character UUID tokens with 2-hour expiration  
**Recommendation:** Implement single-use tokens (already done)

### 27. Email Verification Token Security
**Current Status:** Good - Uses UUID tokens with 24-hour expiration  
**Status:** Secure implementation

### 28. Payment Provider Integration Security
- Implement webhook signature verification for both Transbank and Mercado Pago
- Store payment provider responses securely (encrypted)
- Implement idempotency keys for payment operations
- Add payment audit trail

### 29. Database Security
**Current State:** Uses environment variables for credentials  
**Recommendation:**
- Enable PostgreSQL connection SSL
- Use database-level encryption for sensitive columns (payment data)
- Implement row-level security for multi-tenant data

### 30. Monitoring and Alerting
- Monitor failed login attempts (threshold alerts)
- Alert on unusual payment activities
- Monitor application errors and security events
- Set up WAF (Web Application Firewall) rules

---

## SUMMARY OF ISSUES BY SEVERITY

| Severity | Count | Status |
|----------|-------|--------|
| CRITICAL | 4     | MUST FIX |
| HIGH     | 7     | SHOULD FIX |
| MEDIUM   | 9     | RECOMMENDED |
| BEST PRACTICE | 11 | NICE TO HAVE |

---

## REMEDIATION PRIORITY TIMELINE

**IMMEDIATE (Before any deployment to staging):**
1. Fix toString() methods exposing passwords (Issue #1)
2. Add @Valid annotations to auth endpoints (Issue #2)
3. Remove payment token logging (Issue #3)
4. Implement rate limiting (Issue #4)

**URGENT (Before production deployment):**
5. Add security headers (Issue #5)
6. Fix cart cookie security attributes (Issue #6)
7. Validate redirect URLs (Issue #7)
8. Implement account lockout (Issue #8)
9. Configure HTTPS/TLS (Issue #9)
10. Protect webhooks from replay (Issue #10)

**SHORT-TERM (Within 2 weeks of production):**
11. Fix CORS configuration
12. Implement logout/token blacklist
13. Add token refresh mechanism
14. Enforce password complexity
15. Add audit logging

---

## TESTING RECOMMENDATIONS

1. **Authentication Testing:**
   - Attempt SQL injection in login
   - Test account lockout after 5 failed attempts
   - Verify JWT expiration enforcement
   - Test logout invalidates token

2. **Authorization Testing:**
   - Verify PARTNER_ADMIN cannot access other owners' tours
   - Test SUPER_ADMIN has full access
   - Verify unpublished tours not accessible to public

3. **Input Validation Testing:**
   - Send malformed JSON to auth endpoints
   - Test XSS payloads in profile updates
   - Test CSRF attacks
   - Verify file upload restrictions

4. **Payment Security Testing:**
   - Test replay attack on webhook with duplicate payload
   - Verify payment cannot be initiated with negative amount
   - Test redirect URL validation

5. **Rate Limiting Testing:**
   - Attempt 100 login requests in 1 minute
   - Verify 429 Too Many Requests response
   - Test IP-based rate limiting

---

## COMPLIANCE NOTES

### OWASP Top 10 Coverage

| OWASP Issue | Status | Notes |
|-------------|--------|-------|
| A01 Broken Access Control | PARTIAL | Good RBAC, but needs more testing |
| A02 Cryptographic Failures | GOOD | Uses BCrypt, HTTPS required |
| A03 Injection | GOOD | JPA parameterized queries |
| A04 Insecure Design | NEEDS WORK | Missing rate limiting |
| A05 Security Misconfiguration | HIGH RISK | Missing headers, credentials in config |
| A06 Vulnerable Components | GOOD | Keep dependencies updated |
| A07 Authentication Failures | HIGH RISK | Missing logout, rate limiting |
| A08 Software/Data Integrity | GOOD | No direct dependency manipulation |
| A09 Logging & Monitoring | NEEDS WORK | Missing audit logs, tokens in logs |
| A10 SSRF | LOW RISK | Only internal API calls |

---

## APPENDIX: Files Requiring Immediate Changes

1. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/dto/LoginReq.java`
2. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/dto/RegisterReq.java`
3. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/model/User.java`
4. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/auth/AuthController.java`
5. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/payment/PaymentService.java`
6. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/config/SecurityConfig.java`
7. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/cart/CartController.java`
8. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/payment/PaymentController.java`
9. `/home/user/northernchile/backend/src/main/java/com/northernchile/api/payment/WebhookController.java`

---

**Report Generated:** November 14, 2025  
**Auditor:** Security Analysis System  
**Classification:** Internal - For Development Team Use
