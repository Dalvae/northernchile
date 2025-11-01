package com.northernchile.api.storage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for file upload and management
 */
@RestController
@RequestMapping("/api/storage")
@Tag(name = "Storage", description = "File storage and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class StorageController {

    private final S3StorageService s3StorageService;

    public StorageController(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a file to S3", description = "Upload an image or asset file to S3 storage")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // Validate file type (images only for now)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "File size must not exceed 5MB"));
        }

        try {
            String key = s3StorageService.uploadFile(file, folder);
            String url = s3StorageService.getPublicUrl(key);

            Map<String, String> response = new HashMap<>();
            response.put("key", key);
            response.put("url", url);
            response.put("message", "File uploaded successfully");

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{folder}/{filename}")
    @Operation(summary = "Delete a file from S3", description = "Delete an uploaded file from S3 storage")
    public ResponseEntity<?> deleteFile(
            @PathVariable String folder,
            @PathVariable String filename) {

        String key = folder + "/" + filename;

        if (!s3StorageService.fileExists(key)) {
            return ResponseEntity.notFound().build();
        }

        s3StorageService.deleteFile(key);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    @GetMapping("/presigned-upload-url")
    @Operation(summary = "Get presigned upload URL", description = "Generate a temporary URL for direct client upload to S3")
    public ResponseEntity<?> getPresignedUploadUrl(
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            @RequestParam("filename") String filename) {

        String uploadUrl = s3StorageService.generateUploadUrl(folder, filename);

        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", uploadUrl);
        response.put("expiresIn", "15 minutes");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/url")
    @Operation(summary = "Get public URL for a file", description = "Get a presigned public URL for an S3 object")
    public ResponseEntity<?> getFileUrl(@RequestParam("key") String key) {
        if (!s3StorageService.fileExists(key)) {
            return ResponseEntity.notFound().build();
        }

        String url = s3StorageService.getPublicUrl(key);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
