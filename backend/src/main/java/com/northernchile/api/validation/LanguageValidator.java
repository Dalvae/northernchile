package com.northernchile.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator for language codes.
 * Ensures only supported languages (es, en, pt) are used.
 */
public class LanguageValidator implements ConstraintValidator<ValidLanguage, String> {

    private static final Set<String> ALLOWED_LANGUAGES = Set.of("es", "en", "pt");

    @Override
    public void initialize(ValidLanguage constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null checks
        }
        return ALLOWED_LANGUAGES.contains(value.toLowerCase());
    }
}
