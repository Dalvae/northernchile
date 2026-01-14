package com.northernchile.api.payment.dto;

import java.math.BigDecimal;

/**
 * Request DTO for payment refund.
 */
public record RefundReq(
    BigDecimal amount
) {}
