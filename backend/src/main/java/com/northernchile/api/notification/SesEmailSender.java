package com.northernchile.api.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class SesEmailSender {

    private static final Logger log = LoggerFactory.getLogger(SesEmailSender.class);

    @Value("${aws.ses.access-key-id:${AWS_ACCESS_KEY_ID:}}")
    private String accessKeyId;

    @Value("${aws.ses.secret-access-key:${AWS_SECRET_ACCESS_KEY:}}")
    private String secretAccessKey;

    @Value("${aws.ses.region:${AWS_REGION:us-east-1}}")
    private String region;

    @Value("${mail.from.email}")
    private String fromEmail;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    private SesClient sesClient;

    @PostConstruct
    public void init() {
        if (mailEnabled && isConfigured()) {
            try {
                sesClient = SesClient.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                        .build();
                log.info("Amazon SES client initialized for region: {}", region);
            } catch (Exception e) {
                log.error("Failed to initialize SES client: {}", e.getMessage());
            }
        } else {
            log.info("Amazon SES client not initialized (mail.enabled={}, configured={})", 
                    mailEnabled, isConfigured());
        }
    }

    @PreDestroy
    public void destroy() {
        if (sesClient != null) {
            sesClient.close();
        }
    }

    private boolean isConfigured() {
        return accessKeyId != null && !accessKeyId.isBlank() 
                && secretAccessKey != null && !secretAccessKey.isBlank();
    }

    public boolean isAvailable() {
        return mailEnabled && sesClient != null;
    }

    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.warn("Email sending is disabled. Would have sent email to: {} with subject: {}", toEmail, subject);
            return;
        }

        if (sesClient == null) {
            log.error("SES client not initialized. Cannot send email to: {}", toEmail);
            return;
        }

        try {
            String fromAddress = String.format("%s <%s>", fromName, fromEmail);

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromAddress)
                    .destination(Destination.builder()
                            .toAddresses(toEmail)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .charset("UTF-8")
                                    .data(subject)
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .charset("UTF-8")
                                            .data(htmlBody)
                                            .build())
                                    .build())
                            .build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);
            log.info("Email sent successfully via SES to: {} with subject: {} (messageId: {})", 
                    toEmail, subject, response.messageId());

        } catch (SesException e) {
            log.error("SES error sending email to: {} - {}", toEmail, e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.error("Failed to send email via SES to: {}", toEmail, e);
        }
    }
}
