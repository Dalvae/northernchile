package com.northernchile.api.util;

import com.northernchile.api.model.OwnedEntity;
import com.northernchile.api.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Utility class for security/authentication helper methods.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class, no instantiation
    }

    /**
     * Check if the authentication has a specific role.
     */
    public static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }

    /**
     * Check if the authentication has any of the specified roles.
     */
    public static boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || roles == null) {
            return false;
        }
        for (String role : roles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is authenticated.
     */
    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Check if user is a super admin.
     */
    public static boolean isSuperAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_SUPER_ADMIN");
    }

    /**
     * Check if the user is a super admin by role string.
     */
    public static boolean isSuperAdmin(User user) {
        return user != null && "ROLE_SUPER_ADMIN".equals(user.getRole());
    }

    /**
     * Check if user is a partner admin.
     */
    public static boolean isPartnerAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_PARTNER_ADMIN");
    }

    /**
     * Check if user is any kind of admin.
     */
    public static boolean isAdmin(Authentication authentication) {
        return hasAnyRole(authentication, "ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN");
    }

    /**
     * Validate that the current user has permission to access/modify the given owned entity.
     * Super admins can access all entities, while other users can only access their own.
     *
     * @param currentUser the current user
     * @param entity the entity to check ownership of
     * @throws AccessDeniedException if the user does not have permission
     */
    public static void validateOwnership(User currentUser, OwnedEntity entity) {
        validateOwnership(currentUser, entity, "You do not have permission to access this resource.");
    }

    /**
     * Validate that the current user has permission to access/modify the given owned entity.
     * Super admins can access all entities, while other users can only access their own.
     *
     * @param currentUser the current user
     * @param entity the entity to check ownership of
     * @param message the error message to use if access is denied
     * @throws AccessDeniedException if the user does not have permission
     */
    public static void validateOwnership(User currentUser, OwnedEntity entity, String message) {
        if (currentUser == null || entity == null) {
            throw new AccessDeniedException(message);
        }
        
        // Super admins can access everything
        if (isSuperAdmin(currentUser)) {
            return;
        }
        
        // Check if user owns the entity
        if (entity.getOwner() == null || !entity.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(message);
        }
    }

    /**
     * Check if the current user owns the given entity (without throwing).
     *
     * @param currentUser the current user
     * @param entity the entity to check ownership of
     * @return true if the user is a super admin or owns the entity
     */
    public static boolean canAccess(User currentUser, OwnedEntity entity) {
        if (currentUser == null || entity == null) {
            return false;
        }
        
        if (isSuperAdmin(currentUser)) {
            return true;
        }
        
        return entity.getOwner() != null && entity.getOwner().getId().equals(currentUser.getId());
    }
}
