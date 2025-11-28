package com.hongsolo.taskspree.modules.storage.infrastructure.jobs;

import com.hongsolo.taskspree.modules.storage.domain.StoredFile;
import com.hongsolo.taskspree.modules.storage.domain.repository.IStoredFileRepository;
import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3StorageClient;
import com.hongsolo.taskspree.modules.storage.infrastructure.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanFileCleanupJob {

    private final IStoredFileRepository storedFileRepository;
    private final S3StorageClient s3StorageClient;
    private final StorageProperties storageProperties;

    @Scheduled(cron = "0 0 2 * * ?")  // 2:00 AM daily
    @Transactional
    public void cleanupOrphanFiles() {
        log.info("Starting orphan file cleanup job...");

        Instant cutoffTime = Instant.now()
                .minus(storageProperties.getOrphanCleanupDays(), ChronoUnit.DAYS);

        List<StoredFile> orphanedFiles = storedFileRepository
                .findOrphanedFiles(cutoffTime, 100); // Limit to 100 files per run

        int deletedCount = 0;

        for (StoredFile file : orphanedFiles) {
            try {
                // Delete from S3 if uploaded
                if (file.getBucketName() != null && file.getS3Key() != null) {
                    s3StorageClient.deleteFile(file.getBucketName(), file.getS3Key());
                }
            } catch (Exception e) {
                log.error("Failed to cleanup orphaned file {}: {}",
                        file.getId(), e.getMessage());
            }
        }
    }
}
