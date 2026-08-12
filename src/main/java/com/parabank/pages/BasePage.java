package com.parabank.pages;

import com.parabank.utils.WaitUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

/**
 * Every Page Object (LoginPage, RequestLoanPage, TransferFundsPage, ...)
 * extends this. Provides common actions so individual page classes stay
 * focused on locators + business methods, not raw Selenium calls.
 */
public abstract class BasePage {

    protected WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    protected void click(WebElement element) {
        WaitUtils.waitForClickable(driver, element);
        element.click();
    }

    protected void type(WebElement element, String text) {
        WaitUtils.waitForVisible(driver, element);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(WebElement element) {
        WaitUtils.waitForVisible(driver, element);
        return element.getText().trim();
    }

    protected boolean isDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
