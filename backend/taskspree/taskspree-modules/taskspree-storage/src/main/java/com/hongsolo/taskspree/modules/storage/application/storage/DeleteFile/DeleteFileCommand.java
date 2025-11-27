package com.hongsolo.taskspree.modules.storage.application.storage.DeleteFile;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.domain.Result;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteFileCommand(
        @NotNull(message = "File ID is required")
        UUID fileId,

        @NotNull(message = "Requester ID is required")
        UUID requesterId
) implements Command<Result<Void>> {
}
