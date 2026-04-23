package com.fileorganizer.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Represents a single file operation record.
 * Used for tracking moves and enabling the undo feature.
 */
public class FileOperation {

    /** The type of operation performed */
    public enum OperationType {
        MOVED, SKIPPED, DUPLICATE_DETECTED
    }

    private final String fileName;
    private final Path sourcePath;
    private final Path destinationPath;
    private final FileCategory category;
    private final OperationType operationType;
    private final LocalDateTime timestamp;

    /**
     * Full constructor for a FileOperation record.
     *
     * @param fileName      name of the file
     * @param sourcePath    original path of the file
     * @param destinationPath target path after moving
     * @param category      category assigned to the file
     * @param operationType type of operation performed
     */
    public FileOperation(String fileName, Path sourcePath, Path destinationPath,
                         FileCategory category, OperationType operationType) {
        this.fileName = fileName;
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.category = category;
        this.operationType = operationType;
        this.timestamp = LocalDateTime.now();
    }

    // --- Getters ---

    public String getFileName() { return fileName; }
    public Path getSourcePath() { return sourcePath; }
    public Path getDestinationPath() { return destinationPath; }
    public FileCategory getCategory() { return category; }
    public OperationType getOperationType() { return operationType; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s -> %s | Category: %s",
                operationType, fileName, sourcePath, destinationPath,
                category != null ? category.getFolderName() : "N/A");
    }
}
