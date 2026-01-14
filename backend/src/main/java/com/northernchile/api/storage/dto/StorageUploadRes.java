package com.northernchile.api.storage.dto;

/**
 * Response DTO for file upload operations.
 */
public record StorageUploadRes(
    String url,
    String key,
    String message
) {
    public static StorageUploadRes success(String url, String key) {
        return new StorageUploadRes(url, key, "File uploaded successfully");
    }
}
