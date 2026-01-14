package com.northernchile.api.contact;

import com.northernchile.api.contact.dto.ContactMessageReq;
import com.northernchile.api.model.ContactMessage;
import com.northernchile.api.notification.event.ContactMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactMessageService {

    private static final Logger log = LoggerFactory.getLogger(ContactMessageService.class);

    private final ContactMessageRepository contactMessageRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ContactMessageService(
            ContactMessageRepository contactMessageRepository,
            ApplicationEventPublisher eventPublisher) {
        this.contactMessageRepository = contactMessageRepository;
        this.eventPublisher = eventPublisher;
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

        // Publish event for admin notification (decoupled from email sending)
        eventPublisher.publishEvent(new ContactMessageReceivedEvent(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getMessage()
        ));

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
