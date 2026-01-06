# Similar Nuxt + Spring Boot / Vue + Spring Boot Repositories

## Overview

This document catalogs open-source repositories that combine Vue/Nuxt frontend with Spring Boot backend, with a focus on security, permissions, and multi-tenant architectures that are relevant to the Northern Chile tour booking platform.

## 1. Full-Stack E-Commerce & Booking Systems

### 1.1 Shopizer E-Commerce
- **Repository**: https://github.com/shopizer-ecommerce/shopizer
- **Tech Stack**: Spring Boot + Angular (adaptable patterns)
- **Stars**: ~3k
- **Security Features**:
  - Role-based access control (Admin, Customer, Merchant)
  - Multi-store/multi-tenant architecture
  - Product and catalog permissions
  - Order management security
  - Payment integration security
- **Why Relevant**:
  - Multi-merchant model similar to our partner admin model
  - Product catalog similar to tour catalog
  - Order processing similar to booking management
- **Code Examples to Study**:
  - `sm-core/src/main/java/com/salesmanager/core/business/services/user/PermissionService.java`
  - Security configuration with role hierarchy
  - Store-level data isolation

### 1.2 Broadleaf Commerce
- **Repository**: https://github.com/BroadleafCommerce/BroadleafCommerce
- **Tech Stack**: Spring Boot + React/Admin UI
- **Stars**: ~1.7k
- **Security Features**:
  - Permission-based security framework
  - Admin role hierarchy
  - Customer segmentation
  - Fine-grained catalog permissions
- **Why Relevant**:
  - Flexible permission system with inheritance
  - Resource-level authorization
  - Admin vs. customer separation
- **Code Examples to Study**:
  - `core/broadleaf-framework/src/main/java/org/broadleafcommerce/core/security/`
  - Permission entity model
  - Role composition patterns

### 1.3 Saleor Platform (Python, but great patterns)
- **Repository**: https://github.com/saleor/saleor
- **Tech Stack**: Python Django + React (architecture patterns applicable)
- **Stars**: ~20k
- **Security Features**:
  - GraphQL with permission decorators
  - App-based permission system
  - Webhook authentication
- **Why Relevant**:
  - Modern permission patterns
  - Multi-tenant architecture
  - Extensive permission documentation
- **Patterns to Adapt**:
  - Permission naming conventions
  - Permission groups
  - API-first security approach

## 2. JHipster Generated Applications

### 2.1 JHipster Generator
- **Repository**: https://github.com/jhipster/generator-jhipster
- **Tech Stack**: Spring Boot + Vue/React/Angular
- **Stars**: ~21k
- **Security Features**:
  - JWT authentication out of the box
  - Authority management
  - User management UI
  - OAuth2/OIDC integration
  - Method security annotations
- **Why Relevant**:
  - Industry-standard patterns
  - Production-ready security
  - Well-documented approach
- **Code Examples to Study**:
  - `generators/spring-boot/templates/src/main/java/package/config/SecurityConfiguration.java.ejs`
  - `AuthorityConstants.java` - Role constants pattern
  - `SecurityUtils.java` - Utility methods
  - JWT token provider implementation
- **How to Use**:
  ```bash
  # Generate a sample app to study
  npm install -g generator-jhipster
  jhipster
  # Select: Vue + Spring Boot + JWT
  ```

### 2.2 JHipster Sample Applications
- **Repository**: https://github.com/jhipster/jhipster-sample-app-vuejs
- **Tech Stack**: Spring Boot + Vue 3
- **Why Relevant**:
  - Complete working example
  - Frontend permission handling
  - Role-based UI rendering
- **Code Examples**:
  - `src/main/webapp/app/account/` - Account management
  - `src/main/webapp/app/admin/` - Admin UI with role checks
  - Authority guards and directives

## 3. Booking & Reservation Systems

### 3.1 Open Source Hotel Management Systems

#### a) QloApps (PHP, but good patterns)
- **Repository**: https://github.com/webkul/hotelcommerce
- **Why Relevant**:
  - Room booking similar to tour scheduling
  - Multi-property management
  - Booking calendar patterns
- **Patterns to Adapt**:
  - Availability calculation
  - Booking conflict prevention
  - Property owner permissions

#### b) Hotel Management System (Spring Boot)
- **Search**: GitHub for "hotel management spring boot"
- **Example**: https://github.com/surendra-kushwaha/hotel-management-system
- **Why Relevant**:
  - Room booking logic
  - Guest management
  - Reservation security
- **Code Examples**:
  - Booking entity design
  - Availability checking
  - Customer vs. admin roles

### 3.2 Airbnb Clone Projects

#### a) Airbnb Clone with Spring Boot
- **Example**: https://github.com/topics/airbnb-clone?l=java
- **Why Relevant**:
  - Host vs. Guest permission model (similar to Partner vs. Client)
  - Property ownership validation
  - Booking authorization
  - Review system permissions
- **Patterns to Adapt**:
  - Owner verification before property operations
  - Booking lifecycle state machine
  - Guest authentication for bookings

#### b) Full-Stack Airbnb Clones
- **Search**: "airbnb clone spring boot vue"
- **Common Patterns**:
  - Property management with owner_id
  - Booking requests and approvals
  - Calendar availability
  - Multi-tenant data isolation

## 4. Multi-Tenant SaaS Applications

### 4.1 Spring Boot Multi-Tenant Examples

#### a) Baeldung Multi-Tenancy Guide
- **Article**: https://www.baeldung.com/spring-boot-multitenancy
- **Repository**: https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-data-2
- **Patterns Covered**:
  - Database per tenant
  - Schema per tenant
  - Shared database with discriminator (our approach)
- **Code Examples**:
  - `TenantContext` - Thread-local tenant storage
  - `TenantAwareDataSource` - Dynamic data source routing
  - `TenantFilter` - Tenant identification from request

#### b) Multi-Tenant SaaS Starter
- **Search**: "spring boot multi-tenant saas"
- **Example**: https://github.com/Wenox/spring-boot-multi-tenancy
- **Why Relevant**:
  - Row-level security with tenant_id
  - Tenant isolation patterns
  - Security context configuration
- **Patterns to Adapt**:
  - Automatic tenant_id filtering in queries
  - Tenant-aware repositories
  - Cross-tenant access prevention

### 4.2 Project Management Tools

#### a) Taiga (Python/Angular, but excellent patterns)
- **Repository**: https://github.com/taigaio/taiga-back
- **Why Relevant**:
  - Project permissions (similar to tour ownership)
  - Team member roles
  - Resource-level permissions
- **Patterns to Adapt**:
  - Permission matrix design
  - Invitation and access control
  - Audit logging

## 5. CMS & Admin Panel Systems

### 5.1 Spring Boot Admin Panels

#### a) Halo Blog System
- **Repository**: https://github.com/halo-dev/halo
- **Tech Stack**: Spring Boot + Vue
- **Stars**: ~30k
- **Security Features**:
  - User and role management
  - Permission system for content
  - Plugin security
- **Why Relevant**:
  - Modern Vue + Spring Boot integration
  - Clean architecture
  - Admin UI patterns
- **Code Examples**:
  - `src/main/java/run/halo/app/security/` - Security config
  - Role and permission entities
  - Authorization filters

#### b) RuoYi Vue
- **Repository**: https://github.com/yangzongzhuan/RuoYi-Vue
- **Tech Stack**: Spring Boot + Vue
- **Stars**: ~5k
- **Security Features**:
  - Menu permissions
  - Button-level permissions
  - Data scope permissions
  - Role management UI
- **Why Relevant**:
  - Comprehensive permission system
  - Chinese project but excellent architecture
  - Frontend permission directives
- **Code Examples**:
  - Permission annotation: `@PreAuthorize("@ss.hasPermi('system:user:list')")`
  - Data scope filtering
  - Dynamic menu generation based on permissions

### 5.2 Headless CMS Examples

#### a) Strapi (Node.js, but patterns applicable)
- **Repository**: https://github.com/strapi/strapi
- **Stars**: ~60k
- **Why Relevant**:
  - Flexible role/permission system
  - Content type permissions
  - API-level security
- **Patterns to Adapt**:
  - Permission naming: `resource:action` (e.g., `tour:create`)
  - Permission conditions
  - Role templates

## 6. Vue/Nuxt + Spring Boot Starters

### 6.1 Vue Admin Templates with Spring Boot

#### a) Vue Element Admin + Spring Boot
- **Search**: "vue-element-admin spring boot"
- **Example**: https://github.com/PanJiaChen/vue-element-admin (frontend)
- **Backend**: Various Spring Boot backends integrate with this
- **Security Features**:
  - Role-based routing
  - Permission directives (v-permission)
  - Dynamic menu based on roles
- **Code Examples**:
  ```vue
  <!-- Permission directive usage -->
  <el-button v-permission="['admin','editor']">Edit</el-button>
  ```

#### b) vue-admin-template Spring Boot Integration
- **Search**: "vue-admin-template spring boot backend"
- **Patterns**:
  - JWT token management in frontend
  - Permission store in Pinia/Vuex
  - Route guards based on roles

### 6.2 Nuxt 3 + Spring Boot Examples

#### a) Nuxt 3 Authentication Patterns
- **Module**: https://github.com/sidebase/nuxt-auth
- **Why Relevant**:
  - Modern Nuxt 3 auth patterns
  - JWT handling
  - Session management
- **Integration Examples**:
  - Connect with Spring Boot JWT endpoints
  - Role-based route guards
  - Automatic token refresh

#### b) Full-Stack Nuxt + Spring Boot Starter
- **Search**: GitHub for "nuxt 3 spring boot"
- **Common Patterns**:
  - OpenAPI client generation (like our setup)
  - SSR with authentication
  - API proxy configuration
  - Environment-based API URLs

## 7. Security-Specific Projects

### 7.1 Spring Security Examples

#### a) Spring Security Samples
- **Repository**: https://github.com/spring-projects/spring-security-samples
- **Official Spring Security examples**
- **Examples to Study**:
  - `servlet/spring-boot/java/jwt/login` - JWT authentication
  - `servlet/spring-boot/java/oauth2/login` - OAuth2 login
  - Method security examples

#### b) Spring Security JWT Tutorial
- **Repository**: https://github.com/Yoh0xFF/java-spring-security-example
- **Why Relevant**:
  - Complete JWT implementation
  - Refresh token patterns
  - Role-based security
- **Code Examples**:
  - `JwtTokenProvider.java`
  - `JwtTokenFilter.java`
  - Security configuration

### 7.2 OAuth2 and Social Login

#### a) Spring Boot OAuth2 Examples
- **Repository**: https://github.com/spring-projects/spring-authorization-server
- **Why Relevant**:
  - OAuth2 server implementation
  - Social login integration
  - Token management
- **Patterns for Google Login**:
  - OAuth2 client configuration
  - Custom user info mapping
  - Account linking

## 8. Specific Feature Patterns

### 8.1 Calendar/Scheduling Systems

#### a) FullCalendar + Spring Boot
- **Search**: "fullcalendar spring boot"
- **Why Relevant**:
  - Tour schedule calendar display
  - Event management with permissions
  - Drag-and-drop scheduling
- **Patterns**:
  - Event CRUD with authorization
  - Calendar event filtering by role
  - Recurring event patterns

### 8.2 Payment Integration Examples

#### a) Spring Boot Stripe Integration
- **Search**: "spring boot stripe payment"
- **Patterns Applicable to Transbank/MercadoPago**:
  - Payment session creation
  - Webhook validation
  - Payment status tracking
  - Refund handling

#### b) Spring Boot Payment Gateway
- **Search**: "spring boot payment gateway integration"
- **Patterns**:
  - Strategy pattern for multiple providers
  - Payment state machine
  - Transaction security
  - PCI compliance considerations

## 9. Testing and Security Testing

### 9.1 Spring Security Test Examples
- **Documentation**: https://docs.spring.io/spring-security/reference/servlet/test/
- **Patterns**:
  - `@WithMockUser` annotation
  - Security test utilities
  - Integration testing with authentication

### 9.2 Vue/Nuxt Testing with Auth
- **Tools**:
  - Vitest with mocked auth
  - Playwright E2E with login flows
  - Component testing with role-based rendering

## 10. Recommended Study Path

### Phase 1: Understand Current Patterns
1. Study JHipster generated app (most comprehensive)
2. Review Spring Security official samples
3. Examine our current implementation

### Phase 2: Permission Systems
1. Study RuoYi-Vue permission system (most detailed)
2. Review Broadleaf Commerce role hierarchy
3. Examine Halo blog permission patterns

### Phase 3: Multi-Tenancy
1. Study Baeldung multi-tenancy guide
2. Review Shopizer multi-store patterns
3. Examine row-level security implementations

### Phase 4: Frontend Integration
1. Study vue-element-admin role-based UI
2. Review Nuxt auth module patterns
3. Examine JHipster frontend security

## 11. Code Pattern Library

### 11.1 Permission Check Patterns

**Pattern 1: Simple Role Check**
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnly() { }
```

**Pattern 2: Multiple Role Check**
```java
@PreAuthorize("hasAnyRole('ADMIN', 'PARTNER')")
public void adminOrPartner() { }
```

**Pattern 3: Custom Expression**
```java
@PreAuthorize("@securityService.canAccess(#resourceId, authentication)")
public void conditionalAccess(Long resourceId) { }
```

**Pattern 4: Permission-Based**
```java
@PreAuthorize("hasAuthority('tour:write')")
public void tourWrite() { }
```

**Pattern 5: Combined Logic**
```java
@PreAuthorize("hasRole('ADMIN') or @tourSecurity.isOwner(#tourId, authentication)")
public void ownerOrAdmin(Long tourId) { }
```

### 11.2 Frontend Permission Patterns

**Pattern 1: Composable**
```typescript
// composables/usePermission.ts
export const usePermission = () => {
  const { user } = useAuth()
  
  const can = (permission: string) => {
    return user.value?.permissions?.includes(permission)
  }
  
  return { can }
}
```

**Pattern 2: Directive**
```typescript
// plugins/permission.ts
export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.directive('permission', {
    mounted(el, binding) {
      const { user } = useAuth()
      const permission = binding.value
      
      if (!user.value?.permissions?.includes(permission)) {
        el.parentNode?.removeChild(el)
      }
    }
  })
})
```

**Pattern 3: Component**
```vue
<!-- components/Permission.vue -->
<template>
  <slot v-if="hasPermission" />
</template>

<script setup lang="ts">
const props = defineProps<{
  permission: string
}>()

const { can } = usePermission()
const hasPermission = computed(() => can(props.permission))
</script>
```

## 12. Architecture Decision Records

### ADR 1: Role-Based vs. Permission-Based
**Current**: Role-based with 3 roles
**Recommendation**: Evolve to permission-based for flexibility
**Timeline**: Medium-term

### ADR 2: Enum vs. Database Permissions
**Current**: String-based roles
**Recommendation**: Enum-based for type safety, database for long-term
**Timeline**: Enum (short-term), Database (long-term)

### ADR 3: Method vs. URL Security
**Current**: Mix of URL patterns and method security
**Recommendation**: Prefer method security for granularity
**Timeline**: Gradually migrate

## 13. Useful Resources

### Documentation
- **Spring Security**: https://docs.spring.io/spring-security/reference/
- **Vue Router Auth**: https://router.vuejs.org/guide/advanced/navigation-guards.html
- **Nuxt Auth**: https://auth.sidebase.io/

### Tutorials
- **Baeldung Spring Security**: https://www.baeldung.com/security-spring
- **Vue Security Best Practices**: https://vuejs.org/guide/best-practices/security.html

### Communities
- **Stack Overflow**: [spring-security], [vue.js], [nuxt.js]
- **Reddit**: r/SpringBoot, r/vuejs
- **Discord**: Nuxt Discord, Spring Discord

## Conclusion

This collection of repositories and patterns provides a comprehensive reference for evolving the Northern Chile security system. The key takeaways are:

1. **JHipster** provides the most production-ready, well-documented patterns for Spring Boot + Vue/Nuxt
2. **RuoYi-Vue** offers the most comprehensive permission system implementation
3. **Multi-tenant patterns** from e-commerce platforms directly apply to our partner admin model
4. **Frontend permission handling** is crucial and should be consistent with backend

The current implementation is solid for MVP. Use this document as a reference when implementing future security enhancements.
