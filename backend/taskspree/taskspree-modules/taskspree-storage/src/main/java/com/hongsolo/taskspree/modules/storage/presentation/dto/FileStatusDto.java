package com.hongsolo.taskspree.modules.storage.presentation.dto;

import java.util.UUID;

/**
 * DTO representing the status of a stored file.
 */
public record FileStatusDto(
        UUID fileId,
        String originalName,
        long fileSize,
        String contentType,
        String status,
        String errorMessage,
        String downloadUrl
) {
}
