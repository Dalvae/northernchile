package com.northernchile.api.payment;

import com.northernchile.api.booking.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final BookingRepository bookingRepository;

    public WebhookController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/payment")
    public ResponseEntity<Void> handlePaymentWebhook(@RequestBody Map<String, Object> payload) {
        // TODO: La lógica real dependerá de la pasarela de pago elegida (Transbank, Mercado Pago).
        // Por ahora, solo registramos la recepción del webhook.
        log.info("Received payment webhook: {}", payload);

        /*
        // Ejemplo de lógica a implementar en el futuro:
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
        */

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
