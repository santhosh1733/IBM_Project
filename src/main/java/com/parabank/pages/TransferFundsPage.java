package com.parabank.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

/**
 * Transfer Funds page object (Owner: M3).
 * STARTER SCAFFOLD -- locators below are best-guess based on ParaBank's known
 * structure. Verify each one against the live DOM (right-click > Inspect)
 * before relying on them, and follow the RequestLoanPage pattern for any
 * additional methods you add.
 */
public class TransferFundsPage extends BasePage {

    @FindBy(id = "amount")
    private WebElement amountInput;

    @FindBy(id = "fromAccountId")
    private WebElement fromAccountDropdown;

    @FindBy(id = "toAccountId")
    private WebElement toAccountDropdown;

    @FindBy(css = "input.button[value='Transfer']")
    private WebElement transferButton;

    @FindBy(css = "#showResult h1")
    private WebElement confirmationHeading;

    @FindBy(id = "amountResult")
    private WebElement confirmedAmount;
    
    @FindBy(linkText = "Accounts Overview")
	private WebElement accountOverviewLink;
    
    public AccountOverviewPage clickAccountOverview() {

	    click(accountOverviewLink);

	    return new AccountOverviewPage(driver);
	}

    public TransferFundsPage(WebDriver driver) {
        super(driver);
    }

    public TransferFundsPage enterAmount(String amount) {
        type(amountInput, amount);
        return this;
    }

    public TransferFundsPage selectFromAccount(String accountId) {
        new Select(fromAccountDropdown).selectByVisibleText(accountId);
        return this;
    }

    public TransferFundsPage selectToAccount(String accountId) {
        new Select(toAccountDropdown).selectByVisibleText(accountId);
        return this;
    }

    public TransferFundsPage clickTransfer() {
        click(transferButton);
        return this;
    }

    public TransferFundsPage transfer(String amount, String fromAccountId, String toAccountId) {
        enterAmount(amount);
        selectFromAccount(fromAccountId);
        selectToAccount(toAccountId);
        clickTransfer();
        return this;
    }

    public boolean isTransferSuccessful() {
        return isDisplayed(confirmationHeading) && getText(confirmationHeading).contains("Complete");
    }

    public String getConfirmedAmount() {
        return getText(confirmedAmount);
    }

    // TODO (M3): add methods for negative-amount / insufficient-balance error checks
    // once you've inspected how ParaBank renders that validation message.
}
