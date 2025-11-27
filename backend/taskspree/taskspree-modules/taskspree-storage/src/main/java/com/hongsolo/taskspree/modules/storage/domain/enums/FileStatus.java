package com.hongsolo.taskspree.modules.storage.domain.enums;

public enum FileStatus {
    PENDING("File saved to temp, awaiting upload"),
    UPLOADING("Upload to S3 in progress"),
    COMPLETED("Successfully uploaded to S3"),
    FAILED("Upload failed, may retry");

    private final String description;

    FileStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canRetry() {
        return this == PENDING || this == FAILED;
    }

    public boolean isTerminal() {
        return this == COMPLETED;
    }
}
