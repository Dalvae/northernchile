package com.northernchile.api.contact;

import com.northernchile.api.contact.dto.ContactMessageReq;
import com.northernchile.api.model.ContactMessage;
import com.northernchile.api.security.Permission;
import com.northernchile.api.security.annotations.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
    /**
     * @deprecated Use paginated endpoint /admin/contact/messages/paged instead
     */
    @GetMapping("/admin/contact/messages")
    @RequiresPermission(Permission.VIEW_CONTACT_MESSAGES)
    @Operation(summary = "Get all contact messages", description = "Get all contact form messages (admin only)")
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        List<ContactMessage> messages = contactMessageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin/contact/messages/paged")
    @RequiresPermission(Permission.VIEW_CONTACT_MESSAGES)
    @Operation(summary = "Get paginated contact messages", description = "Get paginated contact form messages (admin only)")
    public ResponseEntity<Page<ContactMessage>> getAllMessagesPaged(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ContactMessage> messages = contactMessageService.getAllMessagesPaged(pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * @deprecated Use paginated endpoint /admin/contact/messages/new/paged instead
     */
    @GetMapping("/admin/contact/messages/new")
    @RequiresPermission(Permission.VIEW_CONTACT_MESSAGES)
    @Operation(summary = "Get new contact messages", description = "Get unread contact messages (admin only)")
    public ResponseEntity<List<ContactMessage>> getNewMessages() {
        List<ContactMessage> messages = contactMessageService.getMessagesByStatus("NEW");
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin/contact/messages/new/paged")
    @RequiresPermission(Permission.VIEW_CONTACT_MESSAGES)
    @Operation(summary = "Get paginated new contact messages", description = "Get paginated unread contact messages (admin only)")
    public ResponseEntity<Page<ContactMessage>> getNewMessagesPaged(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ContactMessage> messages = contactMessageService.getMessagesByStatusPaged("NEW", pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/admin/contact/messages/count/new")
    @RequiresPermission(Permission.VIEW_CONTACT_MESSAGES)
    @Operation(summary = "Count new messages", description = "Get count of unread messages")
    public ResponseEntity<Map<String, Long>> getNewMessagesCount() {
        long count = contactMessageService.getNewMessagesCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/admin/contact/messages/{id}/status")
    @RequiresPermission(Permission.MANAGE_CONTACT_MESSAGES)
    @Operation(summary = "Update message status", description = "Mark message as read/replied/archived")
    public ResponseEntity<ContactMessage> updateMessageStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        ContactMessage updated = contactMessageService.updateMessageStatus(id, statusUpdate.get("status"));
        return ResponseEntity.ok(updated);
    }
}
