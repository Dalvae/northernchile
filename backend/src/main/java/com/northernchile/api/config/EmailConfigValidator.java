package com.northernchile.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Validates email configuration at application startup.
 * Logs warnings if email is enabled but configuration is incomplete.
 */
@Component
public class EmailConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(EmailConfigValidator.class);

    @Value("${mail.enabled:false}")
    private Boolean mailEnabled;

    @Value("${mail.host:}")
    private String mailHost;

    @Value("${mail.port:587}")
    private Integer mailPort;

    @Value("${mail.username:}")
    private String mailUsername;

    @Value("${mail.password:}")
    private String mailPassword;

    @Value("${mail.from.email:}")
    private String mailFromEmail;

    @Value("${mail.from.name:Northern Chile Tours}")
    private String mailFromName;

    @EventListener(ApplicationReadyEvent.class)
    public void validateEmailConfiguration() {
        log.info("Validating email configuration...");

        if (!mailEnabled) {
            log.warn("Email sending is DISABLED (MAIL_ENABLED=false)");
            log.warn("Emails will be logged but not sent. Enable for production.");
            return;
        }

        // Email is enabled, validate required configuration
        boolean hasErrors = false;

        if (isBlank(mailHost)) {
            log.error("MAIL_HOST is not configured but email is enabled!");
            hasErrors = true;
        }

        if (mailPort == null || mailPort <= 0) {
            log.error("MAIL_PORT is invalid: {}", mailPort);
            hasErrors = true;
        }

        if (isBlank(mailUsername)) {
            log.error("MAIL_USERNAME is not configured but email is enabled!");
            hasErrors = true;
        }

        if (isBlank(mailPassword)) {
            log.error("MAIL_PASSWORD is not configured but email is enabled!");
            log.error("For Gmail/Google Workspace, generate an app-specific password.");
            hasErrors = true;
        }

        if (isBlank(mailFromEmail)) {
            log.error("MAIL_FROM_EMAIL is not configured but email is enabled!");
            hasErrors = true;
        }

        if (hasErrors) {
            log.error("================================================");
            log.error("EMAIL CONFIGURATION IS INCOMPLETE!");
            log.error("================================================");
            log.error("Email sending is ENABLED but required configuration is missing.");
            log.error("Please set the following environment variables:");
            log.error("  - MAIL_HOST (e.g., smtp.gmail.com)");
            log.error("  - MAIL_PORT (e.g., 587)");
            log.error("  - MAIL_USERNAME (your email address)");
            log.error("  - MAIL_PASSWORD (app-specific password)");
            log.error("  - MAIL_FROM_EMAIL (sender email address)");
            log.error("");
            log.error("Or set MAIL_ENABLED=false to disable email sending.");
            log.error("See backend/docs/EMAIL_SETUP.md for setup instructions.");
            log.error("================================================");
        } else {
            log.info("Email configuration is valid:");
            log.info("  Host: {}", mailHost);
            log.info("  Port: {}", mailPort);
            log.info("  Username: {}", maskEmail(mailUsername));
            log.info("  From Email: {}", mailFromEmail);
            log.info("  From Name: {}", mailFromName);
            log.info("Email sending is ENABLED and ready.");
        }
    }

    /**
     * Check if a string is blank (null, empty, or whitespace)
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Mask email address for logging (show first char and domain only)
     */
    private String maskEmail(String email) {
        if (isBlank(email)) {
            return "[not set]";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***@[invalid]";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return "***" + domain;
        }

        return localPart.charAt(0) + "***" + domain;
    }
}
