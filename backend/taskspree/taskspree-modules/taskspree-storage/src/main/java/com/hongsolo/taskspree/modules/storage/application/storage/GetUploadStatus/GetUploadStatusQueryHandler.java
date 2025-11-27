package com.hongsolo.taskspree.modules.storage.application.storage.GetUploadStatus;

import com.hongsolo.taskspree.common.application.cqrs.QueryHandler;
import com.hongsolo.taskspree.common.application.services.IFileStorageService.FileStatusDto;
import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.enums.FileStatus;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUploadStatusQueryHandler
        implements QueryHandler<GetUploadStatusQuery, List<FileStatusDto>> {

    private final IStoredFileRepository storedFileRepository;
    private final S3StorageClient s3StorageClient;

    @Override
    @Transactional(readOnly = true)
    public List<FileStatusDto> handle(GetUploadStatusQuery query) {
        log.debug("Fetching status for {} files", query.fileIds().size());

        List<StoredFile> files = storedFileRepository.findAllById(query.fileIds());

        return files.stream()
                .map(this::toFileStatusDto)
                .toList();
    }

    private FileStatusDto toFileStatusDto(StoredFile file) {
        String downloadUrl = null;

        // Generate download URL only for completed files
        if (file.getStatus() == FileStatus.COMPLETED && file.isAvailable()) {
            try {
                downloadUrl = s3StorageClient.generatePresignedUrl(
                        file.getBucketName(),
                        file.getS3Key()
                );
            } catch (Exception e) {
                log.warn("Failed to generate presigned URL for file {}: {}",
                        file.getId(), e.getMessage());
            }
        }

        return new FileStatusDto(
                file.getId(),
                file.getOriginalName(),
                file.getFileSize(),
                file.getContentType(),
                file.getStatus().name(),
                file.getErrorMessage(),
                downloadUrl
        );
    }
}
