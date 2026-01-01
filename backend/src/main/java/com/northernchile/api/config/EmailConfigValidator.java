package com.northernchile.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Validates email configuration at application startup.
 * Logs warnings if email is enabled but AWS SES configuration is incomplete.
 */
@Component
public class EmailConfigValidator {

    private static final Logger log = LoggerFactory.getLogger(EmailConfigValidator.class);

    @Value("${mail.enabled:false}")
    private Boolean mailEnabled;

    @Value("${aws.ses.access-key-id:${AWS_ACCESS_KEY_ID:}}")
    private String awsAccessKeyId;

    @Value("${aws.ses.secret-access-key:${AWS_SECRET_ACCESS_KEY:}}")
    private String awsSecretAccessKey;

    @Value("${aws.ses.region:${AWS_REGION:us-east-1}}")
    private String awsRegion;

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

        // Email is enabled, validate required AWS SES configuration
        boolean hasErrors = false;

        if (isBlank(awsAccessKeyId)) {
            log.error("AWS_ACCESS_KEY_ID is not configured but email is enabled!");
            hasErrors = true;
        }

        if (isBlank(awsSecretAccessKey)) {
            log.error("AWS_SECRET_ACCESS_KEY is not configured but email is enabled!");
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
            log.error("Email sending is ENABLED but required AWS SES configuration is missing.");
            log.error("Please set the following environment variables:");
            log.error("  - AWS_ACCESS_KEY_ID (your AWS access key)");
            log.error("  - AWS_SECRET_ACCESS_KEY (your AWS secret key)");
            log.error("  - AWS_REGION (e.g., us-east-1) [optional, defaults to us-east-1]");
            log.error("  - MAIL_FROM_EMAIL (sender email address verified in SES)");
            log.error("");
            log.error("Or set MAIL_ENABLED=false to disable email sending.");
            log.error("See backend/docs/EMAIL_SETUP.md for setup instructions.");
            log.error("================================================");
        } else {
            log.info("Email configuration is valid (AWS SES):");
            log.info("  Region: {}", awsRegion);
            log.info("  From Email: {}", mailFromEmail);
            log.info("  From Name: {}", mailFromName);
            log.info("  AWS credentials: configured");
            log.info("Email sending via AWS SES is ENABLED and ready.");
        }
    }

    /**
     * Check if a string is blank (null, empty, or whitespace)
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
