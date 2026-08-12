package com.parabank.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Request Loan page object (Owner: M5).
 * This class is the FULLY BUILT reference implementation -- use this pattern
 * (locators via @FindBy, business-readable methods, no raw Selenium calls in
 * test classes) as the template for the other 4 modules.
 */
public class RequestLoanPage extends BasePage {

    @FindBy(id = "amount")
    private WebElement loanAmountInput;

    @FindBy(id = "downPayment")
    private WebElement downPaymentInput;

    @FindBy(id = "fromAccountId")
    private WebElement fromAccountDropdown;

    @FindBy(css = "input.button[value='Apply Now']")
    private WebElement applyNowButton;

    @FindBy(id = "loanStatus")
    private WebElement loanStatusText;

    @FindBy(id = "newAccountId")
    private WebElement newLoanAccountIdText;

    public RequestLoanPage(WebDriver driver) {
        super(driver);
    }

    public RequestLoanPage enterLoanAmount(String amount) {
        type(loanAmountInput, amount);
        return this;
    }

    public RequestLoanPage enterDownPayment(String amount) {
        type(downPaymentInput, amount);
        return this;
    }

    public RequestLoanPage selectFromAccount(String accountId) {
        org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(fromAccountDropdown);
        select.selectByVisibleText(accountId);
        return this;
    }

    public RequestLoanPage submitApplication() {
        click(applyNowButton);
        return this;
    }

    /** Full flow in one call -- handy for @Test methods that don't need step-by-step control. */
    public RequestLoanPage applyForLoan(String loanAmount, String downPayment, String fromAccountId) {
        enterLoanAmount(loanAmount);
        enterDownPayment(downPayment);
        selectFromAccount(fromAccountId);
        submitApplication();
        return this;
    }

    public String getLoanStatus() {
        return getText(loanStatusText);
    }

    public boolean isApproved() {
        return getLoanStatus().equalsIgnoreCase("Approved");
    }

    public boolean isDenied() {
        return getLoanStatus().equalsIgnoreCase("Denied");
    }

    public String getNewLoanAccountId() {
        return isDisplayed(newLoanAccountIdText) ? getText(newLoanAccountIdText) : null;
    }
}
