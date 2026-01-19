package com.northernchile.api.auth;

import com.northernchile.api.auth.dto.LoginReq;
import com.northernchile.api.auth.dto.LoginRes;
import com.northernchile.api.auth.dto.PasswordResetReq;
import com.northernchile.api.auth.dto.PasswordResetRequestReq;
import com.northernchile.api.auth.dto.RegisterReq;
import com.northernchile.api.common.dto.MessageRes;
import com.northernchile.api.util.CookieHelper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<LoginRes> login(@Valid @RequestBody LoginReq loginReq, HttpServletResponse response) {
        LoginRes loginResponse = authService.login(loginReq);

        // Set HttpOnly cookie with token
        cookieHelper.setAuthTokenCookie(response, loginResponse.token());

        // Return response without token in body (security best practice)
        return ResponseEntity.ok(loginResponse.withoutToken());
    }

    @PostMapping("/register")
    public ResponseEntity<MessageRes> register(@Valid @RequestBody RegisterReq registerReq, jakarta.servlet.http.HttpServletRequest request) {
        authService.register(registerReq, request);
        return ResponseEntity.ok(MessageRes.of("User registered successfully. Please check your email to verify your account."));
    }

    /**
     * Verify email with token
     */
    @GetMapping("/verify-email")
    public ResponseEntity<MessageRes> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(MessageRes.of("Email verified successfully. You can now log in."));
    }

    /**
     * Request password reset - sends email with reset link
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<MessageRes> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestReq request,
            @RequestHeader(value = "Accept-Language", defaultValue = "es-CL") String language) {
        try {
            authService.requestPasswordReset(request.email(), language);
            return ResponseEntity.ok(MessageRes.of("Password reset email sent. Please check your inbox."));
        } catch (Exception e) {
            // Don't reveal if user exists or not for security
            return ResponseEntity.ok(MessageRes.of("If the email exists, a password reset link has been sent."));
        }
    }

    /**
     * Reset password with token
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<MessageRes> resetPassword(@Valid @RequestBody PasswordResetReq request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(MessageRes.of("Password reset successfully. You can now log in with your new password."));
    }

    /**
     * Resend verification email
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<MessageRes> resendVerificationEmail(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Accept-Language", defaultValue = "es-CL") String language) {
        authService.resendVerificationEmail(request.get("email"), language);
        return ResponseEntity.ok(MessageRes.of("Verification email sent. Please check your inbox."));
    }

    /**
     * Logout - clears the auth cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageRes> logout(HttpServletResponse response) {
        cookieHelper.clearAuthTokenCookie(response);
        return ResponseEntity.ok(MessageRes.of("Logged out successfully"));
    }

    /**
     * Check if an email is already registered (for checkout flow)
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = authService.emailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}