package com.fileorganizer.core;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FileScanner.
 *
 * Uses JUnit 5's @TempDir to create a temporary directory
 * with test files and validates the scan results.
 */
class FileScannerTest {

    @TempDir
    Path tempDir;

    private FileScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new FileScanner(false); // Non-recursive by default
    }

    @Test
    @DisplayName("Scanner should find all files in a flat directory")
    void testScanFlatDirectory() throws IOException {
        // Create test files
        Files.createFile(tempDir.resolve("photo.jpg"));
        Files.createFile(tempDir.resolve("report.pdf"));
        Files.createFile(tempDir.resolve("video.mp4"));

        List<Path> files = scanner.scan(tempDir);
        assertEquals(3, files.size(), "Should find exactly 3 files");
    }

    @Test
    @DisplayName("Scanner should return empty list for empty directory")
    void testScanEmptyDirectory() throws IOException {
        List<Path> files = scanner.scan(tempDir);
        assertTrue(files.isEmpty(), "Empty directory should return empty list");
    }

    @Test
    @DisplayName("Scanner should not include subdirectory files in non-recursive mode")
    void testNonRecursiveIgnoresSubdirs() throws IOException {
        // Create a file in a subdirectory
        Path subDir = Files.createDirectory(tempDir.resolve("subFolder"));
        Files.createFile(subDir.resolve("hidden.txt"));
        Files.createFile(tempDir.resolve("visible.txt"));

        FileScanner shallowScanner = new FileScanner(false);
        List<Path> files = shallowScanner.scan(tempDir);

        assertEquals(1, files.size(), "Non-recursive scan should find only 1 file");
        assertTrue(files.get(0).getFileName().toString().equals("visible.txt"));
    }

    @Test
    @DisplayName("Recursive scanner should find files in subdirectories")
    void testRecursiveScan() throws IOException {
        Path subDir = Files.createDirectory(tempDir.resolve("nested"));
        Files.createFile(tempDir.resolve("root.txt"));
        Files.createFile(subDir.resolve("child.txt"));

        FileScanner recursiveScanner = new FileScanner(true);
        List<Path> files = recursiveScanner.scan(tempDir);

        assertEquals(2, files.size(), "Recursive scan should find 2 files");
    }

    @Test
    @DisplayName("Scanner should throw IllegalArgumentException for non-existent path")
    void testScanNonExistentDirectory() {
        Path fakePath = Path.of("/non/existent/path");
        assertThrows(IllegalArgumentException.class, () -> scanner.scan(fakePath));
    }

    @Test
    @DisplayName("Scanner should throw IllegalArgumentException for a file (not directory)")
    void testScanFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("aFile.txt"));
        assertThrows(IllegalArgumentException.class, () -> scanner.scan(file));
    }

    @Test
    @DisplayName("Scanner should skip known category folders")
    void testSkipsCategoryFolders() throws IOException {
        // Simulate an already-organized state
        Path imagesDir = Files.createDirectory(tempDir.resolve("Images"));
        Files.createFile(imagesDir.resolve("photo.jpg")); // Already organized
        Files.createFile(tempDir.resolve("newfile.txt")); // New unorganized file

        FileScanner recursiveScanner = new FileScanner(true);
        List<Path> files = recursiveScanner.scan(tempDir);

        // Should only find newfile.txt, not photo.jpg inside Images/
        assertEquals(1, files.size(), "Should skip files inside category folders");
        assertEquals("newfile.txt", files.get(0).getFileName().toString());
    }
}
