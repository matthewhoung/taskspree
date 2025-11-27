package com.hongsolo.taskspree.modules.storage.presentation.dto;

import java.util.UUID;

/**
 * DTO for download URL response.
 */
public record DownloadUrlResponse(
        UUID fileId,
        String downloadUrl,
        int expiresInSeconds
) {
    private static final int DEFAULT_EXPIRY_SECONDS = 3600; // 1 hour

    public static DownloadUrlResponse of(UUID fileId, String downloadUrl) {
        return new DownloadUrlResponse(fileId, downloadUrl, DEFAULT_EXPIRY_SECONDS);
    }
}