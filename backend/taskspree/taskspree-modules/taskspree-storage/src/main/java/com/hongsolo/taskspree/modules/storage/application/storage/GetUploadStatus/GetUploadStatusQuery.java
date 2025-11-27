package com.hongsolo.taskspree.modules.storage.application.storage.GetUploadStatus;

import com.hongsolo.taskspree.common.application.cqrs.Query;
import com.hongsolo.taskspree.common.application.services.IFileStorageService.FileStatusDto;

import java.util.List;
import java.util.UUID;

public record GetUploadStatusQuery(
        List<UUID> fileIds
) implements Query<List<FileStatusDto>> {
}
