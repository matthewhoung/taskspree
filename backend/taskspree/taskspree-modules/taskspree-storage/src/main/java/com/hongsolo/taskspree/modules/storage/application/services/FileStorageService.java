package com.hongsolo.taskspree.modules.storage.application.services;

import com.hongsolo.taskspree.common.application.cqrs.ICommandBus;
import com.hongsolo.taskspree.common.application.cqrs.IQueryBus;
import com.hongsolo.taskspree.common.application.services.IFileStorageService;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.storage.application.storage.DeleteFile.DeleteFileCommand;
import com.hongsolo.taskspree.modules.storage.application.storage.InitiateUpload.InitiateUploadCommand;
import com.hongsolo.taskspree.modules.storage.application.storage.LinkFile.LinkFileCommand;
import com.hongsolo.taskspree.modules.storage.application.storage.GetDownloadUrl.GetDownloadUrlQuery;
import com.hongsolo.taskspree.modules.storage.application.storage.GetUploadStatus.GetUploadStatusQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService implements IFileStorageService {

    private final ICommandBus commandBus;
    private final IQueryBus queryBus;

    @Override
    public List<FileUploadResult> initiateUpload(List<MultipartFile> files, UUID userId) {
        log.info("Initiating upload for {} files from user {}", files.size(), userId);

        InitiateUploadCommand command = new InitiateUploadCommand(files, userId);
        Result<List<FileUploadResult>> result = commandBus.execute(command);

        if (result.isSuccess() && result instanceof Result.Success<List<FileUploadResult>> success) {
            return success.value();
        }

        log.warn("Upload initiation failed: {}", result.error().description());
        return List.of(FileUploadResult.failure(
                "multiple files",
                0,
                result.error().description()
        ));
    }

    @Override
    public List<FileStatusDto> getFileStatuses(List<UUID> fileIds) {
        log.debug("Getting status for {} files", fileIds.size());

        GetUploadStatusQuery query = new GetUploadStatusQuery(fileIds);
        return queryBus.execute(query);
    }

    @Override
    public Result<String> getDownloadUrl(UUID fileId) {
        log.debug("Getting download URL for file {}", fileId);

        GetDownloadUrlQuery query = new GetDownloadUrlQuery(fileId);
        return queryBus.execute(query);
    }

    @Override
    public void linkToEntity(UUID fileId, String entityType, UUID entityId) {
        log.debug("Linking file {} to {} {}", fileId, entityType, entityId);

        LinkFileCommand command = new LinkFileCommand(fileId, entityType, entityId);
        Result<Void> result = commandBus.execute(command);

        if (!result.isSuccess()) {
            log.warn("Failed to link file {} to {} {}: {}",
                    fileId, entityType, entityId, result.error().description());
            throw new RuntimeException(result.error().description());
        }
    }

    @Override
    public Result<Void> deleteFile(UUID fileId, UUID userId) {
        log.info("Deleting file {} requested by user {}", fileId, userId);

        DeleteFileCommand command = new DeleteFileCommand(fileId, userId);
        return commandBus.execute(command);
    }
}