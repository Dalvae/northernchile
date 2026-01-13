package com.northernchile.api.auth;

import com.northernchile.api.auth.dto.LoginReq;
import com.northernchile.api.auth.dto.PasswordResetReq;
import com.northernchile.api.auth.dto.PasswordResetRequestReq;
import com.northernchile.api.auth.dto.RegisterReq;
import com.northernchile.api.util.CookieHelper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieHelper cookieHelper;

    public AuthController(AuthService authService, CookieHelper cookieHelper) {
        this.authService = authService;
        this.cookieHelper = cookieHelper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq loginReq, HttpServletResponse response) {
        Map<String, Object> loginResponse = authService.login(loginReq);

        // Extract token and set HttpOnly cookie
        String token = (String) loginResponse.get("token");
        cookieHelper.setAuthTokenCookie(response, token);

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
            @Valid @RequestBody PasswordResetRequestReq request,
            @RequestHeader(value = "Accept-Language", defaultValue = "es-CL") String language) {
        try {
            authService.requestPasswordReset(request.email(), language);
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
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetReq request) {
        try {
            authService.resetPassword(request.token(), request.newPassword());
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
        cookieHelper.clearAuthTokenCookie(response);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Logged out successfully");
        return ResponseEntity.ok(responseBody);
    }
}