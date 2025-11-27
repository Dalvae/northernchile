package com.northernchile.api.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for validating file types using magic numbers (file signatures).
 * This provides security against file type spoofing attacks where an attacker
 * might rename a malicious file to have an innocent-looking extension.
 */
@Service
public class FileValidationService {

    private static final Map<String, byte[][]> MAGIC_NUMBERS = new HashMap<>();

    static {
        // JPEG signatures
        MAGIC_NUMBERS.put("image/jpeg", new byte[][]{
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        });

        // PNG signature
        MAGIC_NUMBERS.put("image/png", new byte[][]{
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        });

        // GIF signatures (GIF87a and GIF89a)
        MAGIC_NUMBERS.put("image/gif", new byte[][]{
            {0x47, 0x49, 0x46, 0x38, 0x37, 0x61},
            {0x47, 0x49, 0x46, 0x38, 0x39, 0x61}
        });

        // WebP signature (RIFF....WEBP)
        MAGIC_NUMBERS.put("image/webp", new byte[][]{
            {0x52, 0x49, 0x46, 0x46}
        });

        // HEIC/HEIF signatures
        MAGIC_NUMBERS.put("image/heic", new byte[][]{
            {0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, 0x68, 0x65, 0x69, 0x63},
            {0x00, 0x00, 0x00, 0x1C, 0x66, 0x74, 0x79, 0x70, 0x68, 0x65, 0x69, 0x63}
        });

        // PDF signature
        MAGIC_NUMBERS.put("application/pdf", new byte[][]{
            {0x25, 0x50, 0x44, 0x46}
        });
    }

    /**
     * Allowed MIME types for image uploads.
     */
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/heic"
    };

    /**
     * Maximum file size for image uploads (10 MB).
     */
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    /**
     * Validates an uploaded file for image uploads.
     *
     * @param file The uploaded file
     * @throws IllegalArgumentException if validation fails
     */
    public void validateImageFile(MultipartFile file) throws IllegalArgumentException, IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        // Check file size
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (10 MB)");
        }

        // Check declared content type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP, HEIC");
        }

        // Validate magic numbers
        if (!validateMagicNumber(file, contentType)) {
            throw new IllegalArgumentException("File content does not match its declared type");
        }

        // Validate file extension matches content type
        String filename = file.getOriginalFilename();
        if (filename != null && !extensionMatchesContentType(filename, contentType)) {
            throw new IllegalArgumentException("File extension does not match its content type");
        }
    }

    /**
     * Validates that the file's actual content matches its declared type
     * by checking the magic numbers (file signature).
     */
    public boolean validateMagicNumber(MultipartFile file, String declaredContentType) throws IOException {
        byte[][] signatures = MAGIC_NUMBERS.get(declaredContentType);
        if (signatures == null) {
            return true;
        }

        int maxLength = 0;
        for (byte[] sig : signatures) {
            maxLength = Math.max(maxLength, sig.length);
        }

        byte[] header = new byte[maxLength];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header);
            if (bytesRead < maxLength) {
                header = Arrays.copyOf(header, bytesRead);
            }
        }

        for (byte[] signature : signatures) {
            if (startsWith(header, signature)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the header starts with the given signature bytes.
     */
    private boolean startsWith(byte[] header, byte[] signature) {
        if (header.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (header[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the content type is in the allowed list.
     */
    private boolean isAllowedImageType(String contentType) {
        for (String allowed : ALLOWED_IMAGE_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the file extension matches the declared content type.
     */
    private boolean extensionMatchesContentType(String filename, String contentType) {
        String extension = getFileExtension(filename).toLowerCase();
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> extension.equals("jpg") || extension.equals("jpeg");
            case "image/png" -> extension.equals("png");
            case "image/gif" -> extension.equals("gif");
            case "image/webp" -> extension.equals("webp");
            case "image/heic" -> extension.equals("heic") || extension.equals("heif");
            case "application/pdf" -> extension.equals("pdf");
            default -> true;
        };
    }

    /**
     * Extract the file extension from a filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
