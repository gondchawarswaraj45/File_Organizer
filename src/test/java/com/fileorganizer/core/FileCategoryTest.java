package com.fileorganizer.core;

import com.fileorganizer.model.FileCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileCategorizer.
 *
 * Validates that file extensions are correctly mapped to their categories.
 */
class FileCategoryTest {

    private FileCategorizer categorizer;

    @BeforeEach
    void setUp() {
        categorizer = new FileCategorizer();
    }

    @Test
    @DisplayName("JPEG file should be categorized as IMAGES")
    void testJpegIsImage() {
        Path file = Path.of("photo.jpg");
        assertEquals(FileCategory.IMAGES, categorizer.categorize(file));
    }

    @Test
    @DisplayName("PNG file should be categorized as IMAGES")
    void testPngIsImage() {
        Path file = Path.of("diagram.png");
        assertEquals(FileCategory.IMAGES, categorizer.categorize(file));
    }

    @Test
    @DisplayName("PDF file should be categorized as DOCUMENTS")
    void testPdfIsDocument() {
        Path file = Path.of("report.pdf");
        assertEquals(FileCategory.DOCUMENTS, categorizer.categorize(file));
    }

    @Test
    @DisplayName("DOCX file should be categorized as DOCUMENTS")
    void testDocxIsDocument() {
        Path file = Path.of("resume.docx");
        assertEquals(FileCategory.DOCUMENTS, categorizer.categorize(file));
    }

    @Test
    @DisplayName("MP4 file should be categorized as VIDEOS")
    void testMp4IsVideo() {
        Path file = Path.of("movie.mp4");
        assertEquals(FileCategory.VIDEOS, categorizer.categorize(file));
    }

    @Test
    @DisplayName("MKV file should be categorized as VIDEOS")
    void testMkvIsVideo() {
        Path file = Path.of("lecture.mkv");
        assertEquals(FileCategory.VIDEOS, categorizer.categorize(file));
    }

    @Test
    @DisplayName("MP3 file should be categorized as AUDIO")
    void testMp3IsAudio() {
        Path file = Path.of("song.mp3");
        assertEquals(FileCategory.AUDIO, categorizer.categorize(file));
    }

    @Test
    @DisplayName("WAV file should be categorized as AUDIO")
    void testWavIsAudio() {
        Path file = Path.of("sound_effect.wav");
        assertEquals(FileCategory.AUDIO, categorizer.categorize(file));
    }

    @Test
    @DisplayName("ZIP file should be categorized as ARCHIVES")
    void testZipIsArchive() {
        Path file = Path.of("project.zip");
        assertEquals(FileCategory.ARCHIVES, categorizer.categorize(file));
    }

    @Test
    @DisplayName("RAR file should be categorized as ARCHIVES")
    void testRarIsArchive() {
        Path file = Path.of("backup.rar");
        assertEquals(FileCategory.ARCHIVES, categorizer.categorize(file));
    }

    @Test
    @DisplayName("Java file should be categorized as CODE")
    void testJavaIsCode() {
        Path file = Path.of("Main.java");
        assertEquals(FileCategory.CODE, categorizer.categorize(file));
    }

    @Test
    @DisplayName("Python file should be categorized as CODE")
    void testPyIsCode() {
        Path file = Path.of("script.py");
        assertEquals(FileCategory.CODE, categorizer.categorize(file));
    }

    @Test
    @DisplayName("Unknown extension should be categorized as OTHERS")
    void testUnknownExtensionIsOthers() {
        Path file = Path.of("data.xyz123");
        assertEquals(FileCategory.OTHERS, categorizer.categorize(file));
    }

    @Test
    @DisplayName("File with no extension should be categorized as OTHERS")
    void testNoExtensionIsOthers() {
        Path file = Path.of("Makefile");
        assertEquals(FileCategory.OTHERS, categorizer.categorize(file));
    }

    @Test
    @DisplayName("Extension matching should be case-insensitive")
    void testCaseInsensitiveExtension() {
        Path file1 = Path.of("Photo.JPG");
        Path file2 = Path.of("Report.PDF");
        Path file3 = Path.of("Video.MP4");

        assertEquals(FileCategory.IMAGES,    categorizer.categorize(file1));
        assertEquals(FileCategory.DOCUMENTS, categorizer.categorize(file2));
        assertEquals(FileCategory.VIDEOS,    categorizer.categorize(file3));
    }

    @Test
    @DisplayName("TXT file should be categorized as DOCUMENTS")
    void testTxtIsDocument() {
        Path file = Path.of("notes.txt");
        assertEquals(FileCategory.DOCUMENTS, categorizer.categorize(file));
    }

    @Test
    @DisplayName("XLSX file should be categorized as DOCUMENTS")
    void testXlsxIsDocument() {
        Path file = Path.of("budget.xlsx");
        assertEquals(FileCategory.DOCUMENTS, categorizer.categorize(file));
    }
}
