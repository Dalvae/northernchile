package com.northernchile.api.booking.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publisher for booking-related domain events.
 * Decouples event creation from Spring's ApplicationEventPublisher.
 */
@Component
public class BookingEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public BookingEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes a BookingCreatedEvent to trigger confirmation notifications.
     *
     * @param bookingId The ID of the newly created booking
     */
    public void publishBookingCreated(UUID bookingId) {
        eventPublisher.publishEvent(new BookingCreatedEvent(bookingId));
    }
}
