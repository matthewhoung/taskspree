package com.hongsolo.taskspree.modules.storage.application.storage.DeleteFile;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteFileCommand(
        @NotNull(message = "File ID is required")
        UUID fileId,

        @NotNull(message = "User ID is required")
        UUID userId
) implements Command<Result<Void>> {
}