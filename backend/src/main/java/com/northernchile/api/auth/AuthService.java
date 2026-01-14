package com.northernchile.api.auth;

import com.northernchile.api.auth.dto.LoginReq;
import com.northernchile.api.auth.dto.RegisterReq;
import com.northernchile.api.config.security.JwtUtil;
import com.northernchile.api.exception.EmailAlreadyExistsException;
import com.northernchile.api.model.EmailVerificationToken;
import com.northernchile.api.model.PasswordResetToken;
import com.northernchile.api.model.User;
import com.northernchile.api.notification.event.PasswordResetRequestedEvent;
import com.northernchile.api.notification.event.UserRegisteredEvent;
import com.northernchile.api.security.Role;
import com.northernchile.api.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${NUXT_PUBLIC_BASE_URL:http://localhost:3000}")
    private String frontendBaseUrl;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationConfiguration authenticationConfiguration, JwtUtil jwtUtil,
                       TokenService tokenService, ApplicationEventPublisher eventPublisher) throws Exception {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User register(RegisterReq registerReq, HttpServletRequest request) {
        if (userRepository.findByEmail(registerReq.email()).isPresent()) {
            throw new EmailAlreadyExistsException(registerReq.email());
        }

        User user = new User();
        user.setEmail(registerReq.email());
        user.setFullName(registerReq.fullName());
        user.setPasswordHash(passwordEncoder.encode(registerReq.password()));
        user.setNationality(registerReq.nationality());
        user.setPhoneNumber(registerReq.phoneNumber());
        user.setDateOfBirth(registerReq.dateOfBirth());
        user.setRole(Role.CLIENT.getRoleName()); // Default role
        user.setAuthProvider("LOCAL");
        user.setEmailVerified(false); // Email not verified yet

        User savedUser = userRepository.save(user);

        // Publish event for verification email (decoupled from email sending)
        EmailVerificationToken token = tokenService.createEmailVerificationToken(savedUser);
        String verificationUrl = frontendBaseUrl + "/verify-email?token=" + token.getToken();
        String languageCode = getLanguageFromRequest(request);
        eventPublisher.publishEvent(new UserRegisteredEvent(
                savedUser.getEmail(),
                savedUser.getFullName(),
                verificationUrl,
                languageCode
        ));

        return savedUser;
    }

    /**
     * Extract language code from Accept-Language header
     * Supports: es, en, pt
     * Default: es-CL
     */
    private String getLanguageFromRequest(HttpServletRequest request) {
        String acceptLanguage = request != null ? request.getHeader("Accept-Language") : null;

        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return "es-CL"; // Default to Spanish (Chile)
        }

        // Parse Accept-Language header (e.g., "en-US,en;q=0.9,es;q=0.8")
        String[] languages = acceptLanguage.split(",");
        for (String lang : languages) {
            String langCode = lang.split(";")[0].trim().toLowerCase();

            // Map to our supported locales
            if (langCode.startsWith("es")) {
                return "es-CL";
            } else if (langCode.startsWith("pt")) {
                return "pt-BR";
            } else if (langCode.startsWith("en")) {
                return "en-US";
            }
        }

        return "es-CL"; // Default fallback
    }

    public Map<String, Object> login(LoginReq loginReq) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginReq.email(), loginReq.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication"));

        // Generate JWT with userId and fullName included
        String jwt = jwtUtil.generateToken(userDetails, user.getId().toString(), user.getFullName());

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("fullName", user.getFullName());
        userMap.put("nationality", user.getNationality());
        userMap.put("phoneNumber", user.getPhoneNumber());
        userMap.put("dateOfBirth", user.getDateOfBirth());
        userMap.put("role", user.getRole()); // Return role as singular String, not array

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userMap);

        return response;
    }

    /**
     * Verify user email with token
     */
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenService.validateEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenService.markEmailVerificationTokenAsUsed(verificationToken);
    }

    /**
     * Request password reset - sends email with reset link
     */
    @Transactional
    public void requestPasswordReset(String email, String languageCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        PasswordResetToken token = tokenService.createPasswordResetToken(user);
        String resetUrl = frontendBaseUrl + "/auth?token=" + token.getToken();

        eventPublisher.publishEvent(new PasswordResetRequestedEvent(
                user.getEmail(),
                user.getFullName(),
                resetUrl,
                languageCode != null ? languageCode : "es-CL"
        ));
    }

    /**
     * Reset password with token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenService.validatePasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenService.markPasswordResetTokenAsUsed(resetToken);
    }

    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(String email, String languageCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email already verified");
        }

        EmailVerificationToken token = tokenService.createEmailVerificationToken(user);
        String verificationUrl = frontendBaseUrl + "/verify-email?token=" + token.getToken();

        eventPublisher.publishEvent(new UserRegisteredEvent(
                user.getEmail(),
                user.getFullName(),
                verificationUrl,
                languageCode != null ? languageCode : "es-CL"
        ));
    }
}