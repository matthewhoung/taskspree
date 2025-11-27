package com.hongsolo.taskspree.modules.storage.application.storage.LinkFile;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LinkFileCommand(
        @NotNull(message = "File ID is required")
        UUID fileId,

        @NotBlank(message = "Entity type is required")
        String entityType,

        @NotNull(message = "Entity ID is required")
        UUID entityId
) implements Command<Result<Void>> {
}
