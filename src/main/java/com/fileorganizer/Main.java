package com.fileorganizer;

import com.fileorganizer.cli.CLIHandler;
import com.fileorganizer.core.UndoManager;
import com.fileorganizer.util.ConfigManager;
import com.fileorganizer.util.LoggerUtility;
import com.fileorganizer.web.WebApp;

import java.nio.file.Path;

/**
 * ╔══════════════════════════════════════════════════════╗
 * ║         FILE ORGANIZER TOOL — Main Entry Point       ║
 * ║                   Version 1.0                        ║
 * ╚══════════════════════════════════════════════════════╝
 *
 * Bootstraps the application in this order:
 *   1. Load configuration (config/organizer.properties)
 *   2. Initialize the logger (writes to logs/ directory)
 *   3. Load undo history (from .undo_history file)
 *   4. Launch the Web Interface
 *   5. Launch the CLI interface
 *
 * Usage:
 *   java -jar file-organizer.jar
 */
public class Main {

    /**
     * Application entry point.
     *
     * @param args command-line arguments (currently unused; all input is interactive)
     */
    public static void main(String[] args) {

        // ─── 1. Load Configuration ────────────────────────────────────────
        ConfigManager config = new ConfigManager();

        // ─── 2. Initialize Logger ─────────────────────────────────────────
        LoggerUtility logger = LoggerUtility.getInstance();
        logger.setDebugEnabled(config.isDebugEnabled());
        logger.initialize(Path.of(config.getLogDirectory()));

        logger.info("File Organizer Tool started.");
        logger.debug("Configuration loaded: recursive=" + config.isRecursiveScan()
                + ", duplicates=" + config.isDuplicateDetectEnabled());

        // ─── 3. Load Undo History ─────────────────────────────────────────
        UndoManager undoManager = new UndoManager();
        undoManager.loadHistory();

        // ─── 4. Start Web Application ─────────────────────────────────────
        try {
            WebApp webApp = new WebApp(config, undoManager);
            webApp.start(8080);
        } catch (Exception e) {
            logger.error("Failed to start Web Interface: " + e.getMessage());
        }

        // ─── 5. Start CLI ─────────────────────────────────────────────────
        CLIHandler cli = new CLIHandler(config, undoManager);
        cli.start();

        logger.info("File Organizer Tool exited.");
    }
}
