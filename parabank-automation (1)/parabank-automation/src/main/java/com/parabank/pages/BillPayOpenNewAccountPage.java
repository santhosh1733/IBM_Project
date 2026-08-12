package com.parabank.pages;

import com.parabank.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

/**
 * M4-only page object for the Bill Pay E2E scenario that creates an account and
 * immediately uses that account as the Bill Pay source.
 *
 * The shared teammate OpenNewAccountPage remains untouched.
 */
public class BillPayOpenNewAccountPage extends BasePage {

    @FindBy(id = "type")
    private WebElement accountTypeDropdown;

    @FindBy(id = "fromAccountId")
    private WebElement fundingAccountDropdown;

    @FindBy(css = "input.button[value='Open New Account']")
    private WebElement openAccountButton;

    @FindBy(id = "newAccountId")
    private WebElement newAccountIdText;

    @FindBy(linkText = "Accounts Overview")
    private WebElement accountsOverviewLink;

    public BillPayOpenNewAccountPage(WebDriver driver) {
        super(driver);
    }

    public BillPayOpenNewAccountPage openAccount(String accountType, String fundingAccountId) {
        WaitUtils.waitForVisible(driver, accountTypeDropdown);
        new Select(accountTypeDropdown).selectByVisibleText(accountType);

        WaitUtils.waitForVisible(driver, fundingAccountDropdown);
        new Select(fundingAccountDropdown).selectByVisibleText(fundingAccountId);

        click(openAccountButton);
        WaitUtils.waitForVisible(driver, By.id("newAccountId"));
        return this;
    }

    public String getNewAccountId() {
        return getText(newAccountIdText);
    }

    public BillPayAccountOverviewPage goToAccountsOverview() {
        click(accountsOverviewLink);
        return new BillPayAccountOverviewPage(driver).waitUntilLoaded();
    }
}
