package com.parabank.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

/**
 * Open New Account page object (Owner: M2).
 * STARTER SCAFFOLD -- verify locators against the live DOM.
 */
public class OpenNewAccountPage extends BasePage {

    @FindBy(id = "type")
    private WebElement accountTypeDropdown;

    @FindBy(id = "fromAccountId")
    private WebElement fromAccountDropdown;

    @FindBy(css = "input.button[value='Open New Account']")
    private WebElement openAccountButton;

    @FindBy(id = "newAccountId")
    private WebElement newAccountIdText;

    public OpenNewAccountPage(WebDriver driver) {
        super(driver);
    }

    public OpenNewAccountPage selectAccountType(String type) {
        new Select(accountTypeDropdown).selectByVisibleText(type);
        return this;
    }

    public OpenNewAccountPage selectFundingAccount(String accountId) {
        new Select(fromAccountDropdown).selectByVisibleText(accountId);
        return this;
    }

    public OpenNewAccountPage clickOpenAccount() {
        click(openAccountButton);
        return this;
    }

    public String getNewAccountId() {
        return getText(newAccountIdText);
    }

    // TODO (M2): add openAccount(type, fundingAccountId) convenience method
    // once field IDs are confirmed against the live DOM.
}
