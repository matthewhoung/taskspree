package com.hongsolo.taskspree.modules.storage.application.storage.InitiateUpload;

import com.hongsolo.taskspree.common.application.cqrs.Command;
import com.hongsolo.taskspree.common.application.services.IFileStorageService.FileUploadResult;
import com.hongsolo.taskspree.common.domain.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public record InitiateUploadCommand(
        List<MultipartFile> files,
        UUID uploaderId
) implements Command<Result<List<FileUploadResult>>> {
}
