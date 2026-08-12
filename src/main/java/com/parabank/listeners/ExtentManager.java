package com.parabank.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.parabank.utils.ConfigReader;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Owns the single ExtentReports instance for the whole run, plus a
 * ThreadLocal<ExtentTest> so parallel="classes" execution in testng.xml
 * doesn't cross-wire log entries between modules running at the same time.
 *
 * Don't call `new ExtentReports()` anywhere else in the framework -- always
 * go through getReportInstance() so every module writes into the same
 * report file.
 */
public class ExtentManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static synchronized ExtentReports getReportInstance() {
        if (extent == null) {
            String reportPath = ConfigReader.get("report.path");
            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setDocumentTitle("ParaBank UI Automation Report");
            spark.config().setReportName("ParaBank Regression Suite - "
                    + new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date()));

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Application", "ParaBank");
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("Browser", ConfigReader.get("browser"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }

    public static void startTest(String testName, String description) {
        ExtentTest extentTest = getReportInstance().createTest(testName, description);
        test.set(extentTest);
    }

    /** Assigns a category/tag to the current test, e.g. module name, for filtering in the report UI. */
    public static void assignCategory(String... categories) {
        getTest().assignCategory(categories);
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void unload() {
        test.remove();
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
