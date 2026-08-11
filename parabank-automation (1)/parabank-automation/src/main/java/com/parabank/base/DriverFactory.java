package com.parabank.base;

import com.parabank.utils.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and manages WebDriver instances.
 *
 * ThreadLocal is used so each test method/thread gets its own WebDriver
 * instance -- this is what makes parallel="methods"/"classes" execution in
 * testng.xml safe. Never make WebDriver a plain static field, or parallel
 * threads will fight over the same browser session.
 */
public class DriverFactory {

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void initDriver() {
        String browser = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");

        WebDriver webDriver;

        switch (browser) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOptions = new FirefoxOptions();
                if (headless) ffOptions.addArguments("-headless");
                webDriver = new FirefoxDriver(ffOptions);
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                webDriver = new EdgeDriver();
                break;

            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new");
                options.addArguments("--start-maximized");
                options.addArguments("--disable-notifications");

                // Chrome's "Change your password" / compromised-password warning is
                // browser UI, not a web-page alert, so Selenium cannot reliably
                // handle it with driver.switchTo().alert(). Prevent it at browser
                // startup by disabling password-manager and leak-detection features.
                Map<String, Object> chromePrefs = new HashMap<>();
                chromePrefs.put("credentials_enable_service",
                        ConfigReader.getBoolean("chrome.credentials.service.enabled"));
                chromePrefs.put("profile.password_manager_enabled",
                        ConfigReader.getBoolean("chrome.password.manager.enabled"));
                chromePrefs.put("profile.password_manager_leak_detection",
                        ConfigReader.getBoolean("chrome.password.leak.detection.enabled"));
                options.setExperimentalOption("prefs", chromePrefs);

                webDriver = new ChromeDriver(options);
                break;
        }

        webDriver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigReader.getInt("implicit.wait")));
        webDriver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(ConfigReader.getInt("page.load.timeout")));
        webDriver.manage().window().maximize();

        driver.set(webDriver);
    }

    public static void quitDriver() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
        }
    }
}
