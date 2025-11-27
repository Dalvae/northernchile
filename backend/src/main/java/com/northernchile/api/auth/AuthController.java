package com.northernchile.api.auth;

import com.northernchile.api.auth.dto.LoginReq;
import com.northernchile.api.auth.dto.PasswordResetDto;
import com.northernchile.api.auth.dto.PasswordResetRequestDto;
import com.northernchile.api.auth.dto.RegisterReq;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${cookie.insecure:false}")
    private boolean cookieInsecure;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq loginReq, HttpServletResponse response) {
        Map<String, Object> loginResponse = authService.login(loginReq);

        // Extract token from response
        String token = (String) loginResponse.get("token");

        // Create HttpOnly cookie with JWT token using ResponseCookie
        // secure=true by default (production), set cookie.insecure=true only for local dev
        ResponseCookie jwtCookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .secure(!cookieInsecure) // true in production, false only if cookie.insecure=true
                .path("/")
                .maxAge(Duration.ofDays(7)) // 7 days
                .sameSite("Lax") // CSRF protection
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        // Remove token from response body (security best practice)
        loginResponse.remove("token");

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq registerReq, jakarta.servlet.http.HttpServletRequest request) {
        authService.register(registerReq, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully. Please check your email to verify your account.");
        return ResponseEntity.ok(response);
    }

    /**
     * Verify email with token
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmail(token);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email verified successfully. You can now log in.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Request password reset - sends email with reset link
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto request,
            @RequestHeader(value = "Accept-Language", defaultValue = "es-CL") String language) {
        try {
            authService.requestPasswordReset(request.getEmail(), language);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset email sent. Please check your inbox.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Don't reveal if user exists or not for security
            Map<String, String> response = new HashMap<>();
            response.put("message", "If the email exists, a password reset link has been sent.");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Reset password with token
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDto request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully. You can now log in with your new password.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Resend verification email
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationEmail(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Accept-Language", defaultValue = "es-CL") String language) {
        try {
            authService.resendVerificationEmail(request.get("email"), language);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Verification email sent. Please check your inbox.");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to send verification email."));
        }
    }

    /**
     * Logout - clears the auth cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear the auth_token cookie by setting MaxAge to 0
        ResponseCookie jwtCookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ZERO) // Delete cookie
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Logged out successfully");
        return ResponseEntity.ok(responseBody);
    }
}