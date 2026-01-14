package com.northernchile.api.common.dto;

/**
 * Generic response DTO for simple message responses.
 * Use this instead of Map<String, String> for consistency.
 */
public record MessageRes(
    String message
) {
    public static MessageRes of(String message) {
        return new MessageRes(message);
    }
}
