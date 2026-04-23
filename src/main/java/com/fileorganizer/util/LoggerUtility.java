package com.fileorganizer.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LoggerUtility — Centralized logging utility for the File Organizer Tool.
 *
 * Logs all operations to:
 *   1. The console (stdout)
 *   2. A persistent log file: file_organizer.log (in the user's directory)
 *
 * Log Levels: INFO, WARN, ERROR, DEBUG
 */
public class LoggerUtility {

    // --- Singleton Pattern ---
    private static LoggerUtility instance;

    /** Path to the log file */
    private Path logFilePath;

    /** Whether debug-level messages should be printed */
    private boolean debugEnabled = false;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ANSI color codes for console output
    private static final String RESET  = "\u001B[0m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GRAY   = "\u001B[90m";

    /**
     * Private constructor. Use getInstance() to get the singleton.
     */
    private LoggerUtility() {}

    /**
     * Returns the singleton instance of LoggerUtility.
     *
     * @return the single LoggerUtility instance
     */
    public static synchronized LoggerUtility getInstance() {
        if (instance == null) {
            instance = new LoggerUtility();
        }
        return instance;
    }

    /**
     * Initializes the logger with a specific log file directory.
     *
     * @param logDirectory the directory in which to create the log file
     */
    public void initialize(Path logDirectory) {
        try {
            if (!Files.exists(logDirectory)) {
                Files.createDirectories(logDirectory);
            }
            this.logFilePath = logDirectory.resolve("file_organizer.log");

            // Write session header to log file
            String header = String.format(
                    "%n=====================================================%n" +
                    "  File Organizer Tool — Session Started%n" +
                    "  Timestamp: %s%n" +
                    "=====================================================%n",
                    LocalDateTime.now().format(FORMATTER));
            Files.writeString(logFilePath, header, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            System.err.println("[LoggerUtility] Failed to initialize log file: " + e.getMessage());
        }
    }

    /**
     * Enables or disables debug-level logging.
     *
     * @param enabled true to enable debug output
     */
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    // =============================================
    // Public Logging Methods
    // =============================================

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    public void info(String message) {
        log("INFO ", GREEN, message);
    }

    /**
     * Logs a warning message.
     *
     * @param message the warning message
     */
    public void warn(String message) {
        log("WARN ", YELLOW, message);
    }

    /**
     * Logs an error message.
     *
     * @param message the error message
     */
    public void error(String message) {
        log("ERROR", RED, message);
    }

    /**
     * Logs a debug message (only printed if debug mode is enabled).
     *
     * @param message the debug message
     */
    public void debug(String message) {
        if (debugEnabled) {
            log("DEBUG", GRAY, message);
        }
    }

    /**
     * Logs a separator line for readability.
     */
    public void separator() {
        String line = "-".repeat(60);
        System.out.println(CYAN + line + RESET);
        writeToFile("     " + line);
    }

    // =============================================
    // Private Helpers
    // =============================================

    /**
     * Core logging method — prints to console and writes to file.
     *
     * @param level      the log level label
     * @param color      ANSI color code for console
     * @param message    the message to log
     */
    private void log(String level, String color, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String consoleLine = String.format("%s[%s] [%s] %s%s",
                color, timestamp, level, message, RESET);
        String fileLine = String.format("[%s] [%s] %s",
                timestamp, level, message);

        System.out.println(consoleLine);
        writeToFile(fileLine);
    }

    /**
     * Writes a raw string to the log file.
     *
     * @param line the line to write
     */
    private void writeToFile(String line) {
        if (logFilePath == null) return;
        try {
            Files.writeString(logFilePath, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[LoggerUtility] Failed to write to log file: " + e.getMessage());
        }
    }

    /**
     * Returns the path of the log file.
     *
     * @return path of the log file
     */
    public Path getLogFilePath() {
        return logFilePath;
    }
}
