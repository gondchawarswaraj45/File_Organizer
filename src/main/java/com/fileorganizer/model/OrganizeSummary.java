package com.fileorganizer.model;

/**
 * Holds the summary statistics of a completed organize operation.
 * Displayed to the user at the end of each run.
 */
public class OrganizeSummary {

    private int totalFilesScanned;
    private int totalFilesMoved;
    private int totalFilesSkipped;
    private int totalDuplicatesFound;

    // Per-category counters map (category name -> count)
    private final java.util.Map<String, Integer> categoryCountMap;

    /**
     * Default constructor initializing all counters.
     */
    public OrganizeSummary() {
        this.totalFilesScanned = 0;
        this.totalFilesMoved = 0;
        this.totalFilesSkipped = 0;
        this.totalDuplicatesFound = 0;
        this.categoryCountMap = new java.util.LinkedHashMap<>();
    }

    /**
     * Increments the count for a specific category.
     *
     * @param categoryName the category folder name
     */
    public void incrementCategory(String categoryName) {
        categoryCountMap.merge(categoryName, 1, Integer::sum);
    }

    /**
     * Prints a formatted summary to the console.
     *
     * @param isDryRun whether this was a dry run
     */
    public void printSummary(boolean isDryRun) {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("   📊  OPERATION SUMMARY" + (isDryRun ? " [DRY RUN]" : ""));
        System.out.println("=".repeat(55));
        System.out.printf("  %-30s : %d%n", "Total Files Scanned", totalFilesScanned);
        System.out.printf("  %-30s : %d%n", "Files " + (isDryRun ? "To Be Moved" : "Moved"), totalFilesMoved);
        System.out.printf("  %-30s : %d%n", "Files Skipped (Duplicates)", totalFilesSkipped);
        System.out.printf("  %-30s : %d%n", "Duplicates Detected", totalDuplicatesFound);
        System.out.println("-".repeat(55));
        System.out.println("  Files Per Category:");
        categoryCountMap.forEach((cat, count) ->
                System.out.printf("    %-28s : %d%n", "📁 " + cat, count));
        System.out.println("=".repeat(55));
    }

    // --- Getters & Setters ---

    public int getTotalFilesScanned() { return totalFilesScanned; }
    public void setTotalFilesScanned(int v) { this.totalFilesScanned = v; }

    public int getTotalFilesMoved() { return totalFilesMoved; }
    public void incrementMoved() { this.totalFilesMoved++; }

    public int getTotalFilesSkipped() { return totalFilesSkipped; }
    public void incrementSkipped() { this.totalFilesSkipped++; }

    public int getTotalDuplicatesFound() { return totalDuplicatesFound; }
    public void incrementDuplicates() { this.totalDuplicatesFound++; }

    public java.util.Map<String, Integer> getCategoryCountMap() { return categoryCountMap; }
}
