package com.northernchile.api.payment.dto;

/**
 * Response DTO for delete operations.
 */
public record DeleteResultRes(
    String message,
    int deletedCount
) {}
