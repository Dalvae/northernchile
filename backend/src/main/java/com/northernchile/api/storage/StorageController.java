package com.northernchile.api.storage;

import com.northernchile.api.common.dto.MessageRes;
import com.northernchile.api.security.Permission;
import com.northernchile.api.security.annotations.RequiresPermission;
import com.northernchile.api.storage.dto.StorageUploadRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @RequiresPermission(Permission.UPLOAD_FILE)
    @Operation(summary = "Upload a file to S3", description = "Upload an image or asset file to S3 storage")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {

        // Validate folder to prevent path traversal attacks
        if (isInvalidPath(folder)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid folder name"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // Validate file type (images only for now)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only image files are allowed"));
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "File size must not exceed 10MB"));
        }

        try {
            String key = s3StorageService.uploadFile(file, folder);
            String url = s3StorageService.getPublicUrl(key);
            return ResponseEntity.ok(StorageUploadRes.success(url, key));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{folder}/{filename}")
    @RequiresPermission(Permission.DELETE_FILE)
    @Operation(summary = "Delete a file from S3", description = "Delete an uploaded file from S3 storage")
    public ResponseEntity<?> deleteFile(
            @PathVariable String folder,
            @PathVariable String filename) {

        // Validate folder and filename to prevent path traversal attacks
        if (isInvalidPath(folder) || isInvalidPath(filename)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid folder or filename"));
        }

        String key = folder + "/" + filename;

        if (!s3StorageService.fileExists(key)) {
            return ResponseEntity.notFound().build();
        }

        s3StorageService.deleteFile(key);
        return ResponseEntity.ok(MessageRes.of("File deleted successfully"));
    }

    @GetMapping("/presigned-upload-url")
    @RequiresPermission(Permission.UPLOAD_FILE)
    @Operation(summary = "Get presigned upload URL", description = "Generate a temporary URL for direct client upload to S3")
    public ResponseEntity<?> getPresignedUploadUrl(
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            @RequestParam("filename") String filename) {

        // Validate folder and filename to prevent path traversal attacks
        if (isInvalidPath(folder) || isInvalidPath(filename)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid folder or filename"));
        }

        String uploadUrl = s3StorageService.generateUploadUrl(folder, filename);

        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", uploadUrl);
        response.put("expiresIn", "15 minutes");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/url")
    @Operation(summary = "Get public URL for a file", description = "Get a presigned public URL for an S3 object")
    public ResponseEntity<?> getFileUrl(@RequestParam("key") String key) {
        // Validate key to prevent path traversal attacks
        if (key.contains("..")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid key"));
        }

        if (!s3StorageService.fileExists(key)) {
            return ResponseEntity.notFound().build();
        }

        String url = s3StorageService.getPublicUrl(key);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Validates that a path segment doesn't contain path traversal characters.
     * @param pathSegment the folder or filename to validate
     * @return true if the path is invalid (contains traversal characters)
     */
    private boolean isInvalidPath(String pathSegment) {
        return pathSegment == null ||
               pathSegment.contains("..") ||
               pathSegment.contains("/") ||
               pathSegment.contains("\\");
    }
}
