package com.parabank.tests;

import com.parabank.base.DriverFactory;
import com.parabank.listeners.TestListener;
import com.parabank.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Every test class (RequestLoanTest, TransferFundsTest, BillPayTest, etc.)
 * extends this. It handles:
 *   - fresh WebDriver per test method (thread-safe, supports parallel runs)
 *   - navigation to the app URL before each test
 *   - screenshot-on-failure (saved as a file, for CI artifact upload)
 *   - driver teardown after each test
 *
 * @Listeners(TestListener.class) is declared here rather than only in
 * testng.xml so the Extent report + logging still work even if someone runs
 * a class directly (e.g. `mvn test -Dtest=RequestLoanTest`, or via IDE)
 * without going through the suite XML.
 *
 * Team members should NOT create their own driver init/quit logic in their
 * test classes -- keep that here so it stays consistent across modules.
 */
@Listeners(TestListener.class)
public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverFactory.initDriver();
        driver = DriverFactory.getDriver();
        driver.get(ConfigReader.get("url"));
        logger.info("Browser launched and navigated to: " + ConfigReader.get("url"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            // File-based screenshot for CI artifact upload / local debugging.
            // TestListener (registered above) separately embeds a base64
            // screenshot directly into the Extent HTML report -- this is not
            // duplicate effort, just two different consumption points.
            takeScreenshot(result.getName());
            logger.error("Test FAILED: " + result.getName());
        }
        DriverFactory.quitDriver();
    }

    private void takeScreenshot(String testName) {
        try {
            String dir = "test-output/screenshots";
            Files.createDirectories(Paths.get(dir));

            org.openqa.selenium.TakesScreenshot ts = (org.openqa.selenium.TakesScreenshot) driver;
            File src = ts.getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            String path = dir + "/" + testName + "_" + System.currentTimeMillis() + ".png";

            try (FileOutputStream fos = new FileOutputStream(path)) {
                fos.write(Files.readAllBytes(src.toPath()));
            }
            logger.info("Screenshot saved: " + path);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: " + e.getMessage());
        }
    }
}
