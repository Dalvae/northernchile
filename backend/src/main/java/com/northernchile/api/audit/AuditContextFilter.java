package com.northernchile.api.audit;

import com.northernchile.api.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that populates AuditContext with the current authenticated user.
 *
 * This filter runs after Spring Security's authentication filters and extracts
 * the authenticated User from the SecurityContext, making it available to
 * AuditEntityListener for automatic audit logging.
 *
 * Order: Runs after authentication (Ordered.LOWEST_PRECEDENCE - 100) to ensure
 * the SecurityContext is populated before we read from it.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class AuditContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof User user) {
                    AuditContext.setCurrentUser(user);
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            // Always clear context to prevent memory leaks
            AuditContext.clear();
        }
    }
}
