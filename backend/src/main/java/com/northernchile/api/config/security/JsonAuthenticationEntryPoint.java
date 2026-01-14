package com.northernchile.api.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom authentication entry point that returns JSON error responses
 * instead of redirecting to a login page (which would return HTML).
 * 
 * This ensures API clients always receive JSON responses for 401 errors.
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Check for specific JWT error from filter
        String errorCode = (String) request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_CODE_ATTR);
        String errorMessage = (String) request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_MESSAGE_ATTR);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", Instant.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("path", request.getRequestURI());

        // Provide specific error message if available
        if (errorCode != null) {
            errorResponse.put("code", errorCode);
            errorResponse.put("message", errorMessage);
        } else {
            errorResponse.put("message", "Authentication required to access this resource");
        }

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
