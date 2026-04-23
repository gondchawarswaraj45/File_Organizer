package com.fileorganizer.core;

import com.fileorganizer.model.FileCategory;
import com.fileorganizer.model.FileOperation;
import com.fileorganizer.model.FileOperation.OperationType;
import com.fileorganizer.model.OrganizeSummary;
import com.fileorganizer.util.LoggerUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * FileMover — Handles moving files into their categorized target directories.
 *
 * Features:
 *   - Creates category subdirectories if they don't already exist
 *   - Dry Run support (previews without actually moving files)
 *   - Duplicate detection by file name AND file size
 *   - Returns a list of all FileOperation records (for undo support)
 *   - Updates the OrganizeSummary with operation statistics
 */
public class FileMover {

    private final LoggerUtility logger = LoggerUtility.getInstance();

    /** Whether to simulate moves without actually writing to disk */
    private final boolean dryRun;

    /** Whether to detect duplicate files before moving */
    private final boolean detectDuplicates;

    /**
     * Constructs a FileMover.
     *
     * @param dryRun           true for preview mode (no actual file moves)
     * @param detectDuplicates true to enable duplicate detection
     */
    public FileMover(boolean dryRun, boolean detectDuplicates) {
        this.dryRun = dryRun;
        this.detectDuplicates = detectDuplicates;
    }

    /**
     * Moves a single file to the appropriate category folder inside the target directory.
     *
     * Steps:
     *   1. Determine target category folder path
     *   2. Check for duplicates if enabled
     *   3. Create category folder if it doesn't exist
     *   4. Move the file (or simulate in dry-run mode)
     *   5. Record the operation and update the summary
     *
     * @param filePath        the source file to move
     * @param category        the target FileCategory
     * @param targetDirectory the root directory where category folders will be created
     * @param summary         the OrganizeSummary to update
     * @return a FileOperation record of the action taken
     */
    public FileOperation moveFile(Path filePath, FileCategory category,
                                   Path targetDirectory, OrganizeSummary summary) {

        String fileName = filePath.getFileName().toString();
        Path categoryFolder = targetDirectory.resolve(category.getFolderName());
        Path destinationPath = categoryFolder.resolve(fileName);

        // ─── Duplicate Detection ──────────────────────────────────────────
        if (detectDuplicates && isDuplicate(filePath, destinationPath)) {
            logger.warn("⚠ Duplicate detected: " + fileName + " already exists in " + category.getFolderName());
            summary.incrementDuplicates();
            summary.incrementSkipped();

            return new FileOperation(fileName, filePath, destinationPath,
                    category, OperationType.DUPLICATE_DETECTED);
        }

        // ─── Dry Run Mode ─────────────────────────────────────────────────
        if (dryRun) {
            logger.info("[DRY RUN] Would move → " + fileName + " ➜ " + category.getFolderName() + "/");
            summary.incrementMoved();
            summary.incrementCategory(category.getFolderName());
            return new FileOperation(fileName, filePath, destinationPath,
                    category, OperationType.MOVED);
        }

        // ─── Actual File Move ─────────────────────────────────────────────
        try {
            // Create the category directory if it doesn't exist
            if (!Files.exists(categoryFolder)) {
                Files.createDirectories(categoryFolder);
                logger.debug("Created directory: " + categoryFolder);
            }

            // Handle filename collision — append (1), (2), etc.
            destinationPath = resolveNameConflict(destinationPath);

            // Move the file using Java NIO
            Files.move(filePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("✔ Moved: " + fileName + " ➜ " + category.getFolderName() + "/");
            summary.incrementMoved();
            summary.incrementCategory(category.getFolderName());

            return new FileOperation(fileName, filePath, destinationPath,
                    category, OperationType.MOVED);

        } catch (IOException e) {
            logger.error("Failed to move file: " + fileName + " | Reason: " + e.getMessage());
            summary.incrementSkipped();
            return new FileOperation(fileName, filePath, destinationPath,
                    category, OperationType.SKIPPED);
        }
    }

    /**
     * Checks whether a file at the destination is a duplicate of the source.
     * A duplicate is defined as: same file name AND same file size.
     *
     * @param source      path of the file to be moved
     * @param destination expected destination path
     * @return true if a duplicate is detected
     */
    private boolean isDuplicate(Path source, Path destination) {
        if (!Files.exists(destination)) {
            return false;
        }
        try {
            long sourceSize = Files.size(source);
            long destSize   = Files.size(destination);
            return sourceSize == destSize;
        } catch (IOException e) {
            logger.debug("Could not compare file sizes for duplicate check: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resolves filename conflicts at the destination by appending a counter.
     * Example: if "report.pdf" exists, tries "report (1).pdf", "report (2).pdf", etc.
     *
     * @param destination the initial destination path
     * @return a non-conflicting destination path
     */
    private Path resolveNameConflict(Path destination) {
        if (!Files.exists(destination)) {
            return destination;
        }

        String fileName  = destination.getFileName().toString();
        Path parentDir   = destination.getParent();

        // Split into base name and extension
        int dotIndex     = fileName.lastIndexOf('.');
        String baseName  = (dotIndex > 0) ? fileName.substring(0, dotIndex)  : fileName;
        String extension = (dotIndex > 0) ? fileName.substring(dotIndex)     : "";

        int counter = 1;
        Path resolved;
        do {
            String newName = baseName + " (" + counter + ")" + extension;
            resolved = parentDir.resolve(newName);
            counter++;
        } while (Files.exists(resolved));

        logger.debug("Name conflict resolved: " + fileName + " → " + resolved.getFileName());
        return resolved;
    }
}
