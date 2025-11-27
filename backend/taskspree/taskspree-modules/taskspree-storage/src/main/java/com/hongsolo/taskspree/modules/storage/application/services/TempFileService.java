package com.hongsolo.taskspree.modules.storage.application.services;

import com.hongsolo.taskspree.modules.storage.infrastructure.storage.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempFileService {

    private final StorageProperties storageProperties;
    private Path tempDirectory;

    @PostConstruct
    public void init() throws IOException {
        tempDirectory = Path.of(storageProperties.getTempDirectory());

        if (!Files.exists(tempDirectory)) {
            Files.createDirectories(tempDirectory);
            log.info("Created temp upload directory: {}", tempDirectory.toAbsolutePath());
        } else {
            log.info("Using existing temp upload directory: {}", tempDirectory.toAbsolutePath());
        }
    }

    /**
     * Save a multipart file to temp storage
     *
     * @param file   The multipart file to save
     * @param fileId The file ID to use as base name
     * @return The path to the saved temp file
     * @throws IOException if save fails
     */
    public Path saveToTemp(MultipartFile file, UUID fileId) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String tempFileName = fileId.toString() + (extension.isEmpty() ? "" : "." + extension);
        Path tempFilePath = tempDirectory.resolve(tempFileName);

        log.debug("Saving file to temp: {} -> {}", file.getOriginalFilename(), tempFilePath);

        Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved to temp: {}, size: {} bytes", tempFilePath, file.getSize());
        return tempFilePath;
    }

    /**
     * Delete a temp file
     *
     * @param tempPath Path to the temp file
     */
    public void deleteTempFile(Path tempPath) {
        try {
            if (Files.exists(tempPath)) {
                Files.delete(tempPath);
                log.debug("Deleted temp file: {}", tempPath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete temp file: {}, error: {}", tempPath, e.getMessage());
        }
    }

    /**
     * Delete a temp file by path string
     */
    public void deleteTempFile(String tempPathString) {
        if (tempPathString != null) {
            deleteTempFile(Path.of(tempPathString));
        }
    }

    /**
     * Check if a temp file exists
     */
    public boolean exists(String tempPathString) {
        if (tempPathString == null) {
            return false;
        }
        return Files.exists(Path.of(tempPathString));
    }

    /**
     * Extract file extension
     */
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
