package com.hongsolo.taskspree.modules.storage.application.services;

import com.hongsolo.taskspree.modules.storage.domain.enums.FileType;
import com.hongsolo.taskspree.modules.storage.domain.StorageErrors;
import com.hongsolo.taskspree.modules.storage.infrastructure.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileValidationService {

    private final StorageProperties storageProperties;

    /**
     * Validate a file for upload.
     * Returns Optional.empty() if valid, or an error message if invalid.
     */
    public Optional<String> validateFile(MultipartFile file) {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            return Optional.of(StorageErrors.FILE_EMPTY.description());
        }

        // Check file size
        if (file.getSize() > storageProperties.getMaxFileSize()) {
            long maxSizeMB = storageProperties.getMaxFileSize() / (1024 * 1024);
            return Optional.of("File exceeds maximum allowed size of " + maxSizeMB + "MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (!FileType.isAllowed(contentType)) {
            return Optional.of("File type '" + contentType + "' is not allowed. " +
                    "Allowed types: " + String.join(", ", FileType.getAllowedExtensions()));
        }

        // Validate file extension matches content type
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            String extension = originalName.substring(originalName.lastIndexOf(".") + 1);
            FileType fromExtension = FileType.fromExtension(extension);
            FileType fromContentType = FileType.fromContentType(contentType);

            if (fromExtension != null && fromContentType != null && fromExtension != fromContentType) {
                log.warn("File extension mismatch: name={}, extension={}, contentType={}",
                        originalName, extension, contentType);
                // Allow it but log - some browsers send wrong content types
            }
        }

        return Optional.empty();
    }

    /**
     * Check if the file count is within limits
     */
    public Optional<String> validateFileCount(int fileCount) {
        if (fileCount == 0) {
            return Optional.of(StorageErrors.NO_FILES_PROVIDED.description());
        }

        if (fileCount > storageProperties.getMaxConcurrentUploadsPerUser()) {
            return Optional.of("Maximum " + storageProperties.getMaxConcurrentUploadsPerUser() +
                    " files can be uploaded at once");
        }

        return Optional.empty();
    }

    /**
     * Check if user has reached concurrent upload limit
     */
    public boolean hasReachedUploadLimit(long currentPendingCount) {
        return currentPendingCount >= storageProperties.getMaxConcurrentUploadsPerUser();
    }
}
