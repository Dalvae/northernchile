package com.northernchile.api.validation;

import com.northernchile.api.config.properties.AppProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validator for return URLs to prevent open redirect vulnerabilities.
 * Only allows URLs that start with configured allowed domains.
 */
@Component
public class ReturnUrlValidator implements ConstraintValidator<ValidReturnUrl, String> {

    private final AppProperties appProperties;
    private boolean nullable;

    public ReturnUrlValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

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

        // Get allowed domains from configuration
        List<String> allowedDomains = appProperties.getSecurity().getAllowedRedirectDomains();

        // Check if URL starts with any allowed domain
        boolean isValid = allowedDomains.stream()
            .anyMatch(domain -> url.startsWith(domain.trim()));

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Return URL must start with one of the allowed domains: " + String.join(",", allowedDomains)
            ).addConstraintViolation();
        }

        return isValid;
    }
}
