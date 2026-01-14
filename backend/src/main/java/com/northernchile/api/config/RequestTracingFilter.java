package com.northernchile.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that adds request tracing information to MDC (Mapped Diagnostic Context).
 * This enables correlation of log entries across a single request.
 *
 * The requestId is either:
 * - Taken from X-Request-ID header (if provided by load balancer/gateway)
 * - Generated as a short UUID (8 characters)
 *
 * The requestId is returned in the X-Request-ID response header for debugging.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTracingFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Use existing request ID from header or generate new one
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Add to MDC for structured logging
        MDC.put(MDC_REQUEST_ID, requestId);

        // Return request ID in response header for debugging
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            chain.doFilter(request, response);
        } finally {
            // Always clean up MDC to prevent memory leaks in thread pools
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
