# Spring Boot Security Research and Similar Repositories

## Executive Summary

This document provides research on Spring Boot security modules, permission management patterns, and similar Nuxt + Spring Boot repositories that can serve as references for the Northern Chile project.

## Current Security Implementation

The Northern Chile project currently uses:

- **Spring Security 6** with JWT authentication
- **Role-based access control (RBAC)** with three roles:
  - `ROLE_SUPER_ADMIN` - Full system access
  - `ROLE_PARTNER_ADMIN` - Can manage their own tours (filtered by owner_id)
  - `ROLE_CLIENT` - Standard booking user
- **Method-level security** via `@EnableMethodSecurity`
- **JWT-based stateless authentication**
- **OAuth2 integration** (Google login)
- **Custom security services** for resource-level authorization

## Spring Boot Security Module Options

### 1. Spring Security with Method-Level Security (Current Approach)

**What we currently have:**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // Current implementation
}
```

**Enhanced patterns to consider:**

#### a) Annotation-Based Security on Service Methods
```java
@Service
public class TourService {
    
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
    public Tour createTour(TourCreateReq req) {
        // Create tour logic
    }
    
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @tourSecurityService.isOwner(#tourId)")
    public Tour updateTour(UUID tourId, TourUpdateReq req) {
        // Update tour logic
    }
    
    @PreAuthorize("@tourSecurityService.canDelete(#tourId, principal)")
    public void deleteTour(UUID tourId) {
        // Delete tour logic
    }
}
```

#### b) Custom Security Expressions
```java
@Component("tourSecurity")
public class TourSecurityExpressions {
    
    public boolean canManageTour(Authentication authentication, UUID tourId) {
        // Custom logic to check if user can manage tour
        return true;
    }
    
    public boolean hasPermission(Authentication authentication, String permission) {
        // Check if user has specific permission
        return true;
    }
}

// Usage:
@PreAuthorize("@tourSecurity.canManageTour(authentication, #tourId)")
public void updateTour(UUID tourId, TourUpdateReq req) { }
```

### 2. Fine-Grained Permission System

**Option A: Enum-Based Permissions**

```java
// Permission enum
public enum Permission {
    // Tour permissions
    TOUR_CREATE,
    TOUR_READ,
    TOUR_UPDATE,
    TOUR_DELETE,
    
    // Booking permissions
    BOOKING_CREATE,
    BOOKING_READ,
    BOOKING_UPDATE,
    BOOKING_CANCEL,
    
    // Admin permissions
    USER_MANAGE,
    REPORTS_VIEW,
    SYSTEM_CONFIG;
}

// Role with permissions
public enum Role {
    SUPER_ADMIN(Set.of(
        Permission.TOUR_CREATE, Permission.TOUR_READ, Permission.TOUR_UPDATE, Permission.TOUR_DELETE,
        Permission.BOOKING_CREATE, Permission.BOOKING_READ, Permission.BOOKING_UPDATE, Permission.BOOKING_CANCEL,
        Permission.USER_MANAGE, Permission.REPORTS_VIEW, Permission.SYSTEM_CONFIG
    )),
    PARTNER_ADMIN(Set.of(
        Permission.TOUR_CREATE, Permission.TOUR_READ, Permission.TOUR_UPDATE, Permission.TOUR_DELETE,
        Permission.BOOKING_READ, Permission.BOOKING_UPDATE, Permission.REPORTS_VIEW
    )),
    CLIENT(Set.of(
        Permission.TOUR_READ, Permission.BOOKING_CREATE, Permission.BOOKING_READ
    ));
    
    private final Set<Permission> permissions;
    
    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }
    
    public Set<Permission> getPermissions() {
        return permissions;
    }
}
```

**Option B: Database-Driven Permissions (More Flexible)**

```java
// Entity: Permission
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String name; // e.g., "tour:create", "booking:read"
    
    private String description;
    private String resource; // e.g., "tour", "booking"
    private String action; // e.g., "create", "read", "update", "delete"
}

// Entity: Role
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String name; // e.g., "ROLE_SUPER_ADMIN"
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}

// Updated User entity
@Entity
@Table(name = "users")
public class User {
    // ... existing fields
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;
}
```

**Custom Permission Evaluator:**
```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        User user = (User) authentication.getPrincipal();
        String permissionName = permission.toString();
        
        return user.getRole().getPermissions().stream()
            .anyMatch(p -> p.getName().equals(permissionName));
    }
    
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }
}

// Usage:
@PreAuthorize("hasPermission(null, 'tour:create')")
public Tour createTour(TourCreateReq req) { }
```

### 3. Spring Security ACL (Access Control Lists)

For more complex scenarios where you need object-level permissions:

```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AclSecurityConfig {
    
    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
            new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")
        );
    }
    
    @Bean
    public PermissionGrantingStrategy permissionGrantingStrategy() {
        return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
    }
}

// Usage:
@PreAuthorize("hasPermission(#tourId, 'com.northernchile.api.model.Tour', 'WRITE')")
public Tour updateTour(UUID tourId, TourUpdateReq req) { }
```

### 4. Attribute-Based Access Control (ABAC)

For complex business rules:

```java
@Component
public class AbacService {
    
    public boolean canAccessTour(User user, Tour tour) {
        // Check multiple attributes
        if (user.getRole().equals("ROLE_SUPER_ADMIN")) {
            return true;
        }
        
        if (user.getRole().equals("ROLE_PARTNER_ADMIN") && 
            tour.getOwnerId().equals(user.getId())) {
            return true;
        }
        
        // Additional business rules
        if (tour.getStatus().equals("PUBLISHED") && user.getRole().equals("ROLE_CLIENT")) {
            return true;
        }
        
        return false;
    }
}
```

## Similar Nuxt + Spring Boot Repositories

### 1. Full-Stack E-Commerce Platforms

#### a) shopizer/shopizer
- **URL**: https://github.com/shopizer-ecommerce/shopizer
- **Tech Stack**: Spring Boot + Angular (but architecture is similar)
- **Security Features**:
  - Role-based access control
  - Customer vs. Admin separation
  - Product permissions
  - Order management security
- **Relevant Patterns**:
  - Multi-tenant store management
  - Resource-level permissions
  - Admin dashboard with granular access

#### b) broadleafcommerce/BroadleafCommerce
- **URL**: https://github.com/BroadleafCommerce/BroadleafCommerce
- **Tech Stack**: Spring Boot + React
- **Security Features**:
  - Permission-based security
  - Admin role hierarchy
  - Customer segmentation
- **Relevant Patterns**:
  - Flexible permission system
  - Customer groups and roles
  - Admin user management

### 2. Booking/Reservation Systems

#### a) airbnb-clone projects
- **Example**: Various open-source Airbnb clones
- **Relevant Patterns**:
  - Host vs. Guest permissions
  - Property management authorization
  - Booking lifecycle security
  - Multi-tenant property ownership

#### b) Hotel/Tour Booking Systems
- **Patterns to adopt**:
  - Resource ownership validation
  - Booking authorization rules
  - Admin vs. Partner separation
  - Guest user handling

### 3. Multi-Tenant SaaS Applications

#### a) jhipster/generator-jhipster
- **URL**: https://github.com/jhipster/generator-jhipster
- **Tech Stack**: Spring Boot + Vue/React/Angular
- **Security Features**:
  - JWT authentication
  - Role-based access
  - Authority management
  - User management UI
- **Relevant Patterns**:
  - Authority constants
  - SecurityUtils helper
  - Method-level security
  - Audit logging

#### b) Spring Petclinic Microservices
- **URL**: https://github.com/spring-petclinic/spring-petclinic-microservices
- **Security Features**:
  - OAuth2 with Spring Security
  - Service-to-service auth
  - Role management
- **Relevant Patterns**:
  - Distributed security
  - Token validation
  - API Gateway security

### 4. CMS/Admin Panels

#### a) craftercms/craftercms
- **URL**: https://github.com/craftercms/craftercms
- **Security Features**:
  - Fine-grained permissions
  - Content access control
  - Role hierarchy
- **Relevant Patterns**:
  - Permission inheritance
  - Resource-level security
  - Workflow permissions

### 5. Nuxt-Specific Full-Stack Examples

#### a) Nuxt + Spring Boot Starters
- **Search Terms**: "nuxt spring boot", "nuxt java backend"
- **Common Patterns**:
  - Nuxt 3 as frontend
  - Spring Boot REST API
  - JWT-based auth
  - Role management

#### b) Vue + Spring Boot Security Examples
- **Example Projects**:
  - vue-admin-template + Spring Boot
  - vuepress + Spring Security
- **Security Patterns**:
  - Token refresh mechanisms
  - Permission directives in Vue
  - Dynamic route generation based on roles

## Recommended Security Enhancements for Northern Chile

### Short-Term (Minimal Changes)

1. **Add Permission Constants**
```java
public class SecurityConstants {
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_PARTNER_ADMIN = "ROLE_PARTNER_ADMIN";
    public static final String ROLE_CLIENT = "ROLE_CLIENT";
    
    public static final String[] ADMIN_ROLES = {ROLE_SUPER_ADMIN, ROLE_PARTNER_ADMIN};
    public static final String[] ALL_ROLES = {ROLE_SUPER_ADMIN, ROLE_PARTNER_ADMIN, ROLE_CLIENT};
}
```

2. **Extract Security Logic to Reusable Service**
```java
@Service
public class AuthorizationService {
    
    public boolean canManageTour(User user, UUID tourId) {
        if (user.getRole().equals(SecurityConstants.ROLE_SUPER_ADMIN)) {
            return true;
        }
        
        if (user.getRole().equals(SecurityConstants.ROLE_PARTNER_ADMIN)) {
            Tour tour = tourRepository.findById(tourId).orElse(null);
            return tour != null && tour.getOwnerId().equals(user.getId());
        }
        
        return false;
    }
    
    public boolean canManageBooking(User user, UUID bookingId) {
        // Similar logic for bookings
        return true;
    }
}
```

3. **Add Security Testing**
```java
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTests {
    
    @Test
    @WithMockUser(username = "admin", roles = "SUPER_ADMIN")
    public void superAdminCanAccessAllTours() {
        // Test super admin access
    }
    
    @Test
    @WithMockUser(username = "partner", roles = "PARTNER_ADMIN")
    public void partnerAdminCanOnlyAccessOwnTours() {
        // Test partner admin access with owner_id filtering
    }
    
    @Test
    public void unauthenticatedUserCanViewPublicTours() {
        // Test public access
    }
}
```

### Medium-Term (Moderate Changes)

1. **Implement Permission Enum System**
   - Add Permission enum
   - Map roles to permissions
   - Update SecurityConfig to use permissions
   - Add permission-based authorization

2. **Add Audit Logging for Security Events**
   - Log authentication attempts
   - Log authorization failures
   - Log sensitive data access
   - Track permission changes

3. **Implement Resource-Level Permissions**
   - Add permission checks at service layer
   - Create custom security annotations
   - Add permission caching for performance

### Long-Term (Major Enhancements)

1. **Database-Driven Permission System**
   - Create Permission and Role entities
   - Add role management UI in admin panel
   - Implement dynamic permission checking
   - Add permission migration tools

2. **Implement ACL for Fine-Grained Control**
   - Add Spring Security ACL
   - Create ACL tables
   - Implement object-level permissions
   - Add ACL management UI

3. **Add Multi-Factor Authentication**
   - Implement TOTP/SMS verification
   - Add backup codes
   - Integrate with authenticator apps

## Frontend Security Considerations (Nuxt)

### 1. Role-Based UI Rendering

```typescript
// composables/useAuth.ts
export const useAuth = () => {
  const user = useAuthUser()
  
  const hasRole = (role: string) => {
    return user.value?.role === role
  }
  
  const hasAnyRole = (...roles: string[]) => {
    return roles.some(role => hasRole(role))
  }
  
  const hasPermission = (permission: string) => {
    // Check if user has specific permission
    return true
  }
  
  return {
    user,
    hasRole,
    hasAnyRole,
    hasPermission
  }
}

// Usage in components:
<template>
  <div v-if="hasRole('ROLE_SUPER_ADMIN')">
    <AdminPanel />
  </div>
  
  <UButton 
    v-if="hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')"
    @click="createTour"
  >
    Create Tour
  </UButton>
</template>
```

### 2. Route Guards with Permissions

```typescript
// middleware/auth.global.ts
export default defineNuxtRouteMiddleware((to, from) => {
  const { user, hasRole } = useAuth()
  
  // Check if route requires authentication
  if (to.meta.requiresAuth && !user.value) {
    return navigateTo('/auth/login')
  }
  
  // Check if route requires specific role
  if (to.meta.requiredRole) {
    const requiredRole = to.meta.requiredRole as string
    if (!hasRole(requiredRole)) {
      return navigateTo('/unauthorized')
    }
  }
})

// Usage in pages:
definePageMeta({
  requiresAuth: true,
  requiredRole: 'ROLE_SUPER_ADMIN'
})
```

### 3. API Client with Authorization

```typescript
// Already implemented in lib/api-client/
// Ensure JWT token is automatically included
const api = useApiClient()
api.setAuthToken(authStore.token)
```

## Security Best Practices Checklist

### Authentication
- [x] JWT-based stateless authentication
- [x] Secure password hashing (BCrypt)
- [x] Email verification
- [x] Password reset flow
- [x] OAuth2 social login (Google)
- [ ] Multi-factor authentication
- [ ] Account lockout after failed attempts
- [ ] Session management and token refresh

### Authorization
- [x] Role-based access control
- [x] Method-level security
- [x] Multi-tenant data isolation (owner_id filtering)
- [ ] Fine-grained permissions
- [ ] Resource-level authorization
- [ ] ACL for complex scenarios
- [ ] Dynamic permission loading

### Data Protection
- [x] HTTPS/TLS in production
- [x] Password hashing with BCrypt
- [x] SQL injection prevention (JPA)
- [x] XSS protection headers
- [x] CSRF protection
- [ ] Data encryption at rest
- [ ] PII handling and anonymization
- [ ] GDPR compliance features

### Audit & Monitoring
- [x] Basic audit logging
- [ ] Security event logging
- [ ] Failed authentication tracking
- [ ] Permission change auditing
- [ ] Anomaly detection
- [ ] Security alerts

### API Security
- [x] JWT token validation
- [x] CORS configuration
- [x] Rate limiting (basic)
- [x] Input validation
- [ ] API versioning
- [ ] Request signing
- [ ] Advanced rate limiting per user

## References and Resources

### Official Documentation
- **Spring Security Reference**: https://docs.spring.io/spring-security/reference/
- **Spring Security Architecture**: https://spring.io/guides/topicals/spring-security-architecture
- **Nuxt Security**: https://nuxt.com/modules/security

### Tutorials and Guides
- **Baeldung Spring Security**: https://www.baeldung.com/security-spring
- **Spring Security Method Security**: https://www.baeldung.com/spring-security-method-security
- **JWT with Spring Boot**: https://www.baeldung.com/spring-security-oauth-jwt

### Example Repositories
- **JHipster**: https://github.com/jhipster/generator-jhipster
- **Spring Petclinic**: https://github.com/spring-petclinic/spring-petclinic-microservices
- **Shopizer**: https://github.com/shopizer-ecommerce/shopizer

### Books
- "Spring Security in Action" by Laurentiu Spilca
- "OAuth 2 in Action" by Justin Richer and Antonio Sanso
- "Microservices Security in Action" by Prabath Siriwardena

## Conclusion

The Northern Chile project already has a solid security foundation with Spring Security 6, JWT authentication, and role-based access control. The current implementation is appropriate for the MVP stage.

For future enhancements, consider:

1. **Short-term**: Add security constants, extract authorization logic to reusable services, and add comprehensive security tests.

2. **Medium-term**: Implement an enum-based permission system for more granular control while maintaining simplicity.

3. **Long-term**: If the application grows to require very fine-grained permissions (e.g., different partners need different capabilities), implement a database-driven permission system with a management UI.

The key is to evolve the security system incrementally based on actual requirements rather than over-engineering upfront. The current RBAC system with owner_id filtering is a good balance of simplicity and security for a multi-tenant tour booking platform.
