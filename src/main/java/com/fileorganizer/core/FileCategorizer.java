package com.fileorganizer.core;

import com.fileorganizer.model.FileCategory;
import com.fileorganizer.util.LoggerUtility;

import java.nio.file.Path;

/**
 * FileCategorizer — Determines the FileCategory of a given file based on its extension.
 *
 * Matching logic:
 *   1. Extract the file extension (lowercase)
 *   2. Iterate through all FileCategory enum values
 *   3. Return the first category whose extension list contains the file's extension
 *   4. If no match is found, return FileCategory.OTHERS
 */
public class FileCategorizer {

    private final LoggerUtility logger = LoggerUtility.getInstance();

    /**
     * Categorizes a file based on its extension.
     *
     * @param filePath the path to the file to categorize
     * @return the matching FileCategory (OTHERS if no match)
     */
    public FileCategory categorize(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String extension = extractExtension(fileName);

        if (extension.isEmpty()) {
            logger.debug("No extension found for: " + fileName + " → Categorized as OTHERS");
            return FileCategory.OTHERS;
        }

        // Check each category's extension list
        for (FileCategory category : FileCategory.values()) {
            if (category == FileCategory.OTHERS) continue; // Skip the catch-all

            if (category.getExtensions().contains(extension)) {
                logger.debug("File: " + fileName + " [" + extension + "] → " + category.getFolderName());
                return category;
            }
        }

        // No match found → Others
        logger.debug("Unrecognized extension: " + extension + " → Categorized as OTHERS");
        return FileCategory.OTHERS;
    }

    /**
     * Extracts the file extension from a filename.
     * Handles compound extensions like ".tar.gz" by returning the last segment.
     *
     * @param fileName the full filename (e.g., "report.pdf")
     * @return the lowercase extension including the dot (e.g., ".pdf"),
     *         or an empty string if no extension exists
     */
    private String extractExtension(String fileName) {
        // Handle hidden files on Unix (e.g., ".gitignore") — no extension
        if (fileName.startsWith(".") && fileName.indexOf('.', 1) == -1) {
            return "";
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return ""; // No extension or trailing dot
        }

        return fileName.substring(lastDot).toLowerCase();
    }
}
