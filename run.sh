#!/bin/bash
# ============================================================
#  File Organizer Tool — Linux/macOS Build & Run Script
# ============================================================

echo ""
echo " Building File Organizer Tool..."
echo ""

# Build the fat JAR using Maven
mvn clean package -q

if [ $? -ne 0 ]; then
    echo " [ERROR] Build failed! Please check your Maven installation."
    exit 1
fi

echo " Build successful! Starting application..."
echo ""

# Run the packaged JAR
java -jar target/file-organizer.jar
