package com.fileorganizer.core;

import com.fileorganizer.model.FileOperation;
import com.fileorganizer.model.FileOperation.OperationType;
import com.fileorganizer.util.LoggerUtility;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * UndoManager — Records all file operations and supports reversing the last organize run.
 *
 * How it works:
 *   - After each organize session, the list of FileOperation records is pushed to a stack
 *   - Calling undo() pops the latest session and moves all files back to their original paths
 *   - Sessions are persisted in a hidden file (.undo_history) so undo works across JVM restarts
 */
public class UndoManager {

    private final LoggerUtility logger = LoggerUtility.getInstance();

    /** Stack of operation sessions; each session is a list of FileOperations */
    private final Stack<List<FileOperation>> sessionStack = new Stack<>();

    /** Path to the serialized undo history file */
    private static final String UNDO_HISTORY_FILE = ".undo_history";

    /**
     * Saves a completed organize session to the undo stack.
     * Only MOVED operations are recorded (skipped/duplicates can't be undone).
     *
     * @param operations the list of FileOperation objects from the latest session
     */
    public void saveSession(List<FileOperation> operations) {
        List<FileOperation> movedOps = operations.stream()
                .filter(op -> op.getOperationType() == OperationType.MOVED)
                .toList();

        if (!movedOps.isEmpty()) {
            sessionStack.push(new ArrayList<>(movedOps));
            logger.info("Undo session saved. " + movedOps.size() + " operations recorded.");
            persistHistory();
        } else {
            logger.info("No moves to record for undo.");
        }
    }

    /**
     * Undoes the most recent organize session.
     * Moves each file from its destination back to its original source path.
     *
     * @return true if undo was successful, false if no sessions available
     */
    public boolean undo() {
        if (sessionStack.isEmpty()) {
            logger.warn("Nothing to undo. No previous sessions found.");
            return false;
        }

        List<FileOperation> lastSession = sessionStack.pop();
        logger.info("Undoing last session: " + lastSession.size() + " file(s) to restore.");

        int successCount = 0;
        int failCount    = 0;

        // Process in reverse order (last moved is first restored)
        for (int i = lastSession.size() - 1; i >= 0; i--) {
            FileOperation op = lastSession.get(i);
            Path current = op.getDestinationPath();
            Path original = op.getSourcePath();

            try {
                if (!Files.exists(current)) {
                    logger.warn("File not found at destination, cannot restore: " + op.getFileName());
                    failCount++;
                    continue;
                }

                // Ensure the original parent directory still exists
                if (!Files.exists(original.getParent())) {
                    Files.createDirectories(original.getParent());
                }

                Files.move(current, original, StandardCopyOption.REPLACE_EXISTING);
                logger.info("↩ Restored: " + op.getFileName() + " ← " + current + " → " + original);
                successCount++;

            } catch (IOException e) {
                logger.error("Failed to restore: " + op.getFileName() + " | " + e.getMessage());
                failCount++;
            }
        }

        logger.info("Undo complete. Restored: " + successCount + " | Failed: " + failCount);
        persistHistory();
        return true;
    }

    /**
     * Checks whether there is a session available to undo.
     *
     * @return true if an undo session exists
     */
    public boolean canUndo() {
        return !sessionStack.isEmpty();
    }

    /**
     * Returns the number of saved undo sessions.
     *
     * @return count of undo sessions on the stack
     */
    public int sessionCount() {
        return sessionStack.size();
    }

    /**
     * Saves the current session stack to a binary file for persistence.
     * This allows undo to work across JVM restarts.
     */
    private void persistHistory() {
        Path historyPath = Paths.get(UNDO_HISTORY_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(historyPath))) {
            // Serialize session stack — FileOperation must implement Serializable
            // Here we store a lightweight representation (paths as strings)
            List<List<String[]>> serializable = new ArrayList<>();
            for (List<FileOperation> session : sessionStack) {
                List<String[]> sessionData = new ArrayList<>();
                for (FileOperation op : session) {
                    sessionData.add(new String[]{
                            op.getFileName(),
                            op.getSourcePath().toString(),
                            op.getDestinationPath().toString()
                    });
                }
                serializable.add(sessionData);
            }
            oos.writeObject(serializable);
            logger.debug("Undo history persisted to: " + UNDO_HISTORY_FILE);
        } catch (IOException e) {
            logger.warn("Could not persist undo history: " + e.getMessage());
        }
    }

    /**
     * Loads the undo history from disk (if available) and rebuilds the session stack.
     * Called on startup to restore undo capability across sessions.
     */
    @SuppressWarnings("unchecked")
    public void loadHistory() {
        Path historyPath = Paths.get(UNDO_HISTORY_FILE);
        if (!Files.exists(historyPath)) {
            logger.debug("No undo history file found. Starting fresh.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(historyPath))) {
            List<List<String[]>> serializable = (List<List<String[]>>) ois.readObject();
            sessionStack.clear();

            for (List<String[]> sessionData : serializable) {
                List<FileOperation> session = new ArrayList<>();
                for (String[] entry : sessionData) {
                    // Reconstruct FileOperation from stored paths
                    FileOperation op = new FileOperation(
                            entry[0],
                            Paths.get(entry[1]),
                            Paths.get(entry[2]),
                            null,
                            OperationType.MOVED
                    );
                    session.add(op);
                }
                sessionStack.push(session);
            }

            logger.info("Undo history loaded. " + sessionStack.size() + " session(s) available.");
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Could not load undo history: " + e.getMessage());
        }
    }
}
