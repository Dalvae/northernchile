package com.northernchile.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for return URLs to prevent open redirect vulnerabilities.
 * Only allows URLs that start with configured allowed domains.
 */
@Component
public class ReturnUrlValidator implements ConstraintValidator<ValidReturnUrl, String> {

    @Value("${app.security.allowed-redirect-domains:http://localhost:3000,https://www.northernchile.cl}")
    private String allowedDomainsConfig;

    private boolean nullable;

    @Override
    public void initialize(ValidReturnUrl constraintAnnotation) {
        this.nullable = constraintAnnotation.nullable();
    }

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        // Allow null if configured
        if (url == null || url.isBlank()) {
            return nullable;
        }

        // Parse allowed domains from configuration
        List<String> allowedDomains = Arrays.asList(allowedDomainsConfig.split(","));

        // Check if URL starts with any allowed domain
        boolean isValid = allowedDomains.stream()
            .anyMatch(domain -> url.startsWith(domain.trim()));

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Return URL must start with one of the allowed domains: " + allowedDomainsConfig
            ).addConstraintViolation();
        }

        return isValid;
    }
}
