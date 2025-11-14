package com.northernchile.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a URL is from an allowed domain.
 * Prevents open redirect vulnerabilities.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReturnUrlValidator.class)
@Documented
public @interface ValidReturnUrl {
    String message() default "Invalid return URL - must be from an allowed domain";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * Allow null values (optional field)
     */
    boolean nullable() default true;
}
