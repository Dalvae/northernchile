package com.northernchile.api.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Centralized cookie management utility.
 * Handles secure cookie creation with configurable domain and security settings.
 */
@Component
public class CookieHelper {

    @Value("${cookie.insecure:false}")
    private boolean cookieInsecure;

    @Value("${cookie.domain:}")
    private String cookieDomain;

    /**
     * Creates and adds a cookie to the response.
     *
     * @param response HttpServletResponse to add the cookie to
     * @param name Cookie name
     * @param value Cookie value
     * @param maxAge Duration for cookie validity
     */
    public void setCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(!cookieInsecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax");

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookieBuilder.domain(cookieDomain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }

    /**
     * Clears a cookie by setting its value to empty and maxAge to zero.
     *
     * @param response HttpServletResponse to clear the cookie from
     * @param name Cookie name to clear
     */
    public void clearCookie(HttpServletResponse response, String name) {
        setCookie(response, name, "", Duration.ZERO);
    }

    /**
     * Sets the auth token cookie (7-day validity).
     */
    public void setAuthTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, "auth_token", token, Duration.ofDays(7));
    }

    /**
     * Clears the auth token cookie.
     */
    public void clearAuthTokenCookie(HttpServletResponse response) {
        clearCookie(response, "auth_token");
    }

    /**
     * Sets the cart ID cookie (30-day validity).
     */
    public void setCartIdCookie(HttpServletResponse response, String cartId) {
        setCookie(response, "cartId", cartId, Duration.ofDays(30));
    }

    /**
     * Clears the cart ID cookie.
     */
    public void clearCartIdCookie(HttpServletResponse response) {
        clearCookie(response, "cartId");
    }
}
