package com.fileorganizer.core;

import com.fileorganizer.util.LoggerUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * FileScanner — Scans a given directory and returns a flat list of all files found.
 *
 * Supports:
 *   - Single-level scan (non-recursive)
 *   - Deep recursive scan (optional)
 *   - Skips category subdirectories to avoid re-processing organized files
 */
public class FileScanner {

    private final LoggerUtility logger = LoggerUtility.getInstance();

    /** Whether to scan subdirectories recursively */
    private final boolean recursive;

    /**
     * Constructs a FileScanner.
     *
     * @param recursive true to scan subdirectories recursively
     */
    public FileScanner(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Scans the given directory and returns a list of all regular files.
     * Automatically skips the known category subdirectories to prevent
     * re-organizing already organized files.
     *
     * @param targetDirectory the directory to scan
     * @return list of Path objects representing found files
     * @throws IOException if the directory cannot be read
     * @throws IllegalArgumentException if the path is not a valid directory
     */
    public List<Path> scan(Path targetDirectory) throws IOException {
        // Validate input directory
        if (!Files.exists(targetDirectory)) {
            throw new IllegalArgumentException("Directory does not exist: " + targetDirectory);
        }
        if (!Files.isDirectory(targetDirectory)) {
            throw new IllegalArgumentException("Path is not a directory: " + targetDirectory);
        }

        logger.info("Scanning directory: " + targetDirectory.toAbsolutePath());

        List<Path> collectedFiles = new ArrayList<>();

        if (recursive) {
            // Deep scan — walk entire directory tree
            logger.info("Mode: Recursive scan enabled.");
            try (Stream<Path> stream = Files.walk(targetDirectory)) {
                collectedFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> !isInsideCategoryFolder(p, targetDirectory))
                        .collect(Collectors.toList());
            }
        } else {
            // Shallow scan — only immediate children
            logger.info("Mode: Shallow scan (top-level only).");
            try (Stream<Path> stream = Files.list(targetDirectory)) {
                collectedFiles = stream
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());
            }
        }

        logger.info("Total files found: " + collectedFiles.size());
        return collectedFiles;
    }

    /**
     * Checks if a given file path resides inside one of the known category folders.
     * This prevents already-organized files from being re-processed.
     *
     * @param filePath        the path of the file to check
     * @param baseDirectory   the root scan directory
     * @return true if the file is inside a category folder
     */
    private boolean isInsideCategoryFolder(Path filePath, Path baseDirectory) {
        Path relative = baseDirectory.relativize(filePath);
        if (relative.getNameCount() > 1) {
            String firstFolder = relative.getName(0).toString();
            // Category folder names (must match FileCategory enum folder names)
            List<String> categoryFolders = List.of(
                    "Images", "Documents", "Videos", "Audio",
                    "Archives", "Code", "Others"
            );
            return categoryFolders.contains(firstFolder);
        }
        return false;
    }
}
