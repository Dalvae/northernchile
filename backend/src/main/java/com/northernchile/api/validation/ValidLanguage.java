package com.northernchile.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a language code is one of the supported languages.
 * Supported languages: es, en, pt
 */
@Documented
@Constraint(validatedBy = LanguageValidator.class)
@Target({ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLanguage {
    String message() default "Invalid language code. Allowed values: es, en, pt";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
