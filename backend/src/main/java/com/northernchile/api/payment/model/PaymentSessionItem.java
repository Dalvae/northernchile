package com.northernchile.api.payment.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single item in a payment session (one tour booking).
 * This is stored as JSON in the payment_sessions.items column.
 */
public record PaymentSessionItem(
        UUID scheduleId,
        String tourName,
        LocalDate tourDate,
        int numParticipants,
        BigDecimal pricePerPerson,
        BigDecimal itemTotal,
        String specialRequests,
        List<ParticipantData> participants
) implements Serializable {

    /**
     * Participant data snapshot stored in the payment session.
     */
    public record ParticipantData(
            String fullName,
            String documentId,
            String nationality,
            LocalDate dateOfBirth,
            String pickupAddress,
            String specialRequirements,
            String phoneNumber,
            String email
    ) implements Serializable {}
}
