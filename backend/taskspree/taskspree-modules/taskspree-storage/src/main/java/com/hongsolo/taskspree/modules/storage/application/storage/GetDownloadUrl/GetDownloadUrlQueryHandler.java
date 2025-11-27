package com.hongsolo.taskspree.modules.storage.application.storage.GetDownloadUrl;

import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.modules.storage.domain.StorageErrors;
import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetDownloadUrlQueryHandler
        implements QueryHandler<GetDownloadUrlQuery, Result<String>> {

    private final IStoredFileRepository storedFileRepository;
    private final S3StorageClient s3StorageClient;

    @Override
    @Transactional(readOnly = true)
    public Result<String> handle(GetDownloadUrlQuery query) {
        log.debug("Generating download URL for file: {}", query.fileId());

        StoredFile storedFile = storedFileRepository.findById(query.fileId())
                .orElse(null);

        if (storedFile == null) {
            log.warn("File not found: {}", query.fileId());
            return Result.failure(StorageErrors.FILE_NOT_FOUND);
        }

        if (!storedFile.isAvailable()) {
            log.warn("File not available for download: id={}, status={}, deleted={}",
                    query.fileId(), storedFile.getStatus(), storedFile.isDeleted());
            return Result.failure(StorageErrors.FILE_NOT_AVAILABLE);
        }

        try {
            String url = s3StorageClient.generatePresignedUrl(
                    storedFile.getBucketName(),
                    storedFile.getS3Key()
            );

            log.debug("Download URL generated for file: {}", query.fileId());
            return Result.success(url);

        } catch (Exception e) {
            log.error("Failed to generate download URL for file {}: {}",
                    query.fileId(), e.getMessage());
            return Result.failure(StorageErrors.UPLOAD_FAILED);
        }
    }
}
