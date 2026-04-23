@echo off
REM ============================================================
REM  File Organizer Tool — Windows Build & Run Script
REM ============================================================

REM Enable UTF-8 output for proper Unicode/box-drawing characters
chcp 65001 > nul

SET JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.16.8-hotspot
SET JAVAC=%JAVA_HOME%\bin\javac.exe
SET JAVA=%JAVA_HOME%\bin\java.exe
SET JAR_TOOL=%JAVA_HOME%\bin\jar.exe

echo.
echo  Building File Organizer Tool...
echo.

REM Compile all source files
IF NOT EXIST "out\classes" mkdir "out\classes"
IF NOT EXIST "out\META-INF" mkdir "out\META-INF"

REM Collect sources into a list file
dir /s /b src\main\java\*.java > sources.txt

"%JAVAC%" -encoding UTF-8 -d "out\classes" @sources.txt
del sources.txt

IF %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] Compilation failed!
    pause
    exit /b 1
)

REM Create manifest
echo Manifest-Version: 1.0> out\META-INF\MANIFEST.MF
echo Main-Class: com.fileorganizer.Main>> out\META-INF\MANIFEST.MF

REM Package JAR
"%JAR_TOOL%" --create --file "file-organizer.jar" --manifest "out\META-INF\MANIFEST.MF" -C "out\classes" .

IF %ERRORLEVEL% NEQ 0 (
    echo  [ERROR] JAR creation failed!
    pause
    exit /b 1
)

echo  Build successful! Starting File Organizer Tool...
echo.

REM Run the application
"%JAVA%" -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar file-organizer.jar

pause
