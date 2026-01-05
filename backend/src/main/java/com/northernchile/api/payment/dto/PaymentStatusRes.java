package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payment status response DTO.
 * Contains the current status of a payment transaction.
 */
public record PaymentStatusRes(
    UUID paymentId,
    String externalPaymentId,
    PaymentStatus status,
    BigDecimal amount,
    String currency,
    String message,
    Instant updatedAt,
    boolean isTest
) {}
