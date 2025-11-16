package com.northernchile.api.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Configuration for internationalization (i18n) and localization (l10n).
 * Provides locale resolution based on Accept-Language header.
 */
@Configuration
public class I18nConfig {

    /**
     * Supported locales for the application.
     */
    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
        new Locale("es", "CL"),  // Spanish (Chile) - default
        new Locale("en", "US"),  // English (United States)
        new Locale("pt", "BR")   // Portuguese (Brazil)
    );

    /**
     * Default locale if no Accept-Language header is provided.
     */
    private static final Locale DEFAULT_LOCALE = new Locale("es", "CL");

    /**
     * Configure MessageSource for loading i18n messages from messages_*.properties files.
     * Supports message formatting with parameters.
     *
     * @return MessageSource bean
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(true); // Return code if message not found
        return messageSource;
    }

    /**
     * Configure LocaleResolver to determine the current locale from the Accept-Language header.
     * Falls back to default locale (es-CL) if no header is provided or locale is not supported.
     *
     * @return LocaleResolver bean
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setSupportedLocales(SUPPORTED_LOCALES);
        localeResolver.setDefaultLocale(DEFAULT_LOCALE);
        return localeResolver;
    }
}
