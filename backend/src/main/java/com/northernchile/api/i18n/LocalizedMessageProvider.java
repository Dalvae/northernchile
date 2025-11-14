package com.northernchile.api.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

/**
 * Service for retrieving localized messages from resource bundles.
 * Automatically determines the locale from the current HTTP request's Accept-Language header.
 */
@Component
public class LocalizedMessageProvider {

    private final MessageSource messageSource;

    public LocalizedMessageProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get a localized message by key without parameters.
     *
     * @param key The message key from messages_*.properties
     * @return The localized message
     */
    public String getMessage(String key) {
        return getMessage(key, null);
    }

    /**
     * Get a localized message by key with parameters.
     * Parameters are replaced in the message using {0}, {1}, etc.
     *
     * @param key The message key from messages_*.properties
     * @param params Parameters to replace in the message
     * @return The localized message with parameters replaced
     */
    public String getMessage(String key, Object... params) {
        return messageSource.getMessage(key, params, getCurrentLocale());
    }

    /**
     * Get a localized message by key with a default fallback message.
     *
     * @param key The message key from messages_*.properties
     * @param defaultMessage Fallback message if key is not found
     * @param params Parameters to replace in the message
     * @return The localized message or default message
     */
    public String getMessage(String key, String defaultMessage, Object... params) {
        return messageSource.getMessage(key, params, defaultMessage, getCurrentLocale());
    }

    /**
     * Get the current locale from the HTTP request's Accept-Language header.
     * Falls back to es-CL if no request context is available.
     *
     * @return The current locale
     */
    private Locale getCurrentLocale() {
        try {
            // Try to get locale from LocaleContextHolder (set by LocaleResolver)
            Locale locale = LocaleContextHolder.getLocale();
            if (locale != null) {
                return locale;
            }
        } catch (Exception e) {
            // Ignore and fall through to default
        }

        try {
            // Try to get locale from current HTTP request
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Locale locale = request.getLocale();
                if (locale != null) {
                    return locale;
                }
            }
        } catch (Exception e) {
            // Ignore and fall through to default
        }

        // Default to Spanish (Chile)
        return new Locale("es", "CL");
    }

    /**
     * Get the current locale string (e.g., "es-CL", "en-US", "pt-BR").
     *
     * @return The current locale as a string
     */
    public String getCurrentLocaleString() {
        Locale locale = getCurrentLocale();
        return locale.getLanguage() + "-" + locale.getCountry();
    }
}
