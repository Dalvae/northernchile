package com.northernchile.api.auth;

import com.northernchile.api.auth.dto.LoginReq;
import com.northernchile.api.auth.dto.RegisterReq;
import com.northernchile.api.config.security.JwtUtil;
import com.northernchile.api.exception.EmailAlreadyExistsException;
import com.northernchile.api.model.EmailVerificationToken;
import com.northernchile.api.model.PasswordResetToken;
import com.northernchile.api.model.User;
import com.northernchile.api.notification.EmailService;
import com.northernchile.api.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private HttpServletRequest httpRequest;

    private AuthService authService;

    private User testUser;
    private RegisterReq validRegisterReq;
    private LoginReq validLoginReq;

    @BeforeEach
    void setUp() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
        
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationConfiguration,
                jwtUtil,
                tokenService,
                emailService
        );
        
        ReflectionTestUtils.setField(authService, "frontendBaseUrl", "http://localhost:3000");

        // Set up test user using constructor (id, email, passwordHash, fullName, nationality, phoneNumber, dateOfBirth, role, authProvider, providerId)
        testUser = new User(
            UUID.randomUUID(),
            "test@example.com",
            "hashedPassword",
            "Test User",
            null,
            null,
            null,
            "ROLE_CLIENT",
            "LOCAL",
            null
        );
        testUser.setEmailVerified(true);

        validRegisterReq = new RegisterReq();
        validRegisterReq.setEmail("newuser@example.com");
        validRegisterReq.setFullName("New User");
        validRegisterReq.setPassword("securePassword123");
        validRegisterReq.setNationality("CL");
        validRegisterReq.setPhoneNumber("+56912345678");

        validLoginReq = new LoginReq();
        validLoginReq.setEmail("test@example.com");
        validLoginReq.setPassword("password123");
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() {
            // Given
            when(userRepository.findByEmail(validRegisterReq.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(validRegisterReq.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
                return user;
            });
            
            EmailVerificationToken token = new EmailVerificationToken();
            token.setToken("verification-token-123");
            when(tokenService.createEmailVerificationToken(any(User.class))).thenReturn(token);

            // When
            User result = authService.register(validRegisterReq, httpRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(validRegisterReq.getEmail());
            assertThat(result.getRole()).isEqualTo("ROLE_CLIENT");
            assertThat(result.isEmailVerified()).isFalse();
            
            verify(userRepository).save(any(User.class));
            verify(emailService).sendVerificationEmail(
                    eq(validRegisterReq.getEmail()),
                    eq(validRegisterReq.getFullName()),
                    contains("verification-token-123"),
                    anyString()
            );
        }

        @Test
        @DisplayName("Should reject registration when email already exists")
        void shouldRejectRegistrationWhenEmailExists() {
            // Given
            when(userRepository.findByEmail(validRegisterReq.getEmail()))
                    .thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.register(validRegisterReq, httpRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class);
            
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void shouldEncodePasswordBeforeSaving() {
            // Given
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(passwordEncoder.encode("securePassword123")).thenReturn("$2a$10$encodedHash");
            
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
                return user;
            });
            
            EmailVerificationToken token = new EmailVerificationToken();
            token.setToken("token");
            when(tokenService.createEmailVerificationToken(any())).thenReturn(token);

            // When
            authService.register(validRegisterReq, httpRequest);

            // Then
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPasswordHash()).isEqualTo("$2a$10$encodedHash");
        }

        @Test
        @DisplayName("Should set default role as ROLE_CLIENT")
        void shouldSetDefaultRoleAsClient() {
            // Given
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hash");
            
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
                return u;
            });
            
            EmailVerificationToken token = new EmailVerificationToken();
            token.setToken("token");
            when(tokenService.createEmailVerificationToken(any())).thenReturn(token);

            // When
            authService.register(validRegisterReq, httpRequest);

            // Then
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo("ROLE_CLIENT");
        }

        @Test
        @DisplayName("Should detect language from Accept-Language header")
        void shouldDetectLanguageFromHeader() {
            // Given
            when(httpRequest.getHeader("Accept-Language")).thenReturn("en-US,en;q=0.9");
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                ReflectionTestUtils.setField(u, "id", UUID.randomUUID());
                return u;
            });
            
            EmailVerificationToken token = new EmailVerificationToken();
            token.setToken("token");
            when(tokenService.createEmailVerificationToken(any())).thenReturn(token);

            // When
            authService.register(validRegisterReq, httpRequest);

            // Then
            verify(emailService).sendVerificationEmail(any(), any(), any(), eq("en-US"));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            // Given
            Authentication authentication = mock(Authentication.class);
            UserDetails userDetails = mock(UserDetails.class);
            
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(authentication.getName()).thenReturn(testUser.getEmail());
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(jwtUtil.generateToken(eq(userDetails), anyString(), anyString()))
                    .thenReturn("jwt-token-123");

            // When
            Map<String, Object> result = authService.login(validLoginReq);

            // Then
            assertThat(result).containsKey("token");
            assertThat(result).containsKey("user");
            assertThat(result.get("token")).isEqualTo("jwt-token-123");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) result.get("user");
            assertThat(userMap.get("email")).isEqualTo(testUser.getEmail());
            assertThat(userMap.get("role")).isEqualTo("ROLE_CLIENT");
        }

        @Test
        @DisplayName("Should reject login with invalid credentials")
        void shouldRejectLoginWithInvalidCredentials() {
            // Given
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When/Then
            assertThatThrownBy(() -> authService.login(validLoginReq))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Should include user info in response")
        void shouldIncludeUserInfoInResponse() {
            // Given
            testUser.setNationality("CL");
            testUser.setPhoneNumber("+56912345678");
            testUser.setDateOfBirth(LocalDate.of(1990, 1, 15));
            
            Authentication authentication = mock(Authentication.class);
            UserDetails userDetails = mock(UserDetails.class);
            
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(authentication.getName()).thenReturn(testUser.getEmail());
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token");

            // When
            Map<String, Object> result = authService.login(validLoginReq);

            // Then
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) result.get("user");
            assertThat(userMap.get("nationality")).isEqualTo("CL");
            assertThat(userMap.get("phoneNumber")).isEqualTo("+56912345678");
            assertThat(userMap.get("dateOfBirth")).isEqualTo(LocalDate.of(1990, 1, 15));
        }
    }

    @Nested
    @DisplayName("Email Verification Tests")
    class EmailVerificationTests {

        @Test
        @DisplayName("Should verify email successfully with valid token")
        void shouldVerifyEmailSuccessfully() {
            // Given
            EmailVerificationToken token = new EmailVerificationToken();
            token.setToken("valid-token");
            token.setUser(testUser);
            testUser.setEmailVerified(false);
            
            when(tokenService.validateEmailVerificationToken("valid-token"))
                    .thenReturn(Optional.of(token));

            // When
            authService.verifyEmail("valid-token");

            // Then
            assertThat(testUser.isEmailVerified()).isTrue();
            verify(userRepository).save(testUser);
            verify(tokenService).markEmailVerificationTokenAsUsed(token);
        }

        @Test
        @DisplayName("Should reject invalid verification token")
        void shouldRejectInvalidVerificationToken() {
            // Given
            when(tokenService.validateEmailVerificationToken("invalid-token"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.verifyEmail("invalid-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid or expired");
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("Should request password reset successfully")
        void shouldRequestPasswordResetSuccessfully() {
            // Given
            when(userRepository.findByEmail(testUser.getEmail()))
                    .thenReturn(Optional.of(testUser));
            
            PasswordResetToken token = new PasswordResetToken();
            token.setToken("reset-token-123");
            when(tokenService.createPasswordResetToken(testUser)).thenReturn(token);

            // When
            authService.requestPasswordReset(testUser.getEmail(), "es-CL");

            // Then
            verify(emailService).sendPasswordResetEmail(
                    eq(testUser.getEmail()),
                    eq(testUser.getFullName()),
                    contains("reset-token-123"),
                    eq("es-CL")
            );
        }

        @Test
        @DisplayName("Should reject password reset for non-existent user")
        void shouldRejectPasswordResetForNonExistentUser() {
            // Given
            when(userRepository.findByEmail("nonexistent@example.com"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.requestPasswordReset("nonexistent@example.com", "es-CL"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("Should reset password successfully with valid token")
        void shouldResetPasswordSuccessfully() {
            // Given
            PasswordResetToken token = new PasswordResetToken();
            token.setToken("valid-reset-token");
            token.setUser(testUser);
            
            when(tokenService.validatePasswordResetToken("valid-reset-token"))
                    .thenReturn(Optional.of(token));
            when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newHash");

            // When
            authService.resetPassword("valid-reset-token", "newPassword123");

            // Then
            assertThat(testUser.getPasswordHash()).isEqualTo("$2a$10$newHash");
            verify(userRepository).save(testUser);
            verify(tokenService).markPasswordResetTokenAsUsed(token);
        }

        @Test
        @DisplayName("Should reject password reset with invalid token")
        void shouldRejectPasswordResetWithInvalidToken() {
            // Given
            when(tokenService.validatePasswordResetToken("invalid-token"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.resetPassword("invalid-token", "newPassword"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid or expired");
        }
    }

    @Nested
    @DisplayName("Resend Verification Email Tests")
    class ResendVerificationTests {

        @Test
        @DisplayName("Should resend verification email successfully")
        void shouldResendVerificationEmailSuccessfully() {
            // Given
            testUser.setEmailVerified(false);
            when(userRepository.findByEmail(testUser.getEmail()))
                    .thenReturn(Optional.of(testUser));
            
            EmailVerificationToken token = new EmailVerificationToken();
            token.setToken("new-verification-token");
            when(tokenService.createEmailVerificationToken(testUser)).thenReturn(token);

            // When
            authService.resendVerificationEmail(testUser.getEmail(), "pt-BR");

            // Then
            verify(emailService).sendVerificationEmail(
                    eq(testUser.getEmail()),
                    eq(testUser.getFullName()),
                    contains("new-verification-token"),
                    eq("pt-BR")
            );
        }

        @Test
        @DisplayName("Should reject resend for already verified email")
        void shouldRejectResendForAlreadyVerifiedEmail() {
            // Given
            testUser.setEmailVerified(true);
            when(userRepository.findByEmail(testUser.getEmail()))
                    .thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.resendVerificationEmail(testUser.getEmail(), "es-CL"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already verified");
        }

        @Test
        @DisplayName("Should reject resend for non-existent user")
        void shouldRejectResendForNonExistentUser() {
            // Given
            when(userRepository.findByEmail("nonexistent@example.com"))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.resendVerificationEmail("nonexistent@example.com", "es-CL"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }
    }
}
