package com.northernchile.api.util;

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
}
