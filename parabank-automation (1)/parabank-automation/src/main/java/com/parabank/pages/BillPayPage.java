package com.parabank.pages;

import com.parabank.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.Locale;

/**
 * Bill Pay page object (Owner: M4).
 *
 * Locators in this class were aligned to the live ParaBank 2.0 DOM verified
 * during M4 development. Stable name/id attributes are preferred over the
 * generated UUID-style IDs used by the phone/email fields.
 */
public class BillPayPage extends BasePage {

    @FindBy(name = "payee.name")
    private WebElement payeeNameInput;

    @FindBy(name = "payee.address.street")
    private WebElement payeeStreetInput;

    @FindBy(name = "payee.address.city")
    private WebElement payeeCityInput;

    @FindBy(name = "payee.address.state")
    private WebElement payeeStateInput;

    @FindBy(name = "payee.address.zipCode")
    private WebElement payeeZipInput;

    @FindBy(name = "payee.contactInformation.phoneNumber")
    private WebElement payeePhoneInput;

    @FindBy(name = "payee.contactInformation.email")
    private WebElement payeeEmailInput;

    @FindBy(name = "payee.accountNumber")
    private WebElement payeeAccountInput;

    @FindBy(name = "verifyAccount")
    private WebElement verifyAccountInput;

    @FindBy(name = "amount")
    private WebElement amountInput;

    @FindBy(name = "fromAccountId")
    private WebElement fromAccountDropdown;

    @FindBy(css = "input.button[value='Send Payment']")
    private WebElement sendPaymentButton;

    // Successful bill-payment result. These IDs were visible in the live DOM.
    @FindBy(css = "#billpayResult h1")
    private WebElement confirmationHeading;

    @FindBy(id = "payeeName")
    private WebElement confirmedPayeeName;

    @FindBy(id = "amount")
    private WebElement confirmedAmount;

    @FindBy(id = "fromAccountId")
    private WebElement confirmedFromAccount;

    // Client-side validation confirmed from the live DOM.
    @FindBy(id = "validationModel-verifyAccount-mismatch")
    private WebElement accountMismatchError;

    // ParaBank creates more than one amount validation span (empty/invalid).
    // Keeping the locator generic lets us pick whichever one is visible.
    @FindBy(css = "span.error[id*='validationModel-amount']")
    private List<WebElement> amountValidationErrors;

    // Generic backend/AJAX error block used by Bill Pay.
    @FindBy(id = "billpayError")
    private WebElement billPayError;

    // Full-page application error used when the public demo backend fails.
    @FindBy(css = "#rightPanel p.error")
    private WebElement pageErrorMessage;

    @FindBy(linkText = "Accounts Overview")
    private WebElement accountsOverviewLink;

    public BillPayPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Fills every payee field. verifyAccountNumber is intentionally separate
     * so negative mismatch validation can be automated without raw Selenium in
     * the test class.
     */
    public BillPayPage fillPayeeDetails(String name, String street, String city, String state,
                                        String zip, String phone, String email,
                                        String accountNumber, String verifyAccountNumber) {
        type(payeeNameInput, name);
        type(payeeStreetInput, street);
        type(payeeCityInput, city);
        type(payeeStateInput, state);
        type(payeeZipInput, zip);
        type(payeePhoneInput, phone);
        type(payeeEmailInput, email);
        type(payeeAccountInput, accountNumber);
        type(verifyAccountInput, verifyAccountNumber);
        return this;
    }

    /** Backward-compatible convenience overload for matching account numbers. */
    public BillPayPage fillPayeeDetails(String name, String street, String city, String state,
                                        String zip, String phone, String accountNumber) {
        return fillPayeeDetails(name, street, city, state, zip, phone, "",
                accountNumber, accountNumber);
    }

    public BillPayPage enterAmount(String amount) {
        type(amountInput, amount);
        return this;
    }

    public BillPayPage leaveAmountBlank() {
        type(amountInput, "");
        return this;
    }

    public BillPayPage selectFromAccount(String accountId) {
        WaitUtils.waitForVisible(driver, fromAccountDropdown);
        new Select(fromAccountDropdown).selectByVisibleText(accountId);
        return this;
    }

    public BillPayPage waitUntilLoaded() {
        WaitUtils.waitForVisible(driver, payeeNameInput);
        WaitUtils.waitForVisible(driver, fromAccountDropdown);
        WaitUtils.waitForClickable(driver, sendPaymentButton);
        return this;
    }

    public boolean isCriticalBillPayFormDisplayed() {
        return isDisplayed(payeeNameInput)
                && isDisplayed(payeeAccountInput)
                && isDisplayed(verifyAccountInput)
                && isDisplayed(amountInput)
                && isDisplayed(fromAccountDropdown)
                && isDisplayed(sendPaymentButton);
    }

    public List<String> getFromAccountOptions() {
        waitUntilLoaded();
        Select select = new Select(fromAccountDropdown);
        return select.getOptions().stream()
                .map(option -> option.getText().trim())
                .filter(text -> !text.isBlank())
                .toList();
    }

    public String getSelectedFromAccount() {
        waitUntilLoaded();
        return new Select(fromAccountDropdown).getFirstSelectedOption().getText().trim();
    }

    /**
     * Returns every visible client-side validation message on the Bill Pay form.
     * The generic span.error lookup avoids coupling simple negative tests to
     * generated Angular validation IDs.
     */
    public List<String> getVisibleValidationMessages() {
        return driver.findElements(By.cssSelector("span.error")).stream()
                .filter(WebElement::isDisplayed)
                .map(element -> element.getText().trim())
                .filter(text -> !text.isBlank())
                .toList();
    }

    public boolean hasVisibleValidationContaining(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return false;
        }
        String expected = keyword.toLowerCase(Locale.ROOT);
        return getVisibleValidationMessages().stream()
                .map(text -> text.toLowerCase(Locale.ROOT))
                .anyMatch(text -> text.contains(expected));
    }

    public BillPayPage submitPayment() {
        click(sendPaymentButton);
        return this;
    }

    public boolean isPaymentSuccessful() {
        try {
            WaitUtils.waitForVisible(driver, By.cssSelector("#billpayResult h1"));
            return getText(confirmationHeading).contains("Bill Payment Complete");
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getConfirmationHeading() {
        return getText(confirmationHeading);
    }

    public String getConfirmedPayeeName() {
        return getText(confirmedPayeeName);
    }

    public String getConfirmedAmount() {
        return getText(confirmedAmount);
    }

    public String getConfirmedFromAccount() {
        return getText(confirmedFromAccount);
    }

    public boolean isAccountMismatchErrorDisplayed() {
        try {
            WaitUtils.waitForVisible(driver, By.id("validationModel-verifyAccount-mismatch"));
            return isDisplayed(accountMismatchError);
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getAccountMismatchErrorText() {
        return getText(accountMismatchError);
    }

    public boolean isAmountRequiredErrorDisplayed() {
        try {
            WaitUtils.waitForVisible(driver, By.cssSelector("span.error[id*='validationModel-amount']"));
        } catch (TimeoutException e) {
            return false;
        }
        for (WebElement error : amountValidationErrors) {
            if (isDisplayed(error)) {
                String text = error.getText().trim().toLowerCase();
                if (text.contains("amount")) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getVisibleAmountValidationText() {
        for (WebElement error : amountValidationErrors) {
            if (isDisplayed(error)) {
                return error.getText().trim();
            }
        }
        return "";
    }

    public boolean isBillPayErrorDisplayed() {
        return isDisplayed(billPayError) || isDisplayed(pageErrorMessage);
    }

    public String getBillPayErrorText() {
        if (isDisplayed(billPayError)) {
            return getText(billPayError);
        }
        if (isDisplayed(pageErrorMessage)) {
            return getText(pageErrorMessage);
        }
        return "";
    }

    public BillPayAccountOverviewPage goToAccountsOverview() {
        click(accountsOverviewLink);
        return new BillPayAccountOverviewPage(driver).waitUntilLoaded();
    }
}
