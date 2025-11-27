package com.hongsolo.taskspree.modules.storage.presentation.dto;

import com.hongsolo.taskspree.common.application.services.IFileStorageService.FileUploadResult;

import java.util.List;

/**
 * Response DTO for file upload initiation.
 * Contains summary counts and individual file results.
 */
public record UploadInitiatedResponse(
        int totalFiles,
        int successCount,
        int failedCount,
        List<FileUploadResultDto> files
) {
    /**
     * Create response from IFileStorageService results
     */
    public static UploadInitiatedResponse from(List<FileUploadResult> results) {
        int successCount = (int) results.stream()
                .filter(r -> r.fileId() != null)
                .count();

        List<FileUploadResultDto> dtos = results.stream()
                .map(r -> new FileUploadResultDto(
                        r.fileId(),
                        r.originalName(),
                        r.fileSize(),
                        r.status(),
                        r.errorMessage()
                ))
                .toList();

        return new UploadInitiatedResponse(
                results.size(),
                successCount,
                results.size() - successCount,
                dtos
        );
    }
}
