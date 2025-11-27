package com.hongsolo.taskspree.modules.storage.application.storage.LinkFile;

import com.hongsolo.taskspree.common.application.cqrs.CommandHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.storage.domain.FileReference;
import com.hongsolo.taskspree.modules.storage.domain.StorageErrors;
import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.repository.IFileReferenceRepository;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkFileCommandHandler
        implements CommandHandler<LinkFileCommand, Result<Void>> {

    private final IStoredFileRepository storedFileRepository;
    private final IFileReferenceRepository fileReferenceRepository;

    @Override
    @Transactional
    public Result<Void> handle(LinkFileCommand command) {
        log.debug("Linking file {} to {} {}",
                command.fileId(), command.entityType(), command.entityId());

        // Find the file
        StoredFile storedFile = storedFileRepository.findById(command.fileId())
                .orElse(null);

        if (storedFile == null || storedFile.isDeleted()) {
            log.warn("File not found or deleted: {}", command.fileId());
            return Result.failure(StorageErrors.FILE_NOT_FOUND);
        }

        // Check if already linked
        boolean alreadyLinked = fileReferenceRepository.existsByFileIdAndEntityTypeAndEntityId(
                command.fileId(),
                command.entityType(),
                command.entityId()
        );

        if (alreadyLinked) {
            log.debug("File already linked: fileId={}, entityType={}, entityId={}",
                    command.fileId(), command.entityType(), command.entityId());
            return Result.failure(StorageErrors.FILE_ALREADY_LINKED);
        }

        // Create the reference
        FileReference reference = FileReference.create(
                storedFile,
                command.entityType(),
                command.entityId()
        );

        fileReferenceRepository.save(reference);

        log.info("File linked: fileId={}, entityType={}, entityId={}",
                command.fileId(), command.entityType(), command.entityId());

        return Result.success(null);
    }
}
