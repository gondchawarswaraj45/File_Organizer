package com.fileorganizer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * ConfigManager — Loads and saves application configuration from a .properties file.
 *
 * Configuration file: config/organizer.properties
 *
 * Supported keys:
 *   - recursive.scan    : true/false — scan subdirectories
 *   - debug.enabled     : true/false — enable debug logging
 *   - duplicate.detect  : true/false — enable duplicate detection
 *   - log.directory     : path to the log output directory
 */
public class ConfigManager {

    private static final String CONFIG_FILE = "config/organizer.properties";
    private final Properties properties;
    private final LoggerUtility logger = LoggerUtility.getInstance();

    /**
     * Constructor. Loads config from file, or uses defaults if file doesn't exist.
     */
    public ConfigManager() {
        this.properties = new Properties();
        loadDefaults();
        loadFromFile();
    }

    /**
     * Sets hardcoded default values for all configuration keys.
     */
    private void loadDefaults() {
        properties.setProperty("recursive.scan",   "false");
        properties.setProperty("debug.enabled",    "false");
        properties.setProperty("duplicate.detect", "true");
        properties.setProperty("log.directory",    "logs");
    }

    /**
     * Attempts to load the properties file from disk.
     * If it doesn't exist, creates it with default values.
     */
    private void loadFromFile() {
        Path configPath = Path.of(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                properties.load(in);
                logger.debug("Configuration loaded from: " + CONFIG_FILE);
            } catch (IOException e) {
                logger.warn("Could not read config file. Using defaults. Error: " + e.getMessage());
            }
        } else {
            // Create default config file
            saveToFile();
            logger.info("Default configuration file created at: " + CONFIG_FILE);
        }
    }

    /**
     * Saves the current properties to the config file.
     */
    public void saveToFile() {
        Path configPath = Path.of(CONFIG_FILE);
        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream out = Files.newOutputStream(configPath)) {
                properties.store(out, "File Organizer Tool — Configuration");
            }
        } catch (IOException e) {
            logger.error("Failed to save config file: " + e.getMessage());
        }
    }

    // =============================================
    // Config Accessors
    // =============================================

    /**
     * Returns whether recursive directory scanning is enabled.
     *
     * @return true if recursive scanning is on
     */
    public boolean isRecursiveScan() {
        return Boolean.parseBoolean(properties.getProperty("recursive.scan", "false"));
    }

    /**
     * Returns whether debug logging is enabled.
     *
     * @return true if debug mode is on
     */
    public boolean isDebugEnabled() {
        return Boolean.parseBoolean(properties.getProperty("debug.enabled", "false"));
    }

    /**
     * Returns whether duplicate file detection is enabled.
     *
     * @return true if duplicate detection is on
     */
    public boolean isDuplicateDetectEnabled() {
        return Boolean.parseBoolean(properties.getProperty("duplicate.detect", "true"));
    }

    /**
     * Returns the directory path where log files should be saved.
     *
     * @return log directory as a string
     */
    public String getLogDirectory() {
        return properties.getProperty("log.directory", "logs");
    }

    /**
     * Returns the raw Properties object.
     *
     * @return the loaded Properties
     */
    public Properties getProperties() {
        return properties;
    }
}
