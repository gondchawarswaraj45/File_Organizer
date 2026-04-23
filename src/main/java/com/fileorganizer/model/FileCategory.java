package com.fileorganizer.model;

import java.util.Arrays;
import java.util.List;

/**
 * Enum representing file categories with their associated extensions.
 * Each category maps to a target folder name and a list of file extensions.
 */
public enum FileCategory {

    IMAGES("Images", Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp", ".svg", ".ico")),
    DOCUMENTS("Documents", Arrays.asList(".pdf", ".docx", ".doc", ".txt", ".pptx", ".ppt", ".xlsx", ".xls", ".odt", ".csv", ".rtf", ".md")),
    VIDEOS("Videos", Arrays.asList(".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv", ".webm", ".m4v", ".mpeg")),
    AUDIO("Audio", Arrays.asList(".mp3", ".wav", ".aac", ".flac", ".ogg", ".wma", ".m4a", ".aiff")),
    ARCHIVES("Archives", Arrays.asList(".zip", ".rar", ".tar", ".gz", ".7z", ".bz2", ".xz", ".tar.gz")),
    CODE("Code", Arrays.asList(".java", ".py", ".js", ".ts", ".html", ".css", ".cpp", ".c", ".h", ".go", ".rb", ".php", ".sh", ".bat", ".json", ".xml", ".yaml", ".yml")),
    OTHERS("Others", Arrays.asList());

    private final String folderName;
    private final List<String> extensions;

    /**
     * Constructor for FileCategory enum.
     *
     * @param folderName the name of the target folder
     * @param extensions list of file extensions belonging to this category
     */
    FileCategory(String folderName, List<String> extensions) {
        this.folderName = folderName;
        this.extensions = extensions;
    }

    /**
     * Returns the folder name for this category.
     *
     * @return folder name as a String
     */
    public String getFolderName() {
        return folderName;
    }

    /**
     * Returns the list of file extensions for this category.
     *
     * @return list of extension strings (e.g., ".jpg", ".png")
     */
    public List<String> getExtensions() {
        return extensions;
    }
}
