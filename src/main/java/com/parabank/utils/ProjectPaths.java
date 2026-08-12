package com.parabank.utils;
import java.io.File;
import java.net.URISyntaxException;

public final class ProjectPaths {

    private static final String PROJECT_ROOT = resolveProjectRoot();
    private static final String OUTPUT_DIR = PROJECT_ROOT + File.separator + "test-output";

    static {
        new File(OUTPUT_DIR).mkdirs();
        new File(OUTPUT_DIR, "screenshots").mkdirs();
        new File(OUTPUT_DIR, "logs").mkdirs();

        // Makes the resolved logs folder available to log4j2.xml via
        // ${sys:LOG_DIR}, so file logging lands in the same test-output
        // folder as the report and screenshots. Must be set before Log4j2
        // is first used -- see the static init trigger in BaseTest and
        // TestListener.
        System.setProperty("LOG_DIR", OUTPUT_DIR + File.separator + "logs");
    }

    private ProjectPaths() {
    }

    private static String resolveProjectRoot() {
        try {
            File codeLocation = new File(
                    ProjectPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            // codeLocation is typically <project>/target/classes -- walk up
            // looking for pom.xml, capped so a misconfigured environment
            // can't walk forever.
            File dir = codeLocation;
            for (int i = 0; i < 6 && dir != null; i++) {
                if (new File(dir, "pom.xml").exists()) {
                    return dir.getAbsolutePath();
                }
                dir = dir.getParentFile();
            }
        } catch (URISyntaxException | NullPointerException | SecurityException ignored) {
            // fall through to the working-directory fallback below
        }
        // Fallback if pom.xml couldn't be located (e.g. running from a
        // packaged jar without the source tree present).
        return System.getProperty("user.dir");
    }

    public static String getOutputDir() {
        return OUTPUT_DIR;
    }

    public static String getScreenshotsDir() {
        return OUTPUT_DIR + File.separator + "screenshots";
    }

    public static String getLogsDir() {
        return OUTPUT_DIR + File.separator + "logs";
    }

    public static String getReportPath() {
        return OUTPUT_DIR + File.separator + "ExtentReport.html";
    }
}

