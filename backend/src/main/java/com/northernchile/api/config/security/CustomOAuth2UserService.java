package com.northernchile.api.config.security;

import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Custom OAuth2 user service that persists users from OAuth2 providers (e.g., Google)
 * to the local database. This ensures that users authenticated via OAuth2 have
 * corresponding entries in our users table for booking associations.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("Processing OAuth2 login for provider: {}", registrationId);

        try {
            processOAuth2User(registrationId, oAuth2User);
        } catch (Exception e) {
            log.error("Error processing OAuth2 user", e);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user: " + e.getMessage());
        }

        return oAuth2User;
    }

    /**
     * Process the OAuth2 user and create/update in the local database.
     */
    private void processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = extractEmail(registrationId, attributes);
        String fullName = extractFullName(registrationId, attributes);
        String providerId = extractProviderId(registrationId, attributes);

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email not provided by OAuth2 provider");
        }

        log.debug("OAuth2 user details - email: {}, name: {}, providerId: {}", email, fullName, providerId);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            updateExistingUser(user, registrationId, providerId, fullName);
        } else {
            createNewUser(email, fullName, registrationId, providerId);
        }
    }

    /**
     * Update an existing user with OAuth2 provider info.
     */
    private void updateExistingUser(User user, String provider, String providerId, String fullName) {
        boolean updated = false;

        if (user.getAuthProvider() == null || "LOCAL".equals(user.getAuthProvider())) {
            user.setAuthProvider(provider.toUpperCase());
            user.setProviderId(providerId);
            updated = true;
        }

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
            log.info("Updated existing user {} with OAuth2 provider: {}", user.getEmail(), provider);
        }
    }

    /**
     * Create a new user from OAuth2 login.
     */
    private void createNewUser(String email, String fullName, String provider, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName != null ? fullName : email.split("@")[0]);
        user.setRole("ROLE_CLIENT");
        user.setAuthProvider(provider.toUpperCase());
        user.setProviderId(providerId);
        user.setEmailVerified(true);

        userRepository.save(user);
        log.info("Created new user from OAuth2: {} with provider: {}", email, provider);
    }

    /**
     * Extract email from OAuth2 attributes based on provider.
     */
    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("email");
            case "facebook" -> (String) attributes.get("email");
            case "github" -> (String) attributes.get("email");
            default -> (String) attributes.get("email");
        };
    }

    /**
     * Extract full name from OAuth2 attributes based on provider.
     */
    private String extractFullName(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("name");
            case "facebook" -> (String) attributes.get("name");
            case "github" -> {
                String name = (String) attributes.get("name");
                yield name != null ? name : (String) attributes.get("login");
            }
            default -> (String) attributes.get("name");
        };
    }

    /**
     * Extract provider ID from OAuth2 attributes based on provider.
     */
    private String extractProviderId(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("sub");
            case "facebook" -> (String) attributes.get("id");
            case "github" -> String.valueOf(attributes.get("id"));
            default -> (String) attributes.get("sub");
        };
    }
}
