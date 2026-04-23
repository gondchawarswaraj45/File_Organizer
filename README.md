# 📂 File Organizer Tool

> A professional-grade Java CLI application that automatically organizes files in any directory into categorized folders based on file extensions.

[![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-blue?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

---

## 📋 Table of Contents

- [Project Description](#-project-description)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [How to Run](#-how-to-run)
- [Example Usage](#-example-usage)
- [Supported Categories](#-supported-file-categories)
- [Configuration](#-configuration)
- [Sample Output](#-sample-output)
- [Running Tests](#-running-tests)
- [Future Improvements](#-future-improvements)

---

## 📖 Project Description

The **File Organizer Tool** is a Java-based command-line application that scans a user-specified directory and automatically moves files into organized subfolders based on their file type. It supports dry-run previews, duplicate detection, undo functionality, and persistent logging.

Built with **Java 17**, **Java NIO**, and clean **OOP principles**, this tool is modular, testable, and production-ready.

---

## ✨ Features

### Core Features
| Feature | Description |
|--------|-------------|
| 📁 Auto-categorization | Organizes files into Images, Documents, Videos, Audio, Archives, Code, and Others |
| 🗂 Folder creation | Creates category subfolders automatically if they don't exist |
| 🔄 File moving | Uses Java NIO `Files.move()` for safe, atomic file moves |

### Advanced Features
| Feature | Description |
|--------|-------------|
| 👁 Dry Run Mode | Preview what would happen without actually moving any files |
| 🔁 Undo Operation | Reverse the last organize run and restore files to original locations |
| 🔍 Duplicate Detection | Detects duplicates by name + file size; skips them to avoid overwriting |
| 📝 Logging System | All operations logged to `logs/file_organizer.log` with timestamps |
| 🌐 Recursive Scan | Optionally scan subdirectories (configurable in `organizer.properties`) |
| ⚙️ Configuration File | Behavior controlled via `config/organizer.properties` |
| ♻️ Name Conflict Resolution | Renames conflicting files as `file (1).ext`, `file (2).ext`, etc. |

### UI & Quality
- 🎨 Colored ANSI terminal output
- 📊 Operation summary after each run
- ✅ 17+ JUnit 5 unit & integration tests

---

## 📁 Project Structure

```
File_organizer/
├── src/
│   ├── main/
│   │   └── java/com/fileorganizer/
│   │       ├── Main.java                        ← Application entry point
│   │       ├── cli/
│   │       │   └── CLIHandler.java              ← Interactive CLI menu
│   │       ├── core/
│   │       │   ├── FileScanner.java             ← Directory scanner
│   │       │   ├── FileCategorizer.java         ← Extension → Category mapper
│   │       │   ├── FileMover.java               ← File move engine
│   │       │   ├── OrganizerEngine.java         ← Orchestrates the pipeline
│   │       │   └── UndoManager.java             ← Undo session manager
│   │       ├── model/
│   │       │   ├── FileCategory.java            ← Enum of categories + extensions
│   │       │   ├── FileOperation.java           ← Record of a single file move
│   │       │   └── OrganizeSummary.java         ← Stats collector + printer
│   │       └── util/
│   │           ├── LoggerUtility.java           ← Singleton logger (console + file)
│   │           └── ConfigManager.java           ← Properties file manager
│   └── test/
│       └── java/com/fileorganizer/core/
│           ├── FileCategoryTest.java            ← 17 unit tests for categorizer
│           └── FileScannerTest.java             ← 7 integration tests for scanner
├── config/
│   └── organizer.properties                    ← Application configuration
├── logs/                                       ← Generated log files (auto-created)
├── pom.xml                                     ← Maven build file
├── run.bat                                     ← Windows one-click run script
├── run.sh                                      ← Linux/macOS run script
└── README.md
```

---

## 🚀 How to Run

### Prerequisites

| Requirement | Version |
|------------|---------|
| Java JDK   | 17 or above |
| Apache Maven | 3.8 or above |

### Option 1: One-Click Script (Recommended)

**Windows:**
```cmd
run.bat
```

**Linux / macOS:**
```bash
chmod +x run.sh
./run.sh
```

### Option 2: Manual Maven Commands

```bash
# Step 1: Build the fat JAR
mvn clean package

# Step 2: Run the application
java -jar target/file-organizer.jar
```

### Option 3: Run Without Building (Development)

```bash
mvn exec:java -Dexec.mainClass="com.fileorganizer.Main"
```

---

## 💡 Example Usage

### Scenario: Organize a messy Downloads folder

**Before:**
```
Downloads/
├── vacation_photo.jpg
├── project_report.pdf
├── tutorial.mp4
├── song.mp3
├── backup.zip
├── script.py
└── random_file.dat
```

**Run the tool:**
```
  ➤ Enter your choice: 1
  ➤ Enter the full path of the directory to organize: C:\Users\John\Downloads
  ➤ This will MOVE files. Proceed? (yes/no): yes
```

**After:**
```
Downloads/
├── Images/
│   └── vacation_photo.jpg
├── Documents/
│   └── project_report.pdf
├── Videos/
│   └── tutorial.mp4
├── Audio/
│   └── song.mp3
├── Archives/
│   └── backup.zip
├── Code/
│   └── script.py
└── Others/
    └── random_file.dat
```

---

## 📂 Supported File Categories

| Category   | Extensions |
|-----------|-----------|
| 🖼 Images    | `.jpg` `.jpeg` `.png` `.gif` `.bmp` `.tiff` `.webp` `.svg` `.ico` |
| 📄 Documents | `.pdf` `.docx` `.doc` `.txt` `.pptx` `.xlsx` `.csv` `.rtf` `.md` |
| 🎬 Videos    | `.mp4` `.mkv` `.avi` `.mov` `.wmv` `.flv` `.webm` `.m4v` |
| 🎵 Audio     | `.mp3` `.wav` `.aac` `.flac` `.ogg` `.wma` `.m4a` `.aiff` |
| 🗜 Archives  | `.zip` `.rar` `.tar` `.gz` `.7z` `.bz2` `.xz` |
| 💻 Code      | `.java` `.py` `.js` `.ts` `.html` `.css` `.cpp` `.go` `.rb` `.php` `.sh` |
| 📦 Others    | everything else |

---

## ⚙️ Configuration

Edit `config/organizer.properties` to customize behavior:

```properties
# Scan subdirectories recursively (true/false)
recursive.scan=false

# Enable debug-level logging in the console (true/false)
debug.enabled=false

# Detect duplicate files before moving (true/false)
duplicate.detect=true

# Directory where log files are saved
log.directory=logs
```

> Restart the application after making changes.

---

## 📊 Sample Output

```
  ╔══════════════════════════════════════════════════╗
  ║        📂  FILE ORGANIZER TOOL  v1.0            ║
  ║    Organize your files effortlessly with Java   ║
  ╚══════════════════════════════════════════════════╝

  ┌─────────────────────────────────┐
  │         MAIN MENU               │
  ├─────────────────────────────────┤
  │  [1]  Organize Directory        │
  │  [2]  Dry Run (Preview Only)    │
  │  [3]  Undo Last Operation       │
  │  [4]  Settings                  │
  │  [5]  Help / File Categories    │
  │  [0]  Exit                      │
  └─────────────────────────────────┘

  ────────────────────────────────────────────────────────────
  [2026-04-24 00:15:30] [INFO ] Scanning directory: C:\Downloads
  [2026-04-24 00:15:30] [INFO ] Total files found: 7
  [2026-04-24 00:15:30] [INFO ] ✔ Moved: vacation_photo.jpg ➜ Images/
  [2026-04-24 00:15:30] [INFO ] ✔ Moved: project_report.pdf ➜ Documents/
  [2026-04-24 00:15:30] [WARN ] ⚠ Duplicate detected: song.mp3 already exists in Audio/
  ────────────────────────────────────────────────────────────

  =======================================================
     📊  OPERATION SUMMARY
  =======================================================
    Total Files Scanned            : 7
    Files Moved                    : 6
    Files Skipped (Duplicates)     : 1
    Duplicates Detected            : 1
  -------------------------------------------------------
    Files Per Category:
      📁 Images                    : 1
      📁 Documents                 : 1
      📁 Videos                    : 1
      📁 Audio                     : 1
      📁 Archives                  : 1
      📁 Code                      : 1
  =======================================================
```

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=FileCategoryTest
mvn test -Dtest=FileScannerTest
```

**Test Coverage:**

| Test Class | Tests | Description |
|-----------|-------|-------------|
| `FileCategoryTest` | 17 | All extension mappings, case-insensitivity, edge cases |
| `FileScannerTest` | 7 | Flat/recursive scan, empty dir, category folder skipping |

---

## 🔮 Future Improvements

| Priority | Feature |
|---------|---------|
| 🔴 High | **GUI version** using Java Swing with drag-and-drop directory selection |
| 🔴 High | **Regex-based custom rules** (e.g., files matching `*_2024*` → `Archive_2024/`) |
| 🟡 Medium | **Scheduled auto-organize** using a cron job or Java ScheduledExecutorService |
| 🟡 Medium | **File hash-based duplicate detection** (MD5/SHA-256) for more accuracy |
| 🟡 Medium | **Watch mode** — monitor a directory and organize files as they are added |
| 🟢 Low | **REST API mode** — expose organize functionality over HTTP (Spring Boot) |
| 🟢 Low | **JSON config support** — replace `.properties` with a richer JSON format |
| 🟢 Low | **Multi-language CLI** — support for i18n (English, Spanish, Hindi, etc.) |
| 🟢 Low | **Undo history browser** — view all past sessions and selectively restore |

---

## 📜 License

This project is licensed under the **MIT License** — free to use, modify, and distribute.

---

> Built with ❤️ using **Java 17** | **Java NIO** | **Maven** | **JUnit 5**
