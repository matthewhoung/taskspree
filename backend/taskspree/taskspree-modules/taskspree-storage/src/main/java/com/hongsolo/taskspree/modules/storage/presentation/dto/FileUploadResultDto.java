package com.hongsolo.taskspree.modules.storage.presentation.dto;

import java.util.UUID;

/**
 * DTO representing the result of a single file upload initiation.
 */
public record FileUploadResultDto(
        UUID fileId,
        String originalName,
        long fileSize,
        String status,
        String errorMessage
) {
    public static FileUploadResultDto success(UUID fileId, String originalName, long fileSize) {
        return new FileUploadResultDto(fileId, originalName, fileSize, "PENDING", null);
    }

    public static FileUploadResultDto failure(String originalName, long fileSize, String errorMessage) {
        return new FileUploadResultDto(null, originalName, fileSize, "REJECTED", errorMessage);
    }
}
