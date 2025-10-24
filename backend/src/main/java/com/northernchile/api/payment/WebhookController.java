package com.northernchile.api.payment;

import com.northernchile.api.booking.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private BookingRepository bookingRepository;

    @PostMapping("/payment")
    public ResponseEntity<Void> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        // This is a placeholder. A real implementation would:
        // 1. Verify the webhook signature to ensure it's from the payment gateway.
        // 2. Parse the payload to get the booking ID and payment status.
        // 3. Update the booking status in the database.

        System.out.println("Received payment webhook: " + payload);

        // Example of updating booking status
        String eventType = (String) payload.get("type");
        if ("payment_intent.succeeded".equals(eventType)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> object = (Map<String, Object>) data.get("object");
            String bookingIdStr = (String) object.get("metadata.booking_id");
            if (bookingIdStr != null) {
                UUID bookingId = UUID.fromString(bookingIdStr);
                bookingRepository.findById(bookingId).ifPresent(booking -> {
                    booking.setStatus("CONFIRMED");
                    bookingRepository.save(booking);
                });
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
