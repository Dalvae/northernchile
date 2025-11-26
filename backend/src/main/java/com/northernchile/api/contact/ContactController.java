package com.northernchile.api.contact;

import com.northernchile.api.contact.dto.ContactMessageReq;
import com.northernchile.api.model.ContactMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Contact", description = "Contact form and message management endpoints")
public class ContactController {

    private final ContactMessageService contactMessageService;

    public ContactController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    /**
     * PUBLIC endpoint - Submit a contact form message
     */
    @PostMapping("/contact")
    @Operation(summary = "Submit contact form", description = "Send a message through the contact form")
    public ResponseEntity<Map<String, String>> submitContactForm(@Valid @RequestBody ContactMessageReq request) {
        contactMessageService.createContactMessage(request);
        return ResponseEntity.ok(Map.of(
                "message", "Thank you for your message. We will get back to you soon."
        ));
    }

    /**
     * ADMIN endpoints - Manage contact messages
     */
    @GetMapping("/admin/contact/messages")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
    @Operation(summary = "Get all contact messages", description = "Get all contact form messages (admin only)")
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        List<ContactMessage> messages = contactMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin/contact/messages/new")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
    @Operation(summary = "Get new contact messages", description = "Get unread contact messages (admin only)")
    public ResponseEntity<List<ContactMessage>> getNewMessages() {
        List<ContactMessage> messages = contactMessageService.getMessagesByStatus("NEW");
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin/contact/messages/count/new")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
    @Operation(summary = "Count new messages", description = "Get count of unread messages")
    public ResponseEntity<Map<String, Long>> getNewMessagesCount() {
        long count = contactMessageService.getNewMessagesCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/admin/contact/messages/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
    @Operation(summary = "Update message status", description = "Mark message as read/replied/archived")
    public ResponseEntity<ContactMessage> updateMessageStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        ContactMessage updated = contactMessageService.updateMessageStatus(id, statusUpdate.get("status"));
        return ResponseEntity.ok(updated);
    }
}
