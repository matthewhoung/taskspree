package com.hongsolo.taskspree.modules.storage.infrastructure.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
@Getter
@Setter
public class StorageProperties {

    /**
     * Directory for temporary file storage before S3 upload
     */
    private String tempDirectory = "./upload-temp";

    /**
     * Maximum file size in bytes (default: 50MB)
     */
    private long maxFileSize = 52428800L;

    /**
     * Maximum number of concurrent uploads per user
     */
    private int maxConcurrentUploadsPerUser = 10;

    /**
     * Days before orphaned files are cleaned up
     */
    private int orphanCleanupDays = 3;

    /**
     * Hours before presigned URLs expire
     */
    private int presignedUrlExpiryHours = 1;

    /**
     * Retry configuration
     */
    private Retry retry = new Retry();

    @Getter
    @Setter
    public static class Retry {
        /**
         * Maximum retry attempts for failed uploads
         */
        private int maxAttempts = 3;

        /**
         * Delay between retries in milliseconds
         */
        private long delayMs = 5000L;

        /**
         * Multiplier for exponential backoff
         */
        private double multiplier = 2.0;
    }
}
