package com.hongsolo.taskspree.modules.storage.presentation;

import com.hongsolo.taskspree.common.application.services.IFileStorageService;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.common.domain.Result;
import com.hongsolo.taskspree.common.presentation.ApiController;
import com.hongsolo.taskspree.modules.storage.presentation.dto.DownloadUrlResponse;
import com.hongsolo.taskspree.modules.storage.presentation.dto.FileStatusDto;
import com.hongsolo.taskspree.modules.storage.presentation.dto.UploadInitiatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileStorageController extends ApiController {

    private final IFileStorageService fileStorageService;
    private final IUserFacadeService userFacadeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFiles(
            @RequestParam("files") List<MultipartFile> files
    ) {
        UUID userId = userFacadeService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"))
                .userId();

        log.info("Received upload request: {} files from user {}", files.size(), userId);

        List<IFileStorageService.FileUploadResult> results =
                fileStorageService.initiateUpload(files, userId);

        UploadInitiatedResponse response = UploadInitiatedResponse.from(results);

        log.info("Upload initiated: {} successful, {} failed",
                response.successCount(), response.failedCount());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<List<FileStatusDto>> getUploadStatus(
            @RequestParam("fileIds") List<UUID> fileIds
    ) {
        log.debug("Status request for {} files", fileIds.size());

        List<IFileStorageService.FileStatusDto> statuses =
                fileStorageService.getFileStatuses(fileIds);

        List<FileStatusDto> dtos = statuses.stream()
                .map(s -> new FileStatusDto(
                        s.fileId(),
                        s.originalName(),
                        s.fileSize(),
                        s.contentType(),
                        s.status(),
                        s.errorMessage(),
                        s.downloadUrl()
                ))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{fileId}/download-url")
    public ResponseEntity<?> getDownloadUrl(@PathVariable("fileId") UUID fileId) {
        log.debug("Download URL request for file {}", fileId);

        Result<String> result = fileStorageService.getDownloadUrl(fileId);

        if (result.isSuccess() && result instanceof Result.Success<String> success) {
            DownloadUrlResponse response = DownloadUrlResponse.of(fileId, success.value());
            return ResponseEntity.ok(response);
        }

        return handleResult(result);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable("fileId") UUID fileId) {
        UUID userId = userFacadeService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"))
                .userId();

        log.info("Delete request for file {} from user {}", fileId, userId);

        Result<Void> result = fileStorageService.deleteFile(fileId, userId);

        return handleResult(result);
    }
}