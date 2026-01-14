package com.northernchile.api.payment.dto;

import java.util.List;

/**
 * Response DTO for test payments endpoint.
 */
public record TestPaymentsRes(
    int count,
    List<PaymentRes> payments
) {}
