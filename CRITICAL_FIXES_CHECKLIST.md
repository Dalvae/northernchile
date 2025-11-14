# CRITICAL SECURITY FIXES - QUICK ACTION CHECKLIST

## Issue #1: Password Exposure in toString() Methods
**Status:** NOT FIXED  
**Priority:** CRITICAL  
**Time to Fix:** 45 minutes

### Files to Fix:
1. **LoginReq.java** (Line 54-58)
   ```java
   // CURRENT (VULNERABLE):
   public String toString() {
       return "LoginReq{" +
               "email='" + email + '\'' +
               ", password='" + password + '\'' +  // REMOVES THIS
               '}';
   }
   
   // FIXED:
   public String toString() {
       return "LoginReq{" +
               "email='" + email + '\'' +
               '}';
   }
   ```

2. **RegisterReq.java** (Line 99-108) - Same fix

3. **User.java** (Line 195-210)
   ```java
   // REMOVES: ", passwordHash='" + passwordHash + "'"
   // KEEPS: email, id, role, etc.
   ```

---

## Issue #2: Missing @Valid on Auth Endpoints
**Status:** NOT FIXED  
**Priority:** CRITICAL  
**Time to Fix:** 5 minutes

### File: AuthController.java
```java
// Line 25 - ADD @Valid:
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginReq loginReq) {
    return ResponseEntity.ok(authService.login(loginReq));
}

// Line 30 - ADD @Valid:
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterReq registerReq) {
    authService.register(registerReq);
    ...
}
```

---

## Issue #3: Payment Token in Logs
**Status:** NOT FIXED  
**Priority:** CRITICAL  
**Time to Fix:** 5 minutes

### File: PaymentService.java (Line 69)
```java
// CURRENT (VULNERABLE):
log.info("Confirming payment with token: {}", token);

// FIXED:
log.info("Confirming payment with masked token");
// Or if you need to log part of it:
log.info("Confirming payment with token: {}", 
    token.substring(0, Math.min(8, token.length())) + "***");
```

---

## Issue #4: No Rate Limiting (Brute Force)
**Status:** NOT IMPLEMENTED  
**Priority:** CRITICAL  
**Time to Fix:** 2-4 hours

### Implementation Option 1: Using Bucket4j (Recommended)

**Step 1:** Add dependency to `pom.xml`
```xml
<dependency>
    <groupId>io.github.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

**Step 2:** Create RateLimitingInterceptor
```java
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private final Bucket loginBucket;
    
    public RateLimitingInterceptor() {
        Refill refill = Refill.intervally(5, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5, refill);
        this.loginBucket = Bucket4j.builder()
            .addLimit(limit)
            .build();
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        if (request.getRequestURI().contains("/api/auth/login")) {
            if (!loginBucket.tryConsume(1)) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many login attempts\"}");
                return false;
            }
        }
        return true;
    }
}
```

**Step 3:** Register interceptor in WebConfig
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor);
    }
}
```

### Implementation Option 2: Using Spring Cloud Resilience4j
See detailed guide in SECURITY_AUDIT_REPORT.md

---

## Verification Steps

After applying fixes, verify with these tests:

### Test #1: toString() doesn't expose passwords
```bash
cd /home/user/northernchile/backend
mvn clean test -Dtest="*Auth*" -DskipTests=false
# Grep for "password=" or "passwordHash=" in output - should NOT appear
```

### Test #2: Validation is enforced
```bash
# Try sending invalid login request:
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "invalid-email", "password": ""}'
# Should return 400 Bad Request with validation errors
```

### Test #3: Rate limiting works
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email": "test@test.com", "password": "wrong"}'
  echo "Attempt $i"
done
# Should return 429 after 5 attempts
```

### Test #4: Logs don't contain tokens
```bash
tail -f /var/log/app.log | grep -i "token\|password"
# Initiate a payment and check logs - should NOT contain actual tokens/passwords
```

---

## Timeline for Implementation

| Task | Est. Time | Assigned To |
|------|-----------|------------|
| Issue #1 (toString) | 45 min | Frontend Dev or Infra |
| Issue #2 (@Valid) | 5 min | Frontend Dev |
| Issue #3 (Logging) | 5 min | Backend Dev |
| Issue #4 (Rate Limit) | 3-4 hrs | Backend Dev |
| Testing & QA | 1 hr | QA Engineer |
| Code Review | 30 min | Tech Lead |
| **TOTAL** | **~5-6 hours** | |

---

## Rollout Plan

1. **Branch Creation**: Create feature branch `bugfix/critical-security-fixes`
2. **Implementation**: Implement all 4 fixes
3. **Testing**: Run test suite and manual verification
4. **Code Review**: Get approval from 2 reviewers
5. **Merge**: Merge to main branch
6. **Deployment**: Deploy to staging immediately
7. **Production**: Can deploy after 24-hour staging validation

---

## Deployment Checklist

- [ ] All 4 issues fixed and tested locally
- [ ] Code review approved
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual security verification complete
- [ ] Logs verified clean (no password/token exposure)
- [ ] Rate limiting verified working
- [ ] Deployed to staging
- [ ] Staging validation passed (24 hours)
- [ ] Approved for production by Tech Lead
- [ ] Backup taken before production deployment
- [ ] Deployed to production
- [ ] Monitoring alerts configured
- [ ] Production validation complete

---

## Monitoring After Deployment

Monitor these metrics:

```
1. Failed login attempts per minute (should spike due to rate limiting)
2. 429 HTTP responses (should increase with rate limiting)
3. Application logs for password/token exposure (should be zero)
4. @Valid validation errors (should decrease)
5. Exception rates (should remain stable)
```

---

## Emergency Rollback Plan

If critical issues found after deployment:

```bash
# Rollback to previous version
git revert <commit-hash>
git push origin main
docker pull northernchile/api:previous
docker-compose down && docker-compose up -d

# Verify rollback
curl http://localhost:8080/actuator/health
```

---

**Status Dashboard**
- [ ] Identified: ✓ (2025-11-14)
- [ ] In Progress: ⏳
- [ ] Testing: ⏳
- [ ] Deployed: ⏳
- [ ] Verified: ⏳

**Last Updated:** 2025-11-14  
**Next Review:** After implementation
