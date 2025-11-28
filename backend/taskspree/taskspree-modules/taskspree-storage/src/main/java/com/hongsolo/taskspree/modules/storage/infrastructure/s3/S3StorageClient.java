package com.hongsolo.taskspree.modules.storage.infrastructure.s3;

import com.hongsolo.taskspree.modules.storage.infrastructure.s3.S3Properties;
import com.hongsolo.taskspree.modules.storage.infrastructure.storage.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3StorageClient {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    private final StorageProperties storageProperties;

    /**
     * Initialize buckets on startup
     */
    @PostConstruct
    public void initializeBuckets() {
        log.info("Initializing S3 buckets...");

        Map<String, String> buckets = s3Properties.getBuckets().all();

        for (Map.Entry<String, String> entry : buckets.entrySet()) {
            createBucketIfNotExists(entry.getValue());
        }

        log.info("S3 buckets initialization complete");
    }

    /**
     * Create a bucket if it doesn't exist
     */
    private void createBucketIfNotExists(String bucketName) {
        try {
            HeadBucketRequest headRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headRequest);
            log.debug("Bucket already exists: {}", bucketName);

        } catch (NoSuchBucketException e) {
            log.info("Creating bucket: {}", bucketName);

            CreateBucketRequest createRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.createBucket(createRequest);
            log.info("Bucket created successfully: {}", bucketName);
        }
    }

    /*
     * Uploads a stream directly to S3 without disk buffering.
     * Ideal for use with Virtual Threads.
     */
    public void uploadStream(String bucketName, String key, InputStream inputStream,long length, String contentType) {
        try {
            log.debug("Streaming upload start: bucket={}, key={}, size={}", bucketName, key, length);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, length));
        } catch (S3Exception e) {
            log.error("S3 Upload Error: bucket={}, key={}, message={}",
                    bucketName, key, e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    /**
     * Upload a file to S3
     *
     * @param bucketName Target bucket
     * @param key        Object key (path in bucket)
     * @param filePath   Local file path
     * @param contentType MIME type
     * @return true if successful
     */
    public boolean uploadFile(String bucketName, String key, Path filePath, String contentType) {
        try {
            log.debug("Uploading file to S3: bucket={}, key={}, path={}", bucketName, key, filePath);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromFile(filePath));

            log.info("File uploaded successfully: bucket={}, key={}", bucketName, key);
            return true;

        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: bucket={}, key={}, error={}",
                    bucketName, key, e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    /**
     * Upload bytes to S3
     */
    public boolean uploadBytes(String bucketName, String key, byte[] data, String contentType) {
        try {
            log.debug("Uploading bytes to S3: bucket={}, key={}, size={}", bucketName, key, data.length);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));

            log.info("Bytes uploaded successfully: bucket={}, key={}", bucketName, key);
            return true;

        } catch (S3Exception e) {
            log.error("Failed to upload bytes to S3: bucket={}, key={}, error={}",
                    bucketName, key, e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    /**
     * Delete a file from S3
     */
    public boolean deleteFile(String bucketName, String key) {
        try {
            log.debug("Deleting file from S3: bucket={}, key={}", bucketName, key);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("File deleted successfully: bucket={}, key={}", bucketName, key);
            return true;

        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: bucket={}, key={}, error={}",
                    bucketName, key, e.awsErrorDetails().errorMessage());
            return false;
        }
    }

    /**
     * Check if a file exists in S3
     */
    public boolean fileExists(String bucketName, String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Generate a presigned URL for downloading a file
     */
    public String generatePresignedUrl(String bucketName, String key) {
        Duration expiry = Duration.ofHours(storageProperties.getPresignedUrlExpiryHours());
        return generatePresignedUrl(bucketName, key, expiry);
    }

    /**
     * Generate a presigned URL with custom expiry
     */
    public String generatePresignedUrl(String bucketName, String key, Duration expiry) {
        try {
            log.debug("Generating presigned URL: bucket={}, key={}, expiry={}", bucketName, key, expiry);

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getRequest)
                    .signatureDuration(expiry)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String url = presignedRequest.url().toString();

            log.debug("Presigned URL generated: {}", url);
            return url;

        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL: bucket={}, key={}, error={}",
                    bucketName, key, e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

    /**
     * Get file metadata
     */
    public S3FileMetadata getFileMetadata(String bucketName, String key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);

            return new S3FileMetadata(
                    response.contentLength(),
                    response.contentType(),
                    response.lastModified()
            );

        } catch (NoSuchKeyException e) {
            return null;
        }
    }

    /**
     * File metadata record
     */
    public record S3FileMetadata(
            Long contentLength,
            String contentType,
            java.time.Instant lastModified
    ) {}
}
