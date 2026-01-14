package com.northernchile.api.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Request attribute key for JWT error code.
     * Used by JsonAuthenticationEntryPoint to provide specific error messages.
     */
    public static final String JWT_ERROR_CODE_ATTR = "jwt_error_code";
    public static final String JWT_ERROR_MESSAGE_ATTR = "jwt_error_message";

    // Error codes for JWT validation failures
    public static final String ERROR_TOKEN_EXPIRED = "token_expired";
    public static final String ERROR_TOKEN_INVALID = "token_invalid";
    public static final String ERROR_TOKEN_SIGNATURE = "token_signature_invalid";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String username = null;
        String jwt = null;

        // First, try to get token from Authorization header (for backwards compatibility)
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }

        // If no Authorization header, try to get token from HttpOnly cookie
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("auth_token".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // Extract username from JWT if token was found
        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                log.debug("JWT token expired: {}", e.getMessage());
                request.setAttribute(JWT_ERROR_CODE_ATTR, ERROR_TOKEN_EXPIRED);
                request.setAttribute(JWT_ERROR_MESSAGE_ATTR, "Token has expired");
            } catch (MalformedJwtException e) {
                log.debug("Malformed JWT token: {}", e.getMessage());
                request.setAttribute(JWT_ERROR_CODE_ATTR, ERROR_TOKEN_INVALID);
                request.setAttribute(JWT_ERROR_MESSAGE_ATTR, "Invalid token format");
            } catch (SignatureException e) {
                log.warn("JWT signature validation failed");
                request.setAttribute(JWT_ERROR_CODE_ATTR, ERROR_TOKEN_SIGNATURE);
                request.setAttribute(JWT_ERROR_MESSAGE_ATTR, "Token signature invalid");
            } catch (Exception e) {
                log.debug("JWT token validation failed: {}", e.getMessage());
                request.setAttribute(JWT_ERROR_CODE_ATTR, ERROR_TOKEN_INVALID);
                request.setAttribute(JWT_ERROR_MESSAGE_ATTR, "Token validation failed");
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }
}
