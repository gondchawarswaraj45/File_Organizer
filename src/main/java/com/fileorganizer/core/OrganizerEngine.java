package com.fileorganizer.core;

import com.fileorganizer.model.FileCategory;
import com.fileorganizer.model.FileOperation;
import com.fileorganizer.model.OrganizeSummary;
import com.fileorganizer.util.ConfigManager;
import com.fileorganizer.util.LoggerUtility;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * OrganizerEngine — Orchestrates the full file organization pipeline.
 *
 * Pipeline:
 *   1. FileScanner     → discover files in the target directory
 *   2. FileCategorizer → assign a FileCategory to each file
 *   3. FileMover       → move (or preview) each file to its category folder
 *   4. UndoManager     → record the session for potential undo
 *
 * All behavior (dry-run, recursive, duplicates) is controlled by ConfigManager.
 */
public class OrganizerEngine {

    private final LoggerUtility logger = LoggerUtility.getInstance();
    private final ConfigManager  config;
    private final UndoManager    undoManager;

    /**
     * Constructs an OrganizerEngine with the given config and undo manager.
     *
     * @param config      application configuration
     * @param undoManager the shared undo session manager
     */
    public OrganizerEngine(ConfigManager config, UndoManager undoManager) {
        this.config      = config;
        this.undoManager = undoManager;
    }

    /**
     * Executes the organization pipeline on the given directory.
     *
     * @param targetDirectory the directory to organize
     * @param dryRun          true to preview without moving files
     * @return OrganizeSummary containing statistics of the operation
     */
    public OrganizeSummary organize(Path targetDirectory, boolean dryRun) {
        OrganizeSummary summary = new OrganizeSummary();
        List<FileOperation> operations = new ArrayList<>();

        logger.separator();
        logger.info(dryRun
                ? "Starting DRY RUN on: " + targetDirectory
                : "Starting ORGANIZE on: " + targetDirectory);
        logger.separator();

        // ─── Step 1: Scan ─────────────────────────────────────────────────
        FileScanner scanner = new FileScanner(config.isRecursiveScan());
        List<Path> files;
        try {
            files = scanner.scan(targetDirectory);
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Scan failed: " + e.getMessage());
            return summary;
        }

        summary.setTotalFilesScanned(files.size());

        if (files.isEmpty()) {
            logger.warn("No files found in directory: " + targetDirectory);
            return summary;
        }

        // ─── Step 2 & 3: Categorize + Move ───────────────────────────────
        FileCategorizer categorizer = new FileCategorizer();
        FileMover mover = new FileMover(dryRun, config.isDuplicateDetectEnabled());

        for (Path file : files) {
            FileCategory category = categorizer.categorize(file);
            FileOperation operation = mover.moveFile(file, category, targetDirectory, summary);
            operations.add(operation);
        }

        // ─── Step 4: Save Undo Session (only for real runs) ──────────────
        if (!dryRun) {
            undoManager.saveSession(operations);
        }

        logger.separator();
        return summary;
    }
}
