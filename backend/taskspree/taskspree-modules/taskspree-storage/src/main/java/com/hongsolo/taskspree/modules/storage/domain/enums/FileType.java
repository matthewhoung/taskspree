package com.hongsolo.taskspree.modules.storage.domain.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum FileType {
    // Media types
    MP4("video/mp4", Category.MEDIA),
    JPG("image/jpeg", Category.MEDIA),
    JPEG("image/jpeg", Category.MEDIA),
    PNG("image/png", Category.MEDIA),
    GIF("image/gif", Category.MEDIA),

    // Document types
    PDF("application/pdf", Category.DOCUMENT),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Category.DOCUMENT),
    CSV("text/csv", Category.DOCUMENT),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Category.DOCUMENT);

    private final String mimeType;
    private final Category category;

    FileType(String mimeType, Category category) {
        this.mimeType = mimeType;
        this.category = category;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Category getCategory() {
        return category;
    }

    public enum Category {
        MEDIA,
        DOCUMENT
    }

    /**
     * Find FileType by content type (MIME type)
     */
    public static FileType fromContentType(String contentType) {
        if (contentType == null) {
            return null;
        }

        String normalizedType = contentType.toLowerCase().split(";")[0].trim();

        return Arrays.stream(values())
                .filter(ft -> ft.mimeType.equalsIgnoreCase(normalizedType))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find FileType by file extension
     */
    public static FileType fromExtension(String extension) {
        if (extension == null) {
            return null;
        }

        String normalizedExt = extension.toLowerCase().replace(".", "");

        return Arrays.stream(values())
                .filter(ft -> ft.name().equalsIgnoreCase(normalizedExt))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if content type is allowed
     */
    public static boolean isAllowed(String contentType) {
        return fromContentType(contentType) != null;
    }

    /**
     * Get all allowed MIME types
     */
    public static Set<String> getAllowedMimeTypes() {
        return Arrays.stream(values())
                .map(FileType::getMimeType)
                .collect(Collectors.toSet());
    }

    /**
     * Get all allowed extensions
     */
    public static Set<String> getAllowedExtensions() {
        return Arrays.stream(values())
                .map(ft -> ft.name().toLowerCase())
                .collect(Collectors.toSet());
    }
}
