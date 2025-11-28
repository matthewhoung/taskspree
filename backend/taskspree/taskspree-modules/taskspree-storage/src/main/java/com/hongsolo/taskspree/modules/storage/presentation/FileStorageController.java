package com.hongsolo.taskspree.modules.storage.presentation;

import com.hongsolo.taskspree.common.application.services.IFileStorageService;
import com.hongsolo.taskspree.common.application.services.IUserFacadeService;
import com.hongsolo.taskspree.common.presentation.ApiController;
import com.hongsolo.taskspree.modules.storage.presentation.dto.DownloadUrlResponse;
import com.hongsolo.taskspree.modules.storage.presentation.dto.FileStatusDto;
import com.hongsolo.taskspree.modules.storage.presentation.dto.UploadInitiatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<UploadInitiatedResponse> uploadFiles(
            @RequestParam("files") List<MultipartFile> files
    ) {
        UUID uploaderId = userFacadeService.getCurrentUser().get().userId();

        log.info("Received upload request: {} files from user {}", files.size(), uploaderId);

        List<IFileStorageService.FileUploadResult> results = fileStorageService.initiateUpload(files, uploaderId);

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

        List<IFileStorageService.FileStatusDto> statuses = fileStorageService.getFileStatuses(fileIds);

        // Map to presentation DTOs
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
    public ResponseEntity<DownloadUrlResponse> getDownloadUrl(
            @PathVariable UUID fileId
    ) {
        log.debug("Download URL request for file {}", fileId);

        String url = fileStorageService.getDownloadUrl(fileId);

        if (url == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(DownloadUrlResponse.of(fileId, url));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable UUID fileId,
            @AuthenticationPrincipal UUID requesterId
    ) {
        log.info("Delete request for file {} from user {}", fileId, requesterId);

        fileStorageService.deleteFile(fileId, requesterId);

        return ResponseEntity.noContent().build();
    }
}
