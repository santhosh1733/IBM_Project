package com.parabank.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads key-value pairs from config.properties.
 * Single shared instance so every module/page/test reads the same config.
 *
 * Loaded via the classpath (not a filesystem path) so it works no matter
 * where the JVM's working directory is -- Eclipse's TestNG launcher, `mvn
 * test` from the CLI, and CI runners can all have a different working
 * directory, but Maven always puts src/test/resources on the classpath.
 */
public class ConfigReader {

    private static Properties properties;
    private static final String CONFIG_FILE = "config.properties";

    static {
        try (InputStream is = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new RuntimeException(
                        "Unable to find " + CONFIG_FILE + " on the classpath. "
                        + "Confirm it's at src/test/resources/" + CONFIG_FILE
                        + " and that the project has been Maven-built at least once "
                        + "(right-click project > Maven > Update Project).");
            }
            properties = new Properties();
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load " + CONFIG_FILE, e);
        }
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in config.properties");
        }
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /**
     * Resolves a value that's a classpath-relative resource (e.g.
     * "testdata/TestData.xlsx" living under src/test/resources) into an
     * absolute filesystem path. Use this for any config value that needs to
     * be opened via FileInputStream/FileOutputStream (like Apache POI does),
     * since those need a real file path, not a classpath lookup -- but we
     * still want to avoid hardcoding a working-directory-dependent path.
     */
    public static String getResourcePath(String key) {
        String relative = get(key);
        java.net.URL url = ConfigReader.class.getClassLoader().getResource(relative);
        if (url == null) {
            throw new RuntimeException(
                    "Unable to find resource '" + relative + "' on the classpath for config key '"
                    + key + "'. Confirm the file exists under src/test/resources/ and the project "
                    + "has been Maven-built at least once (right-click project > Maven > Update Project).");
        }
        try {
            return java.nio.file.Paths.get(url.toURI()).toString();
        } catch (java.net.URISyntaxException e) {
            throw new RuntimeException("Malformed resource URL for '" + relative + "'", e);
        }
    }
}
