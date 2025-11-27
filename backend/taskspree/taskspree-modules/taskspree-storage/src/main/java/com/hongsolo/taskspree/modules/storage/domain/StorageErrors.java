package com.hongsolo.taskspree.modules.storage.domain;

import com.hongsolo.taskspree.common.domain.Error;

public final class StorageErrors {

    private StorageErrors() {
        // Prevent instantiation
    }

    // File Validation Errors
    public static final Error FILE_TOO_LARGE = new Error(
            "Storage.FileTooLarge",
            "File exceeds maximum allowed size of 50MB",
            Error.ErrorType.VALIDATION
    );

    public static final Error FILE_TYPE_NOT_ALLOWED = new Error(
            "Storage.FileTypeNotAllowed",
            "File type is not allowed. Allowed types: mp4, jpg, jpeg, png, gif, pdf, xlsx, csv, docx",
            Error.ErrorType.VALIDATION
    );

    public static final Error FILE_EMPTY = new Error(
            "Storage.FileEmpty",
            "File is empty or could not be read",
            Error.ErrorType.VALIDATION
    );

    public static final Error NO_FILES_PROVIDED = new Error(
            "Storage.NoFilesProvided",
            "At least one file must be provided for upload",
            Error.ErrorType.VALIDATION
    );

    public static final Error TOO_MANY_FILES = new Error(
            "Storage.TooManyFiles",
            "Maximum 10 files can be uploaded at once",
            Error.ErrorType.VALIDATION
    );

    // File Not Found Errors
    public static final Error FILE_NOT_FOUND = new Error(
            "Storage.FileNotFound",
            "File not found",
            Error.ErrorType.NOT_FOUND
    );

    public static final Error FILE_NOT_AVAILABLE = new Error(
            "Storage.FileNotAvailable",
            "File is not available for download. It may still be processing or has been deleted",
            Error.ErrorType.NOT_FOUND
    );

    // Permission Errors
    public static final Error NOT_FILE_OWNER = new Error(
            "Storage.NotFileOwner",
            "You do not have permission to perform this action on this file",
            Error.ErrorType.VALIDATION
    );

    // Upload Errors
    public static final Error UPLOAD_FAILED = new Error(
            "Storage.UploadFailed",
            "Failed to upload file to storage",
            Error.ErrorType.FAILURE
    );

    public static final Error TEMP_STORAGE_FAILED = new Error(
            "Storage.TempStorageFailed",
            "Failed to save file to temporary storage",
            Error.ErrorType.FAILURE
    );

    // Reference Errors
    public static final Error FILE_ALREADY_LINKED = new Error(
            "Storage.FileAlreadyLinked",
            "File is already linked to this entity",
            Error.ErrorType.CONFLICT
    );

    public static final Error FILE_NOT_LINKED = new Error(
            "Storage.FileNotLinked",
            "File is not linked to this entity",
            Error.ErrorType.NOT_FOUND
    );

    /**
     * Factory method for dynamic file size error
     */
    public static Error fileTooLarge(long maxSizeBytes) {
        long maxSizeMB = maxSizeBytes / (1024 * 1024);
        return new Error(
                "Storage.FileTooLarge",
                "File exceeds maximum allowed size of " + maxSizeMB + "MB",
                Error.ErrorType.VALIDATION
        );
    }

    /**
     * Factory method for dynamic file type error
     */
    public static Error fileTypeNotAllowed(String contentType) {
        return new Error(
                "Storage.FileTypeNotAllowed",
                "File type '" + contentType + "' is not allowed",
                Error.ErrorType.VALIDATION
        );
    }
}
