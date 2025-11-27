package com.hongsolo.taskspree.modules.storage.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for linking a file to an entity.
 */
public record LinkFileRequest(
        @NotNull(message = "File ID is required")
        UUID fileId,

        @NotBlank(message = "Entity type is required")
        String entityType,

        @NotNull(message = "Entity ID is required")
        UUID entityId
) {
}
