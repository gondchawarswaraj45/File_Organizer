package com.fileorganizer.cli;

import com.fileorganizer.core.OrganizerEngine;
import com.fileorganizer.core.UndoManager;
import com.fileorganizer.model.OrganizeSummary;
import com.fileorganizer.util.ConfigManager;
import com.fileorganizer.util.LoggerUtility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * CLIHandler — Interactive command-line interface for the File Organizer Tool.
 *
 * Presents a menu-driven interface that guides the user through:
 *   1. Entering the target directory path
 *   2. Choosing an operation mode (Organize / Dry Run / Undo / Settings)
 *   3. Confirming actions before execution
 *   4. Displaying the operation summary
 */
public class CLIHandler {

    // ANSI color codes
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String BLUE   = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";

    private final Scanner        input;
    private final LoggerUtility  logger;
    private final ConfigManager  config;
    private final UndoManager    undoManager;
    private final OrganizerEngine engine;

    /**
     * Constructs the CLI handler with all required dependencies.
     *
     * @param config      application configuration
     * @param undoManager undo session manager
     */
    public CLIHandler(ConfigManager config, UndoManager undoManager) {
        this.input       = new Scanner(System.in);
        this.logger      = LoggerUtility.getInstance();
        this.config      = config;
        this.undoManager = undoManager;
        this.engine      = new OrganizerEngine(config, undoManager);
    }

    /**
     * Starts the interactive CLI session.
     * Runs in a loop until the user chooses to exit.
     */
    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = readInput("Enter your choice").trim();

            switch (choice) {
                case "1" -> runOrganize(false);
                case "2" -> runOrganize(true);
                case "3" -> runUndo();
                case "4" -> showSettings();
                case "5" -> showHelp();
                case "0" -> {
                    running = false;
                    System.out.println(GREEN + "\n  Goodbye! Your files are organized. 👋\n" + RESET);
                }
                default -> System.out.println(RED + "  Invalid choice. Please enter 0–5.\n" + RESET);
            }
        }
    }

    // =============================================
    // Mode Handlers
    // =============================================

    /**
     * Handles the Organize and Dry Run flows.
     *
     * @param isDryRun true for dry run mode
     */
    private void runOrganize(boolean isDryRun) {
        String modeLabel = isDryRun ? "DRY RUN (Preview)" : "ORGANIZE";
        System.out.println("\n" + CYAN + "  ── " + modeLabel + " MODE ──" + RESET);

        // Get directory path from user
        String rawPath = readInput("Enter the full path of the directory to organize");
        if (rawPath.isBlank()) {
            System.out.println(RED + "  Error: No path entered." + RESET);
            return;
        }

        Path targetDir = Paths.get(rawPath.trim());

        // Validate directory
        if (!Files.exists(targetDir)) {
            System.out.println(RED + "  Error: Directory does not exist: " + rawPath + RESET);
            return;
        }
        if (!Files.isDirectory(targetDir)) {
            System.out.println(RED + "  Error: Path is not a directory: " + rawPath + RESET);
            return;
        }

        // Confirm before executing
        System.out.println(YELLOW + "\n  Target: " + targetDir.toAbsolutePath() + RESET);
        System.out.println(YELLOW + "  Mode  : " + modeLabel + RESET);
        System.out.println(YELLOW + "  Recursive: " + config.isRecursiveScan() + RESET);

        if (!isDryRun) {
            String confirm = readInput("\n  ⚠ This will MOVE files. Proceed? (yes/no)").trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("y")) {
                System.out.println(YELLOW + "  Operation cancelled." + RESET);
                return;
            }
        }

        // Execute
        OrganizeSummary summary = engine.organize(targetDir, isDryRun);
        summary.printSummary(isDryRun);

        if (!isDryRun) {
            System.out.println(GREEN + "\n  ✔ Done! Undo is available (choose option 3 from menu)." + RESET);
        } else {
            System.out.println(CYAN + "\n  ℹ Dry run complete. No files were moved." + RESET);
        }
    }

    /**
     * Handles the Undo operation.
     */
    private void runUndo() {
        System.out.println("\n" + CYAN + "  ── UNDO LAST OPERATION ──" + RESET);

        if (!undoManager.canUndo()) {
            System.out.println(YELLOW + "  Nothing to undo. No previous session found." + RESET);
            return;
        }

        System.out.println(YELLOW + "  Available undo sessions: " + undoManager.sessionCount() + RESET);
        String confirm = readInput("  Undo the last organize operation? (yes/no)").trim().toLowerCase();

        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println(YELLOW + "  Undo cancelled." + RESET);
            return;
        }

        boolean success = undoManager.undo();
        if (success) {
            System.out.println(GREEN + "  ✔ Undo successful! Files have been restored." + RESET);
        } else {
            System.out.println(RED + "  ✘ Undo failed. Check the log file for details." + RESET);
        }
    }

    /**
     * Displays and allows modification of current settings.
     */
    private void showSettings() {
        System.out.println("\n" + CYAN + "  ── CURRENT SETTINGS ──" + RESET);
        System.out.printf("  %-25s : %s%n", "Recursive Scan",    config.isRecursiveScan());
        System.out.printf("  %-25s : %s%n", "Duplicate Detection", config.isDuplicateDetectEnabled());
        System.out.printf("  %-25s : %s%n", "Debug Logging",      config.isDebugEnabled());
        System.out.printf("  %-25s : %s%n", "Log Directory",      config.getLogDirectory());

        System.out.println(YELLOW + "\n  Edit config/organizer.properties to change settings." + RESET);
        System.out.println(YELLOW + "  Then restart the application.\n" + RESET);
    }

    /**
     * Displays a help guide with supported file categories.
     */
    private void showHelp() {
        System.out.println("\n" + CYAN + "  ── HELP / SUPPORTED CATEGORIES ──" + RESET);
        System.out.println();
        System.out.printf("  %-12s  %s%n", "Category",   "Extensions");
        System.out.println("  " + "-".repeat(55));
        System.out.printf("  %-12s  %s%n", "Images",     ".jpg .jpeg .png .gif .bmp .webp .svg");
        System.out.printf("  %-12s  %s%n", "Documents",  ".pdf .docx .txt .pptx .xlsx .csv .md");
        System.out.printf("  %-12s  %s%n", "Videos",     ".mp4 .mkv .avi .mov .wmv .flv");
        System.out.printf("  %-12s  %s%n", "Audio",      ".mp3 .wav .aac .flac .ogg .wma");
        System.out.printf("  %-12s  %s%n", "Archives",   ".zip .rar .tar .gz .7z .bz2");
        System.out.printf("  %-12s  %s%n", "Code",       ".java .py .js .html .css .cpp .go");
        System.out.printf("  %-12s  %s%n", "Others",     "everything else");
        System.out.println();
    }

    // =============================================
    // UI Helpers
    // =============================================

    /**
     * Prints the welcome banner.
     */
    private void printBanner() {
        System.out.println(PURPLE + BOLD);
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║        📂  FILE ORGANIZER TOOL  v1.0            ║");
        System.out.println("  ║    Organize your files effortlessly with Java   ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    /**
     * Prints the main menu options.
     */
    private void printMainMenu() {
        System.out.println(BLUE + "  ┌─────────────────────────────────┐" + RESET);
        System.out.println(BLUE + "  │         MAIN MENU               │" + RESET);
        System.out.println(BLUE + "  ├─────────────────────────────────┤" + RESET);
        System.out.println(BLUE + "  │" + RESET + GREEN  + "  [1]  Organize Directory        " + BLUE + "│" + RESET);
        System.out.println(BLUE + "  │" + RESET + CYAN   + "  [2]  Dry Run (Preview Only)    " + BLUE + "│" + RESET);
        System.out.println(BLUE + "  │" + RESET + YELLOW + "  [3]  Undo Last Operation       " + BLUE + "│" + RESET);
        System.out.println(BLUE + "  │" + RESET + PURPLE + "  [4]  Settings                  " + BLUE + "│" + RESET);
        System.out.println(BLUE + "  │" + RESET + "  [5]  Help / File Categories    " + BLUE + "│" + RESET);
        System.out.println(BLUE + "  │" + RESET + RED    + "  [0]  Exit                      " + BLUE + "│" + RESET);
        System.out.println(BLUE + "  └─────────────────────────────────┘" + RESET);
    }

    /**
     * Reads a line of user input with a displayed prompt.
     *
     * @param prompt the prompt to display
     * @return the trimmed user input string
     */
    private String readInput(String prompt) {
        System.out.print(CYAN + "\n  ➤ " + prompt + ": " + RESET);
        return input.hasNextLine() ? input.nextLine() : "";
    }
}
