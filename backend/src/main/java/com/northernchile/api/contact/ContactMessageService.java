package com.northernchile.api.contact;

import com.northernchile.api.config.NotificationConfig;
import com.northernchile.api.contact.dto.ContactMessageReq;
import com.northernchile.api.model.ContactMessage;
import com.northernchile.api.notification.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactMessageService {

    private static final Logger log = LoggerFactory.getLogger(ContactMessageService.class);

    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;
    private final NotificationConfig notificationConfig;

    public ContactMessageService(
            ContactMessageRepository contactMessageRepository,
            EmailService emailService,
            NotificationConfig notificationConfig) {
        this.contactMessageRepository = contactMessageRepository;
        this.emailService = emailService;
        this.notificationConfig = notificationConfig;
    }

    @Transactional
    public ContactMessage createContactMessage(ContactMessageReq req) {
        ContactMessage message = new ContactMessage();
        message.setName(req.name());
        message.setEmail(req.email());
        message.setPhone(req.phone());
        message.setMessage(req.message());
        message.setStatus("NEW");

        ContactMessage saved = contactMessageRepository.save(message);

        // Send email notification to admin
        try {
            String adminEmail = notificationConfig.getAdminEmail();
            emailService.sendContactNotificationToAdmin(saved, adminEmail);
            log.info("Contact message email notification sent to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send contact notification email", e);
            // Don't fail the request if email fails
        }

        return saved;
    }

    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<ContactMessage> getMessagesByStatus(String status) {
        return contactMessageRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public long getNewMessagesCount() {
        return contactMessageRepository.countByStatus("NEW");
    }

    @Transactional
    public ContactMessage updateMessageStatus(String id, String status) {
        ContactMessage message = contactMessageRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        message.setStatus(status);
        return contactMessageRepository.save(message);
    }
}
