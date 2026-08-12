package com.parabank.listeners;

import com.parabank.base.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import static com.aventstack.extentreports.Status.*;

/**
 * Central TestNG listener. Registered in testng.xml via <listeners>, so it
 * fires for every test class in the suite -- no need to add anything to
 * individual test classes beyond what BaseTest already does.
 *
 * Responsibilities:
 *   - starts/ends an ExtentTest entry per @Test method
 *   - logs pass/fail/skip status with the failure stack trace
 *   - embeds a screenshot (base64, so the report stays a single portable file)
 *     directly into the report on failure
 *   - flushes the report once after the whole suite finishes
 *
 * TestNG invokes these callbacks on the same thread that ran the test
 * method, so reading DriverFactory's ThreadLocal driver here is safe even
 * under parallel="classes" execution.
 */
public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        logger.info("===== Suite started: " + context.getName() + " =====");
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription() != null
                ? result.getMethod().getDescription() : testName;

        ExtentManager.startTest(testName, description);
        ExtentManager.assignCategory(result.getTestClass().getRealClass().getSimpleName());

        logger.info("Test started: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentManager.getTest().log(PASS, "Test passed");
        logger.info("Test PASSED: " + result.getMethod().getMethodName());
        ExtentManager.unload();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentManager.getTest().log(FAIL, result.getThrowable());

        String base64Screenshot = captureScreenshotBase64();
        if (base64Screenshot != null) {
            ExtentManager.getTest().addScreenCaptureFromBase64String(
                    base64Screenshot, result.getMethod().getMethodName());
        }

        logger.error("Test FAILED: " + result.getMethod().getMethodName(), result.getThrowable());
        ExtentManager.unload();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentManager.getTest().log(SKIP, result.getThrowable() != null
                ? result.getThrowable().getMessage() : "Test skipped");
        logger.warn("Test SKIPPED: " + result.getMethod().getMethodName());
        ExtentManager.unload();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
        logger.info("===== Suite finished: " + context.getName()
                + " | Passed: " + context.getPassedTests().size()
                + " | Failed: " + context.getFailedTests().size()
                + " | Skipped: " + context.getSkippedTests().size() + " =====");
    }

    private String captureScreenshotBase64() {
        try {
            WebDriver driver = DriverFactory.getDriver();
            if (driver == null) return null;
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            logger.error("Could not capture screenshot for report: " + e.getMessage());
            return null;
        }
    }
}
