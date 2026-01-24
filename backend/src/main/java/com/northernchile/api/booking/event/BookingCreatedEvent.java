package com.northernchile.api.booking.event;

import java.util.UUID;

/**
 * Event published when a booking is created.
 * Triggers confirmation email sending via async event handler.
 */
public record BookingCreatedEvent(UUID bookingId) {}
